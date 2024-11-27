package yangbajing.utils.s.exception

import yangbajing.utils.s.TMessage

/**
  * 异常
  * Created by Yang Jing (yangbajing@gmail.com) on 2015-04-13.
  */
abstract class SBaseException(val errMsg: String, val errCode: Int, val cause: Throwable) extends RuntimeException(errMsg, cause) with TMessage {
  override def toString = errCode + ": " + super.toString
}

//case class SException(override val code: Int,
//                      override val msg: String,
//                      override val cause: Throwable = null) extends SBaseException(msg, code, cause)

case class SUnauthorizedException(override val errMsg: String = "Unauthorized",
                                  override val errCode: Int = 401,
                                  override val cause: Throwable = null) extends SBaseException(errMsg, errCode, cause)

case class SBadException(override val errMsg: String = "Bad Request",
                         override val errCode: Int = 400,
                         override val cause: Throwable = null) extends SBaseException(errMsg, errCode, cause)

case class SForbiddenException(override val errMsg: String = "Forbidden",
                               override val errCode: Int = 403,
                               override val cause: Throwable = null) extends SBaseException(errMsg, errCode, cause)

case class SNotFoundException(override val errMsg: String = "Not Found",
                              override val errCode: Int = 404,
                              override val cause: Throwable = null) extends SBaseException(errMsg, errCode, cause)

case class SInternalErrorException(override val errMsg: String = "Internal Exception Error",
                                   override val errCode: Int = 500,
                                   override val cause: Throwable = null) extends SBaseException(errMsg, errCode, cause)
