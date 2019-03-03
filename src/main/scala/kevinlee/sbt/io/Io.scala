package kevinlee.sbt.io

import java.io.FileFilter

import kevinlee.CommonPredef._
import org.apache.commons.io.filefilter.WildcardFileFilter
import sbt.{File, file}
import sbt.{DirectoryFilter, IO}

import scala.annotation.tailrec

/**
  * @author Kevin Lee
  * @since 2019-01-20
  */
object Io {

  def wildcardFilters(names: Seq[String], caseSensitivity: CaseSensitivity): FileFilter =
    new WildcardFileFilter(names.toArray, CaseSensitivity.toIOCase(caseSensitivity))

  def wildcardFilter(caseSensitivity: CaseSensitivity, name: String, names: String*): FileFilter =
    wildcardFilters(name +: names.toSeq, caseSensitivity)

  /**
    * Get Seq of all the sub-directories of the given dir. It does not contain
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
    @tailrec
    def allFilenames(file: File, files: List[String]): List[String] =
      (Option(file.getParentFile), file.getName) match {
        case (Some(parent), name) if parent.getPath === "/" =>
          name :: files
        case (Some(parent), name) =>
          allFilenames(parent, name :: files)
        case (None, name) =>
          name :: files
      }
    allFilenames(file(filename), List.empty)
  }

  def findAllFiles(
      caseSensitivity: CaseSensitivity
    , baseDir: File
    , filenames: List[String]
  ): List[File] = {
    @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
    def findFiles(
        cs: CaseSensitivity
      , file: File
      , filenames: List[String]
      , acc: List[File]
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
    IO.copy(sourceFiles.map(source => (source, new File(targetDir, source.getName)))).toVector.sorted
}
