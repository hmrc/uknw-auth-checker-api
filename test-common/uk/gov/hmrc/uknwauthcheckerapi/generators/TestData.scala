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

package uk.gov.hmrc.uknwauthcheckerapi.generators

import org.scalacheck.Arbitrary
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.uknwauthcheckerapi.models.AuthorisationRequest
import uk.gov.hmrc.uknwauthcheckerapi.models.eis.{EisAuthorisationResponse, EisAuthorisationsResponse}
import uk.gov.hmrc.uknwauthcheckerapi.utils.EisAuthTypes

import java.time.LocalDate

trait TestData extends Generators {

  val authorisationEndpoint = "authorisation"
  val emptyJson: JsValue = Json.parse("{}")

  val invalidAuthTypeEisErrorMessage: String = """Invalid authorisation type : UKNW""".stripMargin
  val invalidEorisEisErrorMessage:    String = """Invalid format of EORI(s): 0000000001,0000000003""".stripMargin
  val invalidDateEisErrorMessage:     String = """Invalid supplied date(Date format should be - YYYY-MM-DD) : 202-01-01""".stripMargin
  val invalidMixedEisErrorMessage: String =
    """Invalid format of EORI(s): 0000000001,0000000003,Invalid supplied date(Date format should be - YYYY-MM-DD) : 202-01-01""".stripMargin

  def randomAuthorisationRequest: AuthorisationRequest = arbAuthorisationRequest.arbitrary.sample.get

  implicit val arbValidAuthorisationRequest: Arbitrary[ValidAuthorisationRequest] = Arbitrary {
    for {
      date  <- Arbitrary.arbitrary[LocalDate]
      eoris <- eorisGen
    } yield ValidAuthorisationRequest(
      AuthorisationRequest(
        date.toLocalDateFormatted,
        eoris
      )
    )
  }

  implicit val arbValidEisAuthorisationsResponse: Arbitrary[ValidEisAuthorisationsResponse] = Arbitrary {
    for {
      date  <- Arbitrary.arbitrary[LocalDate]
      eoris <- eorisGen
    } yield ValidEisAuthorisationsResponse(
      EisAuthorisationsResponse(
        date,
        EisAuthTypes.NopWaiver,
        eoris.map(e => EisAuthorisationResponse(e, valid = true, 0))
      )
    )
  }
}

final case class ValidAuthorisationRequest(
  request: AuthorisationRequest
)

final case class ValidEisAuthorisationsResponse(
  response: EisAuthorisationsResponse
)
