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

import scala.reflect.ClassTag

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{EmptyBody, WSClient, WSRequest, WSResponse}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.uknwauthcheckerapi.config.AppConfig
import uk.gov.hmrc.uknwauthcheckerapi.generators.{TestConstants, TestData, TestHeaders}
import play.api.libs.ws.writeableOf_JsValue
import play.api.libs.ws.writeableOf_WsBody

class BaseISpec extends PlaySpec with GuiceOneServerPerSuite with WireMockISpec with TestData with TestHeaders {

  @annotation.nowarn
  protected val additionalAppConfig: Map[String, Any] = Map(
    TestConstants.configMetricsKey  -> false,
    TestConstants.configAuditingKey -> false,
    TestConstants.configRetriesKey -> List(
      TestConstants.configOverrideRetryInterval,
      TestConstants.configOverrideRetryInterval,
      TestConstants.configOverrideRetryInterval
    )
  ) ++ setWireMockPort(
    TestConstants.serviceNameAuth,
    TestConstants.serviceNameIntegrationFramework
  )
  override lazy val app: Application = GuiceApplicationBuilder()
    .configure(additionalAppConfig)
    .build()
  protected lazy val authorisationsUrl = s"http://localhost:$port/authorisations"
  protected lazy val appConfig: AppConfig = injected[AppConfig]
  private lazy val wsClient:    WSClient  = injected[WSClient]

  protected def injected[T](implicit evidence: ClassTag[T]): T = app.injector.instanceOf[T]

  private def createRequest(url: String, headers: Seq[(String, String)] = defaultHeaders): WSRequest =
    wsClient
      .url(url)
      .addHttpHeaders(
        headers: _*
      )

  protected def deleteRequest(url: String, headers: Seq[(String, String)] = defaultHeaders): WSResponse =
    await(createRequest(url, headers).delete())

  protected def headRequest(url: String, headers: Seq[(String, String)] = defaultHeaders): WSResponse =
    await(createRequest(url, headers).head())

  protected def getRequest(url: String, headers: Seq[(String, String)] = defaultHeaders): WSResponse =
    await(createRequest(url, headers).get())

  protected def optionsRequest(url: String, headers: Seq[(String, String)] = defaultHeaders): WSResponse =
    await(createRequest(url, headers).options())

  protected def patchRequest(url: String, body: JsValue, headers: Seq[(String, String)] = defaultHeaders): WSResponse =
    await(createRequest(url, headers).patch(Json.toJson(body)))

  protected def postRequest(url: String, body: JsValue, headers: Seq[(String, String)] = defaultHeaders): WSResponse =
    await(createRequest(url, headers).post(Json.toJson(body)))

  protected def postEmptyRequest(url: String, headers: Seq[(String, String)] = defaultHeaders): WSResponse =
    await(createRequest(url, headers).post(EmptyBody))

  protected def putRequest(url: String, body: JsValue, headers: Seq[(String, String)] = defaultHeaders): WSResponse =
    await(createRequest(url, headers).put(Json.toJson(body)))
}
