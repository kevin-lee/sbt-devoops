package kevinlee.http

import cats.Show
import cats.syntax.all.*
import io.circe.syntax.*
import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import refined4s.*

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
    type Code = Code.Type
    object Code extends Newtype[Int] {
      extension (a: Code) def code: Int = a.value
    }

    type Reason = Reason.Type
    object Reason extends Newtype[String] {
      extension (a: Reason) def reason: String = a.value
    }

    implicit final val show: Show[Status] = {
      case Status(code, reason) =>
        s"Status(${code.code}, ${reason.reason})"
    }
  }

  type Header = Header.Type
  object Header extends Newtype[(String, String)] {
    extension (a: Header) def header: (String, String) = a.value
  }

  type Body = Body.Type
  object Body extends Newtype[String] {
    extension (a: Body) def body: String = a.value
  }

  final case class FailedResponseBodyJson(
    message: String,
    errors: List[FailedResponseBodyJson.Errors],
    documentationUrl: Option[String]
  )
  object FailedResponseBodyJson {
    given encoder: Encoder[FailedResponseBodyJson] =
      responseBodyJson =>
        Json.obj(
          (
            List(
              "message" -> Json.fromString(responseBodyJson.message),
              "errors"  ->
                responseBodyJson
                  .errors
                  .map(errors => Json.obj(errors.value.view.mapValues(Json.fromString).toList*))
                  .asJson
            ) ++
              responseBodyJson
                .documentationUrl
                .toList
                .map(documentationUrl => "documentation_url" -> Json.fromString(documentationUrl))
          )*
        )

    given decoder: Decoder[FailedResponseBodyJson] =
      c =>
        for {
          message <- c.downField("message").as[String]
          errors  <- c.downField("errors")
                       .as[List[Map[String, String]]]
                       .leftFlatMap(_ => List.empty[Map[String, String]].asRight[DecodingFailure])

          documentationUrl <- c.downField("documentation_url").as[Option[String]]
        } yield FailedResponseBodyJson(message, errors.map(Errors(_)), documentationUrl)

    given showFailedResponseBodyJson: Show[FailedResponseBodyJson] = encoder.apply(_).spaces2

    type Errors = Errors.Type
    object Errors extends Newtype[Map[String, String]]
  }
}
