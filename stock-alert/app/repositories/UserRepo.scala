package repositories

import models.User
import tables.UserTable
import slick.jdbc.MySQLProfile.api._
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{Future, ExecutionContext}

@Singleton
class UserRepo @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db
  private val users = TableQuery[UserTable]

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
  // Adds a new notification to a user
// If notifications already exist, appends with a comma separator
// Returns the number of rows affected
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

  def getById(id: Long): Future[Option[User]] = db.run(users.filter(_.id === id).result.headOption)
  def getAll(): Future[Seq[User]] = db.run(users.result)
  def fetchByEmail(email: String): Future[Option[User]] = db.run(users.filter(_.email === email).result.headOption)
  def updatePhone(id: Long, phone: String): Future[Int] = db.run(users.filter(_.id === id).map(_.phone).update(Some(phone)))
  def deleteUser(id: Long): Future[Int] = db.run(users.filter(_.id === id).delete)
}
