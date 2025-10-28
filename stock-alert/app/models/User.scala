package models

import play.api.libs.json._

case class User(
  id: Option[Long],
  name: String,
  email: String,
  address: String,
  phone: Option[String] = None,
  notifications: Option[String] = None,
  isPrime: Boolean = false,
  role: String = "customer",
  totpSecret: Option[String] = None 
)

object User {
  implicit val fmt: OFormat[User] = Json.format[User]// Provide Json format , used in the controllers (used in Validate)

}
