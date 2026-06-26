package kevinlee.http

import cats.Show
import cats.syntax.all.*
import devoops.data.DevOopsLogLevel
import io.circe.parser.decode
import kevinlee.ops.*
import org.http4s.Headers

/** @author Kevin Lee
  * @since 2021-01-03
  */
final case class HttpResponse(
  status: HttpResponse.Status,
  headers: Vector[HttpResponse.Header],
  body: Option[HttpResponse.Body],
)

object HttpResponse extends HttpResponseBase {

  implicit final class HttpResponseOps(val httpResponse: HttpResponse) extends AnyVal {
    def withHeader(header: Header): HttpResponse =
      httpResponse.copy(headers = httpResponse.headers :+ header)

    def findHeaderValueByName(f: String => Boolean): Option[String] =
      httpResponse
        .headers
        .find(_.header match {
          case (name, _) =>
            f(name)
        })
        .map(_.header._2)

    def toFailedResponseBodyJson: Option[FailedResponseBodyJson] =
      for {
        body                   <- httpResponse.body
        failedResponseBodyJson <- decode[FailedResponseBodyJson](body.body).toOption
      } yield failedResponseBodyJson

  }

  implicit def show(implicit sbtLogLevel: DevOopsLogLevel): Show[HttpResponse] = { httpResponse =>
    val headerString =
      (
        if (sbtLogLevel.isDebug)
          httpResponse
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

    val bodyString = httpResponse.body.fold("")(_.body)
    s"HttpRequest(method=${httpResponse.status.show}, headers=$headerString, body=$bodyString)"
  }

  def fromHttp4sHeaders(headers: Headers): Vector[Header] =
    headers.headers.map(header => Header(header.name.toString -> header.value)).toVector

}
