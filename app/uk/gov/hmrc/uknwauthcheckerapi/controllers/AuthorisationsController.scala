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

import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.uknwauthcheckerapi.errors.ErrorResponse
import uk.gov.hmrc.uknwauthcheckerapi.models.{AuthorisationRequest, AuthorisationResponse, AuthorisationsResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton()
class AuthorisationsController @Inject() (cc: ControllerComponents) extends BackendController(cc) {

  def authorisations: Action[JsValue] = Action.async(parse.json) { implicit request =>
    Future.successful(
      request.body.validate[AuthorisationRequest] match {
        case JsSuccess(authorisationRequest: AuthorisationRequest, _) =>
          Ok(
            Json.toJson(
              AuthorisationsResponse(authorisationRequest.date, authorisationRequest.eoris.map(r => AuthorisationResponse(r, authorised = true)))
            )
          )
        case JsError(_) =>
          BadRequest(Json.toJson(ErrorResponse("BAD_REQUEST", "Json is invalid")))
      }
    )
  }

  def methodNotAllowed: Action[AnyContent] = Action {
    MethodNotAllowed(Json.toJson(ErrorResponse("METHOD_NOT_ALLOWED", "This method is not supported")))
  }
}
