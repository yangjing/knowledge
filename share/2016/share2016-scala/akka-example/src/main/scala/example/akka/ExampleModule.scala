package example.akka

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.inject.AbstractModule
import example.akka.inject.{ActorSystemProvider, ExecutionContextProvider, MaterializerProvider}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-17.
  */
class ExampleModule() extends AbstractModule {

  override protected def configure(): Unit = {
    bind(classOf[ActorSystem]).toProvider(classOf[ActorSystemProvider])
    bind(classOf[Materializer]).toProvider(classOf[MaterializerProvider])
    bind(classOf[ExecutionContextExecutor]).toProvider(classOf[ExecutionContextProvider])
    bind(classOf[ExecutionContext]).to(classOf[ExecutionContextExecutor])
  }

}
