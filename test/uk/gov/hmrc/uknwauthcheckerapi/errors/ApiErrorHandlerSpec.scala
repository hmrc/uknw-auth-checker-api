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

package uk.gov.hmrc.uknwauthcheckerapi.errors

import scala.concurrent.Future

import play.api.http.Status._
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, status}
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.http.HttpVerbs.POST
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.uknwauthcheckerapi.controllers.BaseSpec

class ApiErrorHandlerSpec extends BaseSpec {

  private lazy val apiErrorHandler = injected[ApiErrorHandler]
  private val errorMessage         = "ErrorMessage"

  "onClientError" should {
    "convert a FORBIDDEN to Forbidden (403) response" in {
      val result = apiErrorHandler.onClientError(fakePostRequest, FORBIDDEN, errorMessage)

      status(result) shouldEqual FORBIDDEN
      contentAsJson(result) shouldEqual contentAsJson(Future.successful(ForbiddenApiError.toResult))
    }

    "convert a INTERNAL_SERVER_ERROR to Internal Server Error (500) response" in {
      val result = apiErrorHandler.onClientError(fakePostRequest, INTERNAL_SERVER_ERROR, errorMessage)

      status(result) shouldEqual INTERNAL_SERVER_ERROR
      contentAsJson(result) shouldEqual contentAsJson(Future.successful(InternalServerApiError.toResult))
    }

    "convert a METHOD_NOT_ALLOWED to Method Not Allowed (405) response" in {
      val result = apiErrorHandler.onClientError(fakePostRequest, METHOD_NOT_ALLOWED, errorMessage)

      status(result) shouldEqual METHOD_NOT_ALLOWED
      contentAsJson(result) shouldEqual contentAsJson(Future.successful(MethodNotAllowedApiError.toResult))
    }

    "convert a NOT_FOUND to Not Found (404) response" in {
      val result = apiErrorHandler.onClientError(fakePostRequest, NOT_FOUND, errorMessage)

      status(result) shouldEqual NOT_FOUND
      contentAsJson(result) shouldEqual contentAsJson(Future.successful(NotFoundApiError.toResult))
    }

    "convert a NOT_FOUND with /authorisations url to to Method Not Allowed (405) response" in {
      val result = apiErrorHandler.onClientError(FakeRequest(POST, "/authorisations"), NOT_FOUND, errorMessage)

      status(result) shouldEqual METHOD_NOT_ALLOWED
      contentAsJson(result) shouldEqual contentAsJson(Future.successful(MethodNotAllowedApiError.toResult))
    }

    "convert a SERVICE_UNAVAILABLE to Service Unavailable (503) response" in {
      val result = apiErrorHandler.onClientError(fakePostRequest, SERVICE_UNAVAILABLE, errorMessage)

      status(result) shouldEqual SERVICE_UNAVAILABLE
      contentAsJson(result) shouldEqual contentAsJson(Future.successful(ServiceUnavailableApiError.toResult))
    }
  }

  "onServerError" should {
    case class TestAuthorisationException(msg: String = errorMessage) extends AuthorisationException(msg)

    "convert a NotFoundException to Not Found response" in {
      val notfoundException = new NotFoundException(errorMessage)

      val result = apiErrorHandler.onServerError(fakePostRequest, notfoundException)

      status(result) shouldEqual NOT_FOUND
      contentAsJson(result) shouldEqual contentAsJson(Future.successful(NotFoundApiError.toResult))
    }

    "convert a RuntimeException to Internal Server Error response" in {
      val runtimeException = new RuntimeException(errorMessage)

      val result = apiErrorHandler.onServerError(fakePostRequest, runtimeException)

      status(result) shouldEqual INTERNAL_SERVER_ERROR
      contentAsJson(result) shouldEqual contentAsJson(Future.successful(InternalServerApiError.toResult))
    }
  }
}
