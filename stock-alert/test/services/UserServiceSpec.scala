package services

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.concurrent.ScalaFutures
import repositories.UserRepo
import models.User
import shared.notification.{StringServiceGrpc, StringMessage}
import scala.concurrent.duration._
import org.scalatest.funsuite.AnyFunSuite

class UserServiceSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures {

    val mockUserRepo   = mock[UserRepo]

    
    val service = new UserService(mockUserRepo)

    "UserService" should {

       "regiterUser" in {
        val usr : User = User(     id = Some(1),
                            name = "Rock",
                            email = "Rock@gmail.com",
                            password = "123456",
                            role = "Customer")
       

       when(mockUserRepo.addUser(any[User])).thenReturn(Future.successful(Right(1)))

        val result: Future[Either[String, Long]]= service.registerUser(usr)
        val awaitResult = Await.result(result, 1.second)
        

        assert(awaitResult ==  Right(1))
    }
    

    "fetchUser" in {

        val usr : User = User(     id = Some(1),
                            name = "Rock",
                            email = "Rock@gmail.com",
                            password = "123456",
                            role = "Customer")
       when(mockUserRepo.getById(any[Long])).thenReturn(Future.successful(Some(usr)))

        val result: Future[Option[User]]= service.fetchUser(1)
        val awaitResult = Await.result(result, 1.second)
        

        assert(awaitResult ==  Some(usr))
    }
    

    "updatePhone" in {
       when(mockUserRepo.updatePhone(any[Long], any[String])).thenReturn(Future.successful(1))

        val result= service.updatePhone(1,"908020")
        val awaitResult = Await.result(result, 1.second)
        

        assert(awaitResult == 1 )
    }
    
    "fetchByEmail" in {

        val usr : User = User(     id = Some(1),
                            name = "Rock",
                            email = "Rock@gmail.com",
                            password = "123456",
                            role = "Customer")

       when(mockUserRepo.fetchByEmail("Rock@gmail.com")).thenReturn(Future.successful(Some(usr)))

        val result= service.fetchByEmail("Rock@gmail.com")
        val awaitResult = Await.result(result, 1.second)
        

        assert(awaitResult ==  Some(usr))
    }
    }
    }