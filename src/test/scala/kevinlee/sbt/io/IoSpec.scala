package kevinlee.sbt.io

import java.io.File
import java.nio.charset.StandardCharsets
import java.security.SecureRandom

import hedgehog._
import hedgehog.runner._
import kevinlee.IoUtil

import scala.util.Random

/**
  * @author Kevin Lee
  * @since 2019-02-23
  */
object IoSpec extends Properties {
  override def tests: List[Test] = List(
      example("test findFiles with wildcard", testFindFilesWithWildcard)
    , property("test findFiles", testFindFiles)
  )

  def testFindFilesWithWildcard: Result = {
    val filenames = List(
        "target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-javadoc.jar"
      , "target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-sources.jar"
      , "target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT.jar"
      , "target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-javadoc.jar"
      , "target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-sources.jar"
      , "target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT.jar"
      , "target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-javadoc.jar"
      , "target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-sources.jar"
      , "target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT.jar"
    )

    val content =
      """Lorem ipsum dolor sit amet, consectetur adipiscing elit,
        |sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
        |Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris
        |nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor
        |in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
        |Excepteur sint occaecat cupidatat non proident,
        |sunt in culpa qui officia deserunt mollit anim id est laborum.""".stripMargin

    IoUtil.withTempDir { tmp =>
      val expected = for {
        filename <- filenames
        file = new File(tmp, filename)
        _ = IoUtil.writeFile(file, content)
      } yield file

      val actual = Io.findAllFiles(
        CaseSensitivity.caseSensitive
      , tmp
      , List(
            "target/scala-*.10/just-utc_*-0.1.0-*.jar"
          , "target/scala-2.11/just-utc_*-0.1.0-*.jar"
          , "target/scala-2.12/just-utc_*-0.1.0-*.jar"
        )
      )

      Result.all(
        (actual.sorted ==== expected.sorted) :: (for {
          file <- actual
          actualContent = IoUtil.readFile(file)
        } yield actualContent ==== content)
      )

    }
  }


  val random: Random =
    new Random(new SecureRandom("IoSpec".getBytes(StandardCharsets.UTF_8)))

  def testFindFiles: Property = for {
    namesAndContentList <-
      Gens.genFilenamesAndContent
        .list(Range.linear(1, 5))
        .map{ namesList =>
          namesList.map { case (names, content) =>
            (names.headOption.fold(s"abc_${random.nextInt()}")(x => s"${x}_${random.nextInt()}") :: names.drop(1), content)
          }
        }
        .log("namesAndContentList")
  } yield {

    IoUtil.withTempDir { tmp =>
      val namesAndFiles = for {
        (names, content) <- namesAndContentList
        name = names.mkString("/")
        file = new File(tmp, name)
        _ = IoUtil.writeFile(file, content)
      } yield (name, file)

      val names = namesAndFiles.map{ case (ns, _) => ns }
      val expected = namesAndFiles.map{ case (_, fs) => fs }
      val actual = Io.findAllFiles(CaseSensitivity.caseSensitive, tmp, names)
      actual ==== expected
    }
  }
}
