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

package uk.gov.hmrc.uknwauthcheckerapi.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.prop.TableDrivenPropertyChecks.whenever
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.uknwauthcheckerapi.models.eis.{EisAuthorisationRequest, EisAuthorisationResponse, EisAuthorisationsResponse}
import uk.gov.hmrc.uknwauthcheckerapi.models.{AuthorisationRequest, AuthorisationResponse, AuthorisationsResponse}
import uk.gov.hmrc.uknwauthcheckerapi.services.IntegrationFrameworkService

import java.time.LocalDate
import scala.concurrent.Future

class AuthorisationsControllerSpec extends BaseSpec {

  val mockIntegrationFrameworkService: IntegrationFrameworkService       = mock[IntegrationFrameworkService]


  val controller = new AuthorisationsController(
    cc,
    mockIntegrationFrameworkService
  )

  "AuthorisationsController" should {

    "return OK (200) with authorised eoris when request has valid date and eoris" in {

      forAll { authorisationRequest: AuthorisationRequest =>
        whenever (authorisationRequest.date.isDefined) {
          val expectedResponse = AuthorisationsResponse(
            authorisationRequest.date.getOrElse(LocalDate.now),
            authorisationRequest.eoris.map(r => AuthorisationResponse(r, authorised = true))
          )
          val eisAuthorisationsResponse = EisAuthorisationsResponse(
            authorisationRequest.date.getOrElse(LocalDate.now),
            EisAuthorisationRequest.authType,
            authorisationRequest.eoris.map(r => EisAuthorisationResponse(r, valid = true, 0))
          )

          val request = fakeRequestWithJsonBody(Json.toJson(authorisationRequest))
          when(mockIntegrationFrameworkService.getEisAuthorisations(any())(any()))
            .thenReturn(Future.successful(eisAuthorisationsResponse))

          val result = controller.authorisations()(request)

          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.toJson(expectedResponse)
          true
        }
      }
    }
  }
}
