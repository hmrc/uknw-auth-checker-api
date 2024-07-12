/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.uknwauthcheckerapi.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.http.Status._
import play.api.libs.json.{JsError, JsPath, Json, JsonValidationError}
import play.api.test.Helpers.await
import uk.gov.hmrc.http.{BadGatewayException, UpstreamErrorResponse}
import uk.gov.hmrc.uknwauthcheckerapi.connectors.IntegrationFrameworkConnector
import uk.gov.hmrc.uknwauthcheckerapi.controllers.BaseSpec
import uk.gov.hmrc.uknwauthcheckerapi.errors.DataRetrievalError._
import uk.gov.hmrc.uknwauthcheckerapi.models.eis._
import uk.gov.hmrc.uknwauthcheckerapi.models.{AuthorisationRequest, AuthorisationResponse, AuthorisationsResponse}
import uk.gov.hmrc.uknwauthcheckerapi.utils.JsonErrors

import java.time.LocalDate
import scala.concurrent.Future

class IntegrationFrameworkServiceSpec extends BaseSpec {

  private val mockIntegrationFrameworkConnector = mock[IntegrationFrameworkConnector]

  val service = new IntegrationFrameworkService(appConfig, mockIntegrationFrameworkConnector)(ec)

  "getEisAuthorisations" should {
    "return EisAuthorisationsResponse when call to the integration framework succeeds" in forAll {
      (authorisationRequest: AuthorisationRequest, date: LocalDate) =>
        val request = authorisationRequest.copy(date = date.toLocalDateFormatted)

        val expectedResponse = AuthorisationsResponse(
          date,
          request.eoris.map(r => AuthorisationResponse(r, authorised = true))
        )

        val expectedEisAuthorisationsResponse = EisAuthorisationsResponse(
          date,
          appConfig.authType,
          request.eoris.map(r => EisAuthorisationResponse(r, valid = true, 0))
        )

        when(mockIntegrationFrameworkConnector.getEisAuthorisationsResponse(any())(any()))
          .thenReturn(Future.successful(expectedEisAuthorisationsResponse))

        val result = await(service.getAuthorisations(request).value)

        result shouldBe Right(expectedResponse)
    }

    "return BadGatewayRetrievalError error when call to the integration framework fails with BAD_GATEWAY" in forAll {
      (authorisationRequest: AuthorisationRequest, date: LocalDate, errorMessage: String) =>
        val request = authorisationRequest.copy(date = date.toLocalDateFormatted)

        val expectedResponse = new BadGatewayException(errorMessage)

        when(mockIntegrationFrameworkConnector.getEisAuthorisationsResponse(any())(any()))
          .thenReturn(Future.failed(expectedResponse))

        val result = await(service.getAuthorisations(request).value)

        result shouldBe Left(BadGatewayDataRetrievalError())
    }

    "return InternalUnexpectedDataRetrievalError error when call to the integration framework fails with a non fatal error" in forAll {
      (authorisationRequest: AuthorisationRequest, date: LocalDate, errorMessage: String) =>
        val request = authorisationRequest.copy(date = date.toLocalDateFormatted)

        val expectedResponse = new Exception(errorMessage)

        when(mockIntegrationFrameworkConnector.getEisAuthorisationsResponse(any())(any()))
          .thenReturn(Future.failed(expectedResponse))

        val result = await(service.getAuthorisations(request).value)

        result shouldBe Left(InternalUnexpectedDataRetrievalError(expectedResponse.getMessage, expectedResponse))
    }

    "return BadRequestDataRetrievalError error when call to the integration framework fails with a BAD_REQUEST" in forAll {
      (authorisationRequest: AuthorisationRequest, date: LocalDate, eisErrorResponse: EisAuthorisationResponseError) =>
        val request = authorisationRequest.copy(date = date.toLocalDateFormatted)

        val eisError = eisErrorResponse.copy(errorDetail =
          eisErrorResponse.errorDetail
            .copy(errorCode = BAD_REQUEST)
        )

        val expectedResponse = UpstreamErrorResponse(Json.stringify(Json.toJson(eisError)), BAD_REQUEST)

        when(mockIntegrationFrameworkConnector.getEisAuthorisationsResponse(any())(any()))
          .thenReturn(Future.failed(expectedResponse))

        val result = await(service.getAuthorisations(request).value)

        result shouldBe Left(BadRequestDataRetrievalError(eisError.errorDetail.errorMessage))
    }

    "return ForbiddenDataRetrievalError error when call to the integration framework fails with a FORBIDDEN" in forAll {
      (authorisationRequest: AuthorisationRequest, date: LocalDate, eisErrorResponse: EisAuthorisationResponseError) =>
        val request = authorisationRequest.copy(date = date.toLocalDateFormatted)

        val eisError = eisErrorResponse.copy(errorDetail =
          eisErrorResponse.errorDetail
            .copy(errorCode = FORBIDDEN)
        )

        val expectedResponse = UpstreamErrorResponse(Json.stringify(Json.toJson(eisError)), FORBIDDEN)

        when(mockIntegrationFrameworkConnector.getEisAuthorisationsResponse(any())(any()))
          .thenReturn(Future.failed(expectedResponse))

        val result = await(service.getAuthorisations(request).value)

        result shouldBe Left(ForbiddenDataRetrievalError(eisError.errorDetail.errorMessage))
    }

    "return InternalServerDataRetrievalError error when call to the integration framework fails with a INTERNAL_SERVER_ERROR" in forAll {
      (authorisationRequest: AuthorisationRequest, date: LocalDate, eisErrorResponse: EisAuthorisationResponseError) =>
        val request = authorisationRequest.copy(date = date.toLocalDateFormatted)

        val eisError = eisErrorResponse.copy(errorDetail =
          eisErrorResponse.errorDetail
            .copy(errorCode = INTERNAL_SERVER_ERROR)
        )

        val expectedResponse = UpstreamErrorResponse(Json.stringify(Json.toJson(eisError)), INTERNAL_SERVER_ERROR)

        when(mockIntegrationFrameworkConnector.getEisAuthorisationsResponse(any())(any()))
          .thenReturn(Future.failed(expectedResponse))

        val result = await(service.getAuthorisations(request).value)

        result shouldBe Left(InternalServerDataRetrievalError(eisError.errorDetail.errorMessage))
    }

    "return MethodNotAllowedDataRetrievalError error when call to the integration framework fails with a METHOD_NOT_ALLOWED" in forAll {
      (authorisationRequest: AuthorisationRequest, date: LocalDate, eisErrorResponse: EisAuthorisationResponseError) =>
        val request = authorisationRequest.copy(date = date.toLocalDateFormatted)

        val eisError = eisErrorResponse.copy(errorDetail =
          eisErrorResponse.errorDetail
            .copy(errorCode = METHOD_NOT_ALLOWED)
        )

        val expectedResponse = UpstreamErrorResponse(Json.stringify(Json.toJson(eisError)), METHOD_NOT_ALLOWED)

        when(mockIntegrationFrameworkConnector.getEisAuthorisationsResponse(any())(any()))
          .thenReturn(Future.failed(expectedResponse))

        val result = await(service.getAuthorisations(request).value)

        result shouldBe Left(MethodNotAllowedDataRetrievalError(eisError.errorDetail.errorMessage))
    }

    "return InternalServerDataRetrievalError error when call to the integration framework fails with a unmanaged status code" in forAll {
      (authorisationRequest: AuthorisationRequest, date: LocalDate, eisErrorResponse: EisAuthorisationResponseError) =>
        val request = authorisationRequest.copy(date = date.toLocalDateFormatted)

        val eisError = eisErrorResponse.copy(errorDetail =
          eisErrorResponse.errorDetail
            .copy(errorCode = IM_A_TEAPOT)
        )

        val expectedResponse = UpstreamErrorResponse(Json.stringify(Json.toJson(eisError)), IM_A_TEAPOT)

        when(mockIntegrationFrameworkConnector.getEisAuthorisationsResponse(any())(any()))
          .thenReturn(Future.failed(expectedResponse))

        val result = await(service.getAuthorisations(request).value)

        result shouldBe Left(InternalServerDataRetrievalError(eisError.errorDetail.errorMessage))
    }

    "return UnableToDeserialiseDataRetrievalError error when call to the integration framework fails with unvalidated json" in forAll {
      (authorisationRequest: AuthorisationRequest, date: LocalDate) =>
        val request = authorisationRequest.copy(date = date.toLocalDateFormatted)

        val jsError = JsError(
          Seq((JsPath \ "errorDetail", Seq(JsonValidationError(JsonErrors.pathMissing))))
        )

        val expectedResponse = UpstreamErrorResponse("{}", BAD_REQUEST)

        when(mockIntegrationFrameworkConnector.getEisAuthorisationsResponse(any())(any()))
          .thenReturn(Future.failed(expectedResponse))

        val result = await(service.getAuthorisations(request).value)

        result shouldBe Left(UnableToDeserialiseDataRetrievalError(jsError))
    }
  }
}
