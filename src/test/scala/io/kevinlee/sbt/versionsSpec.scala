package io.kevinlee.sbt

import hedgehog._
import hedgehog.runner._

import Gens._

/**
  * @author Kevin Lee
  * @since 2018-11-04
  */
object SemanticVersionMajorSpec extends Properties {

  override def tests: List[Test] = List(
    property("Two SemanticVersions with the same Major and the rest are equal then it should be equal", testSameMajors)
  , property("Two SemanticVersions with the different Majors and the rest are equal then it should be not equal", testDifferentMajors)
  , property("Test SemanticVersion(Major(less)) < SemanticVersion(Major(greater)) is true", testMajorLessCase)
  , property("Test SemanticVersion(Major(greater)) > SemanticVersion(Major(less)) is true", testMajorMoreCase)
  , property("Test SemanticVersion(same Major) <= SemanticVersion(same Major) is true", testLeeThanEqualWithSameMajors)
  , property("Test SemanticVersion(Major(less)) <= SemanticVersion(Major(greater)) is true", testLeeThanEqualWithLess)
  , property("Test SemanticVersion(same Major) >= SemanticVersion(same Major) is true", testMoreThanEqualWithSameMajors)
  , property("Test SemanticVersion(Major(greater)) >= SemanticVersion(Major(less)) is true", testMoreThanEqualWithGreater)
  )

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testSameMajors: Property = for {
    major <- genMajor.log("major")
  } yield {
    val v1 = SemanticVersion.withMajor(major)
    val v2 = SemanticVersion.withMajor(major)
    Result.assert(v1 == v2).log("major == major")
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testDifferentMajors: Property = for {
    major1AndMajor2 <- genMinMaxMajors.log("(major1, major2)")
    (major1, major2) = major1AndMajor2
  } yield {
    val v1 = SemanticVersion.withMajor(major1)
    val v2 = SemanticVersion.withMajor(major2)
    Result.assert(v1 != v2).log("major1 != major2")
  }

  def testMajorLessCase: Property = for {
    major1AndMajor2 <- genMinMaxMajors.log("(major1, major2)")
    (major1, major2) = major1AndMajor2
  } yield {
    val v1 = SemanticVersion.withMajor(major1)
    val v2 = SemanticVersion.withMajor(major2)
    Result.assert(v1 < v2).log("major1 < major2")
  }

  def testMajorMoreCase: Property = for {
    major1AndMajor2 <- genMinMaxMajors.log("(major1, major2)")
    (major1, major2) = major1AndMajor2
  } yield {
    val v1 = SemanticVersion.withMajor(major1)
    val v2 = SemanticVersion.withMajor(major2)
    Result.assert(v2 > v1).log("major2 > major1")
  }

  def testLeeThanEqualWithSameMajors: Property = for {
    major <- genMajor.log("major")
  } yield {
    val v1 = SemanticVersion.withMajor(major)
    val v2 = SemanticVersion.withMajor(major)
    Result.assert(v1 <= v2).log("major1 <= major2")
  }

  def testLeeThanEqualWithLess: Property = for {
    major1AndMajor2 <- genMinMaxMajors.log("(major1, major2)")
    (major1, major2) = major1AndMajor2
  } yield {
    val v1 = SemanticVersion.withMajor(major1)
    val v2 = SemanticVersion.withMajor(major2)
    Result.assert(v1 <= v2).log("major1 <= major2")
  }

  def testMoreThanEqualWithSameMajors: Property = for {
    major <- genMajor.log("major")
  } yield {
    val v1 = SemanticVersion.withMajor(major)
    val v2 = SemanticVersion.withMajor(major)
    Result.assert(v1 >= v2)
  }

  def testMoreThanEqualWithGreater: Property = for {
    major1AndMajor2 <- genMinMaxMajors.log("(major1, major2)")
    (major1, major2) = major1AndMajor2
  } yield {
    val v1 = SemanticVersion.withMajor(major1)
    val v2 = SemanticVersion.withMajor(major2)
    Result.assert(v2 >= v1).log("major2 >= major1")
  }

}

object SemanticVersionMinorSpec extends Properties {

  override def tests: List[Test] = List(
    property("Two SemanticVersions with the same Minor and the rest are equal then it should be equal", testSameMinors)
    , property("Two SemanticVersions with the different Minors and the rest are equal then it should be not equal", testDifferentMinors)
    , property("Test SemanticVersion(Minor(less)) < SemanticVersion(Minor(greater)) is true", testMinorLessCase)
    , property("Test SemanticVersion(Minor(greater)) > SemanticVersion(Minor(less)) is true", testMinorMoreCase)
    , property("Test SemanticVersion(same Minor) <= SemanticVersion(same Minor) is true", testLeeThanEqualWithSameMinors)
    , property("Test SemanticVersion(Minor(less)) <= SemanticVersion(Minor(greater)) is true", testLeeThanEqualWithLess)
    , property("Test SemanticVersion(same Minor) >= SemanticVersion(same Minor) is true", testMoreThanEqualWithSameMinors)
    , property("Test SemanticVersion(Minor(greater)) >= SemanticVersion(Minor(less)) is true", testMoreThanEqualWithGreater)
  )

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testSameMinors: Property = for {
    minor <- genMinor.log("minor")
  } yield {
    val v1 = SemanticVersion.withMinor(minor)
    val v2 = SemanticVersion.withMinor(minor)
    Result.assert(v1 == v2).log("minor == minor")
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testDifferentMinors: Property = for {
    minor1AndMinor2 <- genMinMaxMinors.log("(minor1, minor2)")
    (minor1, minor2) = minor1AndMinor2
  } yield {
    val v1 = SemanticVersion.withMinor(minor1)
    val v2 = SemanticVersion.withMinor(minor2)
    Result.assert(v1 != v2).log("minor1 != minor2")
  }

  def testMinorLessCase: Property = for {
    minor1AndMinor2 <- genMinMaxMinors.log("(minor1, minor2)")
    (minor1, minor2) = minor1AndMinor2
  } yield {
    val v1 = SemanticVersion.withMinor(minor1)
    val v2 = SemanticVersion.withMinor(minor2)
    Result.assert(v1 < v2).log("minor1 < minor2")
  }

  def testMinorMoreCase: Property = for {
    minor1AndMinor2 <- genMinMaxMinors.log("(minor1, minor2)")
    (minor1, minor2) = minor1AndMinor2
  } yield {
    val v1 = SemanticVersion.withMinor(minor1)
    val v2 = SemanticVersion.withMinor(minor2)
    Result.assert(v2 > v1).log("minor2 > minor1")
  }

  def testLeeThanEqualWithSameMinors: Property = for {
    minor <- genMinor.log("minor")
  } yield {
    val v1 = SemanticVersion.withMinor(minor)
    val v2 = SemanticVersion.withMinor(minor)
    Result.assert(v1 <= v2).log("minor1 <= minor2")
  }

  def testLeeThanEqualWithLess: Property = for {
    minor1AndMinor2 <- genMinMaxMinors.log("(minor1, minor2)")
    (minor1, minor2) = minor1AndMinor2
  } yield {
    val v1 = SemanticVersion.withMinor(minor1)
    val v2 = SemanticVersion.withMinor(minor2)
    Result.assert(v1 <= v2).log("minor1 <= minor2")
  }

  def testMoreThanEqualWithSameMinors: Property = for {
    minor <- genMinor.log("minor")
  } yield {
    val v1 = SemanticVersion.withMinor(minor)
    val v2 = SemanticVersion.withMinor(minor)
    Result.assert(v1 >= v2)
  }

  def testMoreThanEqualWithGreater: Property = for {
    minor1AndMinor2 <- genMinMaxMinors.log("(minor1, minor2)")
    (minor1, minor2) = minor1AndMinor2
  } yield {
    val v1 = SemanticVersion.withMinor(minor1)
    val v2 = SemanticVersion.withMinor(minor2)
    Result.assert(v2 >= v1).log("minor2 >= minor1")
  }

}

object SemanticVersionPatchSpec extends Properties {

  override def tests: List[Test] = List(
    property("Two SemanticVersions with the same Patch and the rest are equal then it should be equal", testSamePatchs)
    , property("Two SemanticVersions with the different Patchs and the rest are equal then it should be not equal", testDifferentPatchs)
    , property("Test SemanticVersion(Patch(less)) < SemanticVersion(Patch(greater)) is true", testPatchLessCase)
    , property("Test SemanticVersion(Patch(greater)) > SemanticVersion(Patch(less)) is true", testPatchMoreCase)
    , property("Test SemanticVersion(same Patch) <= SemanticVersion(same Patch) is true", testLeeThanEqualWithSamePatchs)
    , property("Test SemanticVersion(Patch(less)) <= SemanticVersion(Patch(greater)) is true", testLeeThanEqualWithLess)
    , property("Test SemanticVersion(same Patch) >= SemanticVersion(same Patch) is true", testMoreThanEqualWithSamePatchs)
    , property("Test SemanticVersion(Patch(greater)) >= SemanticVersion(Patch(less)) is true", testMoreThanEqualWithGreater)
  )

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testSamePatchs: Property = for {
    patch <- genPatch.log("patch")
  } yield {
    val v1 = SemanticVersion.withPatch(patch)
    val v2 = SemanticVersion.withPatch(patch)
    Result.assert(v1 == v2).log("patch == patch")
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testDifferentPatchs: Property = for {
    patch1AndPatch2 <- genMinMaxPatches.log("(patch1, patch2)")
    (patch1, patch2) = patch1AndPatch2
  } yield {
    val v1 = SemanticVersion.withPatch(patch1)
    val v2 = SemanticVersion.withPatch(patch2)
    Result.assert(v1 != v2).log("patch1 != patch2")
  }

  def testPatchLessCase: Property = for {
    patch1AndPatch2 <- genMinMaxPatches.log("(patch1, patch2)")
    (patch1, patch2) = patch1AndPatch2
  } yield {
    val v1 = SemanticVersion.withPatch(patch1)
    val v2 = SemanticVersion.withPatch(patch2)
    Result.assert(v1 < v2).log("patch1 < patch2")
  }

  def testPatchMoreCase: Property = for {
    patch1AndPatch2 <- genMinMaxPatches.log("(patch1, patch2)")
    (patch1, patch2) = patch1AndPatch2
  } yield {
    val v1 = SemanticVersion.withPatch(patch1)
    val v2 = SemanticVersion.withPatch(patch2)
    Result.assert(v2 > v1).log("patch2 > patch1")
  }

  def testLeeThanEqualWithSamePatchs: Property = for {
    patch <- genPatch.log("patch")
  } yield {
    val v1 = SemanticVersion.withPatch(patch)
    val v2 = SemanticVersion.withPatch(patch)
    Result.assert(v1 <= v2).log("patch1 <= patch2")
  }

  def testLeeThanEqualWithLess: Property = for {
    patch1AndPatch2 <- genMinMaxPatches.log("(patch1, patch2)")
    (patch1, patch2) = patch1AndPatch2
  } yield {
    val v1 = SemanticVersion.withPatch(patch1)
    val v2 = SemanticVersion.withPatch(patch2)
    Result.assert(v1 <= v2).log("patch1 <= patch2")
  }

  def testMoreThanEqualWithSamePatchs: Property = for {
    patch <- genPatch.log("patch")
  } yield {
    val v1 = SemanticVersion.withPatch(patch)
    val v2 = SemanticVersion.withPatch(patch)
    Result.assert(v1 >= v2)
  }

  def testMoreThanEqualWithGreater: Property = for {
    patch1AndPatch2 <- genMinMaxPatches.log("(patch1, patch2)")
    (patch1, patch2) = patch1AndPatch2
  } yield {
    val v1 = SemanticVersion.withPatch(patch1)
    val v2 = SemanticVersion.withPatch(patch2)
    Result.assert(v2 >= v1).log("patch2 >= patch1")
  }

}

object AlphaNumHyphenSpec extends Properties {

  override def tests: List[Test] = List(
    property("Num(same).compare(Num(same)) should return 0", testNumEqual)
  , property("Num(less).compare(Num(greater)) should return -1", testNumLess)
  , property("Num(greater).compare(Num(less)) should return 1", testNumMore)
  , property("AlphaHyphen(same).compare(AlphaHyphen(same)) should return 0", testAlphaHyphenEqual)
  , property("AlphaHyphen(less).compare(AlphaHyphen(greater)) should return the Int < 0", testAlphaHyphenLess)
  , property("AlphaHyphen(greater).compare(AlphaHyphen(less)) should return the Int > 0", testAlphaHyphenMore)
  )

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testNumEqual: Property = for {
    num <- genNum.log("num")
  } yield {
    num.compare(num) ==== 0 and Result.assert(num == num)
  }

  def testNumLess: Property = for {
    minMax <- genMinMaxNum.log("(num1, num2)")
    (num1, num2) = minMax
  } yield {
    num1.compare(num2) ==== -1
  }

  def testNumMore: Property = for {
    minMax <- genMinMaxNum.log("(num1, num2)")
    (num1, num2) = minMax
  } yield {
    num2.compare(num1) ==== 1
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testAlphaHyphenEqual: Property = for {
    alphaHyphen <- genAlphaHyphen(10).log("alphaHyphen")
  } yield {
    alphaHyphen.compare(alphaHyphen) ==== 0 and Result.assert(alphaHyphen == alphaHyphen)
  }

  def testAlphaHyphenLess: Property = for {
    alphaHyphenPair <- genMinMaxAlphaHyphen(10).log("(alphaHyphen1, alphaHyphen2)")
    (alphaHyphen1, alphaHyphen2) = alphaHyphenPair
  } yield {
    Result.assert(alphaHyphen1.compare(alphaHyphen2) < 0)
  }

  def testAlphaHyphenMore: Property = for {
    alphaHyphenPair <- genMinMaxAlphaHyphen(10).log("(alphaHyphen1, alphaHyphen2)")
    (alphaHyphen1, alphaHyphen2) = alphaHyphenPair
  } yield {
    Result.assert(alphaHyphen2.compare(alphaHyphen1) > 0)
  }

}
