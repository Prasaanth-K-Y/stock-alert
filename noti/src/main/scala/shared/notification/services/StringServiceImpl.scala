package shared.notification.services

import scala.concurrent.{Future, ExecutionContext}
import shared.notification.repositories.OrderAlertsRepo
import shared.notification.{StringServiceGrpc, StringMessage}

// StringServiceImpl implements the gRPC service to handle and store string notifications.
class StringServiceImpl(repo: OrderAlertsRepo)(implicit ec: ExecutionContext)
    extends StringServiceGrpc.StringService {

  // The sendString method is called by the gRPC client to send a notification.
  override def sendString(req: StringMessage): Future[StringMessage] = {
    println(s"[NotificationService] Received order ID: ${req.value}") // Logs the received message.

    // Inserts the received string into the database via the repository.
    repo.insertAlert(req.value).map { id =>
      println(s"[NotificationService] Saved order ID with db id: $id") // Logs the successful database insertion.
      StringMessage(s"Order ID ${req.value} inserted successfully") // Returns a confirmation message to the client.
    }
  }
}