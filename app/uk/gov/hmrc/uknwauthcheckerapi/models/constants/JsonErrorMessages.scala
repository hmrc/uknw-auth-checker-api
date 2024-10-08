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

package uk.gov.hmrc.uknwauthcheckerapi.models.constants

object JsonErrorMessages {
  val eorisFieldMissing:      String = "eoris field missing from JSON"
  val expectedJsObject:       String = "error.expected.jsobject"
  val expectedJsArray:        String = "error.expected.jsarray"
  val jsonStructureIncorrect: String = "JSON structure is incorrect"
  val pathMissing:            String = "error.path.missing"
}
