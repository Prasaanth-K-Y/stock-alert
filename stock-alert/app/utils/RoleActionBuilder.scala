package utils

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import models.User
import play.api.mvc.Results.Forbidden

class RoleActionBuilder(role: String, jwtAction: ActionBuilder[Request, AnyContent])(implicit ec: ExecutionContext) {

  // Generic role-based wrapper
  def apply[A](bodyParser: BodyParser[A])(block: Request[A] => Future[Result]): Action[A] =
    jwtAction.async(bodyParser) { request =>
      val user = request.attrs(Attrs.User)
      if (user.role == role || (role == "Customer" && user.role == "Customer")) {
        block(request)
      } else {
        Future.successful(Forbidden("You do not have permission to access this resource"))
      }
    }

  // Overload for AnyContent (default)
  def apply(block: Request[AnyContent] => Future[Result]): Action[AnyContent] =
    apply(jwtAction.parser)(block)
}
