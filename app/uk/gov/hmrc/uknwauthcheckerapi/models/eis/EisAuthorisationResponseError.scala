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

package uk.gov.hmrc.uknwauthcheckerapi.models.eis

import play.api.libs.json.{Json, OFormat}

final case class EisAuthorisationResponseError(
  errorDetail: EisAuthorisationResponseErrorDetail
)

object EisAuthorisationResponseError {
  implicit val format: OFormat[EisAuthorisationResponseError] = Json.format[EisAuthorisationResponseError]
}

final case class EisAuthorisationResponseErrorDetail(
  timestamp: String,
  errorCode: Int,
  errorMessage: String,
  sourcePDSFaultDetails: String
)

object EisAuthorisationResponseErrorDetail {
  implicit val format: OFormat[EisAuthorisationResponseErrorDetail] = Json.format[EisAuthorisationResponseErrorDetail]
}