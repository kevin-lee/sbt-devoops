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


  def genIdentifier: Gen[Identifier] = for {
    values <- Gen.choice1[AlphaNumHyphen](genNum, genAlphaHyphen(10)).list(Range.linear(1, 3))
  } yield Identifier(values)

  def genMinMaxIdentifier: Gen[(Identifier, Identifier)] = for {
    minMaxIds <- Gen.choice1(genMinMaxNum, genMinMaxAlphaHyphen(10)).list(Range.linear(1, 3))
    (minIds, maxIds) = minMaxIds.foldLeft((List.empty[AlphaNumHyphen], List.empty[AlphaNumHyphen])){ case ((ids1, ids2), (id1, id2)) =>
      (ids1 :+ id1, ids2 :+ id2)
    }

  } yield (Identifier(minIds), Identifier(maxIds))


  def genSemanticVersion: Gen[SemanticVersion] = for {
    major <- genMajor
    minor <- genMinor
    patch <- genPatch
    pre <- genIdentifier.option
    meta <- genIdentifier.option
  } yield SemanticVersion(major, minor, patch, pre, meta)

  def genMinMaxSemanticVersions: Gen[(SemanticVersion, SemanticVersion)] = for {
    majorPair <- genMinMaxMajors
    minorPair <- genMinMaxMinors
    patchPair <- genMinMaxPatches
    pre <- genMinMaxIdentifier.option
    meta <- genMinMaxIdentifier.option
  } yield {
    val (pre1, pre2) =
      pre.fold[(Option[Identifier], Option[Identifier])]((None, None))(
        xy => (Option(xy._1), Option(xy._2))
      )
    val (meta1, meta2) =
      meta.fold[(Option[Identifier], Option[Identifier])]((None, None))(
        xy => (Option(xy._1), Option(xy._2))
      )

    (SemanticVersion(
        majorPair._1
      , minorPair._1
      , patchPair._1
      , pre1
      , meta1
    ),
    SemanticVersion(
        majorPair._2
      , minorPair._2
      , patchPair._2
      , pre2
      , meta2
    ))
  }

}

