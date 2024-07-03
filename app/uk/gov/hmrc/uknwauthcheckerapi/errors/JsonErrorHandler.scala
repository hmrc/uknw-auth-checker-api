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

import play.api.http.HttpErrorHandler
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Results.Status
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.http.{BadRequestException, NotAcceptableException}

import javax.inject.Inject
import scala.concurrent.Future

class JsonErrorHandler @Inject() (
) extends HttpErrorHandler {
  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    val jsonErrorResponse = statusCode match {
      case BAD_REQUEST => JsonErrorResponse(BAD_REQUEST, ErrorResponse("INVALID_FORMAT", message))
      case FORBIDDEN   => JsonErrorResponse(FORBIDDEN, ErrorResponse("FORBIDDEN", "You are not allowed to access this resource"))
      case NOT_FOUND =>
        JsonErrorResponse(NOT_FOUND, ErrorResponse("MATCHING_RESOURCE_NOT_FOUND", "No endpoint could be found in the API for the request path"))
      case _ => JsonErrorResponse(INTERNAL_SERVER_ERROR, ErrorResponse("INTERNAL_SERVER_ERROR", "Unexpected internal server error"))
    }

    Future.successful(Status(jsonErrorResponse.statusCode)(Json.toJson(jsonErrorResponse.errorResponse)))
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {

    val jsonErrorResponse = exception match {
      case e: BadRequestException =>
        JsonErrorResponse(BAD_REQUEST, ErrorResponse("INVALID_FORMAT", e.getMessage))
      case _: NotAcceptableException =>
        JsonErrorResponse(NOT_ACCEPTABLE, ErrorResponse("NOT_ACCEPTABLE", "Cannot produce an acceptable response"))
      case _: Throwable =>
        JsonErrorResponse(INTERNAL_SERVER_ERROR, ErrorResponse("INTERNAL_SERVER_ERROR", "Unexpected internal server error"))
    }

    Future.successful(new Status(jsonErrorResponse.statusCode)(Json.toJson(jsonErrorResponse.errorResponse)))
  }
}
