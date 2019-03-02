package kevinlee.sbt.io

import java.nio.charset.StandardCharsets
import java.security.SecureRandom

import hedgehog._
import kevinlee.test.data.{Content, Names, NamesAndContent}

import scala.util.Random

/**
  * @author Kevin Lee
  * @since 2019-02-23
  */
object Gens {

  val random: Random =
    new Random(new SecureRandom("Gens".getBytes(StandardCharsets.UTF_8)))


  def genFilenamesAndContent: Gen[(List[String], String)] = for {
    names <- Gen.string(Gen.alphaNum, Range.linear(1, 5)).list(Range.linear(1, 5))
    content <- Gen.string(Gen.unicode, Range.linear(5, 50))
  } yield (names, content)

  def genFilenamesAndContentWithFirstUniqueName: Gen[List[NamesAndContent]] =
    Gens.genFilenamesAndContent
      .list(Range.linear(1, 5))
      .map { namesList =>
        namesList.map { case (names, content) =>
          val randomNumber = random.nextInt()
          NamesAndContent(
              Names(
                names.headOption.fold(s"abc_$randomNumber")(x => s"${x}_$randomNumber") :: names.drop(1)
              )
            , Content(content)
          )
        }
      }

}
