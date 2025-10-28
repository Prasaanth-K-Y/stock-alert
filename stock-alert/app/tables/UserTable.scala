package tables

import slick.jdbc.MySQLProfile.api._
import models.User

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def email = column[String]("email", O.Unique)
def address = column[String]("address")   
  def phone = column[Option[String]]("phone")
  def notifications = column[Option[String]]("notifications")
  def isPrime = column[Boolean]("is_prime", O.Default(false))
  def totpSecret = column[Option[String]]("totp_secret")
  def role = column[String]("role", O.Default("customer")) 

    // Relates MySQL table and Scala case class

def * = (id.?, name, email, address, phone, notifications, isPrime, role, totpSecret).mapTo[User]  
}
