package uk.gov.hmrc.uknwauthcheckerapi.services

import cats.data.EitherT
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.uknwauthcheckerapi.connectors.IntegrationFrameworkConnector
import uk.gov.hmrc.uknwauthcheckerapi.models.IntegrationFrameworkError
import uk.gov.hmrc.uknwauthcheckerapi.models.eis.{EisAuthorisationRequest, EisAuthorisationsResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class IntegrationFrameworkService @Inject() (integrationFrameworkConnector: IntegrationFrameworkConnector)
                                            (implicit ec: ExecutionContext) {
  def getEisAuthorisations(authorisationRequest: EisAuthorisationRequest)(implicit
                                                                       hc: HeaderCarrier
  ): EitherT[Future, IntegrationFrameworkError, EisAuthorisationsResponse] =
    EitherT {
      integrationFrameworkConnector
        .getEisAuthorisationsResponse(authorisationRequest)
        .map { eisAuthorisationsResponse =>
          Right(eisAuthorisationsResponse)
        }
        .recover {
          case error @ UpstreamErrorResponse(message, code, _, _)
            if UpstreamErrorResponse.Upstream5xxResponse
              .unapply(error)
              .isDefined || UpstreamErrorResponse.Upstream4xxResponse.unapply(error).isDefined =>
            Left(IntegrationFrameworkError.BadGateway(reason = s"Get EIS Authorisations Failed - $message", code = code))

          case NonFatal(thr) => Left(IntegrationFrameworkError.InternalUnexpectedError(Some(thr)))
        }
    }
}
