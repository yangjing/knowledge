package example.implicits.akka

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-17.
  */
object AkkaStreamDemo {
  implicit val system = ActorSystem("AkkaStream")
  implicit val mat = ActorMaterializer()

  import system.dispatcher

  def baisc0(): Unit = {
    val fast = Source.fromIterator(() => Iterator from 0)
    fast
      .map(x => {
        Thread.sleep(1000)
        x
      })
      .runForeach(println)
  }

  def baisc1(): Unit = {
    val source = Source(1 to 42)
    val flow = Flow[Int].map(_ + 1)
    val sink = Sink.foreachParallel(2)(println)
    val graph = source.via(flow).to(sink)
    graph.run()
  }

  def main(args: Array[String]): Unit = {
//    baisc0()
    baisc1()

    val terminatedStatus = Await.result(system.terminate(), 10.seconds)
    println(terminatedStatus)
  }

}
