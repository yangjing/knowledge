package yangbajing.utils.s.security

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import com.typesafe.scalalogging.StrictLogging
import yangbajing.utils.s.exception.SInternalErrorException

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-02-07.
  */
class SecurityService extends StrictLogging {
  private val des3 = new TripleDesUtils

  def encrypt(clearText: String): String = {
    try {
      des3.encrypt(clearText)
    } catch {
      case e: Exception =>
        logger.error(s"3des encrypt fail for clearText: $clearText")
        logger.error("encrypt exception", e)
        throw new SInternalErrorException("加密失败")
    }
  }

  def decrypt(encryptedText: String): String = {
    try {
      des3.decrypt(encryptedText)
    } catch {
      case e: Exception =>
        logger.error("3des decrypt fail for encryptedText: {}", encryptedText)
        logger.error("decrypt exception", e)
        throw new SInternalErrorException("解密失败")
    }
  }

  def generateHashAndSalt(clearText: String): String = {
    PasswordUtil.hash(clearText)
  }

  def compareWithHashAndSalt(clearText: String, hashText: String): Boolean = {
    PasswordUtil.verify(clearText, hashText)
  }

  def urlEncode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.name())

}