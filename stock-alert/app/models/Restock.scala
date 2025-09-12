package models

import play.api.libs.json._

case class Restock(id: Option[Long], itemId: Long, customerId: Long)

 // Provide Json format , used in the controllers (used in Validate)
object Restock {
  implicit val fmt: OFormat[Restock] = Json.format[Restock]
}
