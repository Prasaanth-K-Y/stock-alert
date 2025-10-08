package models

import play.api.libs.json._

case class Orders(id: Option[Long], item: Long, qty: Long)

object Orders{
 implicit val OrdersOF : OFormat[Orders]=Json.format[Orders]  // Provide Json format , used in the controllers (used in Validate)

}