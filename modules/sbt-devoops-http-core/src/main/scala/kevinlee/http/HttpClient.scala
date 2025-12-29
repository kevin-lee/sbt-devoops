package kevinlee.http

import cats.Monad
import cats.data.EitherT
import cats.effect.*
import cats.syntax.all.*
import devoops.data.DevOopsLogLevel
import effectie.core.*
import extras.cats.syntax.all.*
import fs2.io.file.Files
import fs2.text
import io.circe.Decoder
import loggerf.core.*
import loggerf.instances.show.*
import loggerf.syntax.all.*
import org.http4s.*
import org.http4s.Status.Successful
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.*

/** @author Kevin Lee
  * @since 2021-01-03
  */
trait HttpClient[F[?]] {

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  def request[A](
    httpRequest: HttpRequest
  )(
    implicit entityDecoderA: Decoder[A],
    dsl: Http4sClientDsl[F],
    sbtLogLevel: DevOopsLogLevel
  ): F[Either[HttpError, A]]

}

object HttpClient {

  def apply[
    F[?]: Monad: Fx: Log: Async: Files
  ](client: Client[F]): HttpClient[F] =
    new HttpClientF[F](client)

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
  final class HttpClientF[
    F[?]: Monad: Fx: Log: Async: Files
  ](
    client: Client[F]
  ) extends HttpClient[F] {

    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    override def request[A](
      httpRequest: HttpRequest
    )(
      implicit entityDecoderA: Decoder[A],
      dsl: Http4sClientDsl[F],
      sbtLogLevel: DevOopsLogLevel
    ): F[Either[HttpError, A]] =
      sendRequest[A](httpRequest).value

    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    private[this] def sendRequest[A](
      httpRequest: HttpRequest
    )(
      implicit entityDecoderA: Decoder[A],
      dsl: Http4sClientDsl[F],
      sbtLogLevel: DevOopsLogLevel
    ): EitherT[F, HttpError, A] =
      for {
        request <- httpRequest.toHttp4s[F].eitherT

        postProcessedReq <- postProcessRequest(
                              request,
//                                if (httpRequest.isBodyMultipart)
//                                  Set.empty[MediaRange]
//                                else
                              EntityDecoder[F, A].consumes,
                            ).rightTF[F, HttpError]

        res <-
          EitherT(
            client
              .run(postProcessedReq)
              .use[Either[HttpError, A]](responseHandler(httpRequest))
          )
//              .leftFlatMap(err => EitherT(effectOf(HttpError.recoverFromOptional404[A](err))))
            .log(
              debugA,
              res => debug(String.valueOf(res)),
            )
      } yield res

    private[this] def postProcessRequest(request: Request[F], mediaRanges: Set[MediaRange]): Request[F] = {
      val mediaRangeList = mediaRanges.toList
      mediaRangeList.headOption.fold(request) { head =>
        request.putHeaders(
          Accept(MediaRangeAndQValue(head), mediaRangeList.drop(1).map(MediaRangeAndQValue(_))*)
        )
      }
    }

    private[this] def responseHandler[A](
      httpRequest: HttpRequest
    )(
      implicit decoderA: Decoder[A]
    ): Response[F] => F[Either[HttpError, A]] = {
      case Successful(successResponse) =>
        implicitly[EntityDecoder[F, A]]
          .decode(successResponse, strict = false)
          .leftMap(failure =>
            HttpError.responseBodyDecodingFailure(
              failure.message,
              failure.cause,
            )
          )
          .value

      case failedResponse =>
        val fOfBody: F[Option[String]] =
          (
            if (failedResponse.status.isEntityAllowed) {
              failedResponse
                .body
                .through(text.utf8.decode)
                .through(text.lines)
                .compile[F, F, String]
                .string
                .some
            } else {
              none[F[String]]
            }
          ).sequence
        fOfBody.map { maybeBody =>
          val httpResponse = HttpResponse(
            HttpResponse.Status(
              HttpResponse.Status.Code(failedResponse.status.code),
              HttpResponse.Status.Reason(failedResponse.status.reason),
            ),
            HttpResponse.fromHttp4sHeaders(failedResponse.headers),
            maybeBody.map(HttpResponse.Body.apply),
          )
          failedResponse.status.code match {
            case Status.NotFound.code =>
              HttpError.notFound(httpRequest, httpResponse).asLeft[A]

            case Status.BadRequest.code =>
              HttpError.badRequest(httpRequest, httpResponse).asLeft[A]

            case Status.RequestTimeout.code =>
              HttpError.requestTimeout(httpResponse).asLeft[A]

            case Status.InternalServerError.code =>
              HttpError.internalServerError(httpResponse).asLeft[A]

            case Status.UnprocessableContent.code =>
              HttpError.unprocessableContent(httpRequest, httpResponse).asLeft[A]

            case Status.Unauthorized.code =>
              HttpError.unauthorized(httpRequest, httpResponse).asLeft[A]

            case _ =>
              HttpError.failedResponse(httpResponse).asLeft[A]
          }

        }
    }

  }
}
