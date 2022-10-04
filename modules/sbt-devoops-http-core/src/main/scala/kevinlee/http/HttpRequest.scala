package kevinlee.http;

import cats.effect.{Async, Sync}
import cats.syntax.all.*
import cats.{Applicative, Show}
import devoops.data.DevOopsLogLevel
import fs2.Chunk
import io.circe.Encoder
import io.estatico.newtype.macros.*
import kevinlee.ops.*
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{MediaType, Request, Header => Http4sHeader, Headers => Http4sHeaders, Uri => Http4sUri}
import org.typelevel.ci.CIString
import extras.cats.syntax.all.*
import fs2.io.file.{Files, Path => Fs2Path}
import org.http4s.multipart.Multiparts

import java.net.URL
import java.util.Locale
import scala.concurrent.ExecutionContext

/** @author Kevin Lee
  * @since 2021-01-03
  */
final case class HttpRequest(
  httpMethod: HttpRequest.Method,
  uri: HttpRequest.Uri,
  headers: List[HttpRequest.Header],
  params: List[HttpRequest.Param],
  body: Option[HttpRequest.Body],
) {
  override def toString: String = HttpRequest.show(DevOopsLogLevel.info).show(this)
}

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
    case object Get extends Method
    case object Post extends Method
    case object Put extends Method
    case object Patch extends Method
    case object Delete extends Method

    def get: Method    = Get
    def post: Method   = Post
    def put: Method    = Put
    def patch: Method  = Patch
    def delete: Method = Delete

    def render(method: Method): String = method match {
      case Get =>
        "GET"
      case Post =>
        "POST"
      case Put =>
        "PUT"
      case Patch =>
        "PATCH"
      case Delete =>
        "DELETE"
    }

    implicit final val show: Show[Method] = render
  }

  lazy val sensitiveHeadersFromHttp4sInLowerCase: Set[String] =
    Http4sHeaders.SensitiveHeaders.map(_.toString.toLowerCase(Locale.ENGLISH))

  implicit def show(implicit sbtLogLevel: DevOopsLogLevel): Show[HttpRequest] = { httpRequest =>
    val headerString =
      (
        if (sbtLogLevel.isDebug)
          httpRequest
            .headers
            .map { header =>
              val (name, value) = header.header
              if (shouldProtect(name))
                s"($name: ***Protected***)"
              else
                s"($name: $value)"
            }
            .mkString("[", ", ", "]")
        else
          "***[Not Available in Non-Debug]***"
      )

    val paramsString = httpRequest
      .params
      .map { param =>
        val (name, value) = param.param
        if (shouldProtect(name))
          s"($name: ***Protected***)"
        else
          s"($name: $value)"
      }
      .mkString("[", ", ", "]")
    val bodyString   =
      httpRequest.body.fold("") {
        case HttpRequest.Body.Json(json) =>
          json.spaces2

        case HttpRequest.Body.File(file) =>
          s"File(file=${file.getCanonicalPath})"

//        case HttpRequest.Body.Multipart(multipartData) =>
//          multipartData match {
//            case HttpRequest.MultipartData.File(name, file, mediaType, _) =>
//              s"Multipart(name=${name.name}, file=${file.getCanonicalPath}, mediaType=${mediaType.show})"
//            case HttpRequest.MultipartData.Url(name, url, mediaType, _)   =>
//              s"Multipart(name=${name.name}, url=${url.toString}, mediaType=${mediaType.show})"
//          }

      }
    s"HttpRequest(method=${httpRequest.httpMethod.show}, url=${httpRequest.uri.uri}, headers=$headerString, params=$paramsString, body=$bodyString)"
  }

  import org.http4s.circe.CirceEntityCodec.*
  import org.http4s.dsl.request.*

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
  def toHttp4s[F[_]: Applicative: Async: Http4sClientDsl](
    httpRequest: HttpRequest
  ): F[Either[HttpError, Request[F]]] =
    httpRequest
      .uri
      .toHttp4s
      .t
      .flatMapF { uri =>
        val dsl           = Http4sClientDsl[F]
        import dsl.*
        val http4sHeaders = httpRequest.headers.map(_.toHttp4s)
        val uriWithParams =
          httpRequest.params match {
            case Nil =>
              uri
            case params =>
              params.foldLeft(uri) { (uri, param) =>
                uri.withQueryParam(param.param._1, param.param._2)
              }
          }
        httpRequest.httpMethod match {
          case HttpRequest.Method.Get =>
            httpRequest
              .body
              .fold(GET(uriWithParams, http4sHeaders: _*).asRight[HttpError].pure) {
                case HttpRequest.Body.Json(json) =>
                  GET
                    .apply(
                      json,
                      uriWithParams,
                      http4sHeaders: _*
                    )
                    .asRight[HttpError]
                    .pure

                case HttpRequest.Body.File(_) =>
                  HttpError.methodUnsupportedForFileUpload(httpRequest).asLeft[Request[F]].pure

                // TODO: uncomment it once this issue is solved properly. https://github.com/http4s/http4s/issues/4303
//              case HttpRequest.Body.Multipart(_) =>
//                HttpError.methodUnsupportedForMultipart(httpRequest).asLeft[F[Request[F]]]
              }
          case HttpRequest.Method.Post =>
            httpRequest
              .body
              .fold(POST(uriWithParams, http4sHeaders: _*).asRight[HttpError].pure) {
                case HttpRequest.Body.Json(json) =>
                  POST(
                    json,
                    uriWithParams,
                    http4sHeaders: _*
                  )
                    .asRight[HttpError]
                    .pure

                case HttpRequest.Body.File(file) =>
                  val byteChunk = fs2
                    .io
                    .file
                    .Files[F]
                    .readAll(fs2.io.file.Path.fromNioPath(file.toPath), 8192, fs2.io.file.Flags.Read)
                    .compile
                    .to(Chunk)
                  byteChunk
                    .map { chunk =>
                      POST(
                        chunk,
                        uriWithParams,
                        http4sHeaders: _*
                      )
                    }
                    .map(req =>
                      (req.withHeaders(
                        req
                          .headers
                          .headers
                          .filterNot(header =>
                            /* Without this filtering, the headers contain "Transfer-Encoding: chunked"
                             * which causes [400, Bad Content-Length] when uploading a release asset file using GitHub API
                             */
                            header.name === CIString("Transfer-Encoding")
                          )
                      ): Request[F]).asRight[HttpError]
                    )

                // TODO: uncomment it once this issue is solved properly. https://github.com/http4s/http4s/issues/4303
//              case HttpRequest.Body.Multipart(multipartData) =>
//                val body = multipartData.toHttp4s[F]
//                POST
//                  .apply(
//                    body,
//                    uriWithParams,
//                    http4sHeaders: _*
//                  )
//                  .map(req =>
//                    req.withHeaders(
//                      Headers(
//                        req.headers.toList ++ body
//                          .headers
//                          .toList
//                          .filterNot(header =>
//                            /* Without this filtering, the headers contain "Transfer-Encoding: chunked"
//                             * which causes [400, Bad Content-Length] when uploading a release asset file using GitHub API
//                             */
//                            header.name === CaseInsensitiveString("Transfer-Encoding")
//                          )
//                      )
//                    )
//                  )
//                  .asRight[HttpError]
              }
          case HttpRequest.Method.Put =>
            httpRequest
              .body
              .fold(PUT.apply(uriWithParams, http4sHeaders: _*).asRight[HttpError].pure) {
                case HttpRequest.Body.Json(json) =>
                  PUT(
                    json,
                    uriWithParams,
                    http4sHeaders: _*
                  )
                    .asRight[HttpError]
                    .pure

                case HttpRequest.Body.File(_) =>
                  HttpError.methodUnsupportedForFileUpload(httpRequest).asLeft[Request[F]].pure

                // TODO: uncomment it once this issue is solved properly. https://github.com/http4s/http4s/issues/4303
//              case HttpRequest.Body.Multipart(_) =>
//                HttpError.methodUnsupportedForMultipart(httpRequest).asLeft[F[Request[F]]]
              }
          case HttpRequest.Method.Patch =>
            httpRequest
              .body
              .fold(PATCH(uriWithParams, http4sHeaders: _*).asRight[HttpError].pure) {
                case HttpRequest.Body.Json(json) =>
                  PATCH(
                    json,
                    uriWithParams,
                    http4sHeaders: _*
                  )
                    .asRight[HttpError]
                    .pure

                case HttpRequest.Body.File(_) =>
                  HttpError.methodUnsupportedForFileUpload(httpRequest).asLeft[Request[F]].pure

                // TODO: uncomment it once this issue is solved properly. https://github.com/http4s/http4s/issues/4303
//              case HttpRequest.Body.Multipart(_) =>
//                HttpError.methodUnsupportedForMultipart(httpRequest).asLeft[F[Request[F]]]
              }
          case HttpRequest.Method.Delete =>
            httpRequest
              .body
              .fold(DELETE(uriWithParams, http4sHeaders: _*).asRight[HttpError].pure) {
                case HttpRequest.Body.Json(json) =>
                  DELETE(
                    json,
                    uriWithParams,
                    http4sHeaders: _*
                  )
                    .asRight[HttpError]
                    .pure

                case HttpRequest.Body.File(_) =>
                  HttpError.methodUnsupportedForFileUpload(httpRequest).asLeft[Request[F]].pure

                // TODO: uncomment it once this issue is solved properly. https://github.com/http4s/http4s/issues/4303
//              case HttpRequest.Body.Multipart(multipartData) =>
//                HttpError.methodUnsupportedForMultipart(httpRequest).asLeft[F[Request[F]]]
              }
        }
      }
      .value

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

  sealed trait Body

  object Body {
    final case class Json(json: io.circe.Json) extends Body
    // TODO: uncomment it once this issue is solved properly. https://github.com/http4s/http4s/issues/4303
//    final case class Multipart(multipartData: MultipartData) extends Body
    final case class File(file: java.io.File) extends Body

    def json(json: io.circe.Json): Body = Json(json)

    // TODO: uncomment it once this issue is solved properly. https://github.com/http4s/http4s/issues/4303
//    def multipart(multipartData: MultipartData): Body = Multipart(multipartData)

    def file(file: java.io.File): Body = File(file)
  }

  sealed trait MultipartData

  object MultipartData {
    final case class File(
      name: Name,
      file: java.io.File,
      mediaTypes: List[MediaType],
    ) extends MultipartData

    final case class Url(
      name: Name,
      url: URL,
      mediaTypes: List[MediaType],
    ) extends MultipartData

    def file(
      name: Name,
      file: java.io.File,
      mediaTypes: List[MediaType],
    ): MultipartData =
      File(name, file, mediaTypes)

    def url(
      name: Name,
      url: URL,
      mediaTypes: List[MediaType],
    ): MultipartData = Url(name, url, mediaTypes)

    @newtype final case class Name(name: String)

    import org.http4s.multipart.{Part, Multipart => Http4sMultipart}

    implicit final class MultipartDataOps(val multipartData: MultipartData) extends AnyVal {
      def toHttp4s[F[_]: Sync: Files]: F[Http4sMultipart[F]] =
        MultipartData.toHttp4s(multipartData)
    }

    def toHttp4s[F[_]: Sync: Files](multipartData: MultipartData): F[Http4sMultipart[F]] =
      Multiparts
        .forSync[F]
        .flatMap(
          _.multipart(
            multipartData match {
              case MultipartData.File(name, file, mediaTypes) =>
                Vector(
                  Part.fileData(
                    name.name,
                    Fs2Path.fromNioPath(file.toPath),
                    Http4sHeaders(mediaTypes.map(`Content-Type`(_))).headers
                  )
                )

              case MultipartData.Url(name, url, mediaTypes) =>
                Vector(
                  Part.fileData(
                    name.name,
                    url,
                    Http4sHeaders(mediaTypes.map(`Content-Type`(_))).headers
                  )
                )

            }
          )
        )
  }

  implicit final class HttpRequestOps(val httpRequest: HttpRequest) extends AnyVal {
    @SuppressWarnings(Array("org.wartremover.warts.ListAppend"))
    def withHeader(header: Header): HttpRequest =
      httpRequest.copy(headers = httpRequest.headers :+ header)

    def toHttp4s[F[_]: Applicative: Async: Http4sClientDsl]: F[Either[HttpError, Request[F]]] =
      HttpRequest.toHttp4s[F](httpRequest)

    // TODO: uncomment it once this issue is solved properly. https://github.com/http4s/http4s/issues/4303
//    def isBodyMultipart: Boolean =
//      httpRequest.body match {
//        case Some(HttpRequest.Body.Multipart(_)) =>
//          true
//        case _                                   =>
//          false
//      }
  }

  def withParams(httpMethod: Method, uri: Uri, params: List[Param]): HttpRequest =
    HttpRequest(httpMethod, uri, List.empty[Header], params, none[HttpRequest.Body])

  def withBody(httpMethod: Method, uri: Uri, body: HttpRequest.Body): HttpRequest =
    HttpRequest(httpMethod, uri, List.empty[Header], List.empty[Param], body.some)

  def withHeaders(httpMethod: Method, uri: Uri, headers: List[Header]): HttpRequest =
    HttpRequest(httpMethod, uri, headers, List.empty[Param], none[HttpRequest.Body])

  def withHeadersAndJsonBody[A: Encoder](
    httpMethod: Method,
    uri: Uri,
    headers: List[Header],
    body: A,
  ): HttpRequest =
    HttpRequest(
      httpMethod,
      uri,
      headers,
      List.empty[Param],
      HttpRequest.Body.json(Encoder[A].apply(body)).some,
    )

  def withHeadersParamsAndFileBody(
    httpMethod: Method,
    uri: Uri,
    headers: List[Header],
    params: List[Param],
    file: java.io.File,
  )(implicit ec: ExecutionContext): HttpRequest =
    HttpRequest(
      httpMethod,
      uri,
      headers,
      params,
      HttpRequest.Body.file(file).some,
    )

//  def withHeadersParamsAndMultipartBody(
//    httpMethod: Method,
//    uri: Uri,
//    headers: List[Header],
//    params: List[Param],
//    multipartData: MultipartData,
//  ): HttpRequest =
//    HttpRequest(
//      httpMethod,
//      uri,
//      headers,
//      params,
//      HttpRequest.Body.multipart(multipartData).some,
//    )

  def withoutBody(httpMethod: Method, uri: Uri): HttpRequest =
    HttpRequest(httpMethod, uri, List.empty[Header], List.empty[Param], none[HttpRequest.Body])

}
