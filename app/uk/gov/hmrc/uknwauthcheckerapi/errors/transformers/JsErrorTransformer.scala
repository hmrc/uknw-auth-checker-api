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

package uk.gov.hmrc.uknwauthcheckerapi.errors.transformers

import play.api.libs.json.{JsError, JsValue, Json}
import uk.gov.hmrc.uknwauthcheckerapi.models.{ApiErrorCodes, JsonErrorMessages, JsonPaths}

trait JsErrorTransformer {

  def transformJsErrors(errors: JsError): JsValue = Json.toJson(errors.errors.flatMap { case (jsPath, pathErrors) =>
    val dropObjDot = 4
    val path       = jsPath.toJsonString.drop(dropObjDot)
    pathErrors.map(validationError =>
      Json.obj(
        JsonPaths.code -> ApiErrorCodes.invalidFormat,
        JsonPaths.message -> (validationError.message match {
          case message if message == JsonErrorMessages.expectedJsObject                       => JsonErrorMessages.jsonMalformed
          case message if message == JsonErrorMessages.pathMissing && path == JsonPaths.eoris => JsonErrorMessages.eorisFieldMissing
          case message                                                                        => message
        }),
        JsonPaths.path -> path
      )
    )
  })
}
