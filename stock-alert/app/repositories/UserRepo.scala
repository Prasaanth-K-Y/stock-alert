package repositories

import models.User
import tables.UserTable
import slick.jdbc.MySQLProfile.api._
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{Future, ExecutionContext}
import utils.CryptoUtils

@Singleton
class UserRepo @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // Database configuration and table reference
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db
  private val users = TableQuery[UserTable]

  // Adds a new user if email and name are unique
  // Returns Either[String, Long] to indicate success or duplicate error
  def addUser(u: User): Future[Either[String, Long]] = {
    db.run(users.filter(_.email === u.email).result.headOption).flatMap {
      case Some(_) => Future.successful(Left(s"User with email ${u.email} already exists"))
      case None =>
        db.run(users.filter(_.name === u.name).result.headOption).flatMap {
          case Some(_) => Future.successful(Left(s"User with name ${u.name} already exists"))
          case None =>
            db.run((users returning users.map(_.id)) += u).map(Right(_))
        }
    }
  }

  // Adds a new notification for a user
  // If notifications already exist, appends using a comma separator
  // Returns the number of rows affected (1 if success, 0 if user not found)
  def updateNotifications(id: Long, notification: String): Future[Int] = {
    db.run(users.filter(_.id === id).result.headOption).flatMap {
      case Some(user) =>
        val updatedNotifications = user.notifications match {
          case Some(existing) => Some(existing + " ," + notification)
          case None           => Some(notification)
        }
        val updatedUser = user.copy(notifications = updatedNotifications)
        db.run(users.filter(_.id === id).update(updatedUser))
      case None => Future.successful(0)
    }
  }

  // Fetch user by ID
  def getById(id: Long): Future[Option[User]] =
    db.run(users.filter(_.id === id).result.headOption)

  // Fetch all users
  def getAll(): Future[Seq[User]] =
    db.run(users.result)

  // Fetch a user by email
  def fetchByEmail(email: String): Future[Option[User]] =
    db.run(users.filter(_.email === email).result.headOption)

  // Update a user's phone number
  def updatePhone(id: Long, phone: String): Future[Int] =
    db.run(users.filter(_.id === id).map(_.phone).update(Some(phone)))

  // Delete user by ID
  def deleteUser(id: Long): Future[Int] =
    db.run(users.filter(_.id === id).delete)

  // Retrieve user's phone number (flattened from Option[Option[String]])
  def getPhone(id: Long): Future[Option[String]] =
    db.run(users.filter(_.id === id).map(_.phone).result.headOption).map(_.flatten)

// Update a user's role
def updateRole(id: Long, newRole: String): Future[Int] = {
  db.run(users.filter(_.id === id).map(_.role).update(newRole))
}
  // Check Users phone number exists
  def phoneExists(phone: String): Future[Boolean] = {
  val encryptedPhone = CryptoUtils.encrypt(phone)
  db.run(users.filter(_.phone === Option(encryptedPhone)).exists.result)
}

}
