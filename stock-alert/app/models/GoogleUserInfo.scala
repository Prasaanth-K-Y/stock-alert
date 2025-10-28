package models

import play.api.libs.json.{Json, OFormat}

case class GoogleUserInfo(
  id: Option[String],
  email: Option[String],
  verified_email: Option[Boolean],
  name: Option[String],
  given_name: Option[String],
  family_name: Option[String],
  picture: Option[String],
  locale: Option[String]
)

object GoogleUserInfo {
  implicit val fmt: OFormat[GoogleUserInfo] = Json.format[GoogleUserInfo]// Provide Json format , used in the controllers (used in Validate)

}
