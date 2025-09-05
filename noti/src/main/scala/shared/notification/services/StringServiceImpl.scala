package shared.notification.services

import scala.concurrent.{Future, ExecutionContext}
import shared.notification.repositories.OrderAlertsRepo
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
