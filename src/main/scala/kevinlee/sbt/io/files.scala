package kevinlee.sbt.io

import sbt.File
import org.apache.commons.io.IOCase

/**
  * @author Kevin Lee
  * @since 2019-01-27
  */
object Files {
  final case class FromTo(from: File, to: File)
}

sealed trait CaseSensitivity

object CaseSensitivity {
  final case object CaseSensitive extends CaseSensitivity
  final case object CaseInsensitive extends CaseSensitivity

  def caseSensitive: CaseSensitivity = CaseSensitive
  def caseInsensitive: CaseSensitivity = CaseInsensitive

  def toIOCase(caseSensitivity: CaseSensitivity): IOCase = caseSensitivity match {
    case CaseSensitive =>
      IOCase.SENSITIVE
    case CaseInsensitive =>
      IOCase.INSENSITIVE
  }
}
