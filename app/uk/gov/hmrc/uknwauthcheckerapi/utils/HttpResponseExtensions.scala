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

package uk.gov.hmrc.uknwauthcheckerapi.utils

import scala.concurrent.Future

import play.api.libs.json.{JsResult, Reads}
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}

extension (response: HttpResponse) {

  def error[A]: Future[A] =
    Future.failed(UpstreamErrorResponse(response.body, response.status))

  def as[A](using reads: Reads[A]): Future[A] =
    response.json
      .validate[A]
      .map(result => Future.successful(result))
      .recoverTotal(error => Future.failed(JsResult.Exception(error)))
}
