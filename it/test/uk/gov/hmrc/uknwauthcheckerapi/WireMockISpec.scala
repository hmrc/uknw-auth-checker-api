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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlMatching}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

import play.api.http.Status.OK
import uk.gov.hmrc.uknwauthcheckerapi.generators.TestConstants

trait WireMockISpec extends BeforeAndAfterAll, BeforeAndAfterEach {
  this: Suite =>

  protected val server: WireMockServer = new WireMockServer(wireMockConfig().port(TestConstants.configWireMockPort))

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
    // https://github.com/wiremock/wiremock/issues/369
    WireMock.configureFor(TestConstants.configWireMockHost, TestConstants.configWireMockPort)
  }

  override def beforeEach(): Unit = {
    resetWireMock()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  protected def resetWireMock(): Unit = {
    server.resetAll()
    WireMock.reset()
  }

  protected def setWireMockPort(services: String*): Map[String, Any] =
    services.foldLeft(Map.empty[String, Any]) { case (map, service) =>
      map + (s"microservice.services.$service.port" -> TestConstants.configWireMockPort)
    }

  protected def stubAuthorised(): StubMapping =
    server.stubFor(
      post(TestConstants.serviceEndpointAuth)
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(TestConstants.emptyJson)
        )
    )

  protected def stubPost(
    url:            String,
    responseStatus: Int,
    responseBody:   String
  ): StubMapping = {
    server.removeStub(post(urlMatching(url)))
    server.stubFor(
      post(urlMatching(url))
        .willReturn(
          aResponse()
            .withStatus(responseStatus)
            .withBody(responseBody)
        )
    )
  }

  protected def stubPost(
    url:            String,
    responseStatus: Int
  ): StubMapping = {
    server.removeStub(post(urlMatching(url)))
    server.stubFor(
      post(urlMatching(url))
        .willReturn(
          aResponse()
            .withStatus(responseStatus)
        )
    )
  }
}
