package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import services.UserService
import utils.{JwtUtils, Attrs}
import scala.concurrent.{ExecutionContext, Future}
import models.User

@Singleton
class AuthController @Inject()(
    cc: ControllerComponents,
    service: UserService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // Login route - returns JWT
  def login(): Action[JsValue] = Action.async(parse.json) { req =>
    val maybeCreds = for {
      email <- (req.body \ "email").asOpt[String]
      password <- (req.body \ "password").asOpt[String]
    } yield (email, password)

    maybeCreds match {
      case Some((email, password)) =>
        service.fetchByEmail(email).map {
          case Some(user) if user.password == password =>
            val token = JwtUtils.createToken(user)
            Ok(Json.obj("token" -> token))
          case _ =>
            Unauthorized(Json.obj("error" -> "Invalid email or password"))
        }
      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "Missing email or password")))
    }
  }

    // Register user - only Customer can be registered
  def addUser(): Action[JsValue] = Action.async(parse.json) { req =>
    req.body.validate[User].fold(
      err => Future.successful(BadRequest(JsError.toJson(err))),
      user => service.registerUser(user).map {
        case Left(msg) => Conflict(Json.obj("error" -> msg))
        case Right(id) => Created(Json.obj("id" -> id, "message" -> s"User ${user.name} registered"))
      }
    )
  }
}
