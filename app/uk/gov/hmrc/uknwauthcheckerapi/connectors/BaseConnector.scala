package uk.gov.hmrc.uknwauthcheckerapi.connectors

import org.apache.pekko.actor.ActorSystem
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.http.Status._
import play.api.libs.json.JsResult
import play.api.libs.json.Reads
import uk.gov.hmrc.http.client.RequestBuilder
import uk.gov.hmrc.http.{HttpResponse, Retries, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._

trait BaseConnector extends Retries {

  override def actorSystem: ActorSystem
  override def configuration: Config

  def retryCondition: PartialFunction[Exception, Boolean] = {
    case e: UpstreamErrorResponse if UpstreamErrorResponse.Upstream5xxResponse.unapply(e).isDefined => true
  }

  implicit class HttpResponseHelpers(response: HttpResponse) {

    def error[A]: Future[A] =
      Future.failed(UpstreamErrorResponse(response.body, response.status))

    def as[A](implicit reads: Reads[A]): Future[A] =
      response.json
        .validate[A]
        .map(result => Future.successful(result))
        .recoverTotal(error => Future.failed(JsResult.Exception(error)))
  }

  implicit class RequestBuilderHelpers(requestBuilder: RequestBuilder) {
    def executeAndDeserialise[T](implicit ec: ExecutionContext, reads: Reads[T]): Future[T] =
      requestBuilder
        .execute[HttpResponse]
        .flatMap { response =>
          response.status match {
            case OK | CREATED | ACCEPTED => response.as[T]
            case _                       =>
              response.error
          }
        }

    def executeAndExpect(expected: Int)(implicit ec: ExecutionContext): Future[Unit] =
      requestBuilder
        .execute[HttpResponse]
        .flatMap { response =>
          response.status match {
            case `expected` => Future.successful(())
            case _          => response.error
          }
        }
  }
}
