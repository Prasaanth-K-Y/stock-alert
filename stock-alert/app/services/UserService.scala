package services

import javax.inject.{Inject, Singleton}
import repositories.UserRepo
import models.User
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(userRepo: UserRepo)(implicit ec: ExecutionContext) {

  def registerUser(user: User): Future[Either[String, Long]] = userRepo.addUser(user)
  def fetchUser(id: Long): Future[Option[User]] = userRepo.getById(id)
  def fetchAll(): Future[Seq[User]] = userRepo.getAll()
  def updatePhone(id: Long, newPhone: String): Future[Int] = userRepo.updatePhone(id, newPhone)
  def removeUser(id: Long): Future[Int] = userRepo.deleteUser(id)
  def fetchByEmail(email: String): Future[Option[User]] = userRepo.fetchByEmail(email)
}
