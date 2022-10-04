package kevinlee.sbt

import hedgehog.*
import hedgehog.runner.*
import kevinlee.Gens

/** @author Kevin Lee
  * @since 2018-12-28
  */
object SbtCommonSpec extends Properties {
  override def tests: List[Test] = List(
    property(
      "testNoMatchingVersion: crossVersionProps should return only the common props when the version does not match¸",
      testNoMatchingVersion,
    ),
    property(
      "testNoMatchingVersionNonExhaustiveCase: crossVersionProps should return only the common props when the version does not match¸",
      testNoMatchingVersionNonExhaustiveCase,
    ),
    property(
      "testMatchingVersion: crossVersionProps should return the common props with the additional props for the version matches¸",
      testMatchingVersion,
    ),
    property(
      "testOneOfTwoVersionsMatches: crossVersionProps should return the common props with the additional props for the one of two versions matches¸",
      testOneOfTwoVersionsMatches,
    ),
  )

  def testNoMatchingVersion: Property = for {
    semVers <- Gens.genMinMaxSemVers.log("(semVer1, semVer2)")
    (semVer1, semVer2) = semVers
    ss1 <- Gens.genAlphabetString(10).list(Range.linear(1, 5)).log("ss1")
    ss2 <- Gens.genAlphabetString(10).list(Range.linear(1, 5)).log("ss2")
  } yield {
    val expected = ss1
    val actual   =
      SbtCommon.crossVersionProps(ss1, semVer2) {
        case (semVer1.major, semVer1.minor, semVer1.patch) =>
          ss2
        case _ =>
          Seq()
      }
    actual ==== expected
  }

  def testNoMatchingVersionNonExhaustiveCase: Property = for {
    semVers <- Gens.genMinMaxSemVers.log("(semVer1, semVer2)")
    (semVer1, semVer2) = semVers
    ss1 <- Gens.genAlphabetString(10).list(Range.linear(1, 5)).log("ss1")
    ss2 <- Gens.genAlphabetString(10).list(Range.linear(1, 5)).log("ss2")
  } yield {
    try {
      SbtCommon.crossVersionProps(ss1, semVer2) {
        case (semVer1.major, semVer1.minor, semVer1.patch) =>
          ss2
      }
      Result.failure
    } catch {
      case _: MatchError =>
        Result.success
    }
  }

  def testMatchingVersion: Property = for {
    semVer <- Gens.genSemVer.log("semVer")
    ss1    <- Gens.genAlphabetString(10).list(Range.linear(1, 5)).log("ss1")
    ss2    <- Gens.genAlphabetString(10).list(Range.linear(1, 5)).log("ss2")
  } yield {
    val expected = ss1 ++ ss2
    val actual   =
      SbtCommon.crossVersionProps(ss1, semVer) {
        case (semVer.major, semVer.minor, semVer.patch) =>
          ss2
      }
    actual ==== expected
  }

  def testOneOfTwoVersionsMatches: Property = for {
    semVers <- Gens.genMinMaxSemVers.log("(semVer1, semVer2)")
    (semVer1, semVer2) = semVers
    ss1 <- Gens.genAlphabetString(10).list(Range.linear(1, 7)).log("ss1")
    ss2 <- Gens.genAlphabetString(10).list(Range.linear(1, 5)).log("ss2")
    ss3 <- Gens.genAlphabetString(10).list(Range.linear(1, 5)).log("ss3")
  } yield {
    val expected = ss1 ++ ss3
    val actual   =
      SbtCommon.crossVersionProps(ss1, semVer2) {
        case (semVer1.major, semVer1.minor, semVer1.patch) =>
          ss2

        case (semVer2.major, semVer2.minor, semVer2.patch) =>
          ss3

        case _ =>
          Seq()
      }
    actual ==== expected
  }

}
