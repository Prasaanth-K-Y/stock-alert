error id: file:///C:/Users/Pky/Desktop/noti/src/main/scala/services/StringServiceImpl.scala:services/`<error: <none>>`#
file:///C:/Users/Pky/Desktop/noti/src/main/scala/services/StringServiceImpl.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -Singleton#
	 -scala/Predef.Singleton#
offset: 191
uri: file:///C:/Users/Pky/Desktop/noti/src/main/scala/services/StringServiceImpl.scala
text:
```scala
package services

import scala.concurrent.{Future, ExecutionContext}
import repositories.OrderAlertsRepo
import shared.notification.{StringMessage, StringServiceGrpc}

@javax.inject.Sin@@gleton
class StringServiceImpl @javax.inject.Inject()(repo: OrderAlertsRepo)(implicit ec: ExecutionContext)
    extends StringServiceGrpc.StringService {

  override def sendString(req: StringMessage): Future[StringMessage] = {
    println(s"[NotificationService] Received alert: ${req.value}")

    val orderIdOpt = """order (\d+)""".r.findFirstMatchIn(req.value).map(_.group(1))

    orderIdOpt match {
      case Some(orderId) =>
        repo.insertAlert(orderId).map { id =>
          println(s"[NotificationService] Saved alert with ID $id")
          StringMessage(s"Alert processed and saved with id: $id")
        }
      case None =>
        Future.successful(StringMessage("Alert received but no orderId found"))
    }
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 