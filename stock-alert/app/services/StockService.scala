package services

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import models.{Orders,Restock}
import repositories.{ItemsRepo, OrdersRepo,RestockRepo}
import shared.notification.{StringServiceGrpc, StringMessage}

case class ShippingResult(orderId: Long, message: String)

/**
 * The StockService is responsible for handling the core business logic related to stock management and order processing.
 * @param itemsRepo A repository for accessing and modifying `Items` data.
 * @param ordersRepo A repository for accessing and modifying `Orders` data.
 * @param grpcStub A gRPC client stub for sending notifications to an external service. Stub file is added in the Build sbt
 * @param ec An implicit `ExecutionContext` for managing asynchronous operations. All `Future`
 */
@Singleton
class StockService @Inject()(
  itemsRepo: ItemsRepo,
  ordersRepo: OrdersRepo,
  restockRepo:RestockRepo,
  grpcStub: StringServiceGrpc.StringServiceStub
)(implicit ec: ExecutionContext) {

 
  def handleShipping(o: Orders): Future[ShippingResult] = {
    // First, retrieve the item details asynchronously from the database.
    itemsRepo.getItem(o.item).flatMap {
      case Some(item) =>
        // Calculate the new stock level after the order.
        val newStock = item.stock - o.qty

        // Check if the new stock level is at or above the minimum required stock.
        if (newStock >= item.minStock) {
          // If stock is sufficient:
          // 1. Create the new order in the database.
          ordersRepo.newOrder(o).flatMap { orderId =>
            // 2. If the order is created successfully, update the item's stock level.
            itemsRepo.upd(item, newStock).map { _ =>
              // 3. Log the successful operation and return a success result.
              println(s"[StockService] Stock sufficient for ${o.item}. New stock: ${newStock}")
              ShippingResult(orderId, s"Order $orderId placed successfully.")
            }
          }
        } else {
          // If stock is insufficient:
          // 1. Log the low stock condition.
          println(s"[StockService] LOW STOCK for ${o.item}. Triggering gRPC alert...")

          //Adding in the restock table 
          val restockRow = Restock(None, o.item, o.customerId)

          restockRepo.add(restockRow).map { id =>
            println(s"[DEBUG] Restock row added with id: $id for customer ${o.customerId} and item ${o.item}")
          }.recover {
            case e: Exception =>
              println(s"[DEBUG] Failed to add restock row: ${e.getMessage}")
          }
          // restockRepo.add(Restock(None, o.item, o.customerId))
          // 2. Prepare and send a gRPC alert to the external notification service.
          val request = StringMessage(
            s"LOW STOCK ALERT for item ${o.item}, attempted order for qty ${o.qty}"
          )
          grpcStub.sendString(request).map { response =>
            // 3. Handle the gRPC service's response.
            println(s"[StockService] gRPC response: ${response.value}")
            // Return a failed shipping result with a message containing the alert response.
            ShippingResult(0L, s"Order NOT placed. Alert: ${response.value}")
          }.recover {
            // 4. Handle potential failures of the gRPC call (e.g., network issues).
            case e: Exception =>
              println(s"[StockService] gRPC call failed: ${e.getMessage}")
              // Return a failed result indicating the alert failed.
              ShippingResult(0L, s"Order NOT placed. gRPC alert failed.")
          }
        }

      // Case `None`: The item does not exist.
      case None =>
        // Return a failed shipping result immediately without any further database or service calls.
        Future.successful(ShippingResult(0L, "Item not found."))
    }
  }
}