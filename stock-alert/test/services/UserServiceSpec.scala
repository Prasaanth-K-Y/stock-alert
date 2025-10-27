package services

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.concurrent.ScalaFutures
import repositories.UserRepo
import models.User
import scala.concurrent.duration._

class UserServiceSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures {

  val mockUserRepo = mock[UserRepo]
  val service      = new UserService(mockUserRepo)

  "UserService" should {

    "registerUser" in {
      val usr: User = User(
        id = Some(1),
        name = "Rock",
        email = "Rock@gmail.com",
        address = "Earth Street",
        phone = Some("9876543210"),
        notifications = Some("enabled"),
        isPrime = false,
        role = "Customer"
      )

      when(mockUserRepo.addUser(any[User])).thenReturn(Future.successful(Right(1)))

      val result       = service.registerUser(usr)
      val awaitResult  = Await.result(result, 1.second)
      assert(awaitResult == Right(1))
    }

    "fetchUser" in {
      val usr: User = User(
        id = Some(1),
        name = "Rock",
        email = "Rock@gmail.com",
        address = "Mars Avenue",
        phone = Some("9876543210"),
        notifications = Some("enabled"),
        isPrime = false,
        role = "Customer"
      )

      when(mockUserRepo.getById(any[Long])).thenReturn(Future.successful(Some(usr)))

      val result      = service.fetchUser(1)
      val awaitResult = Await.result(result, 1.second)

      assert(awaitResult.contains(usr))
    }

    "updatePhone" in {
      when(mockUserRepo.updatePhone(any[Long], any[String])).thenReturn(Future.successful(1))

      val result      = service.updatePhone(1, "908020")
      val awaitResult = Await.result(result, 1.second)

      assert(awaitResult == 1)
    }

    "fetchByEmail" in {
      val usr: User = User(
        id = Some(1),
        name = "Rock",
        email = "Rock@gmail.com",
        address = "Neptune Colony",
        phone = Some("9090909090"),
        notifications = Some("enabled"),
        isPrime = false,
        role = "Customer"
      )

      when(mockUserRepo.fetchByEmail("Rock@gmail.com")).thenReturn(Future.successful(Some(usr)))

      val result      = service.fetchByEmail("Rock@gmail.com")
      val awaitResult = Await.result(result, 1.second)

      assert(awaitResult.contains(usr))
    }
  }
}
