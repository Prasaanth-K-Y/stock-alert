package controllers

import models.{Items, Orders}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.ControllerComponents
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import repositories.{ItemsRepo, OrdersRepo, CustomersRepo, RestockRepo}
import services.{ShippingResult, StockService}
import scala.concurrent.{ExecutionContext, Future}

class StockControllerSpec extends PlaySpec
  with GuiceOneAppPerTest
  with Injecting
  with MockitoSugar
  with BeforeAndAfterEach {

  // --- Mocks ---
  private val mockItemsRepo     = mock[ItemsRepo]
  private val mockOrdersRepo    = mock[OrdersRepo]
  private val mockCustomersRepo = mock[CustomersRepo]
  private val mockRestockRepo   = mock[RestockRepo]
  private val mockStockService  = mock[StockService]

  implicit private val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val controller = new StockController(
    stubControllerComponents(),
    mockItemsRepo,
    mockOrdersRepo,
    mockCustomersRepo,
    mockRestockRepo,
    mockStockService
  )

  override def afterEach(): Unit = {
    reset(mockItemsRepo, mockOrdersRepo, mockCustomersRepo, mockRestockRepo, mockStockService)
  }

  val sampleItem = Items(Some(1L), "Test-Item", 100L, 10L)

  "StockController" should {

    "return a list of all items from the repository" in {
      when(mockItemsRepo.getAllItems()).thenReturn(Future.successful(Seq(sampleItem)))

      val result = controller.getAllItems().apply(FakeRequest(GET, "/items"))

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsJson(result) mustBe Json.toJson(Seq(sampleItem))
    }

    "return a single item by its id" in {
      when(mockItemsRepo.getItem(1L)).thenReturn(Future.successful(Some(sampleItem)))

      val result = controller.getItem(1L).apply(FakeRequest(GET, "/item/1"))

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      (contentAsJson(result) \ "item" \ "name").as[String] mustBe "Test-Item"
    }

    "add a new item successfully" in {
      val newItemJson: JsValue = Json.obj(
        "name" -> "New-Gadget",
        "stock" -> 50,
        "minStock" -> 5
      )

      when(mockItemsRepo.addItem(any[Items]))
        .thenReturn(Future.successful(Right(1L)))

      val request = FakeRequest(POST, "/items").withBody(newItemJson)
      val result  = controller.addItems().apply(request)

      status(result) mustBe CREATED
      (contentAsJson(result) \ "message").as[String] must include("Successfully created item New-Gadget")
    }

    "create a new order successfully when stock is available" in {
      val orderJson = Json.obj("item" -> 1, "qty" -> 5, "customerId" -> 42)
      val successResult = ShippingResult(orderId = 123L, message = "Order processed successfully")

      when(mockStockService.handleShipping(any[Orders]))
        .thenReturn(Future.successful(successResult))

      val request = FakeRequest(POST, "/order").withBody(orderJson)
      val result  = controller.newOrder().apply(request)

      status(result) mustBe CREATED
      (contentAsJson(result) \ "orderId").as[Long] mustBe 123L
      (contentAsJson(result) \ "message").as[String] mustBe "Order processed successfully"
    }

    "return BadRequest when an order fails due to insufficient stock" in {
      val orderJson = Json.obj("item" -> 1, "qty" -> 999, "customerId" -> 42)
      val failureResult = ShippingResult(orderId = 0L, message = "Insufficient stock for Test-Item")

      when(mockStockService.handleShipping(any[Orders]))
        .thenReturn(Future.successful(failureResult))

      val request = FakeRequest(POST, "/order").withBody(orderJson)
      val result  = controller.newOrder().apply(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Insufficient stock for Test-Item"
    }

    "return BadRequest for an invalid order JSON payload" in {
      val badJson = Json.obj("item" -> 1, "quantity" -> 5) // missing customerId

      val request = FakeRequest(POST, "/order").withBody(badJson)
      val result  = controller.newOrder().apply(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) must include("Provide a valid order")
    }
  }
}
