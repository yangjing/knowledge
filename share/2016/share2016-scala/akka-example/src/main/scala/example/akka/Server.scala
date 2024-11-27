package example.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.google.inject.Guice
import com.typesafe.scalalogging.StrictLogging
import example.akka.inject.DefaultApplicationLifecycle

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.util.{Failure, Success}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-17.
  */
object Server extends StrictLogging {
  val injector = Guice.createInjector(new ExampleModule())

  def stop(): Unit = {
    val future = injector.getInstance(classOf[DefaultApplicationLifecycle]).stop()
    val status = Await.result(future, 60.seconds)
    logger.info("stop status: " + status)
  }

  def main(args: Array[String]): Unit = {
    implicit val system = injector.getInstance(classOf[ActorSystem])
    implicit val dispatcher = injector.getInstance(classOf[ExecutionContextExecutor])
    implicit val mat = injector.getInstance(classOf[Materializer])
    val route = injector.getInstance(classOf[Routes])

    val bindingFuture = Http().bindAndHandle(route(), "localhost", 8080)

    bindingFuture.onComplete {
      case Success(binding) => logger.info(binding.toString)
      case Failure(e) =>
        e.printStackTrace()
        stop()
    }
  }

}
