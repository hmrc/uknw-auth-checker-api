/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.uknwauthcheckerapi.controllers

import cats.data.EitherT
import play.api.Logging
import play.api.libs.json.JsError.toJson
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.uknwauthcheckerapi.errors.DataRetrievalError._
import uk.gov.hmrc.uknwauthcheckerapi.errors._
import uk.gov.hmrc.uknwauthcheckerapi.services.{IntegrationFrameworkService, ValidationService}
import uk.gov.hmrc.uknwauthcheckerapi.utils.ExtensionHelpers

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class AuthorisationsController @Inject() (
  cc:                          ControllerComponents,
  integrationFrameworkService: IntegrationFrameworkService,
  validationService:           ValidationService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with HeaderValidator
    with ExtensionHelpers
    with Logging {

  def authorisations: Action[JsValue] = validateHeaders(cc).async(parse.json) { implicit request =>
    (for {
      authorisationsRequest <- EitherT.fromEither[Future](validationService.validateRequest(request))
      response              <- integrationFrameworkService.getAuthorisations(authorisationsRequest)
    } yield response)
      .fold(
        {
          case BadGatewayDataRetrievalError() =>
            logger.error(toLogMessage(BAD_GATEWAY))
            ServiceUnavailableApiError.toResult

          case BadRequestDataRetrievalError(errorMessages) =>
            logger.warn(toLogMessage(BAD_REQUEST, Some(errorMessages)))
            BadRequestApiError(errorMessages).toResult

          case ForbiddenDataRetrievalError(_) =>
            logger.error(toLogMessage(FORBIDDEN))
            ForbiddenApiError.toResult

          case MethodNotAllowedDataRetrievalError(_) =>
            logger.warn(toLogMessage(METHOD_NOT_ALLOWED))
            MethodNotAllowedApiError.toResult

          case ValidationDataRetrievalError(errors) =>
            logger.warn(toLogMessage(BAD_REQUEST, Some(Json.stringify(toJson(errors)))))
            JsonValidationApiError(errors).toResult

          case InternalServerDataRetrievalError(errorMessage) =>
            logger.error(toLogMessage(INTERNAL_SERVER_ERROR, Some(errorMessage)))
            InternalServerApiError.toResult

          case _ =>
            logger.error(toLogMessage(INTERNAL_SERVER_ERROR))
            InternalServerApiError.toResult
        },
        authorisationsResponse => Ok(authorisationsResponse)
      )
  }

  private def toLogMessage(statusCode: Int, errorMessage: Option[String] = None)(implicit request: Request[JsValue]): String = {
    val logMessage = s"[AuthorisationsController][authorisations] error for (${request.method}) [${request.uri}] with status: $statusCode"
    errorMessage match {
      case Some(message) => s"$logMessage and message $message"
      case None          => logMessage
    }
  }
}
