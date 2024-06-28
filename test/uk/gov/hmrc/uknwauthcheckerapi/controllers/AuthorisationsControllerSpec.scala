package uk.gov.hmrc.uknwauthcheckerapi.controllers

import org.apache.pekko.actor.setup.Setup
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.uknwauthcheckerapi.models.{AuthorisationRequest, AuthorisationsResponse, AuthorisationResponse}
import uk.gov.hmrc.uknwauthcheckerapi.generators.RandomGen.{eoriGen, localDateGen}

import scala.concurrent.Future

class AuthorisationsControllerSpec extends AnyWordSpec with Matchers {

  private val controller = new AuthorisationsController(Helpers.stubControllerComponents())

  "AuthorisationsController" should {

    val date = localDateGen
    val eori1 = eoriGen.sample.get
    val eori2 = eoriGen.sample.get

    val converted =
      AuthorisationsResponse(
        date,
        Array(
          AuthorisationResponse(eori1, authorised = true),
          AuthorisationResponse(eori2, authorised = true)
        )
      )

    "return OK" in new Setup {
      val request: FakeRequest[AuthorisationRequest] =
        FakeRequest().withBody(AuthorisationRequest(date, Array(eori1, eori2)))

      val result: Future[Result] = controller.authorisations()(request)

      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(converted)
    }
  }
}
