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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.uknwauthcheckerapi.models._
import uk.gov.hmrc.uknwauthcheckerapi.models.eis.{EisAuthorisationRequest, EisAuthorisationsResponse}
import uk.gov.hmrc.uknwauthcheckerapi.services.IntegrationFrameworkService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class AuthorisationsController @Inject() (cc: ControllerComponents,
                                          integrationFrameworkService: IntegrationFrameworkService)
                                         (implicit ec: ExecutionContext)
  extends BackendController(cc)
  with ErrorHandler {

  def authorisations: Action[JsValue] = Action.async(parse.json){ implicit request =>
    withJsonBody[AuthorisationRequest] { authorisationRequest =>
      val eisAuthorisationRequest = new EisAuthorisationRequest(authorisationRequest)
      (for {
        eisAuthorisationsResponse <- integrationFrameworkService.getEisAuthorisations(eisAuthorisationRequest).asResponseError
      } yield eisAuthorisationsResponse).convertToResult(OK)
    }
  }

  implicit class ResponseHandler[R](value: EitherT[Future, ResponseError, R]) {

    def convertToResult(responseCode: Int)(implicit c: Converter[R], ec: ExecutionContext): Future[Result] =
      value.fold(
        err => Status(err.code.statusCode)(Json.toJson(err)),
        response => c.getResponseWithCode(response, responseCode)
      )
  }

  trait Converter[R] {
    def getResponseWithCode(response: R, responseCode: Int): Result
  }

  implicit val eisAuthorisationsResponse: Converter[EisAuthorisationsResponse] =
    (response: EisAuthorisationsResponse, responseCode: Int) => Status(responseCode)(Json.toJson(response))
}
