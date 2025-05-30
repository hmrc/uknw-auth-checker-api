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

package uk.gov.hmrc.uknwauthcheckerapi.config

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import uk.gov.hmrc.http.StringContextOps

@Singleton
class AppConfig @Inject() (config: Configuration) {

  private lazy val rootServices = "microservice.services"

  val authorisationsEndpoint = "/authorisations"
  val authType: String = config.get[String]("authType")
  val eisAuthorisationsUrl = url"${baseUrl("integration-framework")}/cau/validatecustomsauth/v1"
  val eoriMax: Int =
    config.get[Int]("microservice.services.self.eoriMax")
  val integrationFrameworkBearerToken: String =
    config.get[String]("microservice.services.integration-framework.bearerToken")

  def baseUrl(serviceName: String): String = {
    val protocol = getConfString(s"$serviceName.protocol", "http")
    val host     = getConfString(s"$serviceName.host", throwConfigNotFoundError(s"$serviceName.host"))
    val port     = getConfInt(s"$serviceName.port", throwConfigNotFoundError(s"$serviceName.port"))
    s"$protocol://$host:$port"
  }

  private def getConfInt(confKey: String, defInt: => Int): Int =
    config
      .getOptional[Int](s"$rootServices.$confKey")
      .getOrElse(defInt)

  private def getConfString(confKey: String, defString: => String): String =
    config
      .getOptional[String](s"$rootServices.$confKey")
      .getOrElse(defString)

  private def throwConfigNotFoundError(key: String): Nothing =
    throw new RuntimeException(s"Could not find config key '$key'")
}
