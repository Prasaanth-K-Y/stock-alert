package controllers

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import scala.concurrent.Future
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatestplus.mockito.MockitoSugar
import models.User 
import services.UserService
import scala.concurrent.ExecutionContext
import play.api.mvc.{Request, Result}
import play.api.mvc.BodyParsers.Default
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import utils.Attrs 
import utils.JwtActionBuilder 

class UserControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite with ScalaFutures {

    // --- SETUP: USERS, PARSERS, MOCKS ---
    
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

    // Get the correct BodyParsers instance from the injector (required for JwtActionBuilder)
    val bodyParsers: Default = app.injector.instanceOf[Default] 

    // Define fixed users for testing roles and ID checks
    val customerId = 101L
    val otherCustomerId = 102L
    val customerUser: User = User(Some(customerId), "Test Customer", "customer@test.com", "pass", role = "Customer")
    val otherCustomerUser: User = User(Some(otherCustomerId), "Other Customer", "other@test.com", "pass", role = "Customer")
    val sellerUser: User = User(Some(201L), "Test Seller", "seller@test.com", "pass", role = "Seller")
    val adminUser: User = User(Some(301L), "Test Admin", "admin@test.com", "pass", role = "Admin")

    // Mock dependencies
    val mockUserService: UserService = mock[UserService]
    val stubCC = stubControllerComponents()
    
    // --- FAKE JWT ACTION BUILDER FUNCTION (The core testing strategy) ---

    def createFakeJwtAction(user: User): JwtActionBuilder = new JwtActionBuilder(bodyParsers) {
        override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
            // Inject the provided user into the request attributes
            block(request.addAttr(Attrs.User, user)) 
        }
    }
    
    // --- TESTS ---

    "UserController" should {

        // =========================================================
        // GET /user/:id - getUser
        // =========================================================
        "allow Admin to fetch any user by ID" in {
            val controller = new UserController(stubCC, mockUserService, createFakeJwtAction(adminUser))(ec)
            
            when(mockUserService.fetchUser(customerId)).thenReturn(Future.successful(Some(customerUser)))

            val result = controller.getUser(customerId).apply(FakeRequest(GET, s"/user/$customerId"))

            status(result) mustBe OK
            (contentAsJson(result) \ "name").as[String] mustBe "Test Customer"
        }

        "forbid Customer from fetching other users" in {
            val controller = new UserController(stubCC, mockUserService, createFakeJwtAction(customerUser))(ec)
            
            val result = controller.getUser(otherCustomerId).apply(FakeRequest(GET, s"/user/$otherCustomerId"))

            status(result) mustBe FORBIDDEN
            contentAsString(result) must include("Only Admins can access this resource")
        }

        // =========================================================
        // GET /user - getAll
        // =========================================================
        "allow Admin to fetch all users" in {
            val controller = new UserController(stubCC, mockUserService, createFakeJwtAction(adminUser))(ec)
            val userList = Seq(customerUser, sellerUser)
            
            when(mockUserService.fetchAll()).thenReturn(Future.successful(userList))

            val result = controller.getAll().apply(FakeRequest(GET, "/user"))

            status(result) mustBe OK
            contentAsJson(result).as[JsArray].value.size mustBe 2
        }

        "forbid Seller from fetching all users" in {
            val controller = new UserController(stubCC, mockUserService, createFakeJwtAction(sellerUser))(ec)
            
            val result = controller.getAll().apply(FakeRequest(GET, "/user"))

            status(result) mustBe FORBIDDEN
        }
        
        // =========================================================
        // PUT /user/:id/phone - updatePhone
        // =========================================================
        "allow Customer to update their own phone number" in {
            val controller = new UserController(stubCC, mockUserService, createFakeJwtAction(customerUser))(ec)
            val newPhone = "555-1234"
            val json = Json.obj("phone" -> newPhone)
            
            when(mockUserService.updatePhone(customerId, newPhone)).thenReturn(Future.successful(1))

            val request = FakeRequest(PUT, s"/user/$customerId/phone").withBody(json)
            val result = controller.updatePhone(customerId).apply(request)

            status(result) mustBe OK
            (contentAsJson(result) \ "message").as[String] must include("Phone updated successfully")
        }

        "forbid Customer from updating another user's phone number" in {
            val controller = new UserController(stubCC, mockUserService, createFakeJwtAction(customerUser))(ec)
            val json = Json.obj("phone" -> "555-5555")
            
            val request = FakeRequest(PUT, s"/user/$otherCustomerId/phone").withBody(json)
            val result = controller.updatePhone(otherCustomerId).apply(request)

            status(result) mustBe FORBIDDEN
            contentAsString(result) must include("You can only update your own phone number")
        }
        
        // =========================================================
        // DELETE /user/:id - deleteUser
        // =========================================================
        "allow Admin to delete any user" in {
            val controller = new UserController(stubCC, mockUserService, createFakeJwtAction(adminUser))(ec)
            
            when(mockUserService.removeUser(otherCustomerId)).thenReturn(Future.successful(1))

            val result = controller.deleteUser(otherCustomerId).apply(FakeRequest(DELETE, s"/user/$otherCustomerId"))

            status(result) mustBe OK
            (contentAsJson(result) \ "message").as[String] must include("User 102 deleted")
        }

        "allow Customer to delete their own account" in {
            val controller = new UserController(stubCC, mockUserService, createFakeJwtAction(customerUser))(ec)
            
            when(mockUserService.removeUser(customerId)).thenReturn(Future.successful(1))

            val result = controller.deleteUser(customerId).apply(FakeRequest(DELETE, s"/user/$customerId"))

            status(result) mustBe OK
            (contentAsJson(result) \ "message").as[String] must include("User 101 deleted")
        }

        "forbid Customer from deleting another user's account" in {
            val controller = new UserController(stubCC, mockUserService, createFakeJwtAction(customerUser))(ec)
            
            val result = controller.deleteUser(otherCustomerId).apply(FakeRequest(DELETE, s"/user/$otherCustomerId"))

            status(result) mustBe FORBIDDEN
            contentAsString(result) must include("You do not have permission to delete this user")
        }
    }
}