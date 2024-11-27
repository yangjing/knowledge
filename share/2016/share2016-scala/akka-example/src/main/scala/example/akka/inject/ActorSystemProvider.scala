package example.akka.inject

import javax.inject.{Inject, Provider, Singleton}

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-17.
  */
@Singleton
class ActorSystemProvider @Inject()(applicationLifecycle: DefaultApplicationLifecycle) extends Provider[ActorSystem] with StrictLogging {
  override def get(): ActorSystem = {
    val system = ActorSystem()
    applicationLifecycle.addStopHook(() => {
      system.terminate()
      // wait until it is shutdown
      val status = Await.result(system.whenTerminated, Duration.Inf)
      Future.successful(status)
    })
    system
  }
}

@Singleton
class MaterializerProvider @Inject()(implicit val system: ActorSystem) extends Provider[Materializer] {
  override def get(): Materializer = ActorMaterializer()
}

@Singleton
class ExecutionContextProvider @Inject()(actorSystem: ActorSystem) extends Provider[ExecutionContextExecutor] {
  def get = actorSystem.dispatcher
}