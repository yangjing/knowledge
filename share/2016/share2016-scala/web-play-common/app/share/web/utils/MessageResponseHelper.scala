package share.web.utils

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{Result, Results}
import yangbajing.utils.s.TMessage
import yangbajing.utils.s.exception.{SBaseException, SNotFoundException}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-16.
  */
trait MessageResponseHelper extends LazyLogging {
  @inline
  private def renderJsonMessage(json: JsValue, errCode: Int = 0): Result = {
    if (errCode == 401) Results.Unauthorized(json)
    else Results.Ok(json)
  }

  @inline
  def messageResult(exception: TMessage): Result = {
    if (exception.errCode != 0) {
      exception match {
        case e: SNotFoundException =>
        // do nothing

        case e: SBaseException =>
          logger.warn(e.toString, e.cause)

        case _ =>
          logger.warn(exception.errCode + ":" + exception.errMsg)
      }
    }
    renderJsonMessage(Json.obj("errCode" -> exception.errCode, "errMsg" -> exception.errMsg))
  }

  @inline
  def jsonResult[T](opt: Option[T])(implicit tjs: Writes[T]): Result = {
    opt match {
      case Some(bo) => jsonResult(bo)
      case None => messageResult(SNotFoundException())
    }
  }

  @inline
  def jsonResult[T](opt: Either[TMessage, T])(implicit tjs: Writes[T]): Result = {
    opt match {
      case Right(bo) => jsonResult(bo)
      case Left(msg: TMessage) => messageResult(msg)
    }
  }

  @inline
  def jsonResult[T](bo: T)(implicit tjs: Writes[T]): Result = {
    renderJsonMessage(Json.toJson(bo))
  }

}
