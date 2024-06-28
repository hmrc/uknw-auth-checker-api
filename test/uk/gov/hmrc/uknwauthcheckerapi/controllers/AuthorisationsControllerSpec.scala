package uk.gov.hmrc.uknwauthcheckerapi.controllers

import org.apache.pekko.actor.setup.Setup
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.uknwauthcheckerapi.models.{AuthorisationRequest, AuthorisationsResponse, EISResult}

import scala.concurrent.Future

class AuthorisationsControllerSpec extends AnyWordSpec with Matchers {

  private val controller = new AuthorisationsController(Helpers.stubControllerComponents())

  "AuthorisationsController" should {

    val converted =
      AuthorisationsResponse(
        "01-01-1999",
        Array(
          EISResult("SampleEORI1", authorised = true),
          EISResult("SampleEORI2", authorised = true)
        )
      )

    "return OK" in new Setup {
      val request: FakeRequest[AuthorisationRequest] =
        FakeRequest().withBody(AuthorisationRequest("01-01-1999", Array("SampleEORI1", "SampleEORI2")))

      val result: Future[Result] = controller.authorisations()(request)

      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(converted)
    }
  }
}
