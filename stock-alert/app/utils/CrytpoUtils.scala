package utils

import javax.crypto.Cipher
import javax.crypto.spec.{SecretKeySpec, IvParameterSpec}
import java.util.Base64

object CryptoUtils {
  private val key = "itsMePkyitsMePky" 
  private val iv  = "RandomInitVector"  

  def encrypt(value: String): String = {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES")
    val ivSpec = new IvParameterSpec(iv.getBytes("UTF-8"))
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
    Base64.getEncoder.encodeToString(cipher.doFinal(value.getBytes("UTF-8")))
  }

  def decrypt(value: String): String = {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES")
    val ivSpec = new IvParameterSpec(iv.getBytes("UTF-8"))
    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
    new String(cipher.doFinal(Base64.getDecoder.decode(value)), "UTF-8")
  }
}
