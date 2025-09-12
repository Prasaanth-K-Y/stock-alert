package controllers

import play.api.mvc._
import repositories.{ItemsRepo, OrdersRepo,CustomersRepo,RestockRepo}
import services.StockService
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import models.Items
import models.Orders

//The StockController manages inventory and order processing via RESTful endpoints. 
class StockController @Inject()(
    cc: ControllerComponents,
    irepo: ItemsRepo,
    orepo: OrdersRepo,
    crepo :CustomersRepo,
    restockRepo:RestockRepo,
    ser: StockService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // POST /api/items: Adds a new item, returning 201 on success or 400/409 on error . Do not allow duplicates.
  def addItems(): Action[JsValue] = Action.async(parse.json) { req =>
    req.body.validate[Items].fold(
      err => Future.successful(BadRequest(JsError.toJson(err))),
      item => irepo.addItem(item).map {
        case Left(msg) => Conflict(Json.obj("error" -> msg))
        case Right(id) => Created(Json.obj("id" -> id, "message" -> s"Successfully created item ${item.name}"))
      }
    )
  }

  // GET /api/items/:id: Retrieves a single item.
  def getItem(id: Long): Action[AnyContent] = Action.async {
    irepo.getItem(id).map {
      case Some(item) => Ok(Json.obj("item" -> item))
      case None       => NotFound(Json.obj("error" -> s"Item $id not found"))
    }
  }


  // GET /api/items: Retrieves all items.
  def getAllItems(): Action[AnyContent] = Action.async {
    irepo.getAllItems().map(items => Ok(Json.toJson(items)))
  }

  // POST /api/orders: Creates a new order, returning 201 on success or 400 on error.
    // POST /api/orders: Creates a new order with customerId
  def newOrder(): Action[JsValue] = Action.async(parse.json) { req =>
    req.body.validate[Orders].fold(
      err => Future.successful(BadRequest("Provide a valid order with customerId")),
      ord => ser.handleShipping(ord).map { result =>
        if (result.orderId == 0L) BadRequest(result.message)
        else Created(Json.obj(
          "orderId" -> result.orderId,
          "customerId" -> ord.customerId,
          "message" -> result.message
        ))
      }
    )
  }
  // PUT /api/items/:id/stock: Updates stock quantity for a given item ID.  
  // If stock is updated, sends notifications to all customers waiting for restock.
  // Returns 200 on success, 404 if item not found.
  def updateStock(id: Long, qty: Long) = Action.async {
  irepo.updateStock(id, qty).flatMap { updatedRows =>
    if (updatedRows > 0) {
      // Stock updated,check restock
      restockRepo.getByItemId(id).flatMap { restocks =>
      // For each customer, append notification string
      Future.sequence(
        restocks.map(r => crepo.updateNotifications(r.customerId, s"Item $id is back in stock"))
      ).map { _ =>
        Ok(s"Stock updated for item $id to $qty, notifications sent to ${restocks.size} customer(s)")
      }
    }

    } else {
      Future.successful(NotFound(s"Item with id $id not found"))
    }
  }
}


}