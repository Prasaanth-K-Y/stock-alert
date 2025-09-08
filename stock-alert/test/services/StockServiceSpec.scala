package services

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.concurrent.ScalaFutures
import models.{Items, Orders}
import repositories.{ItemsRepo, OrdersRepo}
import shared.notification.{StringServiceGrpc, StringMessage}

class StockServiceSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures {

  val mockItemsRepo: ItemsRepo = mock[ItemsRepo]
  val mockOrdersRepo: OrdersRepo = mock[OrdersRepo]
  val mockGrpcStub: StringServiceGrpc.StringServiceStub = mock[StringServiceGrpc.StringServiceStub]

  val service = new StockService(mockItemsRepo, mockOrdersRepo, mockGrpcStub)

  "StockService" should {

    "place an order successfully when stock after order is still >= minStock" in {
      val sampleItem = Items(Some(1L), "Test-Item", stock = 10, minStock = 5)
      val order = Orders(None, item = 1L, qty = 3) // newStock = 7 >= minStock 5

      when(mockItemsRepo.getItem(1L)).thenReturn(Future.successful(Some(sampleItem)))
      when(mockOrdersRepo.newOrder(order)).thenReturn(Future.successful(100L))
      when(mockItemsRepo.upd(sampleItem, 7)).thenReturn(Future.successful(1))

      val resultF = service.handleShipping(order)

      whenReady(resultF) { result =>
        result.orderId mustBe 100L
        result.message must include("Order 100 placed successfully.")
      }
    }

    "trigger gRPC alert when stock after order would drop below minStock" in {
      val sampleItem = Items(Some(2L), "LowStock-Item", stock = 5, minStock = 4)
      val order = Orders(None, item = 2L, qty = 3) // newStock = 2 < minStock 4

      when(mockItemsRepo.getItem(2L)).thenReturn(Future.successful(Some(sampleItem)))
      when(mockGrpcStub.sendString(any[StringMessage]))
        .thenReturn(Future.successful(StringMessage("ALERT SENT")))

      val resultF = service.handleShipping(order)

      whenReady(resultF) { result =>
        result.orderId mustBe 0L
        result.message must include("ALERT SENT")
      }
    }

    "return Item not found when item does not exist" in {
      val order = Orders(None, item = 99L, qty = 5)

      when(mockItemsRepo.getItem(99L)).thenReturn(Future.successful(None))

      val resultF = service.handleShipping(order)
    
      whenReady(resultF) { result =>
        result.orderId mustBe 0L
        result.message must include("Item not found")
      }
    }
  }
}
