package utils

import com.warrenstrange.googleauth.{GoogleAuthenticator, GoogleAuthenticatorKey}
import java.net.URLEncoder

object TOTPUtils {
  private val gAuth = new GoogleAuthenticator()

  // Step 1: Generate secret for user signup
def generateSecret(userEmail: String, issuer: String = "StockAlertApp"): (String, String) = {
  val key: GoogleAuthenticatorKey = gAuth.createCredentials()
  val secret = key.getKey
  val otpAuthUrl = s"otpauth://totp/$issuer:$userEmail?secret=$secret&issuer=$issuer"
  (secret, otpAuthUrl)
}


  // Step 2: Verify code user enters
  def verifyCode(secret: String, code: Int): Boolean = {
    val verifier = new GoogleAuthenticator()
    verifier.authorize(secret, code)
  }
}
