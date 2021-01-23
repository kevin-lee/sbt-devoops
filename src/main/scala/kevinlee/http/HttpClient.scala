package kevinlee.http;

import cats.Monad
import cats.data.EitherT
import cats.effect._
import cats.syntax.all._
import effectie.cats.EffectConstructor
import effectie.cats.EitherTSupport._
import fs2.text
import io.circe.Decoder
import loggerf.cats._
import loggerf.syntax._
import org.http4s.Status.Successful
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.headers._

/** @author Kevin Lee
  * @since 2021-01-03
  */
trait HttpClient[F[_]] {

  def request[A](
    httpRequest: HttpRequest
  )(
    implicit entityDecoderA: Decoder[A]
  ): F[Either[HttpError, A]]

}

object HttpClient {

  def apply[F[_]: Monad: EffectConstructor: ConcurrentEffect: Log](client: Client[F]): HttpClient[F] =
    new HttpClientF[F](client)

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
  final class HttpClientF[F[_]: Monad: EffectConstructor: ConcurrentEffect: Log](
    client: Client[F]
  ) extends HttpClient[F] {

    override def request[A](
      httpRequest: HttpRequest
    )(
      implicit entityDecoderA: Decoder[A]
    ): F[Either[HttpError, A]] =
      sendRequest[A](httpRequest).value

    private[this] def sendRequest[A](
      httpRequest: HttpRequest,
    )(
      implicit entityDecoderA: Decoder[A]
    ): EitherT[F, HttpError, A] =
      for {
        request <- EitherT.fromEither(
                     HttpRequest.toHttp4s(
                       httpRequest,
                     )
                   )

        postProcessedReq <- eitherTRightF[HttpError](postProcessRequest(request, implicitly[EntityDecoder[F, A]].consumes))

        res <-
          log(
            EitherT(
              client
                .run(postProcessedReq)
                .use[F, Either[HttpError, A]](responseHandler(httpRequest))
            )
//              .leftFlatMap(err => EitherT(effectOf(HttpError.recoverFromOptional404[A](err))))
          )(
            err => error(err.show),
            res => info(String.valueOf(res)),
          )
      } yield res

    private[this] def postProcessRequest(request: F[Request[F]], mediaRanges: Set[MediaRange]): F[Request[F]] =
      request.map { req =>
        val mediaRangeList = mediaRanges.toList
        mediaRangeList.headOption.fold(req) { head =>
          req.putHeaders(
            Accept(MediaRangeAndQValue(head), mediaRangeList.drop(1).map(MediaRangeAndQValue(_)): _*)
          )
        }
      }

    private[this] def responseHandler[A](
      httpRequest: HttpRequest
    )(
      implicit decoderA: Decoder[A],
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
                .through(text.utf8Decode)
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

            case Status.UnprocessableEntity.code =>
              HttpError.unprocessableEntity(httpRequest, httpResponse).asLeft[A]

            case _ =>
              HttpError.failedResponse(httpResponse).asLeft[A]
          }

        }
    }

  }
}
