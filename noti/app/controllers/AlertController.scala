package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.{Json, Writes}


@Singleton
class AlertController @Inject()(cc: ControllerComponents)//, repo: OrderAlertsRepo
                               (implicit ec: ExecutionContext) extends AbstractController(cc) {

  // def getAllAlerts: Action[AnyContent] = Action.async {
  //   repo.getAllAlerts().map { alerts =>
  //     val jsonAlerts = alerts.map(a => OrderAlertJson(a.id.getOrElse(0L), a.orderId))
  //     Ok(Json.toJson(jsonAlerts))
  //   }
  // }
}
