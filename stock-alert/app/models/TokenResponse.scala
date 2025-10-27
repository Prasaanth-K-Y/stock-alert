package models

import play.api.libs.json.{Json, OFormat}

case class TokenResponse(
  access_token: String,
  expires_in: Long,
  refresh_token: Option[String],
  scope: Option[String],
  token_type: String
)

object TokenResponse {
  implicit val fmt: OFormat[TokenResponse] = Json.format[TokenResponse]
}
