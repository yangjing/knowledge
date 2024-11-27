package example.akka.controllers

import javax.inject.{Inject, Singleton}

import akka.http.scaladsl.server.Directives._

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-17.
  */
@Singleton
class ApiController @Inject()() {

  def apply(pathname: String = "api") = {
    pathPrefix(pathname) {
      path("user") {
        (pathEnd & get) {
          complete("user is yangbajing")
        }
      }
    }
  }

}
