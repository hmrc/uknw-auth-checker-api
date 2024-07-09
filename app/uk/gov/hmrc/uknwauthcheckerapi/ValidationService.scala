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

package uk.gov.hmrc.uknwauthcheckerapi

import play.api.libs.json._
import play.api.mvc.Request
import uk.gov.hmrc.uknwauthcheckerapi.models.AuthorisationRequest

import java.time.LocalDate
import java.time.format.DateTimeParseException
import scala.collection.Seq

class ValidationService {
  private val eoriPattern = "^(GB|XI)[0-9]{12}|(GB|XI)[0-9]{15}$"

  def validateRequest(request: Request[JsValue]): Either[JsError, AuthorisationRequest] = {
    request.body.validate[AuthorisationRequest] match {
      case JsSuccess(authorisationRequest: AuthorisationRequest, _) =>
        validateAuthorisationRequest(authorisationRequest) match {
          case Left(errors) => Left(errors)
          case Right(r) => Right(r)
        }
      case errors: JsError => Left(errors)
    }
  }

  private def validateAuthorisationRequest(request: AuthorisationRequest): Either[JsError, AuthorisationRequest] = {
    val eoriErrors: Seq[JsonValidationError] = request
      .eoris
      .filterNot(e => e matches eoriPattern)
      .map(e => JsonValidationError(s"$e is not a supported EORI number"))

    val dateError = Seq(JsonValidationError(s"${request.date} is not a valid date in the format YYYY-MM-DD"))

    (eoriErrors.nonEmpty, !request.date.isValidLocalDate) match {
      case (false, false) => Right(request)
      case (true, true) =>
        Left(
          JsError(
            Seq("eoris").map { field =>
              (JsPath \ field, eoriErrors)
            } ++
              Seq("date").map { field =>
                (JsPath \ field, dateError)
              }
          )
        )
      case (false, true) =>
        Left(
          JsError(
            Seq("date").map { field =>
              (JsPath \ field, dateError)
            }
          )
        )
      case (true, false) =>
        Left(
          JsError(
            Seq("eoris").map { field =>
              (JsPath \ field, eoriErrors)
            }
          )
        )
    }
  }

  private implicit class StringExtensions(text: String) {
    def isValidLocalDate: Boolean = {
      try {
        LocalDate.parse(text)
        true
      }
      catch {
        case _: DateTimeParseException => false
      }
    }
  }
}
