package utils

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import play.api.libs.json._
import models.User
import java.time.Clock

object JwtUtils {

  private val secret = "super-secret-key"

  //Validate token and return Users if valid
  def validateToken(token: String): Option[User] = {
    val now = Clock.systemUTC().instant().getEpochSecond

    Jwt.decode(token, secret, Seq(JwtAlgorithm.HS256)).toOption.flatMap { claim =>
      // Check expiration
      claim.expiration match {
        case Some(exp) if exp < now => None
        case _ => Json.parse(claim.content).validate[User].asOpt
      }
    }
  }

  // Generate JWT from Users
  def createToken(user: User, expirationSeconds: Long = 3600): String = {
    val claim = JwtClaim(
      content = Json.toJson(user).toString(),
      expiration = Some(Clock.systemUTC().instant().getEpochSecond + expirationSeconds)
    )
    Jwt.encode(claim, secret, JwtAlgorithm.HS256)
  }
}
