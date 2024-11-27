package yangbajing.utils.s

import java.lang.management.ManagementFactory

import com.google.common.net.UrlEscapers
import yangbajing.utils.s.exception.SBadException

import scala.util.Random

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-04-22.
  */
object Utils {
  final val SAFE_CHARS = ('a' to 'z') ++ ('0' to '9') ++ ('A' to 'Z')

  def printTMessageStack(errMsg: TMessage) = {
    errMsg match {
      case e: SBadException =>
        if (e.cause ne null)
          e.printStackTrace()

      case _ =>
    }
    println("printTMessageStack: " + errMsg)
  }

  @inline def require(requirement: Boolean) {
    if (!requirement)
      throw new SBadException("requirement failed")
  }

  @inline final def require(requirement: Boolean, message: => Any) {
    if (!requirement)
      throw new SBadException(message.toString)
  }

  /**
    * /platform/report.html?id=ent0&companyName=东软集团股份有限公司&personName=刘积仁&idCard=&phone=&card=
    * res0: String = /platform/report.html?id=ent0&companyName=%E4%B8%9C%E8%BD%AF%E9%9B%86%E5%9B%A2%E8%82%A1%E4%BB%BD%E6%9C%89%E9%99%90%E5%85%AC%E5%8F%B8&personName=%E5%88%98%E7%A7%AF%E4%BB%81&idCard=&phone=&card=
    *
    * @param uri
    * @return
    */
  def urlFragmentEscaper(uri: String) = UrlEscapers.urlFragmentEscaper().escape(uri)

  /**
    * /platform/report.html?id=ent0&companyName=东软集团股份有限公司&personName=刘积仁&idCard=&phone=&card=
    * res1: String = %2Fplatform%2Freport.html%3Fid%3Dent0%26companyName%3D%E4%B8%9C%E8%BD%AF%E9%9B%86%E5%9B%A2%E8%82%A1%E4%BB%BD%E6%9C%89%E9%99%90%E5%85%AC%E5%8F%B8%26personName%3D%E5%88%98%E7%A7%AF%E4%BB%81%26idCard%3D%26phone%3D%26card%3D
    *
    * @param uri
    * @return
    */
  def urlFormParameterEscaper(uri: String) = UrlEscapers.urlFormParameterEscaper().escape(uri)

  /**
    * /platform/report.html?id=ent0&companyName=东软集团股份有限公司&personName=刘积仁&idCard=&phone=&card=
    * res2: String = %2Fplatform%2Freport.html%3Fid=ent0&companyName=%E4%B8%9C%E8%BD%AF%E9%9B%86%E5%9B%A2%E8%82%A1%E4%BB%BD%E6%9C%89%E9%99%90%E5%85%AC%E5%8F%B8&personName=%E5%88%98%E7%A7%AF%E4%BB%81&idCard=&phone=&card=
    *
    * @param uri
    * @return
    */
  def urlPathSegmentEscaper(uri: String) = UrlEscapers.urlPathSegmentEscaper().escape(uri)

  def getPid = {
    val runtime = ManagementFactory.getRuntimeMXBean
    runtime.getName.split('@')(0)
  }

  def randomString(size: Int) = {
    require(size > 0, s"size must > 0, current: $size")
    val len = SAFE_CHARS.length
    (0 until size).map(_ => SAFE_CHARS(Random.nextInt(len))).mkString
  }

}
