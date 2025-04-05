package kevinlee.sbt.io

import kevinlee.sbt.SbtCommon.messageOnlyException
import org.apache.commons.io.filefilter.WildcardFileFilter
import sbt.io.Using
import sbt.{DirectoryFilter, File, IO, file}

import java.io.{FileFilter, InputStream, PrintWriter}
import scala.annotation.tailrec
import scala.io.{Codec, Source}

/** @author Kevin Lee
  * @since 2019-01-20
  */
object Io {

  def getUserHome: String =
    sys
      .props
      .getOrElse(
        "user.home",
        messageOnlyException("""User home is not found. sys.props.get("user.home") returns None.""")
      )

  def wildcardFilters(names: Seq[String], caseSensitivity: CaseSensitivity): FileFilter =
    WildcardFileFilter
      .builder()
      .setWildcards(names*)
      .setIoCase(CaseSensitivity.toIOCase(caseSensitivity))
      .get()

  def wildcardFilter(caseSensitivity: CaseSensitivity, name: String, names: String*): FileFilter =
    wildcardFilters(name +: names.toSeq, caseSensitivity)

  /** Get Seq of all the sub-directories of the given dir. It does not contain
    * the given dir itself.
    *
    * @example
    * {{{
    * // If the directory structure is like
    *
    * dir1 ───dir2A ──┬───dir2A1 ──┬───dir2A1A
    *            │    ├───dir2A2   └───dir2A1B
    *            │    └───dir2A3
    *            │
    *         dir2B ──┬───dir2B1 ──────dir2B1A
    *                 └───dir2B2
    *
    * // It returns
    * Seq(
    *   dir2A,
    *   dir2B,
    *   dir2A1, dir2A2, dir2A3,
    *   dir2B1, dir2B2,
    *   dir2A1A, dir2A1B,
    *   dir2B1A
    * )
    * // more precisely
    * Seq(
    *   dir1/dir2A,
    *   dir1/dir2B,
    *   dir1/dir2A/dir2A1,
    *   dir1/dir2A/dir2A2,
    *   dir1/dir2A/dir2A3,
    *   dir1/dir2B/dir2B1,
    *   dir1/dir2B/dir2B2,
    *   dir1/dir2A/dir2A1/dir2A1A,
    *   dir1/dir2A/dir2A1/dir2A1B,
    *   dir1/dir2B/dir2B1/dir2B1A
    * )
    * }}}
    * @param dir the given directory (exclusive)
    * @return All the sub-directories inside the given dir
    */
  def getAllSubDirs(dir: File): Seq[File] = {
    @tailrec
    def getAllSubDirs(dirs: Array[File], acc: Vector[File]): Vector[File] =
      dirs match {
        case Array() =>
          acc

        case Array(x) =>
          getAllSubDirs(x.listFiles(DirectoryFilter), acc :+ x)

        case array @ Array(x, _*) =>
          getAllSubDirs(array.drop(1) ++ x.listFiles(DirectoryFilter), acc :+ x)
      }
    getAllSubDirs(dir.listFiles(DirectoryFilter), Vector.empty)
    //    dir.listFiles(DirectoryFilter).flatMap(x => x +: getAllSubDirs(x))
  }

  def extractFilenames(filename: String): List[String] = {
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    @tailrec
    def allFilenames(file: File, files: List[String]): List[String] =
      (Option(file.getParentFile), file.getName) match {
        case (Some(parent), name) if parent.getPath == "/" =>
          name :: files

        case (Some(parent), name) =>
          allFilenames(parent, name :: files)

        case (None, name) =>
          name :: files
      }
    allFilenames(file(filename), List.empty)
  }

  def findAllFiles(
    caseSensitivity: CaseSensitivity,
    baseDir: File,
    filenames: List[String],
  ): List[File] = {
    @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
    def findFiles(
      cs: CaseSensitivity,
      file: File,
      filenames: List[String],
      acc: List[File],
    ): List[File] =
      filenames match {
        case Nil =>
          Nil

        case x :: Nil =>
          Option(file.listFiles(wildcardFilter(cs, x)))
            .fold[List[File]](Nil)(files => acc ++ files.toList)

        case x :: xs =>
          if (file.isFile) {
            Nil
          } else {
            Option(file.listFiles(wildcardFilter(cs, x)))
              .fold[List[File]](Nil) { fs =>
                fs.toList.flatMap(files => findFiles(cs, files, xs, acc))
              }
          }
      }
    filenames.flatMap { filename =>
      val names = extractFilenames(filename)
      findFiles(caseSensitivity, baseDir, names, Nil)
    }
  }

  def copy(sourceFiles: Seq[File], targetDir: File): Vector[File] =
    IO.copy(
      sourceFiles.map(source =>
        (
          source,
          new File(targetDir, source.getName),
        )
      )
    ).toVector
      .sorted

  def writeResourceToFile(input: String, out: File): File =
    Using.resource[InputStream, Source]((in: InputStream) => Source.fromInputStream(in)(Codec.UTF8))(
      getClass.getResourceAsStream(input)
    ) { in =>
      Using.file(new PrintWriter(_))(out) { o =>
        in.getLines.foreach(o.println)
        out
      }
    }
}
