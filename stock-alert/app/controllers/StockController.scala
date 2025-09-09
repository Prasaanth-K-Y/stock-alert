package controllers

import play.api.mvc._
import repositories.ItemsRepo
import repositories.OrdersRepo
import services.StockService
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import models.Items
import models.Orders

/** The StockController manages inventory and order processing via RESTful endpoints. */
class StockController @Inject()(
    cc: ControllerComponents,
    irepo: ItemsRepo,
    orepo: OrdersRepo,
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
    irepo.getItem(id).map(i => Ok(Json.toJson("item" -> i)))
  }

  // GET /api/items: Retrieves all items.
  def getAllItems(): Action[AnyContent] = Action.async {
    irepo.getAllItems().map(items => Ok(Json.toJson(items)))
  }

  // POST /api/orders: Creates a new order, returning 201 on success or 400 on error.
  def newOrder(): Action[JsValue] = Action.async(parse.json) { req =>
    req.body.validate[Orders].fold(
      err => Future.successful(BadRequest("Provide a valid order")),
      ord => ser.handleShipping(ord).map { result =>
        if (result.orderId == 0L) BadRequest(result.message)
        else Created(Json.obj("orderId" -> result.orderId, "message" -> result.message))
      }
    )
  }
}