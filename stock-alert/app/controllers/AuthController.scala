package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import services.UserService
import utils.JwtUtils
import scala.concurrent.{ExecutionContext, Future}
import models.{User, Ref, TokenResponse, GoogleUserInfo}
import repositories.{RefRepo, UserRepo}
import sttp.client3._
import sttp.client3.playJson._
import sttp.client3.asynchttpclient.future.AsyncHttpClientFutureBackend
import java.util.Base64
import java.security.MessageDigest
import scala.util.Random
import java.net.URLEncoder
import utils.CryptoUtils

@Singleton
class AuthController @Inject()(
    cc: ControllerComponents,
    service: UserService,
    rr: RefRepo,
    ur: UserRepo
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {


  
  val clientId      = ""
  val clientSecret  = ""
  val redirectUri   = ""
  val authServerUri = ""
  val tokenEndpoint = ""
  // PKCE helper
  private def generateCodeVerifier(): (String, String) = {
    val verifier = Random.alphanumeric.take(64).mkString
    val digest   = MessageDigest.getInstance("SHA-256")
    val challenge = Base64.getUrlEncoder.withoutPadding()
      .encodeToString(digest.digest(verifier.getBytes("US-ASCII")))
    (verifier, challenge)
  }

  // Login redirect
def loginRedirect(): Action[AnyContent] = Action { implicit request =>
  val (codeVerifier, codeChallenge) = generateCodeVerifier()
  val scope = URLEncoder.encode("openid email profile", "UTF-8")

  val stateJson = Json.obj("flowType" -> "login")
  val encodedState = Base64.getUrlEncoder.encodeToString(stateJson.toString().getBytes("UTF-8"))

  val redirectToAuth =
    s"$authServerUri?response_type=code" +
      s"&client_id=$clientId" +
      s"&redirect_uri=$redirectUri" +
      s"&scope=$scope" +
      s"&code_challenge=$codeChallenge" +
      s"&code_challenge_method=S256" +
      s"&access_type=offline" +
      s"&prompt=consent" +
      s"&state=$encodedState"

  Redirect(redirectToAuth).withSession(
    "code_verifier" -> codeVerifier
  )
}


def signupWithOAuth(): Action[Map[String, Seq[String]]] = Action.async(parse.formUrlEncoded) { implicit request =>
  val nameOpt  = request.body.get("name").flatMap(_.headOption)
  val roleOpt  = request.body.get("role").flatMap(_.headOption)
  val phoneOpt = request.body.get("phone").flatMap(_.headOption)

  val phonePattern = "^[0-9]{10}$".r

  (nameOpt, roleOpt) match {
    case (Some(name), Some(role)) =>
      phoneOpt match {
        case Some(phone) if !phonePattern.matches(phone) =>
          Future.successful(BadRequest(Json.obj("error" -> "Invalid phone number.")))

        case _ =>
          val (codeVerifier, codeChallenge) = generateCodeVerifier()
          val scope = URLEncoder.encode("openid email profile", "UTF-8")
          val encryptedPhone = phoneOpt.map(CryptoUtils.encrypt).getOrElse("")

          val stateJson = Json.obj(
            "flowType" -> "signup",
            "name"     -> name,
            "role"     -> role,
            "phone"    -> JsString(encryptedPhone),
            "isPrime"  -> true
          )
          val encodedState = Base64.getUrlEncoder.encodeToString(stateJson.toString().getBytes("UTF-8"))

          val redirectToAuth =
            s"$authServerUri?response_type=code" +
              s"&client_id=$clientId" +
              s"&redirect_uri=$redirectUri" +
              s"&scope=$scope" +
              s"&code_challenge=$codeChallenge" +
              s"&code_challenge_method=S256" +
              s"&access_type=offline" +
              s"&prompt=consent" +
              s"&state=$encodedState"

          Future.successful(Redirect(redirectToAuth).withSession(
            "code_verifier" -> codeVerifier
          ))
      }

    case _ =>
      Future.successful(BadRequest(Json.obj("error" -> "Missing required fields: name, role")))
  }
}


  // Callback for OAuth
def callback(code: String, state: Option[String]): Action[AnyContent] = Action.async { implicit request =>
  (request.session.get("code_verifier"), state) match {
    case (Some(codeVerifier), Some(encodedState)) =>
      val signupDataJsonStr = new String(Base64.getUrlDecoder.decode(encodedState), "UTF-8")
      val signupData = Json.parse(signupDataJsonStr)

      val flowType = request.session.get("flowType")
        .orElse((signupData \ "flowType").asOpt[String])
        .getOrElse("login")

      val name    = (signupData \ "name").asOpt[String].getOrElse("")
      val roleRaw = (signupData \ "role").asOpt[String].getOrElse("user")
      val phone   = (signupData \ "phone").asOpt[String]
      val isPrime = true

      val role = roleRaw.capitalize
      val validRoles = Set("Seller", "Customer", "Admin")

      // Validate role
      if (!validRoles.contains(role)) {
        Future.successful(
          BadRequest(Json.obj("error" -> "Invalid role. Allowed roles: Seller, Customer, Admin"))
        )
      } else {
        implicit val backend = AsyncHttpClientFutureBackend()

        val tokenRequest = basicRequest
          .post(uri"$tokenEndpoint")
          .body(
            Map(
              "grant_type"    -> "authorization_code",
              "code"          -> code,
              "redirect_uri"  -> redirectUri,
              "client_id"     -> clientId,
              "client_secret" -> clientSecret,
              "code_verifier" -> codeVerifier
            )
          )
          .response(asJson[TokenResponse])

        tokenRequest.send(backend).flatMap { tokenResp =>
          tokenResp.body match {
            case Right(token) =>
              val userInfoRequest = basicRequest
                .get(uri"https://www.googleapis.com/oauth2/v2/userinfo")
                .header("Authorization", s"Bearer ${token.access_token}")
                .response(asJson[GoogleUserInfo])

              userInfoRequest.send(backend).flatMap { userInfoResp =>
                userInfoResp.body match {
                  case Right(googleUser) =>
                    googleUser.email match {
                      case Some(finalEmail) =>
                        // Admin email domain restriction
                        if (flowType == "signup" && role == "Admin" && !finalEmail.endsWith("@sece.ac.in")) {
                          Future.successful(
                            Unauthorized(Json.obj("error" -> "Admin accounts must use an @sece.ac.in email"))
                          )
                        } else {
                          ur.fetchByEmail(finalEmail).flatMap {
                            case Some(existingUser) =>
                              flowType match {
                                case "signup" =>
                                  Future.successful(
                                    Conflict(Json.obj("error" -> "Email already exists. Please login instead."))
                                  )

                                case "login" =>
                                  val jwtAccessToken = JwtUtils.createToken(existingUser)
                                  rr.addRef(Ref(None, existingUser.id.get, token.refresh_token.getOrElse(""))).map { _ =>
                                    Ok(Json.obj(
                                      "message"          -> s"Welcome back, ${existingUser.name}",
                                      "email"            -> existingUser.email,
                                      "jwt_access_token" -> jwtAccessToken,
                                      "refresh_token"    -> token.refresh_token
                                    ))
                                  }
                              }

                            case None =>
                              flowType match {
                                case "login" =>
                                  Future.successful(
                                    NotFound(Json.obj("error" -> "No account found. Please sign up first."))
                                  )

                                case "signup" =>
                                  val newUser = User(
                                    id            = None,
                                    name          = name,
                                    email         = finalEmail,
                                    address       = "",
                                    phone         = phone,
                                    notifications = None,
                                    isPrime       = isPrime,
                                    role          = role
                                  )

                                  ur.addUser(newUser).flatMap {
                                    case Right(userId) =>
                                      val jwtAccessToken = JwtUtils.createToken(newUser.copy(id = Some(userId)))
                                      rr.addRef(Ref(None, userId, token.refresh_token.getOrElse(""))).map { _ =>
                                        Ok(Json.obj(
                                          "message"          -> "User registered successfully",
                                          "email"            -> finalEmail,
                                          "jwt_access_token" -> jwtAccessToken,
                                          "refresh_token"    -> token.refresh_token,
                                          "role"             -> role
                                        ))
                                      }

                                    case Left(err) =>
                                      Future.successful(
                                        InternalServerError(Json.obj("error" -> s"User creation failed: $err"))
                                      )
                                  }
                              }
                          }
                        }

                      case None =>
                        Future.successful(InternalServerError(Json.obj("error" -> "Email not found in Google profile")))
                    }

                  case Left(err) =>
                    Future.successful(InternalServerError(Json.obj("error" -> s"Failed to fetch user info: $err")))
                }
              }

            case Left(err) =>
              Future.successful(InternalServerError(Json.obj("error" -> s"Token exchange failed: $err")))
          }
        }
      }

    case _ =>
      Future.successful(BadRequest(Json.obj("error" -> "Missing code_verifier or state for OAuth callback")))
  }
}



  // Renew access token
  def renewAccessToken(): Action[JsValue] = Action.async(parse.json) { req =>
    (req.body \ "ref").asOpt[String] match {
      case Some(refToken) =>
        rr.getRef(refToken).flatMap {
          case Some(ref) =>
            ur.getById(ref.userId).flatMap {
              case Some(user) =>
                val newAccessToken = JwtUtils.createToken(user)
                Future.successful(
                  Ok(Json.obj(
                    "message"       -> "JWT token renewed successfully",
                    "jwt_token"     -> newAccessToken,
                    "refresh_token" -> ref.ref
                  ))
                )
              case None =>
                Future.successful(NotFound(Json.obj("error" -> "User not found for this refresh token")))
            }
          case None =>
            Future.successful(Unauthorized(Json.obj("error" -> "Invalid refresh token")))
        }
      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "Missing refresh token in request body")))
    }
  }

  // Logout
  def logout(): Action[JsValue] = Action.async(parse.json) { req =>
    (req.body \ "ref").asOpt[String] match {
      case Some(refValue) =>
        rr.getRef(refValue).flatMap {
          case Some(_) =>
            rr.deleteRef(refValue).map { rowsDeleted =>
              Ok(Json.obj("message" -> "Ref deleted successfully", "deleted_rows" -> rowsDeleted))
            }
          case None =>
            Future.successful(NotFound(Json.obj("error" -> "Refresh token not found")))
        }
      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "Missing 'ref' field in request body")))
    }
  }

}
