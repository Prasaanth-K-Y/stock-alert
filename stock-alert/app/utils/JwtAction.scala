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

  // Define the body parser and execution context
  override def parser: BodyParser[AnyContent] = defaultParser
  override protected def executionContext: ExecutionContext = ec

  // Intercepts each incoming request to validate the JWT token
  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    // Extract token from the Authorization header (Bearer scheme)
    request.headers.get("Authorization").map(_.replace("Bearer ", "")) match {
      case Some(token) =>
        // Validate token and extract user data if valid
        JwtUtils.validateToken(token) match {
          case Some(user: User) =>
            // Attach the user object to request attributes and continue processing
            val reqWithUser = request.addAttr(Attrs.User, user)
            block(reqWithUser)
          case None =>
            // Token is invalid or expired
            Future.successful(Results.Unauthorized(Json.obj("error" -> "Invalid or expired token")))
        }
      case None =>
        // Missing Authorization header
        Future.successful(Results.Unauthorized(Json.obj("error" -> "Missing Authorization header")))
    }
  }
}

// Defines a key to attach the authenticated user to the request attributes
object Attrs {
  val User: TypedKey[User] = TypedKey[User]("user")
}
