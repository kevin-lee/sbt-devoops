package io.kevinlee.sbt

import hedgehog._
import hedgehog.runner._

/**
  * @author Kevin Lee
  * @since 2018-11-04
  */
object SemanticVersionMajorSpec extends Properties {

  override def tests: List[Test] = List(
    property("Two SemanticVersions with the same Major and the rest are equal then it should be equal", testSameMajors)
  , property("Two SemanticVersions with the different Majors and the rest are equal then it should be not equal", testDifferentMajors)
  , property("Test SemanticVersion(Major(less)) < SemanticVersion(Major(more)) is true", testMajorLessCase)
  , property("Test SemanticVersion(Major(more)) > SemanticVersion(Major(less)) is true", testMajorMoreCase)
  , property("Test SemanticVersion(same Major) <= SemanticVersion(same Major) is true", testLeeThanEqualWithSameMajors)
  , property("Test SemanticVersion(same Major) >= SemanticVersion(same Major) is true", testMoreThanEqualWithSameMajors)
  )

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testSameMajors: Property = for {
    major <- Gens.genMajor.log("major")
  } yield {
    val v1 = SemanticVersion.withMajor(major)
    val v2 = SemanticVersion.withMajor(major)
    Result.assert(v1 == v2)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testDifferentMajors: Property = for {
    major1AndMajor2 <- Gens.genDifferentMajors.log("(major1, major2)")
    (major1, major2) = major1AndMajor2
  } yield {
    val v1 = SemanticVersion.withMajor(major1)
    val v2 = SemanticVersion.withMajor(major2)
    Result.assert(v1 != v2)
  }

  def testMajorLessCase: Property = for {
    major1AndMajor2 <- Gens.genDifferentMajors.log("(major1, major2)")
    (major1, major2) = major1AndMajor2
  } yield {
    val (v1, v2) =
      if (major1.major < major2.major)
        (SemanticVersion.withMajor(major1), SemanticVersion.withMajor(major2))
      else
        (SemanticVersion.withMajor(major2), SemanticVersion.withMajor(major1))
    Result.assert(v1 < v2)
  }

  def testMajorMoreCase: Property = for {
    major1AndMajor2 <- Gens.genDifferentMajors.log("(major1, major2)")
    (major1, major2) = major1AndMajor2
  } yield {
    val (v1, v2) =
      if (major1.major > major2.major)
        (SemanticVersion.withMajor(major1), SemanticVersion.withMajor(major2))
      else
        (SemanticVersion.withMajor(major2), SemanticVersion.withMajor(major1))
    Result.assert(v1 > v2)
  }

  def testLeeThanEqualWithSameMajors: Property = for {
    major <- Gens.genMajor.log("major")
  } yield {
    val v1 = SemanticVersion.withMajor(major)
    val v2 = SemanticVersion.withMajor(major)
    Result.assert(v1 <= v2)
  }

  def testMoreThanEqualWithSameMajors: Property = for {
    major <- Gens.genMajor.log("major")
  } yield {
    val v1 = SemanticVersion.withMajor(major)
    val v2 = SemanticVersion.withMajor(major)
    Result.assert(v1 >= v2)
  }

}

object SemanticVersionMinorSpec extends Properties {

  override def tests: List[Test] = List(
    property("Two SemanticVersions with the same Minor and the rest are equal then it should be equal", testSameMinors)
  , property("Two SemanticVersions with the different Minors and the rest are equal then it should be not equal", testDifferentMinors)
  , property("Test SemanticVersion(Minor(less)) < SemanticVersion(Minor(more)) is true", testMinorLessCase)
  , property("Test SemanticVersion(Minor(more)) > SemanticVersion(Minor(less)) is true", testMinorMoreCase)
  , property("Test SemanticVersion(same Minor) <= SemanticVersion(same Minor) is true", testLeeThanEqualWithSameMinors)
  , property("Test SemanticVersion(same Minor) >= SemanticVersion(same Minor) is true", testMoreThanEqualWithSameMinors)
  )

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testSameMinors: Property = for {
    minor <- Gens.genMinor.log("minor")
  } yield {
    val v1 = SemanticVersion.withMinor(minor)
    val v2 = SemanticVersion.withMinor(minor)
    Result.assert(v1 == v2)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testDifferentMinors: Property = for {
    minor1AndMinor2 <- Gens.genDifferentMinors.log("(minor1, minor2)")
    (minor1, minor2) = minor1AndMinor2
  } yield {
    val v1 = SemanticVersion.withMinor(minor1)
    val v2 = SemanticVersion.withMinor(minor2)
    Result.assert(v1 != v2)
  }

  def testMinorLessCase: Property = for {
    minor1AndMinor2 <- Gens.genDifferentMinors.log("(minor1, minor2)")
    (minor1, minor2) = minor1AndMinor2
  } yield {
    val (v1, v2) =
      if (minor1.minor < minor2.minor)
        (SemanticVersion.withMinor(minor1), SemanticVersion.withMinor(minor2))
      else
        (SemanticVersion.withMinor(minor2), SemanticVersion.withMinor(minor1))
    Result.assert(v1 < v2)
  }

  def testMinorMoreCase: Property = for {
    minor1AndMinor2 <- Gens.genDifferentMinors.log("(minor1, minor2)")
    (minor1, minor2) = minor1AndMinor2
  } yield {
    val (v1, v2) =
      if (minor1.minor > minor2.minor)
        (SemanticVersion.withMinor(minor1), SemanticVersion.withMinor(minor2))
      else
        (SemanticVersion.withMinor(minor2), SemanticVersion.withMinor(minor1))
    Result.assert(v1 > v2)
  }

  def testLeeThanEqualWithSameMinors: Property = for {
    minor <- Gens.genMinor.log("minor")
  } yield {
    val v1 = SemanticVersion.withMinor(minor)
    val v2 = SemanticVersion.withMinor(minor)
    Result.assert(v1 <= v2)
  }

  def testMoreThanEqualWithSameMinors: Property = for {
    minor <- Gens.genMinor.log("minor")
  } yield {
    val v1 = SemanticVersion.withMinor(minor)
    val v2 = SemanticVersion.withMinor(minor)
    Result.assert(v1 >= v2)
  }

}

object SemanticVersionPatchSpec extends Properties {

  override def tests: List[Test] = List(
    property("Two SemanticVersions with the same Patch and the rest are equal then it should be equal", testSamePatches)
  , property("Two SemanticVersions with the different Patches and the rest are equal then it should be not equal", testDifferentPatches)
  , property("Test SemanticVersion(Patch(less)) < SemanticVersion(Patch(more)) is true", testPatchLessCase)
  , property("Test SemanticVersion(Patch(more)) > SemanticVersion(Patch(less)) is true", testPatchMoreCase)
  , property("Test SemanticVersion(same Patch) <= SemanticVersion(same Patch) is true", testLeeThanEqualWithSamePatches)
  , property("Test SemanticVersion(same Patch) >= SemanticVersion(same Patch) is true", testMoreThanEqualWithSamePatches)
  )

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testSamePatches: Property = for {
    patch <- Gens.genPatch.log("patch")
  } yield {
    val v1 = SemanticVersion.withPatch(patch)
    val v2 = SemanticVersion.withPatch(patch)
    Result.assert(v1 == v2)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def testDifferentPatches: Property = for {
    patch1AndPatch2 <- Gens.genDifferentPatches.log("(patch1, patch2)")
    (patch1, patch2) = patch1AndPatch2
  } yield {
    val v1 = SemanticVersion.withPatch(patch1)
    val v2 = SemanticVersion.withPatch(patch2)
    Result.assert(v1 != v2)
  }

  def testPatchLessCase: Property = for {
    patch1AndPatch2 <- Gens.genDifferentPatches.log("(patch1, patch2)")
    (patch1, patch2) = patch1AndPatch2
  } yield {
    val (v1, v2) =
      if (patch1.patch < patch2.patch)
        (SemanticVersion.withPatch(patch1), SemanticVersion.withPatch(patch2))
      else
        (SemanticVersion.withPatch(patch2), SemanticVersion.withPatch(patch1))
    Result.assert(v1 < v2)
  }

  def testPatchMoreCase: Property = for {
    patch1AndPatch2 <- Gens.genDifferentPatches.log("(patch1, patch2)")
    (patch1, patch2) = patch1AndPatch2
  } yield {
    val (v1, v2) =
      if (patch1.patch > patch2.patch)
        (SemanticVersion.withPatch(patch1), SemanticVersion.withPatch(patch2))
      else
        (SemanticVersion.withPatch(patch2), SemanticVersion.withPatch(patch1))
    Result.assert(v1 > v2)
  }

  def testLeeThanEqualWithSamePatches: Property = for {
    patch <- Gens.genPatch.log("patch")
  } yield {
    val v1 = SemanticVersion.withPatch(patch)
    val v2 = SemanticVersion.withPatch(patch)
    Result.assert(v1 <= v2)
  }

  def testMoreThanEqualWithSamePatches: Property = for {
    patch <- Gens.genPatch.log("patch")
  } yield {
    val v1 = SemanticVersion.withPatch(patch)
    val v2 = SemanticVersion.withPatch(patch)
    Result.assert(v1 >= v2)
  }

}
