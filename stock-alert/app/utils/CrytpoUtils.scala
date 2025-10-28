package utils

import javax.crypto.Cipher
import javax.crypto.spec.{SecretKeySpec, IvParameterSpec}
import java.util.Base64

object CryptoUtils {
  // AES encryption key (must be 16 bytes for AES-128)
  private val key = "itsMePkyitsMePky"
  
  // Initialization Vector (IV) used for AES in CBC mode (must be 16 bytes)
  private val iv  = "RandomInitVector"

  // Encrypts the given string using AES/CBC/PKCS5Padding
  // Returns a Base64-encoded encrypted string
  def encrypt(value: String): String = {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES")
    val ivSpec = new IvParameterSpec(iv.getBytes("UTF-8"))
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
    Base64.getEncoder.encodeToString(cipher.doFinal(value.getBytes("UTF-8")))
  }
  

  // Decrypts a Base64-encoded string encrypted with the above method
  // Returns the original plaintext string
  def decrypt(value: String): String = {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES")
    val ivSpec = new IvParameterSpec(iv.getBytes("UTF-8"))
    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
    new String(cipher.doFinal(Base64.getDecoder.decode(value)), "UTF-8")
  }
}
