package services

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.concurrent.ScalaFutures
import models.{Items, Orders, Restock}
import repositories.{ItemsRepo, OrdersRepo, RestockRepo}   
import shared.notification.{StringServiceGrpc, StringMessage}

class StockServiceSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures {

  val mockItemsRepo   = mock[ItemsRepo]
  val mockOrdersRepo  = mock[OrdersRepo]
  val mockRestockRepo = mock[RestockRepo]   
  val mockGrpcStub    = mock[StringServiceGrpc.StringServiceStub]

  val service = new StockService(mockItemsRepo, mockOrdersRepo, mockRestockRepo, mockGrpcStub)

  "StockService" should {

    "place an order successfully when stock after order is still >= minStock" in {
      val sampleItem = Items(Some(1L), "Test-Item", stock = 10, minStock = 5)
      val order = Orders(None, item = 1L, qty = 3, customerId = 1L)

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
      val order = Orders(None, item = 2L, qty = 3, customerId = 1L)

      when(mockItemsRepo.getItem(2L)).thenReturn(Future.successful(Some(sampleItem)))

      // Mock restockRepo.add to return a Future[Long] (simulate DB insert)
      when(mockRestockRepo.add(any[Restock])).thenReturn(Future.successful(1L))

      // Mock gRPC stub
      when(mockGrpcStub.sendString(any[StringMessage]))
        .thenReturn(Future.successful(StringMessage("ALERT SENT")))

      val resultF = service.handleShipping(order)

      whenReady(resultF) { result =>
        result.orderId mustBe 0L
        result.message must include("ALERT SENT")
      }
    }


    "return Item not found when item does not exist" in {
      val order = Orders(None, item = 99L, qty = 3, customerId = 1L)

      when(mockItemsRepo.getItem(99L)).thenReturn(Future.successful(None))

      val resultF = service.handleShipping(order)

      whenReady(resultF) { result =>
        result.orderId mustBe 0L
        result.message must include("Item not found")
      }
    }
  }
}
