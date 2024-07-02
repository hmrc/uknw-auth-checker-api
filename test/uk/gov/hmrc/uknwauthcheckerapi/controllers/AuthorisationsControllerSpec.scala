package uk.gov.hmrc.uknwauthcheckerapi.controllers

import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.uknwauthcheckerapi.models.{AuthorisationRequest, AuthorisationResponse, AuthorisationsResponse}

import java.time.LocalDate
import scala.concurrent.Future

class AuthorisationsControllerSpec extends BaseSpec {

  val controller = new AuthorisationsController(Helpers.stubControllerComponents())

  "AuthorisationsController" should {

    "return OK" in {
      forAll(arbAuthorisationRequest) {
        (authorisationRequest) =>

          val result: Future[Result] = controller.authorisations()(authorisationRequest)

          status(result) shouldBe OK

      }
    }
  }
}
