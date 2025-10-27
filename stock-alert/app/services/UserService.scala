package services

import javax.inject.{Inject, Singleton}
import repositories.UserRepo
import models.User
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(userRepo: UserRepo)(implicit ec: ExecutionContext) {

  // Registers a new user, returns Right(id) if success, Left(msg) if conflict
  def registerUser(user: User): Future[Either[String, Long]] = userRepo.addUser(user)

  // Fetches a user by ID, returns Some(user) or None if not found
  def fetchUser(id: Long): Future[Option[User]] = userRepo.getById(id)

  // Fetches all users, returns a sequence of User
  def fetchAll(): Future[Seq[User]] = userRepo.getAll()

  // Updates the phone number of a user by ID, returns number of rows updated
  def updatePhone(id: Long, newPhone: String): Future[Int] = userRepo.updatePhone(id, newPhone)

  // Deletes a user by ID, returns number of rows deleted
  def removeUser(id: Long): Future[Int] = userRepo.deleteUser(id)

  def getPhone(id: Long): Future[Option[String]] = userRepo.getPhone(id)


  // Fetches a user by email, returns Some(user) or None if not found
  def fetchByEmail(email: String): Future[Option[User]] = userRepo.fetchByEmail(email)
}