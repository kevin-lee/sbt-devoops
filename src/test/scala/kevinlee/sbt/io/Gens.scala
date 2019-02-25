package kevinlee.sbt.io

import hedgehog._

/**
  * @author Kevin Lee
  * @since 2019-02-23
  */
object Gens {
  def genFilenamesAndContent: Gen[(List[String], String)] = for {
    names <- Gen.string(Gen.alphaNum, Range.linear(1, 5)).list(Range.linear(1, 5))
    content <- Gen.string(Gen.unicode, Range.linear(5, 50))
  } yield (names, content)
}
