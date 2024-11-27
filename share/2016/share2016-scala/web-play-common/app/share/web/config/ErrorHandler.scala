package share.web.config

import java.sql.SQLException
import javax.inject.Inject

import com.google.inject.Provider
import com.typesafe.scalalogging.LazyLogging
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router
import play.api.{Configuration, Environment, OptionalSourceMapper}
import share.web.utils.MessageResponseHelper
import yangbajing.utils.s.exception.{SBadException, SBaseException, SInternalErrorException}

import scala.concurrent.Future

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-04-19.
  */
class ErrorHandler @Inject()(env: Environment,
                             config: Configuration,
                             sourceMapper: OptionalSourceMapper,
                             router: Provider[Router])
  extends DefaultHttpErrorHandler(env, config, sourceMapper, router)
    with MessageResponseHelper
    with LazyLogging {

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      case e: SBaseException =>
        if (e.cause == null) {
          logger.warn("[SBaseException]: " + e.toString)
        } else {
          logger.warn("[SBaseException]: " + e.toString, e.cause)
        }
        Future.successful(messageResult(e))

      case e: IllegalArgumentException =>
        logger.warn(e.toString, e)
        Future.successful(messageResult(SBadException(e.getLocalizedMessage)))

      case e: SQLException =>
        logger.warn("[SQLException]: " + e.toString, Option(e.getNextException).getOrElse(e))
        Future.successful(messageResult(SInternalErrorException(e.getLocalizedMessage)))

      case _ =>
        logger.error("onServerError", exception)
        super.onServerError(request, exception)
    }
  }

}
