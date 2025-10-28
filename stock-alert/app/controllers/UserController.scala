package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import services.UserService
import utils.{JwtActionBuilder, Attrs}
import models.User 
import utils.CryptoUtils


@Singleton
class UserController @Inject()(
    cc: ControllerComponents,
    service: UserService,
    jwtAction: JwtActionBuilder
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // GET /user/:id - Admin only
  def getUser(id: Long): Action[AnyContent] = jwtAction.async { request =>
    val user = request.attrs(Attrs.User)
    if (user.role == "Admin") {
      service.fetchUser(id).map {
        case Some(u) => Ok(Json.toJson(u))
        case None    => NotFound(Json.obj("error" -> "User not found"))
      }
    } else Future.successful(Forbidden("Only Admins can access this resource"))
  }

  // GET /user - Admin only
  def getAll(): Action[AnyContent] = jwtAction.async { request =>
    val user = request.attrs(Attrs.User)
    if (user.role == "Admin") {
      service.fetchAll().map(u => Ok(Json.toJson(u)))
    } else Future.successful(Forbidden("Only Admins can access this resource"))
  }

  // PUT /user/:id/phone - Customer can update their own phone
def updatePhone(id: Long): Action[JsValue] = jwtAction(parse.json).async { request =>
  val user = request.attrs(Attrs.User)

  if ( user.id.getOrElse(0L) != id) {
    Future.successful(Forbidden("You can only update your own phone number"))
  } else {
    (request.body \ "phone").asOpt[String] match {
      case Some(phone) =>
        val encryptedPhone = CryptoUtils.encrypt(phone)
        service.updatePhone(id, encryptedPhone).map { updatedRows =>
          if (updatedRows > 0) Ok(Json.obj("message" -> "Phone updated successfully"))
          else NotFound(Json.obj("error" -> "User not found"))
        }

      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "Missing phone field")))
    }
  }
}


  // DELETE /user/:id - Admin or Customer deleting own account
  def deleteUser(id: Long): Action[AnyContent] = jwtAction.async { request =>
    val user = request.attrs(Attrs.User)
    if (user.role == "Admin" || (user.role == "Customer" && user.id.contains(id))) {
      service.removeUser(id).map { deleted =>
        if (deleted > 0) Ok(Json.obj("message" -> s"User $id deleted"))
        else NotFound(Json.obj("error" -> "User not found"))
      }
    } else Future.successful(Forbidden("You do not have permission to delete this user"))
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
  def getPhone(id: Long): Action[AnyContent] = jwtAction.async { request =>
  val user = request.attrs(Attrs.User)

  if ( user.id.getOrElse(0L) != id) {
    Future.successful(Forbidden("You can only view your own phone number"))
  } else {
    service.getPhone(id).map {
      case Some(encryptedPhone) =>
        val decryptedPhone = CryptoUtils.decrypt(encryptedPhone)
        Ok(Json.obj("phone" -> decryptedPhone))

      case None =>
        NotFound(Json.obj("error" -> "User not found"))
    }
  }
}

}
