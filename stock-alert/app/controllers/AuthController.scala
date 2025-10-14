package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import services.UserService
import utils.{JwtUtils, Attrs}
import scala.concurrent.{ExecutionContext, Future}
import models.User
import repositories.{RefRepo,UserRepo}
import models.Ref
import utils.JwtUtils.refValidateToken

@Singleton
class AuthController @Inject()(
    cc: ControllerComponents,
    service: UserService,
    rr : RefRepo,
    ur : UserRepo
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // Login route - returns JWT
  def login(): Action[JsValue] = Action.async(parse.json) { req =>
  val maybeCreds = for {
    email <- (req.body \ "email").asOpt[String]
    password <- (req.body \ "password").asOpt[String]
  } yield (email, password)

  maybeCreds match {
    case Some((email, password)) =>
      service.fetchByEmail(email).flatMap {
        case Some(user) if user.password == password =>
          val token = JwtUtils.createToken(user)
          val reftoken = JwtUtils.RefcreateToken(user)
          // rr.addRef returns Future[Long]
          rr.addRef(Ref(None, reftoken, user.id.getOrElse(0L))).map { r =>
            if (r > 0) Ok(Json.obj("token" -> token, "Reftoken" -> reftoken))
            else NotFound("Token is not inserted")
          }
        case _ =>
          Future.successful(Unauthorized(Json.obj("error" -> "Invalid email or password")))
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

def logout(): Action[JsValue] = Action.async(parse.json) { req =>
    req.body.validate[Ref].fold(
      err => Future.successful(BadRequest(JsError.toJson(err))),
      ref => rr.deleteRef(ref.ref).flatMap {
        case rowsDeleted  if rowsDeleted > 0 => Future.successful(Ok(s"${rowsDeleted}"))
        case _ => Future.successful(NotFound("No ref found"))
      } 
      
      )
  }
   def renewAccessToken(): Action[JsValue] = Action.async(parse.json) { req =>
    val  reftoken = (req.body \ "ref").asOpt[String]
    val id  = (req.body \ "userId").asOpt[Long]

  
    reftoken match {
      case Some(reftoken)  =>{
      refValidateToken(reftoken) match {
        case Some(user:User) if user.id == id =>{
          
          rr.getRef(reftoken).flatMap{
            case Some(ref) => {
              val token = JwtUtils.createToken(user)
              Future.successful(Ok(Json.obj("token" -> token)))
            }
          case None =>
            Future.successful(NotFound(Json.obj("error" -> s"Reftoken not found")))
          }
        }
        case None => 
          Future.successful(Unauthorized(Json.obj("error" -> "Invalid or expired token")))
      }
      }
      case None =>
          Future.successful(BadRequest(Json.obj("error" -> "No token")))
    }
  }

  def expiry():Action[AnyContent] =Action.async{
    rr.garbageCollector().map{dlt=>
      Ok(Json.obj("deleted" -> dlt))
    }
  }
}
