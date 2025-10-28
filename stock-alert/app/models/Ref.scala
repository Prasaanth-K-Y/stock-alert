package models

import play.api.libs.json._



case class Ref(id: Option[Long] = None, 
              userId: Long, 
              ref: String)

object Ref {
  implicit val refFormat: OFormat[Ref] = Json.format[Ref]
}
