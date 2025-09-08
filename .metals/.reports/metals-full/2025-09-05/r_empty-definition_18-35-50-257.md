error id: file:///C:/Users/Pky/Desktop/stockAlert/stock-alert/test/services/StockServiceSpec.scala:services/`<error: <none>>`.
file:///C:/Users/Pky/Desktop/stockAlert/stock-alert/test/services/StockServiceSpec.scala
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -org/mockito/Mockito.repositories.
	 -org/mockito/ArgumentMatchers.repositories.
	 -scala/concurrent/duration/repositories.
	 -repositories.
	 -scala/Predef.repositories.
offset: 420
uri: file:///C:/Users/Pky/Desktop/stockAlert/stock-alert/test/services/StockServiceSpec.scala
text:
```scala
package services

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import scala.concurrent.{Future, ExecutionContext, Await}
import scala.concurrent.duration._
import models.{Items, Orders}
import shared.notification.{StringServiceGrpc, StringMessage}
import @@repositories.{ItemsRepo, OrdersRepo}

class StockServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global

  "StockService" should {

    "place an order successfully when stock is sufficient" in {
      val itemsRepo = mock[ItemsRepo]
      val ordersRepo = mock[OrdersRepo]
      val grpcStub = mock[StringServiceGrpc.StringServiceStub]

      val service = new StockService(itemsRepo, ordersRepo, grpcStub)

      val item = Items(Some(1), "soap", stock = 10, minStock = 2)
      val order = Orders(Some(1), itemId = 1, qty = 3)

      when(itemsRepo.getItem(item.id.get)).thenReturn(Future.successful(Some(item)))
      when(ordersRepo.newOrder(order)).thenReturn(Future.successful(100L))

      val result = Await.result(service.handleShipping(order), 5.seconds)

      result.orderId mustBe 100L
      result.message must include("placed successfully")

      verify(ordersRepo).newOrder(order)
      verifyNoInteractions(grpcStub) // gRPC should NOT be called
    }

    "trigger gRPC alert when stock is below minimum" in {
      val itemsRepo = mock[ItemsRepo]
      val ordersRepo = mock[OrdersRepo]
      val grpcStub = mock[StringServiceGrpc.StringServiceStub]

      val service = new StockService(itemsRepo, ordersRepo, grpcStub)

      val item = Items(Some(2), "soap", stock = 4, minStock = 5)
      val order = Orders(Some(2), itemId = 2, qty = 1)

      when(itemsRepo.getItem(item.id.get)).thenReturn(Future.successful(Some(item)))
      when(ordersRepo.newOrder(order)).thenReturn(Future.successful(101L))
      when(grpcStub.sendString(any[StringMessage])).thenReturn(Future.successful(StringMessage("ALERT SENT")))

      val result = Await.result(service.handleShipping(order), 5.seconds)

      result.orderId mustBe 101L
      result.message must include("ALERT SENT")

      verify(ordersRepo).newOrder(order)
      verify(grpcStub).sendString(any[StringMessage])
    }

    "return Item not found when item does not exist" in {
      val itemsRepo = mock[ItemsRepo]
      val ordersRepo = mock[OrdersRepo]
      val grpcStub = mock[StringServiceGrpc.StringServiceStub]

      val service = new StockService(itemsRepo, ordersRepo, grpcStub)

      val order = Orders(Some(3), itemId = 99, qty = 1)

      when(itemsRepo.getItem(order.item)).thenReturn(Future.successful(None))

      val result = Await.result(service.handleShipping(order), 5.seconds)

      result.orderId mustBe 0L
      result.message mustBe "Item not found."

      verifyNoInteractions(ordersRepo)
      verifyNoInteractions(grpcStub)
    }
  }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: 