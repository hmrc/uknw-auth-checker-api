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

import java.time.ZonedDateTime

import org.scalacheck.Arbitrary

import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.uknwauthcheckerapi.models.AuthorisationRequest
import uk.gov.hmrc.uknwauthcheckerapi.models.constants.MinMaxValues
import uk.gov.hmrc.uknwauthcheckerapi.models.eis._

trait TestData extends Generators {

  protected val authorisationEndpoint:          String  = "authorisation"
  protected val bearerToken:                    String  = "Bearer PFZBTElEX1RPS0VOPg=="
  protected val emptyErrorMessage:              String  = ""
  protected val emptyJson:                      JsValue = Json.parse("{}")
  protected val invalidAuthTypeEisErrorMessage: String  = """Invalid authorisation type : UKNW""".stripMargin
  protected val invalidEorisEisErrorMessage:    String  = """Invalid format of EORI(s): 0000000001,0000000003""".stripMargin

  implicit protected val arbValidAuthorisationRequest: Arbitrary[ValidAuthorisationRequest] = Arbitrary {
    for {
      eoris <- eoriGenerator()
    } yield ValidAuthorisationRequest(
      AuthorisationRequest(
        eoris
      )
    )
  }

  implicit protected val arbValidEisAuthorisationsResponse: Arbitrary[ValidEisAuthorisationsResponse] = Arbitrary {
    for {
      dateTime <- Arbitrary.arbitrary[ZonedDateTime]
      eoris    <- eoriGenerator()
    } yield ValidEisAuthorisationsResponse(
      EisAuthorisationsResponse(
        dateTime,
        EisAuthTypes.nopWaiver,
        eoris.map(e => EisAuthorisationResponse(e, valid = true, 0))
      )
    )
  }

  implicit protected val arbTooManyEorisAuthorisationRequest: Arbitrary[TooManyEorisAuthorisationRequest] = Arbitrary {
    for {
      eoris <- eoriGenerator(MinMaxValues.maxEoriCount + 1, MinMaxValues.maxEoriCount + 5)
    } yield TooManyEorisAuthorisationRequest(
      AuthorisationRequest(
        eoris
      )
    )
  }

  implicit protected val arbNoEorisAuthorisationRequest: Arbitrary[NoEorisAuthorisationRequest] = Arbitrary {
    NoEorisAuthorisationRequest(
      AuthorisationRequest(
        Seq.empty
      )
    )
  }

  implicit protected val arbInvalidEorisAuthorisationRequest: Arbitrary[InvalidEorisAuthorisationRequest] = Arbitrary {
    for {
      randomString <- Arbitrary.arbitrary[String].filterNot(_.isEmpty)
    } yield InvalidEorisAuthorisationRequest(
      AuthorisationRequest(
        Seq(randomString)
      )
    )
  }

  protected val badRequestEisAuthorisationResponseError: EisAuthorisationResponseError =
    EisAuthorisationResponseError(
      errorDetail = EisAuthorisationResponseErrorDetail(
        errorCode = BAD_REQUEST,
        errorMessage = invalidEorisEisErrorMessage
      )
    )

  protected val forbiddenEisAuthorisationResponseError: EisAuthorisationResponseError =
    EisAuthorisationResponseError(
      errorDetail = EisAuthorisationResponseErrorDetail(
        errorCode = FORBIDDEN,
        errorMessage = emptyErrorMessage
      )
    )

  protected val imATeapotEisAuthorisationResponseError: EisAuthorisationResponseError =
    EisAuthorisationResponseError(
      errorDetail = EisAuthorisationResponseErrorDetail(
        errorCode = IM_A_TEAPOT,
        errorMessage = emptyErrorMessage
      )
    )

  protected val internalServerErrorEisAuthorisationResponseError: EisAuthorisationResponseError =
    EisAuthorisationResponseError(
      errorDetail = EisAuthorisationResponseErrorDetail(
        errorCode = INTERNAL_SERVER_ERROR,
        errorMessage = emptyErrorMessage
      )
    )

  protected val methodNotAllowedEisAuthorisationResponseError: EisAuthorisationResponseError =
    EisAuthorisationResponseError(
      errorDetail = EisAuthorisationResponseErrorDetail(
        errorCode = METHOD_NOT_ALLOWED,
        errorMessage = emptyErrorMessage
      )
    )
}

final case class ValidAuthorisationRequest(
  request: AuthorisationRequest
)

final case class ValidEisAuthorisationsResponse(
  response: EisAuthorisationsResponse
)

final case class TooManyEorisAuthorisationRequest(
  request: AuthorisationRequest
)

final case class NoEorisAuthorisationRequest(
  request: AuthorisationRequest
)

final case class InvalidEorisAuthorisationRequest(
  request: AuthorisationRequest
)
