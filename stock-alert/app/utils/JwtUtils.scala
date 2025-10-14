package utils

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import play.api.libs.json._
import models.User
import java.time.Clock

object JwtUtils {

  private val secret = "HelloThisForTest"
  
  private val Refsecret = "07E63901B31899144C0752538E22C796F6841E3B502947F249961A3485E0E45A8296A51413DA37B9C7F72491124C286591A235D62468F4A633F231D56208B1C6"


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
  // Generate RefJWT from Users
   def RefcreateToken(user: User, expirationSeconds: Long = 36000): String = {
    val claim = JwtClaim(
      content = Json.toJson(user).toString(),
      expiration = Some(Clock.systemUTC().instant().getEpochSecond + expirationSeconds)
    )
    Jwt.encode(claim, Refsecret, JwtAlgorithm.HS256)
  }

  //Validate Reftoken and return Users if valid
  def refValidateToken(token: String): Option[User] = {
    val now = Clock.systemUTC().instant().getEpochSecond

    Jwt.decode(token, Refsecret, Seq(JwtAlgorithm.HS256)).toOption.flatMap { claim =>
      // Check expiration
      claim.expiration match {
        case Some(exp) if exp < now => None
        case _ => Json.parse(claim.content).validate[User].asOpt
      }
    }
  }

  
}
