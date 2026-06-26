package kevinlee.http

import cats.Show
import cats.syntax.all.*
import io.circe.syntax.*
import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import io.estatico.newtype.macros.*

/** @author Kevin Lee
  * @since 2021-01-03
  */
trait HttpResponseBase {
  type Status = HttpResponseBase.Status
  val Status = HttpResponseBase.Status

  type Header = HttpResponseBase.Header
  val Header = HttpResponseBase.Header

  type Body = HttpResponseBase.Body
  val Body = HttpResponseBase.Body

  type FailedResponseBodyJson = HttpResponseBase.FailedResponseBodyJson
  val FailedResponseBodyJson = HttpResponseBase.FailedResponseBodyJson
}
object HttpResponseBase {
  final case class Status(code: Status.Code, reason: Status.Reason)
  object Status {
    @newsubtype final case class Code(code: Int)
    @newtype final case class Reason(reason: String)

    implicit final val show: Show[Status] = {
      case Status(code, reason) =>
        s"Status(${code.code}, ${reason.reason})"
    }
  }

  @newtype final case class Header(header: (String, String))

  @newtype final case class Body(body: String)

  final case class FailedResponseBodyJson(
    message: String,
    errors: List[FailedResponseBodyJson.Errors],
    documentationUrl: Option[String]
  )
  object FailedResponseBodyJson {
    implicit val encoder: Encoder[FailedResponseBodyJson] =
      responseBodyJson =>
        Json.obj(
          (
            List(
              "message" -> Json.fromString(responseBodyJson.message),
              "errors"  ->
                responseBodyJson
                  .errors
                  .map(errors => Json.obj(errors.value.mapValues(Json.fromString).toList: _*))
                  .asJson
            ) ++
              responseBodyJson
                .documentationUrl
                .toList
                .map(documentationUrl => "documentation_url" -> Json.fromString(documentationUrl))
          ): _*
        )

    implicit val decoder: Decoder[FailedResponseBodyJson] =
      c =>
        for {
          message <- c.downField("message").as[String]
          errors  <- c.downField("errors")
                       .as[List[Map[String, String]]]
                       .leftFlatMap(_ => List.empty[Map[String, String]].asRight[DecodingFailure])

          documentationUrl <- c.downField("documentation_url").as[Option[String]]
        } yield FailedResponseBodyJson(message, errors.map(Errors(_)), documentationUrl)

    implicit val showFailedResponseBodyJson: Show[FailedResponseBodyJson] = encoder.apply(_).spaces2

    @newtype final case class Errors(value: Map[String, String])
  }
}
