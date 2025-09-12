package tables

import slick.jdbc.MySQLProfile.api._
import models.Customers

class CustomersTable(tag: Tag) extends Table[Customers](tag, "customers") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def email = column[String]("email")
  def password = column[String]("password")
  def phone = column[String]("phone")
  def notifications = column[String]("notifications")
   
  // Relates MySQL table and Scala case class
  def * = (id.?, name, email, password, phone, notifications) <> ((Customers.apply _).tupled, Customers.unapply)
}
