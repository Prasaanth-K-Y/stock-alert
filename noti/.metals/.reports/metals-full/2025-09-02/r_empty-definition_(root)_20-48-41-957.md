error id: file:///C:/Users/Pky/Desktop/noti/src/main/scala/shared/notification/services/StringServiceImpl.scala:shared/notification/repositories/
file:///C:/Users/Pky/Desktop/noti/src/main/scala/shared/notification/services/StringServiceImpl.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 99
uri: file:///C:/Users/Pky/Desktop/noti/src/main/scala/shared/notification/services/StringServiceImpl.scala
text:
```scala
package shared.notification.services

import scala.concurrent.{Future, ExecutionContext}
import @@repositories.OrderAlertsRepo
import shared.notification.{StringServiceGrpc, StringMessage}

class StringServiceImpl(repo: OrderAlertsRepo)(implicit ec: ExecutionContext)
    extends StringServiceGrpc.StringService {

  override def sendString(req: StringMessage): Future[StringMessage] = {
    println(s"[NotificationService] Received order ID: ${req.value}")

    repo.insertAlert(req.value).map { id =>
      println(s"[NotificationService] Saved order ID with db id: $id")
      StringMessage(s"Order ID ${req.value} inserted successfully")
    }
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 