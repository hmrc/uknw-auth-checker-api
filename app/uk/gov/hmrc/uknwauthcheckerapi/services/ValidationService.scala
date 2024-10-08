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

package uk.gov.hmrc.uknwauthcheckerapi.services

import javax.inject.Inject
import scala.collection.Seq

import play.api.libs.json._
import play.api.mvc.Request
import uk.gov.hmrc.uknwauthcheckerapi.errors.DataRetrievalError
import uk.gov.hmrc.uknwauthcheckerapi.errors.DataRetrievalError.ValidationDataRetrievalError
import uk.gov.hmrc.uknwauthcheckerapi.models.AuthorisationRequest
import uk.gov.hmrc.uknwauthcheckerapi.models.constants.{ApiErrorMessages, CustomRegexes, JsonPaths, MinMaxValues}

class ValidationService @Inject() (minMaxValues: MinMaxValues) {

  private type ValidationResult[T] = Either[JsError, T]

  def validateRequest(request: Request[JsValue]): Either[DataRetrievalError, AuthorisationRequest] =
    request.body.validate[AuthorisationRequest] match {
      case JsSuccess(authorisationRequest: AuthorisationRequest, _) =>
        validateAuthorisationRequest(authorisationRequest) match {
          case Left(errors) => Left(ValidationDataRetrievalError(errors))
          case Right(r)     => Right(r)
        }
      case errors: JsError => Left(ValidationDataRetrievalError(errors))
    }

  private def validateAuthorisationRequest(request: AuthorisationRequest): Either[JsError, AuthorisationRequest] =
    for {
      _ <- validateEoriCount(request)
      _ <- validateEoriStructure(request)
    } yield request

  private def validateEoriCount(request: AuthorisationRequest): ValidationResult[AuthorisationRequest] =
    Either.cond(
      isEoriSizeValid(request.eoris.size),
      request,
      JsError(JsPath \ JsonPaths.eoris, ApiErrorMessages.invalidEoriCount(minMaxValues.maxEoriCount))
    )

  private def validateEoriStructure(request: AuthorisationRequest): ValidationResult[AuthorisationRequest] = {
    val invalidEoriErrors = getInvalidEoriErrors(request.eoris)
    Either.cond(
      invalidEoriErrors.isEmpty,
      request,
      JsError(
        Seq(JsonPaths.eoris).map { field =>
          (JsPath \ field, invalidEoriErrors)
        }
      )
    )
  }

  private def isEoriSizeValid(eorisSize: Int): Boolean =
    eorisSize <= minMaxValues.maxEoriCount && eorisSize >= minMaxValues.minEoriCount

  private def getInvalidEoriErrors(eoris: Seq[String]): Seq[JsonValidationError] =
    eoris.collect {
      case eori if !eori.matches(CustomRegexes.eoriPattern) => JsonValidationError(ApiErrorMessages.invalidEori(eori))
    }.distinct
}
