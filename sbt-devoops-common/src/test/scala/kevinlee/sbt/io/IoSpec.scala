package kevinlee.sbt.io

import hedgehog._
import hedgehog.runner._
import kevinlee.test.IoUtil

import java.io.File

/** @author Kevin Lee
  * @since 2019-02-23
  */
object IoSpec extends Properties {
  override def tests: List[Test] = List(
    example("test findFiles with wildcard", testFindFilesWithWildcard),
    example("test findFiles with wildcard (*/file)", testFindFilesWithWildcardDepth1),
    example("test findFiles with wildcard (*/*/file)", testFindFilesWithWildcardDepth2),
    property("test findFiles", testFindFiles),
    property("test copy", testCopy),
  )

  def testFindFilesWithWildcard: Result = {
    val filenames = List(
      "target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-javadoc.jar",
      "target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-sources.jar",
      "target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT.jar",
      "target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-javadoc.jar",
      "target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-sources.jar",
      "target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT.jar",
      "target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-javadoc.jar",
      "target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-sources.jar",
      "target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT.jar",
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
      val expected =
        for {
          filename <- filenames
          file = new File(tmp, filename)
          _    = IoUtil.writeFile(file, content)
        } yield file

      val actual = Io.findAllFiles(
        CaseSensitivity.caseSensitive,
        tmp,
        List(
          "target/scala-*.10/just-utc_*-0.1.0-*.jar",
          "target/scala-2.11/just-utc_*-0.1.0-*.jar",
          "target/scala-2.12/just-utc_*-0.1.0-*.jar",
        ),
      )

      Result.all(
        (actual.sorted ==== expected.sorted) :: (for {
          file <- actual
          actualContent = IoUtil.readFile(file)
        } yield actualContent ==== content)
      )

    }
  }

  def testFindFilesWithWildcardDepth1: Result = {
    val filenames = List(
      "core/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-javadoc.jar",
      "core/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-sources.jar",
      "core/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT.jar",
      "cli/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-javadoc.jar",
      "cli/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-sources.jar",
      "cli/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT.jar",
      "core/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-javadoc.jar",
      "core/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-sources.jar",
      "core/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT.jar",
      "cli/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-javadoc.jar",
      "cli/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-sources.jar",
      "cli/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT.jar",
      "core/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-javadoc.jar",
      "core/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-sources.jar",
      "core/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT.jar",
      "cli/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-javadoc.jar",
      "cli/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-sources.jar",
      "cli/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT.jar",
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
      val expected =
        for {
          filename <- filenames
          file = new File(tmp, filename)
          _    = IoUtil.writeFile(file, content)
        } yield file

      val actual = Io.findAllFiles(
        CaseSensitivity.caseSensitive,
        tmp,
        List(
          "*/target/scala-*.10/just-utc_*-0.1.0-*.jar",
          "*/target/scala-2.11/just-utc_*-0.1.0-*.jar",
          "*/target/scala-2.12/just-utc_*-0.1.0-*.jar",
        ),
      )

      Result.all(
        (actual.sorted ==== expected.sorted) :: (for {
          file <- actual
          actualContent = IoUtil.readFile(file)
        } yield actualContent ==== content)
      )

    }
  }

  def testFindFilesWithWildcardDepth2: Result = {
    val filenames = List(
      "core/js/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-javadoc.jar",
      "core/js/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-sources.jar",
      "core/js/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT.jar",
      "core/jvm/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-javadoc.jar",
      "core/jvm/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-sources.jar",
      "core/jvm/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT.jar",
      "cli/js/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-javadoc.jar",
      "cli/js/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-sources.jar",
      "cli/js/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT.jar",
      "cli/jvm/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-javadoc.jar",
      "cli/jvm/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT-sources.jar",
      "cli/jvm/target/scala-2.10/just-utc_2.10-0.1.0-SNAPSHOT.jar",
      "core/js/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-javadoc.jar",
      "core/js/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-sources.jar",
      "core/js/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT.jar",
      "core/jvm/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-javadoc.jar",
      "core/jvm/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-sources.jar",
      "core/jvm/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT.jar",
      "cli/js/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-javadoc.jar",
      "cli/js/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-sources.jar",
      "cli/js/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT.jar",
      "cli/jvm/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-javadoc.jar",
      "cli/jvm/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT-sources.jar",
      "cli/jvm/target/scala-2.11/just-utc_2.11-0.1.0-SNAPSHOT.jar",
      "core/js/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-javadoc.jar",
      "core/js/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-sources.jar",
      "core/js/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT.jar",
      "core/jvm/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-javadoc.jar",
      "core/jvm/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-sources.jar",
      "core/jvm/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT.jar",
      "cli/js/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-javadoc.jar",
      "cli/js/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-sources.jar",
      "cli/js/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT.jar",
      "cli/jvm/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-javadoc.jar",
      "cli/jvm/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT-sources.jar",
      "cli/jvm/target/scala-2.12/just-utc_2.12-0.1.0-SNAPSHOT.jar",
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
      val expected =
        for {
          filename <- filenames
          file = new File(tmp, filename)
          _    = IoUtil.writeFile(file, content)
        } yield file

      val actual = Io.findAllFiles(
        CaseSensitivity.caseSensitive,
        tmp,
        List(
          "*/*/target/scala-*.10/just-utc_*-0.1.0-*.jar",
          "*/*/target/scala-2.11/just-utc_*-0.1.0-*.jar",
          "*/*/target/scala-2.12/just-utc_*-0.1.0-*.jar",
        ),
      )

      Result.all(
        (actual.sorted ==== expected.sorted) :: (for {
          file <- actual
          actualContent = IoUtil.readFile(file)
        } yield actualContent ==== content)
      )

    }
  }

  def testFindFiles: Property = for {
    namesAndContentList <- Gens
                             .genFilenamesAndContentWithFirstUniqueName
                             .log("namesAndContentList")
  } yield {

    IoUtil.withTempDir { tmp =>
      val namesAndFiles = IoUtil.createFiles(tmp, namesAndContentList)

      val names    = namesAndFiles.map {
        case (ns, _) =>
          ns
      }
      val expected = namesAndFiles.map {
        case (_, fs) =>
          fs
      }
      val actual   = Io.findAllFiles(CaseSensitivity.caseSensitive, tmp, names)
      actual ==== expected
    }
  }

  def testCopy: Property = for {
    namesAndContentList <- Gens
                             .genFilenamesAndContentWithFirstUniqueName
                             .log("namesAndContentList")
    targetName          <- Gen.string(Gen.alphaNum, Range.linear(10, 10)).log("targetName")
  } yield {
    IoUtil.withTempDir { tmp =>
      val pathAndFiles = IoUtil.createFiles(tmp, namesAndContentList)
      val files        = pathAndFiles.map {
        case (_, file) =>
          file
      }
      val expected     = files.map(file => (file.getName, IoUtil.readFile(file))).toVector.sorted

      val targetDir   = new File(tmp, targetName)
      if (!targetDir.exists()) {
        targetDir.mkdirs()
      }
      val actualFiles = Io.copy(files, targetDir)
      val actual      = actualFiles.map(file => (file.getName, IoUtil.readFile(file)))
      (actual ==== expected).log(s"actual: $actual / expected: $expected").log(s"files: $files").log("")
    }
  }

}
