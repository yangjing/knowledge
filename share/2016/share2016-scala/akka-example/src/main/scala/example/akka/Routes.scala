package example.akka

import javax.inject.{Inject, Singleton}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import example.akka.controllers.{AdminController, ApiController, SystemController}

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-17.
  */
@Singleton
class Routes @Inject()(apiController: ApiController,
                       adminController: AdminController,
                       systemController: SystemController) {

  def apply(): Route =
    apiController() ~
      adminController() ~
      systemController()

}
