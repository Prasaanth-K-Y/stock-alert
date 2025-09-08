error id: file:///C:/Users/Pky/Desktop/noti/app/controllers/AlertController.scala:
file:///C:/Users/Pky/Desktop/noti/app/controllers/AlertController.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -javax/inject/OrderAlertsRepo#
	 -play/api/mvc/OrderAlertsRepo#
	 -OrderAlertsRepo#
	 -scala/Predef.OrderAlertsRepo#
offset: 258
uri: file:///C:/Users/Pky/Desktop/noti/app/controllers/AlertController.scala
text:
```scala
package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.{Json, Writes}


@Singleton
class AlertController @Inject()(cc: ControllerComponents, repo: OrderAlertsRepo@@)
                               (implicit ec: ExecutionContext) extends AbstractController(cc) {

  // def getAllAlerts: Action[AnyContent] = Action.async {
  //   repo.getAllAlerts().map { alerts =>
  //     val jsonAlerts = alerts.map(a => OrderAlertJson(a.id.getOrElse(0L), a.orderId))
  //     Ok(Json.toJson(jsonAlerts))
  //   }
  // }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 