package main.core

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.RouteResult.route2HandlerFlow
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.google.inject.{AbstractModule, Guice, Injector}
import main.util.AkkaSystemUtils

/**
  * Created by emma on 19/02/2018.
  */

trait MainApp extends AkkaSystemUtils {

  lazy val injector: Injector = Guice.createInjector(new Module())
  lazy val appConfig: AppConfig = injector.getInstance(classOf[AppConfig])
  lazy val routes: Flow[HttpRequest, HttpResponse, NotUsed] = route2HandlerFlow(injector.getInstance(classOf[Routes]).routes)

  def main(args: Array[String]): Unit = {
    Http().bindAndHandle(routes, appConfig.httpHost, appConfig.httpPort)
  }
}

object Main extends MainApp

class Module extends AbstractModule {
  def configure: Unit = {
    //no custom bindings required for now
  }
}
