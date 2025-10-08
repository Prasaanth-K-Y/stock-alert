package controllers

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import scala.concurrent.Future
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatestplus.mockito.MockitoSugar
import models.{Items, User} 
import repositories.ItemsRepo
import services.StockService
import scala.concurrent.ExecutionContext
import play.api.mvc.{Request, Result}
import play.api.mvc.BodyParsers.Default
import org.scalatest.concurrent.ScalaFutures
import play.api.test.CSRFTokenHelper._ 
import utils.JwtActionBuilder 
import org.scalatestplus.play.guice.GuiceOneAppPerSuite 
import utils.Attrs 

// Add GuiceOneAppPerSuite and ScalaFutures
class StockControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with ScalaFutures { // <--- FIX 2: GuiceOneAppPerSuite is now found

    // --- SETUP: USERS, PARSERS, MOCKS ---
    
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

    // *FIX 1 & 3: app.injector is now available*
    val bodyParsers: Default = app.injector.instanceOf[Default] 

    // Define fixed users for testing roles
    val customerUser: User = User(Some(1L), "Test Customer", "customer@test.com", "pass", role = "Customer")
    val sellerUser: User = User(Some(2L), "Test Seller", "seller@test.com", "pass", role = "Seller")
    val adminUser: User = User(Some(3L), "Test Admin", "admin@test.com", "pass", role = "Admin")

    // Mock dependencies
    val mockItemsRepo: ItemsRepo = mock[ItemsRepo]
    val mockStockService: StockService = mock[StockService]
    val stubCC = stubControllerComponents()
    
    // --- FAKE JWT ACTION BUILDER FUNCTION ---

    /** Creates a fake JwtActionBuilder that injects a specific User object into the request. */
    def createFakeJwtAction(user: User): JwtActionBuilder = new JwtActionBuilder(bodyParsers) {
        override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
            // Attrs is imported and found
            block(request.addAttr(Attrs.User, user)) 
        }
    }
    
    // --- Helper to get a CSRF token from the controller ---
    private def getCsrfToken(user: User): String = {
        val controller = new StockController(stubCC, mockItemsRepo, null, null, null, mockStockService, createFakeJwtAction(user), null)(ec)
        val tokenResult = controller.csrfToken().apply(FakeRequest(GET, "/api/csrf-token"))
        (contentAsJson(tokenResult) \ "csrfToken").as[String]
    }


    "StockController" should {

        "allow Customer to get all items" in {
            val controller = new StockController(stubCC, mockItemsRepo, null, null, null, mockStockService, createFakeJwtAction(customerUser), null)(ec)
            when(mockItemsRepo.getAllItems()).thenReturn(Future.successful(Seq.empty))

            val request = FakeRequest(GET, "/api/items")
            val result = controller.getAllItems().apply(request)

            status(result) mustBe OK
        }

        "forbid Seller from getting all items" in {
            val controller = new StockController(stubCC, mockItemsRepo, null, null, null, mockStockService, createFakeJwtAction(sellerUser), null)(ec)

            val request = FakeRequest(GET, "/api/items")
            val result = controller.getAllItems().apply(request)

            status(result) mustBe FORBIDDEN
        }

           "forbid Admin from getting all items" in {
            val controller = new StockController(stubCC, mockItemsRepo, null, null, null, mockStockService, createFakeJwtAction(adminUser), null)(ec)

            val request = FakeRequest(GET, "/api/items")
            val result = controller.getAllItems().apply(request)

            status(result) mustBe FORBIDDEN
        }
        "forbid Customer from adding an item" in {
            val controller = new StockController(stubCC, mockItemsRepo, null, null, null, mockStockService, createFakeJwtAction(customerUser), null)(ec)
            
            val newItemJson = Json.obj("id" -> 0, "name" -> "Gadget", "price" -> 50, "stock" -> 5)

            val csrfToken = getCsrfToken(adminUser)
            val request = FakeRequest(POST, "/api/items")
                .withHeaders("Csrf-Token" -> csrfToken)
                .withBody(newItemJson)
                .withCSRFToken

            val result = controller.addItems().apply(request)
            status(result) mustBe FORBIDDEN
        }

      
    }
}