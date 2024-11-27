package example.akka.controllers

import javax.inject.{Inject, Singleton}

import akka.http.scaladsl.server.Directives._
import example.akka.Server

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-17.
  */
@Singleton
class SystemController @Inject()() {

  def apply(pathname: String = "system") =
    pathPrefix(pathname) {
      pathEnd {
        delete {
          new Thread {
            override def run(): Unit = {
              Thread.sleep(1000)
              Server.stop()
            }
          }.start()
          complete("begin stop, please wait...")
        }
      }
    }

}
