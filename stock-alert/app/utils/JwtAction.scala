package utils

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import models.User
import play.api.libs.typedmap.TypedKey
import play.api.mvc._

@Singleton
class JwtActionBuilder @Inject()(defaultParser: BodyParsers.Default)
                                (implicit ec: ExecutionContext)
    extends ActionBuilder[Request, AnyContent] {

  override def parser: BodyParser[AnyContent] = defaultParser
  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    request.headers.get("Authorization").map(_.replace("Bearer ", "")) match {
      case Some(token) =>
        JwtUtils.validateToken(token) match {
          case Some(user: User) =>
            val reqWithUser = request.addAttr(Attrs.User, user)
            block(reqWithUser)
          case None =>
            Future.successful(Results.Unauthorized(Json.obj("error" -> "Invalid or expired token")))
        }
      case None =>
        Future.successful(Results.Unauthorized(Json.obj("error" -> "Missing Authorization header")))
    }
  }
}

object Attrs {
  val User: TypedKey[User] = TypedKey[User]("user")
}
