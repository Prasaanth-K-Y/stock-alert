error id: file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/app/services/StockService.scala:shared/notification/StringMessage.
file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/app/services/StockService.scala
empty definition using pc, found symbol in pc: 
found definition using semanticdb; symbol shared/notification/StringMessage.
empty definition using fallback
non-local guesses:

offset: 237
uri: file:///C:/Users/Pky/Desktop/scala/scala-files/stock-alert/app/services/StockService.scala
text:
```scala
package services

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import models.Orders
import repositories.{ItemsRepo, OrdersRepo}


import shared.notification.{StringServiceGrpc, String@@Message}

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
        if (newStock >= item.minStock) {
          ordersRepo.newOrder(o).map { orderId =>
            println(s"[StockService] Stock sufficient for ${o.item}. New stock: ${newStock}")
            ShippingResult(orderId, s"Order $orderId placed successfully.")
          }
        } else {
          ordersRepo.newOrder(o).flatMap { orderId =>
            println(s"[StockService] LOW STOCK for ${o.item}. Triggering gRPC alert...")

            val request = StringMessage(s"LOW STOCK ALERT for item ${o.item}, order $orderId")

            grpcStub.sendString(request).map { response =>
              println(s"[StockService] gRPC response: ${response.value}")
              ShippingResult(orderId, s"Order $orderId placed. Alert: ${response.value}")
            }.recover {
              case e: Exception =>
                println(s"[StockService] gRPC call failed: ${e.getMessage}")
                ShippingResult(orderId, s"Order $orderId placed, but gRPC alert failed.")
            }
          }
        }

      case None =>
        Future.successful(ShippingResult(0L, "Item not found."))
    }
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 