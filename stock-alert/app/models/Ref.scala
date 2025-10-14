package models

import play.api.libs.json._

case class Ref(id: Option[Long], ref: String, userId: Long)

object Ref{
    implicit val RefOF: OFormat[Ref]= Json.format[Ref]  // Provide Json format , used in the controllers (used in Validate)
}