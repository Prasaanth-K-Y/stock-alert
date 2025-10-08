package tables

import slick.jdbc.MySQLProfile.api._
import models.User

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def email = column[String]("email", O.Unique)
  def password = column[String]("password")
  def phone = column[Option[String]]("phone")
  def notifications = column[Option[String]]("notifications")
  def isPrime = column[Boolean]("is_prime", O.Default(false))
  def role = column[String]("role", O.Default("customer")) // New column

  def * = (id.?, name, email, password, phone, notifications, isPrime, role).mapTo[User]
}
