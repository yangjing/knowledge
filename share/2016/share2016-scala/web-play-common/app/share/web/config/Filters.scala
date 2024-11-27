package share.web.config

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import akka.stream.Materializer
import com.typesafe.scalalogging.StrictLogging
import play.api.http.HttpFilters
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-16.
  */
class Filters @Inject()(/*sdkFilter: SdkFilter,
                        apiFilter: ApiFilter,*/
                        loggingFilter: LoggingFilter) extends HttpFilters {
  override val filters = Seq(loggingFilter /*, sdkFilter, apiFilter*/)
}

//class SdkFilter @Inject()(implicit val mat: Materializer,
//                          implicit val ec: ExecutionContext,
//                          settingComponent: SettingComponent,
//                          sdkService: SdkService,
//                          cacheService: CacheService) extends Filter with MessageResponseHelper {
//
//  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
//    val path = rh.path
//
//    if (settingComponent.filterPaths.existsSdkPaths(path)) {
//      sdkService.getSdkToken(rh) match {
//        case Some(Right((apikey, timestamp, token))) =>
//          sdkService
//            .validationToken(rh.method.toLowerCase, apikey, timestamp, token)
//            .flatMap {
//              case true =>
//                f(rh)
//              case false =>
//                Future.successful(messageResult(SForbiddenException()))
//            }
//            .recover { case e =>
//              logger.error(s"sdk validation error ($apikey, $timestamp, $token)", e)
//              messageResult(SInternalErrorException())
//            }
//
//        case _ =>
//          Future.successful(messageResult(SUnauthorizedException()))
//      }
//    } else {
//      f(rh)
//    }
//  }
//
//}
//
//class ApiFilter @Inject()(implicit val mat: Materializer,
//                          settingComponent: SettingComponent,
//                          webTools: WebTools,
//                          cacheService: CacheService) extends Filter with MessageResponseHelper {
//
//  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
//    val path = rh.path
//    if (settingComponent.filterPaths.existsApiPaths(path)) {
//      webTools.getOwnerToken(rh) match {
//        case Some(Right(_)) =>
//          f(rh)
//        case _ =>
//          Future.successful(messageResult(SUnauthorizedException()))
//      }
//    } else {
//      f(rh)
//    }
//  }
//
//}

class LoggingFilter @Inject()(implicit val mat: Materializer, ec: ExecutionContext) extends Filter with StrictLogging {

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val startTime = System.nanoTime()

    nextFilter(requestHeader).map { result =>
      //      val action = requestHeader.tags(Tags.RouteController) + "." + requestHeader.tags(Tags.RouteActionMethod)
      val endTime = System.nanoTime()
      val requestTime = TimeUnit.NANOSECONDS.toMillis(endTime - startTime)

      logger.info(s"${requestTime}ms and returned ${result.header.status}")

      result.withHeaders("Request-Time" -> requestTime.toString)
    }
  }

}