package io.kevinlee.sbt

import CommonPredef._

import scala.annotation.tailrec

/**
  * @author Kevin Lee
  * @since 2018-10-21
  */
sealed trait AlphaNumHyphen extends Ordered[AlphaNumHyphen] {
  override def compare(that: AlphaNumHyphen): Int =
    (this, that) match {
      case (Num(thisValue), Num(thatValue)) =>
        thisValue.compareTo(thatValue)
      case (Num(_), AlphaHyphen(_)) =>
        -1
      case (AlphaHyphen(_), Num(_)) =>
        1
      case (AlphaHyphen(thisValue), AlphaHyphen(thatValue)) =>
        thisValue.compareTo(thatValue)
    }
}

final case class AlphaHyphen(value: String) extends AlphaNumHyphen
final case class Num(value: Int) extends AlphaNumHyphen

object AlphaNumHyphen {

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def parse(value: String): Either[String, AlphaNumHyphen] =
    if (value.isEmpty) {
      Left("[Invalid] AlphaNumHyphen cannot be empty.")
    } else if (value.forall(_.isDigit)) {
      Right(Num(value.toInt))
    } else if (value.forall(x => x.isUpper || x.isLower || (x === '-'))) {
      Right(AlphaHyphen(value))
    } else {
      Left(
        s"[Invalid] AlphaNumHyphen can contain only alpha numeric and hyphen (-). value: $value"
      )
    }

}

final case class Identifier(values: Seq[AlphaNumHyphen]) extends AnyVal
object Identifier {
  def compare(a: Identifier, b: Identifier): Int = {
    @tailrec
    def compareElems(x: Seq[AlphaNumHyphen], y: Seq[AlphaNumHyphen]): Int =
      (x, y) match {
        case (head1 +: tail1, head2 +: tail2) =>
          val result = head1.compare(head2)
          if (result === 0) {
            compareElems(tail1, tail2)
          } else {
            result
          }
        case (Seq(), _ +: _) =>
          -1
        case (_ +: _, Seq()) =>
          1
      }
    compareElems(a.values, b.values)
  }
}

final case class Major(major: Int) extends AnyVal
final case class Minor(minor: Int) extends AnyVal
final case class Patch(patch: Int) extends AnyVal

trait SequenceBasedVersion[T] extends Ordered[T] {
  def major: Major
  def minor: Minor

  def render: String
}

final case class SemanticVersion(
  major: Major
, minor: Minor
, patch: Patch
, pre: Option[Identifier]
, buildMetadata: Option[Identifier]
) extends SequenceBasedVersion[SemanticVersion] {

  override def compare(that: SemanticVersion): Int = {
    val mj = this.major.major.compareTo(that.major.major)
    if (mj === 0) {
      val mn = this.minor.minor.compareTo(that.minor.minor)
      if (mn === 0) {
        val pt = this.patch.patch.compareTo(that.patch.patch)
        if (pt === 0) {
          (this.pre, that.pre) match {
            case (Some(thisPre), Some(thatPre)) =>
              Identifier.compare(thisPre, thatPre)
            case (Some(_), None) =>
              -1
            case (None, Some(_)) =>
              1
            case (None, None) =>
              0
          }
        } else {
          pt
        }
      } else {
        mn
      }
    } else {
      mj
    }
  }

  def render: String =
    s"${major.major}.${minor.minor}.${patch.patch}" + ((pre, buildMetadata) match {
        case (Some(p), Some(m)) =>
          p.values.mkString("-", ".", "") + m.values.mkString("+", ".", "")
        case (Some(p), None) =>
          p.values.mkString("-", ".", "")
        case (None, Some(m)) =>
          m.values.mkString("+", ".", "")
        case (None, None) =>
          ""
      }).toString
}

object SemanticVersion {
  val major0: Major = Major(0)
  val minor0: Minor = Minor(0)
  val patch0: Patch = Patch(0)

  def noIdentifier(major: Major, minor: Minor, patch: Patch): SemanticVersion =
    SemanticVersion(major, minor, patch, None, None)

  def withMajor(major: Major): SemanticVersion =
    SemanticVersion(major, minor0, patch0, None, None)

  def withMinor(minor: Minor): SemanticVersion =
    SemanticVersion(major0, minor, patch0, None, None)

  def withPatch(patch: Patch): SemanticVersion =
    SemanticVersion(major0, minor0, patch, None, None)
}
