package uk.gov.hmrc.uknwauthcheckerapi.generators

import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.uknwauthcheckerapi.models.AuthorisationRequest
import wolfendale.scalacheck.regexp.RegexpGen

import java.time.LocalDate

trait Generators {

  val arbLocalDate: Arbitrary[LocalDate] = Arbitrary {
    LocalDate.now()
  }

  val eoriGen: Gen[String] = RegexpGen.from("^(GB|XI)[0-9]{12}|(GB|XI)[0-9]{15}$")
  val eorisGen: Gen[Seq[String]] = Gen.chooseNum(1, 3000).flatMap(n => Gen.listOfN(n, eoriGen))


  val arbAuthorisationRequest: Arbitrary[AuthorisationRequest] = Arbitrary {
    for {
      date  <- arbLocalDate.arbitrary
      eoris  <- eorisGen
    } yield AuthorisationRequest(date = date, eoris = eoris)
  }

}
