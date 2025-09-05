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

class StockController @Inject()(
    cc: ControllerComponents,
    irepo: ItemsRepo,
    orepo: OrdersRepo,
    ser: StockService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def addItems(): Action[JsValue] = Action.async(parse.json) { req =>
    req.body.validate[Items].fold(
      err => Future.successful(BadRequest(JsError.toJson(err))),
      item => irepo.addItem(item).map(i => Created(s"Created Successfully Item ${item.name}"))
    )
  }

  def getItem(id: Long): Action[AnyContent] = Action.async {
    irepo.getItem(id).map(i => Ok(Json.toJson("item" -> i)))
  }

  def getAllItems(): Action[AnyContent] = Action.async {
    irepo.getAllItems().map(items => Ok(Json.toJson(items)))
  }

  def newOrder(): Action[JsValue] = Action.async(parse.json) { req =>
    req.body.validate[Orders].fold(
      err => Future.successful(BadRequest("Provide a valid order")),
      ord => ser.handleShipping(ord).map { result =>
        if (result.orderId == 0L) {
          BadRequest(result.message) 
        } else {
          Created(Json.obj(
            "orderId" -> result.orderId,
            "message" -> result.message
          ))
        }
      }
    )
  }
}
