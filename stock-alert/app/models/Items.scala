package models

import play.api.libs.json._

case class Items(id: Option[Long], name: String, stock: Long, minStock: Long)

object Items{
    implicit val ItemsOF: OFormat[Items]= Json.format[Items] // Provide Json format , used in the controllers
}
