package utils

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import models.User

/** A fake JwtActionBuilder for testing that skips JWT validation. */
@Singleton
class FakeJwtAction @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext)
  extends JwtActionBuilder(parser) {

  /** Mutable role so each test can switch user roles easily. */
  @volatile private var currentRole: String = "customer"

  def setRole(role: String): Unit = currentRole = role

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    val fakeUser = User(
      id = Some(1L),
      name = "Test User",
      email = "test@example.com",
      password = "secret",
      phone = Some("1234567890"),
      notifications = Some(""),
      isPrime = true,
      role = currentRole
    )
    val reqWithUser = request.addAttr(Attrs.User, fakeUser)
    block(reqWithUser)
  }
}