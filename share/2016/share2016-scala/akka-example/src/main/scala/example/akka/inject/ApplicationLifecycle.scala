package example.akka.inject

import java.util.concurrent.{Callable, CompletionStage}
import javax.inject.Singleton

import com.typesafe.scalalogging.StrictLogging

import scala.compat.java8.FutureConverters
import scala.concurrent.Future

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-17.
  */
trait ApplicationLifecycle {

  def addStopHook(hook: () => Future[_]): Unit

  def addStopHook(hook: Callable[_ <: CompletionStage[_]]): Unit = {
    addStopHook(() => FutureConverters.toScala(hook.call().asInstanceOf[CompletionStage[_]]))
  }

}

@Singleton
class DefaultApplicationLifecycle extends ApplicationLifecycle with StrictLogging {
  private val mutex = new Object()
  @volatile private var hooks = List.empty[() => Future[_]]

  override def addStopHook(hook: () => Future[_]) = mutex.synchronized {
    hooks = hook :: hooks
  }

  /**
    * Call to shutdown the application.
    *
    * @return A future that will be redeemed once all hooks have executed.
    */
  def stop(): Future[_] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    hooks.foldLeft(Future.successful[Any](())) { (future, hook) =>
      future.flatMap { _ =>
        hook().recover {
          case e => logger.error("Error executing stop hook", e)
        }
      }
    }
  }
}
