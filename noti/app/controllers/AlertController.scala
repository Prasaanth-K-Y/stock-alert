package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.{Json, Writes}


@Singleton
class AlertController @Inject()(cc: ControllerComponents)//, repo: OrderAlertsRepo
                               (implicit ec: ExecutionContext) extends AbstractController(cc) {

  
}
// sbt -Dconfig.file="conf/application.conf" "runMain shared.notification.NotificationServer"