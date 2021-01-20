package kevinlee.http;

import cats.{Applicative, Show}
import cats.syntax.all._
import io.circe.Json
import io.estatico.newtype.macros._
import HttpRequest.Method.{Delete, Get, Patch, Post, Put}
import org.http4s.{Request, Header => Http4sHeader, Uri => Http4sUri}

/** @author Kevin Lee
  * @since 2021-01-03
  */
final case class HttpRequest(
  httpMethod: HttpRequest.Method,
  uri: HttpRequest.Uri,
  headers: List[HttpRequest.Header],
  params: List[HttpRequest.Param],
  body: Option[Json],
)

@SuppressWarnings(
  Array(
    "org.wartremover.warts.ImplicitConversion",
    "org.wartremover.warts.ImplicitParameter",
    "org.wartremover.warts.ExplicitImplicitTypes",
    "org.wartremover.warts.PublicInference",
  )
)
object HttpRequest {
  sealed trait Method

  object Method {
    case object Get    extends Method
    case object Post   extends Method
    case object Put    extends Method
    case object Patch  extends Method
    case object Delete extends Method

    def get: Method    = Get
    def post: Method   = Post
    def put: Method    = Put
    def patch: Method  = Patch
    def delete: Method = Delete

    def render(method: Method): String = method match {
      case Get    =>
        "GET"
      case Post   =>
        "POST"
      case Put    =>
        "PUT"
      case Patch  =>
        "PATCH"
      case Delete =>
        "DELETE"
    }

    implicit final val show: Show[Method] = render
  }

  implicit final val show: Show[HttpRequest] = { httpRequest =>
    val headerString = httpRequest
      .headers
      .map { header =>
        val (name, value) = header.header
        val nameInLower   = name.toLowerCase
        if (nameInLower.contains("auth") || nameInLower.contains("password"))
          s"($name: ***Protected***)"
        else
          s"($name: $value)"
      }
      .mkString("[", ", ", "]")
    val paramsString = httpRequest
      .params
      .map { param =>
        val (name, value) = param.param
        val nameInLower   = name.toLowerCase
        if (nameInLower.contains("auth") || nameInLower.contains("password"))
          s"($name: ***Protected***)"
        else
          s"($name: $value)"
      }
      .mkString("[", ", ", "]")
    val bodyString   = httpRequest.body.fold("")(_.spaces2)
    s"HttpRequest(method=${httpRequest.httpMethod.show}, url=${httpRequest.uri.uri}, headers=$headerString, params=$paramsString, body=$bodyString)"
  }

  import org.http4s.circe.CirceEntityCodec._
  import org.http4s.client.dsl.Http4sClientDsl._
  import org.http4s.dsl.request._

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
  def toHttp4s[F[_]: Applicative](
    httpRequest: HttpRequest
  ): Either[HttpError, F[Request[F]]] =
    httpRequest.uri.toHttp4s.map { uri =>
      val http4sHeaders = httpRequest.headers.map(_.toHttp4s)
      httpRequest.httpMethod match {
        case Get    =>
          httpRequest
            .body
            .fold(GET.apply(uri, http4sHeaders: _*))(
              GET.apply(
                _,
                uri,
                http4sHeaders: _*
              )
            )
        case Post   =>
          httpRequest
            .body
            .fold(POST.apply(uri, http4sHeaders: _*))(
              POST.apply(
                _,
                uri,
                http4sHeaders: _*
              )
            )
        case Put    =>
          httpRequest
            .body
            .fold(PUT.apply(uri, http4sHeaders: _*))(
              PUT.apply(
                _,
                uri,
                http4sHeaders: _*
              )
            )
        case Patch  =>
          httpRequest
            .body
            .fold(PATCH.apply(uri, http4sHeaders: _*))(
              PATCH.apply(
                _,
                uri,
                http4sHeaders: _*
              )
            )
        case Delete =>
          httpRequest
            .body
            .fold(DELETE.apply(uri, http4sHeaders: _*))(
              DELETE.apply(
                _,
                uri,
                http4sHeaders: _*
              )
            )
      }
    }

  @newtype case class Uri(uri: String) {
    def toHttp4s: Either[HttpError, Http4sUri] =
      Http4sUri
        .fromString(uri)
        .leftMap(parseFailure => HttpError.invalidUri(uri, parseFailure.message))
  }

  @newtype case class Header(header: (String, String)) {
    def toHttp4s: Http4sHeader = Http4sHeader(header._1, header._2)
  }

  @newtype case class Param(param: (String, String))

  implicit final class HttpRequestOps(val httpRequest: HttpRequest) extends AnyVal {
    def withHeader(header: Header): HttpRequest =
      httpRequest.copy(headers = httpRequest.headers :+ header)
  }

  def withParams(httpMethod: Method, uri: Uri, params: List[Param]): HttpRequest =
    HttpRequest(httpMethod, uri, List.empty[Header], params, none[Json])

  def withBody(httpMethod: Method, uri: Uri, body: Json): HttpRequest =
    HttpRequest(httpMethod, uri, List.empty[Header], List.empty[Param], body.some)

  def withHeadersAndBody(httpMethod: Method, uri: Uri, headers: List[Header], body: Json): HttpRequest =
    HttpRequest(httpMethod, uri, headers, List.empty[Param], body.some)

  def withoutBody(httpMethod: Method, uri: Uri): HttpRequest =
    HttpRequest(httpMethod, uri, List.empty[Header], List.empty[Param], none[Json])

}
