package service

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import app.AppConfig
import com.google.inject.{Inject, Singleton}
import java.net.URLEncoder

import akka.http.scaladsl.model.headers.`User-Agent`
import akka.http.scaladsl.unmarshalling.Unmarshal
import model.SearchResult
import org.jsoup.Jsoup
import util.AkkaSystemUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by emma on 19/02/2018.
  */
@Singleton
class GoogleService @Inject()(appConfig: AppConfig) extends AkkaSystemUtils {
  val url = appConfig.googleUrl + "/search?q="
  //user agent required due to google using different style sheets depending on browser...
  val USER_AGENT_VALUE = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36"

  def search(query: String): Future[String] = {

    lazy val request = Http().singleRequest(HttpRequest(HttpMethods.GET,
      url + encode(query),
      headers = List(`User-Agent`(USER_AGENT_VALUE))))

    def handleResp(httpResponse: HttpResponse, responseBody: String): String = {
      httpResponse.status match {
        case StatusCodes.OK =>
          responseBody
        case code => throw new Exception(code.toString()) //would normally avoid throwing an exception and instead use EitherT pattern but seems overkill here
      }
    }

    for {
      response <- request
      respMarshal <- Unmarshal(response.entity).to[String] //loading the response body into a String of HTML
    } yield handleResp(response, respMarshal)
  }

  def encode(query: String): String = {
    URLEncoder.encode(query, "UTF-8")
  }

  def stripOutNthResult(query: String, n: Int): Future[SearchResult] = {
    def extractSearchResultFromHTMLBody(googleResults: String): SearchResult = {
      val doc = Jsoup.parse(googleResults)

      val nthResult = doc.select(s"div.srg .g:nth-child(${n-1}) h3").first()
      val h3Text = nthResult.text()
      val h3Url = nthResult.select("a").first().attr("href")
      SearchResult(h3Url, h3Text)
    }

    search(query).map { googleSearchResults => extractSearchResultFromHTMLBody(googleSearchResults) }
  }
}
