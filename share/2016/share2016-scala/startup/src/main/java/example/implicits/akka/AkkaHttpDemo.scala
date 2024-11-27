package example.implicits.akka

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.stream._
import akka.stream.scaladsl.{Flow, Framing, Sink}
import akka.stream.stage.{GraphStage, GraphStageLogic}
import akka.util.ByteString

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-17.
  */
object AkkaHttpDemo extends Directives {
  implicit val system = ActorSystem("AkkaStream")
  implicit val mat = ActorMaterializer()

  def processing() = {
    val chunkBuffer = Framing.delimiter(ByteString("\n"), 8000, false)
      .map(_.dropRight(1))
      .map(_.utf8String)

    Flow[ByteString]
      .via(chunkBuffer)
  }

  def main(args: Array[String]): Unit = {

    val dataRoute = path("data") {
      post {
        extractRequest { request =>
          val source = request.entity.dataBytes
          val flow = processing()
          val sink = Sink.ignore

          val result = source.via(flow).runWith(sink)
          complete(result)
        }
      }
    }

  }

}

//class PersistentBuffer[A]() extends GraphStage[FlowShape[A, A]] {
//  val in = Inlet[A]("PersistentBuffer.in")
//  val out = Outlet[A]("PersistentBuffer.out")
//
//  override def createLogic(attr: Attributes): GraphStageLogic = {
//    new GraphStageLogic(shape) {
//      var state = initialState[A]
//    }
//    ???
//  }
//
//  override def shape: FlowShape[A, A] = FlowShape.of(in, out)
//}