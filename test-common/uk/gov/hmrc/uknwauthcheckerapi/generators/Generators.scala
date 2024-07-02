package uk.gov.hmrc.uknwauthcheckerapi.generators

import org.scalacheck.{Arbitrary, Gen}
import wolfendale.scalacheck.regexp.RegexpGen

import java.time.LocalDate

object Generators {

  val arbLocalDate: Arbitrary[LocalDate] = Arbitrary {
    LocalDate.now()
  }

  val eoriGen: Gen[String] = RegexpGen.from("^(GB|XI)[0-9]{12}|(GB|XI)[0-9]{15}$")

}
