package kevinlee.http

import cats.Show
import cats.syntax.all._
import io.circe.{Decoder, Encoder, Json}
import io.circe.parser.decode
import io.estatico.newtype.macros._
import org.http4s.Headers

/** @author Kevin Lee
  * @since 2021-01-03
  */
final case class HttpResponse(
  status: HttpResponse.Status,
  headers: List[HttpResponse.Header],
  body: Option[HttpResponse.Body],
)

@SuppressWarnings(
  Array(
    "org.wartremover.warts.ImplicitConversion",
    "org.wartremover.warts.ImplicitParameter",
    "org.wartremover.warts.ExplicitImplicitTypes",
    "org.wartremover.warts.PublicInference",
  )
)
object HttpResponse {
  final case class Status(code: Status.Code, reason: Status.Reason)
  object Status {
    @newsubtype case class Code(code: Int)
    object Code {
      def unapply(code: Code): Option[Int] = code.code.some
    }
    @newtype case class Reason(reason: String)

    implicit final val show: Show[Status] = {
      case Status(code, reason) =>
        s"Status(${code.code}, ${reason.reason})"
    }
  }

  @newtype case class Header(header: (String, String))

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
      httpResponse
        .body
        .flatMap(body =>
          decode[FailedResponseBodyJson](body.body) match {
            case Right(responseBodyJson) =>
              responseBodyJson.some
            case Left(err)               =>
              none[FailedResponseBodyJson]
          }
        )
  }

  implicit final val show: Show[HttpResponse] = { httpResponse =>
    val headerString = httpResponse
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
    val bodyString   = httpResponse.body.fold("")(_.body)
    s"HttpRequest(method=${httpResponse.status.show}, headers=$headerString, body=$bodyString)"
  }

  def fromHttp4sHeaders(headers: Headers): List[Header] =
    headers.toList.map(header => Header(header.name.toString -> header.value))

  @newtype case class Body(body: String)


  final case class FailedResponseBodyJson(message: String, documentationUrl: Option[String])
  object FailedResponseBodyJson {
    implicit val encoder: Encoder[FailedResponseBodyJson] =
      responseBodyJson =>
        Json.obj(
          (List("message" -> Json.fromString(responseBodyJson.message)) ++
            responseBodyJson
              .documentationUrl
              .toList
              .map(documentationUrl => "documentation_url" -> Json.fromString(documentationUrl))): _*
        )

    implicit val decoder: Decoder[FailedResponseBodyJson] =
      c =>
        for {
          message          <- c.downField("message").as[String]
          documentationUrl <- c.downField("documentation_url").as[Option[String]]
        } yield FailedResponseBodyJson(message, documentationUrl)

    implicit val show: Show[FailedResponseBodyJson] = encoder.apply(_).spaces2
  }
}
