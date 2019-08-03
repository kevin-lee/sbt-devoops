package kevinlee.fp.syntax

import scala.language.implicitConversions
import scala.util.{Either, Right}

/**
  * @author Kevin Lee
  * @since 2019-03-30
  */
@SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
object EitherOps extends EitherFunctions {

  final class ToEither[X](val x: X) extends AnyVal {
    def left[B]: Either[X, B] = Left(x)
    def right[A]: Either[A, X] = Right(x)
  }

  final class LeftOps[A, B](val l: Left[A, B]) extends AnyVal {
    def castR[C]: Either[A, C] = EitherOps.castR(l)
  }
  final class RightOps[A, B](val r: Right[A, B]) extends AnyVal {
    def castL[C]: Either[C, B] = EitherOps.castL(r)
  }

  final class RightBiasedEither[A, B] private[fp] (val e: Either[A, B]) extends AnyVal {
    /** Executes the given side-effecting function if this is a `Right`.
      *
      *  {{{
      *  Right(12).foreach(x => println(x)) // prints "12"
      *  Left(12).foreach(x => println(x))  // doesn't print
      *  }}}
      *  @param f The side-effecting function to execute.
      */
    def foreach(f: B => Unit): Unit = e match {
      case Right(b) => f(b)
      case Left(_) => ()
    }

    def getOrElse(or: => B): B = e match {
      case Right(b) => b
      case Left(_) => or
    }

    /** Returns `true` if `Left` or returns the result of the application of
      *  the given function to the `Right` value.
      *
      *  {{{
      *  Right(12).forall(_ > 10) // true
      *  Right(7).forall(_ > 10)  // false
      *  Left(12).forall(_ > 10)  // true
      *  }}}
      */
    def forall(f: B => Boolean): Boolean = e match {
      case Right(b) => f(b)
      case Left(_) => true
    }

    /** Returns `false` if `Left` or returns the result of the application of
      *  the given function to the `Right` value.
      *
      *  {{{
      *  Right(12).exists(_ > 10)  // true
      *  Right(7).exists(_ > 10)   // false
      *  Left(12).exists(_ > 10)   // false
      *  }}}
      */
    def exists(f: B => Boolean): Boolean = e match {
      case Right(b) => f(b)
      case Left(_) => false
    }

    /** Binds the given function across `Right`.
      *
      *  @param f The function to bind across `Right`.
      */
    def flatMap[C](f: B => Either[A, C]): Either[A, C] = e match {
      case Right(b) => f(b)
      case l @ Left(_) => castR(l)
    }

    /** The given function is applied if this is a `Right`.
      *
      *  {{{
      *  Right(12).map(x => "flower") // Result: Right("flower")
      *  Left(12).map(x => "flower")  // Result: Left(12)
      *  }}}
      */
    def map[C](f: B => C): Either[A, C] = e match {
      case Right(b) => Right(f(b))
      case l @ Left(_) => castR(l)
    }

    def leftFlatMap[C](f: A => Either[C, B]): Either[C, B] = e match {
      case Left(a) => f(a)
      case r @ Right(_) => castL(r)
    }

    def leftMap[C](f: A => C): Either[C, B] = e match {
      case Left(a) => Left(f(a))
      case r @ Right(_) => castL(r)
    }

    /** Returns `None` if this is a `Left` or if the
      *  given predicate `p` does not hold for the right value,
      *  otherwise, returns a `Right`.
      *
      * {{{
      * Right(12).filter(_ > 10) // Some(Right(12))
      * Right(7).filter(_ > 10)  // None
      * Left(12).filter(_ > 10)  // None
      * }}}
      */
    def filter(f: B => Boolean): Option[Either[A, B]] = e match {
      case Right(b) =>
        if (f(b)) Some(Right(b)) else None
      case Left(_) =>
        None
    }

    /** Returns a `List` containing the `Right` value if
      *  it exists or an empty `List` if this is a `Left`.
      *
      * {{{
      * Right(12).toList // List(12)
      * Left(12).toList // Nil
      * }}}
      */
    def toList: List[B] = e match {
      case Right(b) => List(b)
      case Left(_) => List.empty
    }

    /** Returns a `Some` containing the `Right` value
      *  if it exists or a `None` if this is a `Left`.
      *
      * {{{
      * Right(12).toOption // Some(12)
      * Left(12).toOption // None
      * }}}
      */
    def toOption: Option[B] = e match {
      case Right(b) => Some(b)
      case Left(_) => None
    }
  }
}

trait EitherFunctions {
  def left[A, B](a: A): Either[A, B] = Left(a)
  def right[A, B](b: B): Either[A, B] = Right(b)

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def castR[A, B, C](l: Left[A, B]): Either[A, C] = l.asInstanceOf[Either[A, C]]

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def castL[A, B, C](r: Right[A, B]): Either[C, B] = r.asInstanceOf[Either[C, B]]
}

@SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
trait EitherSyntax {
  import EitherOps._

  implicit def toEither[X](x: X): ToEither[X] = new ToEither[X](x)

  implicit def leftOps[A, B](l: Left[A, B]): LeftOps[A, B] = new LeftOps(l)

  implicit def rightOps[A, B](r: Right[A, B]): RightOps[A, B] = new RightOps(r)

  implicit def rightBiasedEither[A, B](e: Either[A, B]): RightBiasedEither[A, B] = new RightBiasedEither(e)
}
