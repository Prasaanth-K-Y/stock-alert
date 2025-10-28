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
import utils._
import com.warrenstrange.googleauth.GoogleAuthenticator
import com.warrenstrange.googleauth.GoogleAuthenticatorKey




/**
 * Authentication Controller — Handles:
 *  1. Google OAuth2 login/signup flows
 *  2. JWT + Refresh Token management
 *  3. Two-Factor Authentication (TOTP)
 */
@Singleton
class AuthController @Inject()(
    cc: ControllerComponents,
    service: UserService,
    rr: RefRepo,
    ur: UserRepo
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  // ===== OAuth2 Configuration =====
  val clientId      = ""
  val clientSecret  = ""
  val redirectUri   = ""
  val authServerUri = ""
  val tokenEndpoint = ""

  /**
   * Generates a PKCE code verifier and challenge pair
   * Used for secure OAuth2 authentication
   */
  private def generateCodeVerifier(): (String, String) = {
    val verifier = Random.alphanumeric.take(64).mkString
    val digest = MessageDigest.getInstance("SHA-256")
    val challenge = Base64.getUrlEncoder.withoutPadding()
      .encodeToString(digest.digest(verifier.getBytes("US-ASCII")))
    (verifier, challenge)
  }

  // ======================================================================
  // LOGIN FLOW (Redirect user to Google OAuth2)
  // =======================================================================
  def loginRedirect(): Action[AnyContent] = Action { implicit request =>
    val (codeVerifier, codeChallenge) = generateCodeVerifier()
    val scope = URLEncoder.encode("openid email profile", "UTF-8")

    // Extract optional OTP from query params
    val otpOpt: Option[Int] =
      request.getQueryString("otp").flatMap(s => scala.util.Try(s.toInt).toOption)

    // Create encoded JSON state (sent to Google, returned in callback)
    val stateJson = Json.obj(
      "flowType" -> "login",
      "otp"      -> Json.toJson(otpOpt.getOrElse(0)) // Use 0 if OTP missing
    )
    val encodedState = Base64.getUrlEncoder.encodeToString(stateJson.toString().getBytes("UTF-8"))

    // Construct Google OAuth URL
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

    Redirect(redirectToAuth).withSession("code_verifier" -> codeVerifier)
  }

  // =======================================================================
  // SIGNUP FLOW (Redirect to Google OAuth2 with signup metadata)
  // ======================================================================
  def signupWithOAuth(): Action[Map[String, Seq[String]]] =
    Action.async(parse.formUrlEncoded) { implicit request =>
      val nameOpt    = request.body.get("name").flatMap(_.headOption)
      val roleOpt    = request.body.get("role").flatMap(_.headOption)
      val phoneOpt   = request.body.get("phone").flatMap(_.headOption)
      val addressOpt = request.body.get("address").flatMap(_.headOption)

      val phonePattern = "^[0-9]{10}$".r

      (nameOpt, roleOpt) match {
        // Valid name & role provided
        case (Some(name), Some(role)) =>
          phoneOpt match {
            //  Invalid phone number format
            case Some(phone) if !phonePattern.matches(phone) =>
              Future.successful(BadRequest(Json.obj("error" -> "Invalid phone number.")))

            //  Valid case
            case _ =>
              val (codeVerifier, codeChallenge) = generateCodeVerifier()
              val scope = URLEncoder.encode("openid email profile", "UTF-8")

              // Encrypt and hash sensitive data before storing/transmitting
              val encryptedPhone = phoneOpt.map(CryptoUtils.encrypt).getOrElse("")
              val hashedAddress  = addressOpt.map(ArgonUtils.hashAddress).getOrElse("")

              // Encode user data in state param for callback
              val stateJson = Json.obj(
                "flowType" -> "signup",
                "name"     -> name,
                "role"     -> role,
                "phone"    -> JsString(encryptedPhone),
                "address"  -> JsString(hashedAddress),
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

              Future.successful(Redirect(redirectToAuth).withSession("code_verifier" -> codeVerifier))
          }

        //  Missing required fields
        case _ =>
          Future.successful(BadRequest(Json.obj("error" -> "Missing required fields: name, role")))
      }
    }

  // =======================================================================
  // OAUTH2 CALLBACK (Handles both signup and login)
  // =======================================================================
  def callback(code: String, state: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    (request.session.get("code_verifier"), state) match {
      //  Valid callback
      case (Some(codeVerifier), Some(encodedState)) =>
        val signupDataJsonStr = new String(Base64.getUrlDecoder.decode(encodedState), "UTF-8")
        val signupData = Json.parse(signupDataJsonStr)

        val flowType = (signupData \ "flowType").asOpt[String].getOrElse("login")

        // Extract optional OTP (used during login)
        val otpOpt: Option[Int] =
          (signupData \ "otp").asOpt[Int]
            .orElse(request.getQueryString("otp").flatMap(s => scala.util.Try(s.toInt).toOption))

        val name     = (signupData \ "name").asOpt[String].getOrElse("")
        val roleRaw  = (signupData \ "role").asOpt[String].getOrElse("user")
        val phoneOpt = (signupData \ "phone").asOpt[String]
        val address  = (signupData \ "address").asOpt[String].getOrElse("Unknown")
        val role     = roleRaw.capitalize
        val validRoles = Set("Seller", "Customer", "Admin")

        //  Invalid role provided during signup
        if (flowType == "signup" && !validRoles.contains(role)) {
          Future.successful(BadRequest(Json.obj("error" -> "Invalid role. Allowed roles: Seller, Customer, Admin")))
        } else {
          implicit val backend = AsyncHttpClientFutureBackend()

          // Step 1️: Exchange authorization code for tokens
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
                // Step 2️: Fetch Google user profile
                val userInfoRequest = basicRequest
                  .get(uri"https://www.googleapis.com/oauth2/v2/userinfo")
                  .header("Authorization", s"Bearer ${token.access_token}")
                  .response(asJson[GoogleUserInfo])

                userInfoRequest.send(backend).flatMap { userInfoResp =>
                  userInfoResp.body match {
                    case Right(googleUser) =>
                      googleUser.email match {
                        case Some(finalEmail) =>
                          // Restrict Admin signup domain
                          if (flowType == "signup" && role == "Admin" && !finalEmail.endsWith("@sece.ac.in")) {
                            Future.successful(
                              Unauthorized(Json.obj("error" -> "Admin accounts must use an @sece.ac.in email"))
                            )
                          } else {
                            ur.fetchByEmail(finalEmail).flatMap {
                              // =====================
                              //  EXISTING USER LOGIN
                              // =====================
                              case Some(existingUser) =>
                                flowType match {
                                  case "signup" =>
                                    Future.successful(
                                      Conflict(Json.obj("error" -> "Email already exists. Please login instead."))
                                    )

                                  case "login" =>
                                    otpOpt match {
                                      //  Verify OTP using TOTP secret
                                      case Some(otpCode) =>
                                        existingUser.totpSecret match {
                                          case Some(secret) if utils.TOTPUtils.verifyCode(secret, otpCode) =>
                                            val jwtAccessToken = JwtUtils.createToken(existingUser)
                                            rr.addRef(Ref(None, existingUser.id.get, token.refresh_token.getOrElse("")))
                                              .map { _ =>
                                                Ok(Json.obj(
                                                  "message"          -> s"Welcome back, ${existingUser.name}",
                                                  "email"            -> existingUser.email,
                                                  "jwt_access_token" -> jwtAccessToken,
                                                  "refresh_token"    -> token.refresh_token
                                                ))
                                              }

                                          case Some(_) =>
                                            Future.successful(Unauthorized(Json.obj("error" -> "Invalid OTP code")))

                                          case None =>
                                            Future.successful(Unauthorized(Json.obj("error" -> "TOTP not configured for this user")))
                                        }

                                      case None =>
                                        Future.successful(BadRequest(Json.obj("error" -> "Missing OTP code in login request")))
                                    }
                                }

                              // =====================
                              //  NEW USER SIGNUP
                              // =====================
                              case None =>
                                flowType match {
                                  case "login" =>
                                    Future.successful(NotFound(Json.obj("error" -> "No account found. Please sign up first.")))

                                  case "signup" =>
                                    // Generate TOTP secret for new user
                                    val (secret, otpAuthUrl) = utils.TOTPUtils.generateSecret(finalEmail)
                                    val newUser = User(
                                      id            = None,
                                      name          = name,
                                      email         = finalEmail,
                                      address       = address,
                                      phone         = phoneOpt,
                                      notifications = None,
                                      isPrime       = true,
                                      role          = role,
                                      totpSecret    = Some(secret)
                                    )

                                    ur.addUser(newUser).flatMap {
                                      case Right(userId) =>
                                        val jwtAccessToken = JwtUtils.createToken(newUser.copy(id = Some(userId)))
                                        val qrUrl = s"https://api.qrserver.com/v1/create-qr-code/?data=${URLEncoder.encode(otpAuthUrl, "UTF-8")}&size=200x200"
                                        rr.addRef(Ref(None, userId, token.refresh_token.getOrElse(""))).map { _ =>
                                          Ok(Json.obj(
                                            "message"          -> "User registered successfully",
                                            "email"            -> finalEmail,
                                            "jwt_access_token" -> jwtAccessToken,
                                            "refresh_token"    -> token.refresh_token,
                                            "role"             -> role,
                                            "otp_setup_url"    -> qrUrl
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

      //  Missing verification/session data
      case _ =>
        Future.successful(BadRequest(Json.obj("error" -> "Missing code_verifier or state for OAuth callback")))
    }
  }

  // =======================================================================
  // RENEW ACCESS TOKEN
  // =======================================================================
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

  // =======================================================================
  // LOGOUT (Delete Refresh Token)
  // =======================================================================
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

  // =======================================================================
  // TWO-FACTOR AUTHENTICATION (TOTP)
  // =======================================================================
  def setup2FA(userEmail: String): Action[AnyContent] = Action { implicit request =>
    val issuer = "StockAlertApp"
    val (secret, otpUrl) = TOTPUtils.generateSecret(userEmail, issuer)

    // Generate QR code for user to scan in Google Authenticator
    val qrUrl = s"https://api.qrserver.com/v1/create-qr-code/?data=${URLEncoder.encode(otpUrl, "UTF-8")}&size=200x200"

    Ok(Json.obj(
      "message" -> "Scan this QR code in Google Authenticator",
      "secret"  -> secret,  // Encrypt before storing in DB
      "qrUrl"   -> qrUrl
    ))
  }

  def verify2FA: Action[JsValue] = Action(parse.json) { request =>
    val secretOpt = (request.body \ "secret").asOpt[String]
    val codeOpt   = (request.body \ "code").asOpt[Int]

    (secretOpt, codeOpt) match {
      case (Some(secret), Some(code)) =>
        val ok = TOTPUtils.verifyCode(secret, code)
        if (ok) Ok("2FA verification successful")
        else Unauthorized("Invalid OTP")
      case _ =>
        BadRequest("Missing secret or code")
    }
  }
}
