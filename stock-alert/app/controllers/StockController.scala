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
    } else Future.successful(Forbidden("Only Customers can access this"))
  }

  // Customer: POST /api/orders
  def newOrder(): Action[JsValue] = jwtAction(parse.json).async { request =>
    val user = request.attrs(Attrs.User)

    if (user.role != "Customer") {
      Future.successful(Forbidden("Only Customers can place orders"))
    } else {
      request.body.validate[Orders].fold(
        err => Future.successful(BadRequest(JsError.toJson(err))),
        ord => {
          if (!user.isPrime && ord.qty > 2) {
            Future.successful(Forbidden("Normal users cannot order more than 2 items"))
          } else {
            val userId = user.id.getOrElse(0L)
            ser.handleShipping(ord, userId).map { result =>
              if (result.orderId == 0L) BadRequest(result.message)
              else Created(Json.obj(
                "orderId" -> result.orderId,
                "userId" -> userId,
                "message" -> result.message
              ))
            }
          }
        }
      )
    }
  }

  // Seller: POST /api/items
  def addItems(): Action[JsValue] = jwtAction(parse.json).async { request =>
    val user = request.attrs(Attrs.User)

    if (user.role != "Seller") {
      Future.successful(Forbidden("Only Sellers can add items"))
    } else {
      request.body.validate[Items].fold(
        err => Future.successful(BadRequest(JsError.toJson(err))),
        item => irepo.addItem(item).map {
          case Left(msg) => Conflict(Json.obj("error" -> msg))
          case Right(id) => Created(Json.obj("id" -> id, "message" -> s"Successfully created item ${item.name}"))
        }
      )
    }
  }

  // Seller: PUT /api/items/:id/stock
  def updateStock(id: Long, qty: Long): Action[AnyContent] = jwtAction.async { request =>
    val user = request.attrs(Attrs.User)

    if (user.role != "Seller") {
      Future.successful(Forbidden("Only Sellers can update stock"))
    } else {
      irepo.updateStock(id, qty).flatMap { updatedRows =>
        if (updatedRows > 0) {
          restockRepo.getByItemId(id).flatMap { restocks =>
            Future.sequence(
              restocks.map(r => urepo.updateNotifications(r.customerId, s"Item $id is back in stock"))
            ).map { _ =>
              Ok(s"Stock updated for item $id to $qty, notifications sent to ${restocks.size} user(s)")
            }
          }
        } else Future.successful(NotFound(s"Item with id $id not found"))
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
