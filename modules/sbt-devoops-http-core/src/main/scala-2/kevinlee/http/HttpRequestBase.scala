package kevinlee.http

import cats.syntax.all.*
import io.estatico.newtype.macros.*
import org.http4s.{Header as Http4sHeader, Uri as Http4sUri}
import org.typelevel.ci.CIString

/** @author Kevin Lee
  * @since 2021-01-03
  */
trait HttpRequestBase {
  type Uri = HttpRequestBase.Uri
  val Uri = HttpRequestBase.Uri

  type Header = HttpRequestBase.Header
  val Header = HttpRequestBase.Header

  type Param = HttpRequestBase.Param
  val Param = HttpRequestBase.Param

  type Name = HttpRequestBase.Name
  val Name = HttpRequestBase.Name
}
object HttpRequestBase {
  @newtype final case class Uri(uri: String) {
    def toHttp4s: Either[HttpError, Http4sUri] =
      Http4sUri
        .fromString(uri)
        .leftMap(parseFailure => HttpError.invalidUri(uri, parseFailure.message))
  }

  @newtype final case class Header(header: (String, String)) {
    def toHttp4s: Http4sHeader.ToRaw = Http4sHeader.Raw(CIString(header._1), header._2)
  }

  @newtype final case class Param(param: (String, String))

  @newtype final case class Name(name: String)
}
