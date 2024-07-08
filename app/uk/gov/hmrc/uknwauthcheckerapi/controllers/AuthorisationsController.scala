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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.uknwauthcheckerapi.models._
import uk.gov.hmrc.uknwauthcheckerapi.models.eis.EisAuthorisationRequest
import uk.gov.hmrc.uknwauthcheckerapi.services.IntegrationFrameworkService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class AuthorisationsController @Inject() (cc: ControllerComponents,
                                          integrationFrameworkService: IntegrationFrameworkService)
                                         (implicit ec: ExecutionContext)
  extends BackendController(cc) {

  def authorisations: Action[JsValue] = Action.async(parse.json){ implicit request =>
    withJsonBody[AuthorisationRequest] { authorisationRequest =>
      val eisAuthorisationRequest = EisAuthorisationRequest(authorisationRequest)
      integrationFrameworkService.getEisAuthorisations(eisAuthorisationRequest).map(AuthorisationsResponse(_)).map(e => Status(OK)(Json.toJson(e)))
    }
  }

}
