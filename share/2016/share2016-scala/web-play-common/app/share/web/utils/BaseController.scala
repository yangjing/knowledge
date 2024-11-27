package share.web.utils

import play.api.libs.concurrent.Execution
import play.api.mvc.{Controller, RequestHeader}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-16.
  */
trait BaseController  extends Controller with MessageResponseHelper {

  implicit def defaultContext = Execution.Implicits.defaultContext

  @inline
  def getQueryString(key: String)(implicit request: RequestHeader): Option[String] =
    request.getQueryString(key)

  @inline
  def getQueryString(key: String, deft: => String)(implicit request: RequestHeader): String =
    request.getQueryString(key).getOrElse(deft)

}
