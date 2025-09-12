package models

import play.api.libs.json._

case class Customers(id: Option[Long], name: String, email: String, password: String, phone: String, notifications: String )

 // Provide Json format , used in the controllers (used in Validate)
object Customers {
  implicit val fmt: OFormat[Customers] = Json.format[Customers]
}
