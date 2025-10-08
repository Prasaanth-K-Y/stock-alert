package models

import play.api.libs.json._

case class User(
    id: Option[Long],
    name: String,
    email: String,
    password: String,
    phone: Option[String] = None,
    notifications: Option[String] = None,
    isPrime: Boolean = false,
    role: String = "customer" // New field
)

object User {
  implicit val fmt: OFormat[User] = Json.format[User]
}
