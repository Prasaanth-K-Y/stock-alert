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
import io.github.cdimascio.dotenv.Dotenv

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

//Dontenv
  // private val dotenv = Dotenv.load()

  //Dontenv
  private val dotenv = Dotenv.configure().directory("/app").load()

  // ===== OAuth2 Configuration =====
  val clientId      = dotenv.get("GOOGLE_CLIENT_ID")
  val clientSecret  = dotenv.get("GOOGLE_CLIENT_SECRET")
  val redirectUri   = dotenv.get("GOOGLE_REDIRECT_URI")
  val authServerUri = dotenv.get("GOOGLE_AUTH_SERVER_URI")
  val tokenEndpoint = dotenv.get("GOOGLE_TOKEN_ENDPOINT")

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
    println(s"DEBUG Google token request: clientId=$clientId, redirectUri=$redirectUri, tokenEndpoint=$tokenEndpoint")

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
  // Handles OAuth-based signup flow; accepts form-encoded input and redirects to authorization server
def signupWithOAuth(): Action[Map[String, Seq[String]]] =
  Action.async(parse.formUrlEncoded) { implicit request =>
    // Extract form fields from request body
    val nameOpt    = request.body.get("name").flatMap(_.headOption)
    val roleOpt    = request.body.get("role").flatMap(_.headOption)
    val phoneOpt   = request.body.get("phone").flatMap(_.headOption)
    val addressOpt = request.body.get("address").flatMap(_.headOption)

    val phonePattern = "^[0-9]{10}$".r

    // Ensure required fields are present
    (nameOpt, roleOpt) match {
      case (Some(name), Some(role)) =>
        phoneOpt match {
          // Reject if phone format is invalid
          case Some(phone) if !phonePattern.matches(phone) =>
            Future.successful(BadRequest(Json.obj("error" -> "Invalid phone number.")))

          case _ =>
            // Generate PKCE code verifier and challenge
            val (codeVerifier, codeChallenge) = generateCodeVerifier()
            val scope = URLEncoder.encode("openid email profile", "UTF-8")

            // Normalize name to uppercase to avoid duplicates
            val upperName = name.toUpperCase

            // Encrypt phone and hash address for secure transmission
            val encryptedPhone = phoneOpt.map(CryptoUtils.encrypt).getOrElse("")
            val hashedAddress  = addressOpt.map(ArgonUtils.hashAddress).getOrElse("")

            // If phone is provided, check for duplicates before redirecting
            phoneOpt match {
              case Some(phone) =>
                ur.phoneExists(phone).flatMap { existing =>
                  if (existing)
                    Future.successful(Conflict(Json.obj("error" -> "Phone number already exists.")))
                  else {
                    // Construct signup state payload and encode for OAuth redirect
                    val stateJson = Json.obj(
                      "flowType" -> "signup",
                      "name"     -> upperName, 
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

                    // Redirect to OAuth server with encoded state and verifier
                    Future.successful(Redirect(redirectToAuth).withSession("code_verifier" -> codeVerifier))
                  }
                }

              case None =>
                // Proceed without phone check if phone is missing
                val stateJson = Json.obj(
                  "flowType" -> "signup",
                  "name"     -> upperName, 
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
        }

      case _ =>
        // Reject if required fields are missing
        Future.successful(BadRequest(Json.obj("error" -> "Missing required fields: name, role")))
    }
  }

  // =======================================================================
  // OAUTH2 CALLBACK (Handles both signup and login)
  // =======================================================================
  // Handles OAuth callback after user authorization; supports both signup and login flows
def callback(code: String, state: Option[String]): Action[AnyContent] = Action.async { implicit request =>
  // Ensure both code_verifier and state are present in session and query
  (request.session.get("code_verifier"), state) match {
    case (Some(codeVerifier), Some(encodedState)) =>
      // Decode and parse the state payload from OAuth redirect
      val signupDataJsonStr = new String(Base64.getUrlDecoder.decode(encodedState), "UTF-8")
      val signupData = Json.parse(signupDataJsonStr)

      val flowType = (signupData \ "flowType").asOpt[String].getOrElse("login")

      // Extract optional OTP from state or query string
      val otpOpt: Option[Int] =
        (signupData \ "otp").asOpt[Int]
          .orElse(request.getQueryString("otp").flatMap(s => scala.util.Try(s.toInt).toOption))

      val name     = (signupData \ "name").asOpt[String].getOrElse("")
      val roleRaw  = (signupData \ "role").asOpt[String].getOrElse("user")
      val phoneOpt = (signupData \ "phone").asOpt[String]
      val address  = (signupData \ "address").asOpt[String].getOrElse("Unknown")
      val role     = roleRaw.capitalize
      val validRoles = Set("Seller", "Customer", "Admin")

      // Validate role during signup
      if (flowType == "signup" && !validRoles.contains(role)) {
        Future.successful(BadRequest(Json.obj("error" -> "Invalid role. Allowed roles: Seller, Customer, Admin")))
      } else {
        // Exchange authorization code for access token
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
              // Fetch user profile from Google using access token
              val userInfoRequest = basicRequest
                .get(uri"https://www.googleapis.com/oauth2/v2/userinfo")
                .header("Authorization", s"Bearer ${token.access_token}")
                .response(asJson[GoogleUserInfo])

              userInfoRequest.send(backend).flatMap { userInfoResp =>
                userInfoResp.body match {
                  case Right(googleUser) =>
                    //Email duplicate check
                    googleUser.email match {
                      case Some(finalEmail) =>
                        // Restrict Admin signup to institutional domain
                        if (flowType == "signup" && role == "Admin" && !finalEmail.endsWith("@sece.ac.in")) {
                          Future.successful(
                            Unauthorized(Json.obj("error" -> "Admin accounts must use an @sece.ac.in email"))
                          )
                        } else {
                          // Check if user already exists
                          ur.fetchByEmail(finalEmail).flatMap {
                            case Some(existingUser) =>
                              flowType match {
                                case "signup" =>
                                  Future.successful(
                                    Conflict(Json.obj("error" -> "Email already exists. Please login instead."))
                                  )

                                case "login" =>
                                  // Validate OTP if present
                                  otpOpt match {
                                    case Some(otpCode) =>
                                      existingUser.totpSecret match {
                                        // Verify with 2FA secret if present
                                        case Some(secret) if utils.TOTPUtils.verifyCode(secret, otpCode) =>
                                          val jwtAccessToken = JwtUtils.createToken(existingUser)

                                          // Add refresh token to ref table
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

                            case None =>
                              flowType match {
                                case "login" =>
                                  Future.successful(NotFound(Json.obj("error" -> "No account found. Please sign up first.")))

                                case "signup" =>
                                  // Generate TOTP secret and QR code for new user
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
                                  // Add new user to database
                                  ur.addUser(newUser).flatMap {
                                    case Right(userId) =>
                                      // Create access token for new user
                                      val jwtAccessToken = JwtUtils.createToken(newUser.copy(id = Some(userId)))
                                      //QR for 2FA
                                      val qrUrl = s"https://api.qrserver.com/v1/create-qr-code/?data=${URLEncoder.encode(otpAuthUrl, "UTF-8")}&size=200x200"
                                      // Add refresh token to ref table
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

    case _ =>
      // Missing required session or query parameters
      Future.successful(BadRequest(Json.obj("error" -> "Missing code_verifier or state for OAuth callback")))
  }
}

  // =======================================================================
  // RENEW ACCESS TOKEN
  // =======================================================================
  def renewAccessToken(): Action[JsValue] = Action.async(parse.json) { req =>
    (req.body \ "ref").asOpt[String] match {
      case Some(refToken) =>
        //Check if ref is pesent in db
        rr.getRef(refToken).flatMap {
          case Some(ref) =>
            //Check if user is present in db
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

    val qrUrl = s"https://api.qrserver.com/v1/create-qr-code/?data=${URLEncoder.encode(otpUrl, "UTF-8")}&size=200x200"

    Ok(Json.obj(
      "message" -> "Scan this QR code in Google Authenticator",
      "secret"  -> secret,
      "qrUrl"   -> qrUrl
    ))
  }

  def verify2FA: Action[JsValue] = Action(parse.json) { request =>
    val secretOpt = (request.body \ "secret").asOpt[String]
    val codeOpt   = (request.body \ "code").asOpt[Int]

    (secretOpt, codeOpt) match {
      case (Some(secret), Some(code)) =>
        val ok = TOTPUtils.verifyCode(secret, code)
        if (ok) Ok(Json.obj("message" -> "2FA verification successful"))
        else Unauthorized(Json.obj("error" -> "Invalid OTP"))
      case _ =>
        BadRequest(Json.obj("error" -> "Missing secret or code"))
    }
  }
}
