package kevinlee.http

import cats.syntax.all.*
import refined4s.*
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
  type Uri = Uri.Type
  object Uri extends Newtype[String] {
    extension (a: Uri) {
      def uri: String = a.value
      def toHttp4s: Either[HttpError, Http4sUri] =
        Http4sUri
          .fromString(a.value)
          .leftMap(parseFailure => HttpError.invalidUri(a.value, parseFailure.message))
    }
  }

  type Header = Header.Type
  object Header extends Newtype[(String, String)] {
    extension (a: Header) {
      def header: (String, String) = a.value
      def toHttp4s: Http4sHeader.ToRaw = Http4sHeader.Raw(CIString(a.value._1), a.value._2)
    }
  }

  type Param = Param.Type
  object Param extends Newtype[(String, String)] {
    extension (a: Param) def param: (String, String) = a.value
  }

  type Name = Name.Type
  object Name extends Newtype[String] {
    extension (a: Name) def name: String = a.value
  }
}
