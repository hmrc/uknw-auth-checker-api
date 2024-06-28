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

import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.uknwauthcheckerapi.models.{AuthorisationRequest, AuthorisationsResponse, EISResult}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton()
class AuthorisationsController @Inject()(cc: ControllerComponents)
    extends BackendController(cc) {

  def authorisations: Action[AuthorisationRequest] = Action.async(parse.json[AuthorisationRequest]) {
    implicit request =>
      Future {
        Ok (Json.toJson(AuthorisationsResponse(request.body.date,
          request.body.eoris.map(r => EISResult(r, authorised = true)))))
      }
  }
}
