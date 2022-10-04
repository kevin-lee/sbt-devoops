package kevinlee.sbt.io

import hedgehog.*
import kevinlee.test.data.{Content, Names, NamesAndContent}

/** @author Kevin Lee
  * @since 2019-02-23
  */
object Gens {

  def genFilenamesAndContent: Gen[(List[String], String)] = for {
    names   <- Gen.string(Gen.alphaNum, Range.linear(1, 5)).list(Range.linear(1, 5))
    content <- Gen.string(Gen.unicode, Range.linear(5, 20))
  } yield (names, content)

  def genFilenamesAndContentWithFirstUniqueName: Gen[List[NamesAndContent]] =
    Gens
      .genFilenamesAndContent
      .list(Range.linear(1, 5))
      .map { namesList =>
        namesList.zipWithIndex.map {
          case ((names, content), index) =>
            NamesAndContent(
              Names(
                names.map(x => s"${x}_$index")
              ),
              Content(content),
            )
        }
      }

}
