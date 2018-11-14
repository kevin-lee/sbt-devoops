package io.kevinlee.sbt

import hedgehog._
import CommonPredef._

/**
  * @author Kevin Lee
  * @since 2018-11-04
  */
object Gens {

  def genPlus[A](r: Range[A])(f: Range[A] => Gen[A]): Gen[A] ={
    val (min, max) = r.bounds(Size(Size.max))
    Gen.frequency1(
        (1, Gen.constant(min))
      , (1, Gen.constant(max))
      , (1, Gen.constant(r.origin))
      , (97, f(r))
    )
  }

  def genAlphabet: Gen[Char] =
    Gen.frequency1(
      8 -> Gen.char('a', 'z'), 2 -> Gen.char('A', 'Z')
    )

  def genAlphabetHyphen: Gen[Char] =
    Gen.frequency1(
      9 -> genAlphabet, 1 -> Gen.constant('-')
    )

  def genMinMax[T : Ordering](genOrderedPair: Gen[(T, T)]): Gen[(T, T)] =
    genOrderedPair.map { case (x, y) =>
      if (implicitly[Ordering[T]].compare(x, y) < 0) (x, y) else (y, x)
    }

  def genNonNegativeInt: Gen[Int] =
    genPlus(Range.linear(0, Int.MaxValue))(Gen.int)

  def genDifferentNonNegIntPair: Gen[(Int, Int)] = for {
    x <- genNonNegativeInt
    y <- genNonNegativeInt
  } yield {
    val z =
      if (x !== y) y
      else if (y === Int.MaxValue) 0
      else y + 1
    (x, z)
  }

  def genMinMaxNonNegInts: Gen[(Int, Int)] =
    Gens.genMinMax(Gens.genDifferentNonNegIntPair)

  def pairFromIntsTo[T](constructor: Int => T): ((Int, Int)) => (T, T) =
    pair => (constructor(pair._1), constructor(pair._2))


  def genMajor: Gen[Major] =
    genNonNegativeInt.map(Major)

  def genMinMaxMajors: Gen[(Major, Major)] =
    genMinMaxNonNegInts.map(pairFromIntsTo(Major))

  def genMinor: Gen[Minor] =
    genNonNegativeInt.map(Minor)

  def genMinMaxMinors: Gen[(Minor, Minor)] =
    genMinMaxNonNegInts.map(pairFromIntsTo(Minor))

  def genPatch: Gen[Patch] =
    genNonNegativeInt.map(Patch)

  def genMinMaxPatches: Gen[(Patch, Patch)] =
    genMinMaxNonNegInts.map(pairFromIntsTo(Patch))

  def genNum: Gen[AlphaNumHyphen] =
    genNonNegativeInt.map(Num)

  def genMinMaxNum: Gen[(AlphaNumHyphen, AlphaNumHyphen)] =
    genMinMaxNonNegInts.map(pairFromIntsTo(Num))

  def genAlphaHyphenString(max: Int): Gen[String] =
    Gen.string(genAlphabetHyphen, Range.linear(1, max))

  def genAlphaHyphen(max: Int): Gen[AlphaNumHyphen] =
    genAlphaHyphenString(max).map(AlphaHyphen)

  def genDifferentAlphaHyphenPair(max: Int): Gen[(AlphaNumHyphen, AlphaNumHyphen)] =
    for {
      x <- genAlphaHyphenString(max)
      y <- genAlphaHyphenString(max)
      z <- genAlphabetHyphen
    } yield {
      if (x === y)
        (AlphaHyphen(x), AlphaHyphen(y + String.valueOf(z)))
      else
        (AlphaHyphen(x), AlphaHyphen(y))
    }

  def genMinMaxAlphaHyphen(max: Int): Gen[(AlphaNumHyphen, AlphaNumHyphen)] =
    genMinMax(genDifferentAlphaHyphenPair(max))

}
