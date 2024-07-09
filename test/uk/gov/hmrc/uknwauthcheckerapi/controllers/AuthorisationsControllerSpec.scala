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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.{JsError, JsPath, Json, JsonValidationError}
import play.api.test.Helpers._
import uk.gov.hmrc.uknwauthcheckerapi.errors.{ApiErrorResponse, JsonValidationApiError, NotAcceptableApiError}
import uk.gov.hmrc.uknwauthcheckerapi.models.{AuthorisationRequest, AuthorisationResponse, AuthorisationsResponse}
import uk.gov.hmrc.uknwauthcheckerapi.services.ValidationService

import java.time.LocalDate
import scala.concurrent.Future

class AuthorisationsControllerSpec extends BaseSpec {

  private val mockValidationService: ValidationService = mock[ValidationService]
  private val controller = new AuthorisationsController(stubComponents, mockValidationService)

  "AuthorisationsController" should {

    "return OK (200) with authorised eoris when request has valid date and eoris" in {

      forAll { authorisationRequest: AuthorisationRequest =>
        when(mockValidationService.validateRequest(any())).thenReturn(
          Right(authorisationRequest)
        )

        val expectedResponse = AuthorisationsResponse(
          LocalDate.parse(authorisationRequest.date),
          authorisationRequest.eoris.map(r => AuthorisationResponse(r, authorised = true))
        )

        val request = fakeRequestWithJsonBody(Json.toJson(authorisationRequest))

        val result = controller.authorisations()(request)

        status(result)        shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(expectedResponse)
      }
    }

    "return BAD_REQUEST (400) error when request json is invalid" in {
      val request = fakeRequestWithJsonBody(emptyJson)

      val jsError = JsError(
        Seq("date", "eoris").map { field =>
          (JsPath \ field, Seq(JsonValidationError("error.path.missing")))
        }
      )

      val expectedResponse = Json.toJson(
        JsonValidationApiError(jsError)
      )(ApiErrorResponse.validationWrites)

      when(mockValidationService.validateRequest(any())).thenReturn(
        Left(jsError)
      )

      val result = controller.authorisations()(request)

      status(result)        shouldBe BAD_REQUEST
      contentAsJson(result) shouldBe expectedResponse
    }
  }

  "return NOT_ACCEPTABLE (406) error when accept header is not present" in {

    forAll { authorisationRequest: AuthorisationRequest =>
      val headers = defaultHeaders.filterNot(_._1.equals(jsonAcceptHeader._1))

      val request = fakeRequestWithJsonBody(Json.toJson(authorisationRequest), headers = headers)

      val result = controller.authorisations()(request)

      status(result)        shouldBe NOT_ACCEPTABLE
      contentAsJson(result) shouldBe contentAsJson(Future.successful(NotAcceptableApiError.toResult))
    }
  }

  "return NOT_ACCEPTABLE (406) error when content type header is not present" in {

    forAll { authorisationRequest: AuthorisationRequest =>
      val headers = defaultHeaders.filterNot(_._1.equals(jsonContentTypeHeader._1))

      val request = fakeRequestWithJsonBody(Json.toJson(authorisationRequest), headers = headers)

      val result = controller.authorisations()(request)

      status(result)        shouldBe NOT_ACCEPTABLE
      contentAsJson(result) shouldBe contentAsJson(Future.successful(NotAcceptableApiError.toResult))
    }
  }
}
