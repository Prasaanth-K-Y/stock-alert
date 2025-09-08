package services

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import models.Orders
import repositories.{ItemsRepo, OrdersRepo}
import shared.notification.{StringServiceGrpc, StringMessage}

case class ShippingResult(orderId: Long, message: String)

@Singleton
class StockService @Inject()(
  itemsRepo: ItemsRepo,
  ordersRepo: OrdersRepo,
  grpcStub: StringServiceGrpc.StringServiceStub
)(implicit ec: ExecutionContext) {

  def handleShipping(o: Orders): Future[ShippingResult] = {
    itemsRepo.getItem(o.item).flatMap {
      case Some(item) =>
        val newStock = item.stock - o.qty

        if (newStock >= item.minStock ) {  
          ordersRepo.newOrder(o).flatMap { orderId =>
            itemsRepo.upd(item, newStock).map { _ =>
              println(s"[StockService] Stock sufficient for ${o.item}. New stock: ${newStock}")
              ShippingResult(orderId, s"Order $orderId placed successfully.")
            }
          }
        } else {
          println(s"[StockService] LOW STOCK for ${o.item}. Triggering gRPC alert...")

          val request = StringMessage(
            s"LOW STOCK ALERT for item ${o.item}, attempted order for qty ${o.qty}"
          )

          grpcStub.sendString(request).map { response =>
            println(s"[StockService] gRPC response: ${response.value}")
            ShippingResult(0L, s"Order NOT placed. Alert: ${response.value}")
          }.recover {
            case e: Exception =>
              println(s"[StockService] gRPC call failed: ${e.getMessage}")
              ShippingResult(0L, s"Order NOT placed. gRPC alert failed.")
          }
        }

      case None =>
        Future.successful(ShippingResult(0L, "Item not found."))
    }
  }
}
