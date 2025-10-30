package controllers

import play.api.mvc._
import repositories.{ItemsRepo, OrdersRepo, UserRepo, RestockRepo}
import services.StockService
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import models.{Items, Orders}
import utils.{JwtActionBuilder, Attrs}
import play.filters.csrf.{CSRF, CSRFAddToken}
import javax.inject.Singleton


@Singleton
class StockController @Inject()(
    cc: ControllerComponents,
    irepo: ItemsRepo,
    orepo: OrdersRepo,
    urepo: UserRepo,
    restockRepo: RestockRepo,
    ser: StockService,
    jwtAction: JwtActionBuilder,
    csrfAddToken: CSRFAddToken
)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  // Customer: GET /api/items/:id
  def getItem(id: Long): Action[AnyContent] = jwtAction.async { request =>
    val user = request.attrs(Attrs.User)
    if (user.role == "Customer") {
      irepo.getItem(id).map {
        case Some(item) => Ok(Json.obj("item" -> item))
        case None       => NotFound(Json.obj("error" -> s"Item $id not found"))
      }
    } else Future.successful(Forbidden("Only Customers can access this"))
  }

 // Customer: GET /api/items
  def getAllItems(): Action[AnyContent] = jwtAction.async { request =>
    val user = request.attrs(Attrs.User)
    if (user.role == "Customer") {
      irepo.getAllItems().map(items => Ok(Json.toJson(items)))
    } else {
      Future.successful(Forbidden(Json.obj("error" -> "Only Customers can access this")))
    }
  }

  // Customer: POST /api/orders
  // Defines an endpoint to place a new order; only accessible to authenticated users via JWT
def newOrder(): Action[JsValue] = jwtAction(parse.json).async { request =>
  // Extract the authenticated user from request attributes
  val user = request.attrs(Attrs.User)

  // Reject if the user is not a Customer
  if (user.role != "Customer") {
    Future.successful(Forbidden(Json.obj("error" -> "Only Customers can place orders")))
  } else {
    // Validate incoming JSON against the Orders model
    request.body.validate[Orders].fold(
      // If validation fails, respond with 400 and error details
      err => Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(err)))),
      ord => {
        // Reject if quantity is zero or negative
        if (ord.qty <= 0) {
          Future.successful(BadRequest(Json.obj("error" -> "Quantity must be greater than 0")))
        }
        // Reject if non-Prime user tries to order more than 2 items
        else if (!user.isPrime && ord.qty > 2) {
          Future.successful(Forbidden(Json.obj("error" -> "Normal users cannot order more than 2 items")))
        }
        else {
          // Safely extract user ID (fallback to 0L if missing)
          val userId = user.id.getOrElse(0L)

          // Delegate order processing to shipping service
          ser.handleShipping(ord, userId).map { result =>
            // If orderId is 0L, treat as failure and respond with error
            if (result.orderId == 0L)
              BadRequest(Json.obj(
                "error" -> result.message,
                "orderId" -> result.orderId,
                "userId" -> userId
              ))
            else
              // Successful order creation response
              Created(Json.obj(
                "orderId" -> result.orderId,
                "userId" -> userId,
                "message" -> result.message
              ))
          }.recover {
            // Catch unexpected exceptions and respond with 500
            case e: Exception =>
              InternalServerError(Json.obj("error" -> s"Internal server error: ${e.getMessage}"))
          }
        }
      }
    )
  }
}

  // Seller: POST /api/items
// Endpoint to allow Sellers to add new items to inventory
def addItems(): Action[JsValue] = jwtAction(parse.json).async { request =>
  // Extract authenticated user from request attributes
  val user = request.attrs(Attrs.User)
  
  // Enforce role-based access: only Sellers are allowed to add items
  if (user.role != "Seller") {
    Future.successful(Forbidden(Json.obj("error" -> "Only Sellers can add items")))
  } else {
    // Validate incoming JSON against Items model
    request.body.validate[Items].fold(
      // If validation fails, respond with 400 and error details
      err => Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(err)))),
      item => {
        // Business rule: stock must be positive
        if (item.stock <= 0) {
          Future.successful(BadRequest(Json.obj("error" -> s"Stock must be greater than 0")))
        }
        // Business rule: minimum stock cannot be negative
        else if (item.minStock < 0) {
          Future.successful(BadRequest(Json.obj("error" -> "Minimum stock cannot be negative")))
        }
        // Business rule: minimum stock cannot exceed total stock
        else if (item.minStock > item.stock) {
          Future.successful(BadRequest(Json.obj("error" -> "Minimum stock cannot exceed total stock")))
        }
        else {
          // All validations passed; delegate to item repository
          irepo.addItem(item).map {
            // If item creation fails, respond with 409 Conflict
            case Left(msg) =>
              Conflict(Json.obj("error" -> msg))
            // If successful, respond with 201 Created and item ID
            case Right(id) =>
              Created(Json.obj(
                "id" -> id,
                "message" -> s"Successfully created item ${item.name}"
              ))
          }
        }
      }
    )
  }
}

  // Seller: PUT /api/items/:id/stock
// Endpoint to update stock for a given item; restricted to authenticated Sellers
def updateStock(id: Long, qty: Long): Action[AnyContent] = jwtAction.async { request =>
  val user = request.attrs(Attrs.User)

  // Enforce role-based access control: only Sellers can update stock
  if (user.role != "Seller") {
    Future.successful(Forbidden("Only Sellers can update stock"))
  } else {
    // Attempt to update stock for the given item ID
    irepo.updateStock(id, qty).flatMap { updatedRows =>
      if (updatedRows > 0) {
        // If stock update succeeded, check for pending restock requests
        restockRepo.getByItemId(id).flatMap { restocks =>
          if (restocks.nonEmpty) {
            // Notify all users who requested restock for this item
            Future.sequence(
              restocks.map(r => urepo.updateNotifications(r.customerId, s"Item $id is back in stock"))
            ).flatMap { _ =>
              // After notifications, clean up restock requests for this item
              restockRepo.deleteByItemId(id).map { deletedCount =>
                Ok(s"Stock updated for item $id to $qty, notifications sent to ${restocks.size} user(s), and $deletedCount restock record(s) removed.")
              }
            }
          } else {
            // No restock requests found; respond with success message
            Future.successful(Ok(s"Stock updated for item $id to $qty — no pending restock requests."))
          }
        }
      } else {
        // Stock update failed (likely invalid item ID); respond with 404
        Future.successful(NotFound(s"Item with id $id not found"))
      }
    }
  }
}


  // Admin: GET /csrf-token
  def csrfToken(): Action[AnyContent] = jwtAction.async { request =>
    val user = request.attrs(Attrs.User)
    if (user.role != "Admin") Future.successful(Forbidden("Only Admins can access CSRF token"))
    else {
      val token = CSRF.getToken(request).map(_.value).getOrElse("")
      Future.successful(Ok(Json.obj("csrfToken" -> token)))
    }
  }
}
