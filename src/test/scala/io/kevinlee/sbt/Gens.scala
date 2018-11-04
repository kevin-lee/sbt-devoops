package io.kevinlee.sbt

import hedgehog._

/**
  * @author Kevin Lee
  * @since 2018-11-04
  */
object Gens {

  def genNonNegativeInt: Gen[Int] =
    Gen.int(Range.linear(0, Int.MaxValue))

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def genDifferentNonNegativeIntPair: Gen[(Int, Int)] = for {
    x <- genNonNegativeInt
    y <- genNonNegativeInt
  } yield {
    val z =
      if (x != y) y
      else if (y == Int.MaxValue) 0
      else y + 1
    (x, z)
  }


  def genMajor: Gen[Major] =
    genNonNegativeInt.map(Major)


  def genDifferentMajors: Gen[(Major, Major)] = for {
    xAndY <- genDifferentNonNegativeIntPair
    (x, y) = xAndY
  } yield (Major(x), Major(y))


  def genMinor: Gen[Minor] =
    genNonNegativeInt.map(Minor)

  def genDifferentMinors: Gen[(Minor, Minor)] = for {
    xAndY <- genDifferentNonNegativeIntPair
    (x, y) = xAndY
  } yield (Minor(x), Minor(y))

  def genPatch: Gen[Patch] =
    genNonNegativeInt.map(Patch)

  def genDifferentPatches: Gen[(Patch, Patch)] = for {
    xAndY <- genDifferentNonNegativeIntPair
    (x, y) = xAndY
  } yield (Patch(x), Patch(y))

}
