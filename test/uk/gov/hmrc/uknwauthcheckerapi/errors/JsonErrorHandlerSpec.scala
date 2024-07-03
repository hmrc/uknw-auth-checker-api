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

import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, INTERNAL_SERVER_ERROR, NOT_ACCEPTABLE, NOT_FOUND}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import uk.gov.hmrc.http.HttpVerbs.POST
import uk.gov.hmrc.http.{BadRequestException, NotAcceptableException, UpstreamErrorResponse}
import uk.gov.hmrc.uknwauthcheckerapi.controllers.BaseSpec

class JsonErrorHandlerSpec extends BaseSpec {

  private val jsonErrorHandler = new JsonErrorHandler()
  private val requestHeader    = FakeRequest(POST, "")
  private val errorMessage     = "ErrorMessage"

  "onClientError" should {
    "convert a BAD_REQUEST to Bad Request (400) response" in {
      val result = jsonErrorHandler.onClientError(requestHeader, BAD_REQUEST, errorMessage)

      status(result) shouldEqual BAD_REQUEST
      contentAsJson(result) shouldEqual Json.obj("code" -> "INVALID_FORMAT", "message" -> errorMessage)
    }

    "convert a FORBIDDEN to Forbidden (403) response" in {
      val result = jsonErrorHandler.onClientError(requestHeader, FORBIDDEN, errorMessage)

      status(result) shouldEqual FORBIDDEN
      contentAsJson(result) shouldEqual Json.obj("code" -> "FORBIDDEN", "message" -> "You are not allowed to access this resource")
    }

    "convert a NOT_FOUND to Not Found (404) response" in {
      val result = jsonErrorHandler.onClientError(requestHeader, NOT_FOUND, errorMessage)

      status(result) shouldEqual NOT_FOUND
      contentAsJson(result) shouldEqual Json.obj(
        "code"    -> "MATCHING_RESOURCE_NOT_FOUND",
        "message" -> "No endpoint could be found in the API for the request path"
      )
    }

    "convert a INTERNAL_SERVER_ERROR to Internal Server Error (500) response" in {
      val result = jsonErrorHandler.onClientError(requestHeader, INTERNAL_SERVER_ERROR, errorMessage)

      status(result) shouldEqual INTERNAL_SERVER_ERROR
      contentAsJson(result) shouldEqual Json.obj("code" -> "INTERNAL_SERVER_ERROR", "message" -> "Unexpected internal server error")
    }
  }

  "onServerError" should {
    "convert a BadRequestException to Bad Request response" in {
      val badRequestException = new BadRequestException(errorMessage)

      val result = jsonErrorHandler.onServerError(requestHeader, badRequestException)

      status(result) shouldEqual BAD_REQUEST
      contentAsJson(result) shouldEqual Json.obj("code" -> "INVALID_FORMAT", "message" -> errorMessage)
    }

    "convert a NotAcceptableException to Not Acceptable response" in {
      val notAcceptableException = new NotAcceptableException(errorMessage)

      val result = jsonErrorHandler.onServerError(requestHeader, notAcceptableException)

      status(result) shouldEqual NOT_ACCEPTABLE
      contentAsJson(result) shouldEqual Json.obj("code" -> "NOT_ACCEPTABLE", "message" -> "Cannot produce an acceptable response")
    }

    "convert a RuntimeException to Internal Server Error response" in {
      val runtimeException = new RuntimeException(errorMessage)

      val result = jsonErrorHandler.onServerError(requestHeader, runtimeException)

      status(result) shouldEqual INTERNAL_SERVER_ERROR
      contentAsJson(result) shouldEqual Json.obj("code" -> "INTERNAL_SERVER_ERROR", "message" -> "Unexpected internal server error")
    }

    "convert a UpstreamErrorResponse to Internal Server Error response" in {
      val upstreamErrorResponse = UpstreamErrorResponse(errorMessage, INTERNAL_SERVER_ERROR)

      val result = jsonErrorHandler.onServerError(requestHeader, upstreamErrorResponse)

      status(result) shouldEqual INTERNAL_SERVER_ERROR
      contentAsJson(result) shouldEqual Json.obj("code" -> "INTERNAL_SERVER_ERROR", "message" -> "Unexpected internal server error")
    }
  }
}
