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

package uk.gov.hmrc.uknwauthcheckerapi.models

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.uknwauthcheckerapi.models.eis.{EisAuthorisationResponse, EisAuthorisationsResponse}

import java.time.LocalDate

case class AuthorisationsResponse(date: LocalDate, eoris: Seq[AuthorisationResponse])

object AuthorisationsResponse {
  implicit val format: OFormat[AuthorisationsResponse] = Json.format[AuthorisationsResponse]
  def apply(eisAuthorisationsResponse: EisAuthorisationsResponse): AuthorisationsResponse = {
    new AuthorisationsResponse(eisAuthorisationsResponse.processingDate, eisAuthorisationsResponse.results.map(AuthorisationResponse(_)))
  }
}

case class AuthorisationResponse(eori: String, authorised: Boolean)

object AuthorisationResponse {
  implicit val format: OFormat[AuthorisationResponse] = Json.format[AuthorisationResponse]
  def apply(eisAuthorisationResponse: EisAuthorisationResponse): AuthorisationResponse = {
    new AuthorisationResponse(eisAuthorisationResponse.eori, eisAuthorisationResponse.valid)
  }
}
