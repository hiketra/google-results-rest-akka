package main.util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import main.model.{DownstreamError, SearchResult}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  * Created by emma on 21/02/2018.
  */
object ImplicitJsonConversions extends DefaultJsonProtocol with SprayJsonSupport{
    implicit val searchResultJsonFormat: RootJsonFormat[SearchResult] = jsonFormat4(SearchResult)
    implicit val errorJsonFormat: RootJsonFormat[DownstreamError] = jsonFormat1(DownstreamError)
}
