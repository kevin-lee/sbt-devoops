package kevinlee.http

import cats.Show
import cats.syntax.all.*
import devoops.data.DevOopsLogLevel
import kevinlee.ops.instances.*

/** @author Kevin Lee
  * @since 2021-01-03
  */
sealed trait HttpError

object HttpError {
  final case class InvalidUri(
    uriString: String,
    errorMessage: String,
  ) extends HttpError
  final case class ResponseBodyDecodingFailure(
    message: String,
    cause: Option[Throwable],
  ) extends HttpError
  final case class FailedResponse(
    httpResponse: HttpResponse
  ) extends HttpError
  final case class UnhandledThrowable(
    throwable: Throwable
  ) extends HttpError
  final case class NotFound(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
  ) extends HttpError
  final case class BadRequest(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
  ) extends HttpError
  final case class InternalServerError(
    httpResponse: HttpResponse
  ) extends HttpError
  final case class RequestTimeout(
    httpResponse: HttpResponse
  ) extends HttpError
  final case class Forbidden(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
  ) extends HttpError
  final case class UnprocessableContent(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
  ) extends HttpError
  final case class MethodUnsupportedForMultipart(
    httpRequest: HttpRequest
  ) extends HttpError
  final case class MethodUnsupportedForFileUpload(
    httpRequest: HttpRequest
  ) extends HttpError
  final case class Unauthorized(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
  ) extends HttpError

  def invalidUri(uriString: String, errorMessage: String): HttpError =
    InvalidUri(uriString, errorMessage)

  def responseBodyDecodingFailure(message: String, cause: Option[Throwable]): HttpError =
    ResponseBodyDecodingFailure(message, cause)

  def failedResponse(httpResponse: HttpResponse): HttpError = FailedResponse(httpResponse)

  def unhandledThrowable(throwable: Throwable): HttpError = UnhandledThrowable(throwable)

  def notFound(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
  ): HttpError = NotFound(httpRequest, httpResponse)

  def badRequest(httpRequest: HttpRequest, httpResponse: HttpResponse): HttpError =
    BadRequest(httpRequest, httpResponse)

  def internalServerError(httpResponse: HttpResponse): HttpError =
    InternalServerError(httpResponse)

  def requestTimeout(httpResponse: HttpResponse): HttpError = RequestTimeout(httpResponse)

  def unauthorized(httpRequest: HttpRequest, httpResponse: HttpResponse): HttpError =
    Unauthorized(httpRequest, httpResponse)

  def forbidden(httpRequest: HttpRequest, httpResponse: HttpResponse): HttpError =
    Forbidden(httpRequest, httpResponse)

  def unprocessableContent(httpRequest: HttpRequest, httpResponse: HttpResponse): HttpError =
    UnprocessableContent(httpRequest, httpResponse)

  def methodUnsupportedForMultipart(httpRequest: HttpRequest): HttpError =
    MethodUnsupportedForMultipart(httpRequest)

  def methodUnsupportedForFileUpload(httpRequest: HttpRequest): HttpError =
    MethodUnsupportedForFileUpload(httpRequest)

  @SuppressWarnings(Array("org.wartremover.warts.ToString", "org.wartremover.warts.ImplicitParameter"))
  implicit def show(implicit sbtLogLevel: DevOopsLogLevel): Show[HttpError] = {
    case InvalidUri(uriString: String, errorMessage: String) =>
      s"InvalidUri(uriString: $uriString, errorMessage: $errorMessage)"

    case ResponseBodyDecodingFailure(message: String, cause: Option[Throwable]) =>
      s"ResponseBodyDecodingFailure(message: $message, cause: ${cause.show})"

    case FailedResponse(httpResponse: HttpResponse) =>
      s"FailedResponse(httpResponse: ${httpResponse.show})"

    case UnhandledThrowable(throwable: Throwable) =>
      s"UnhandledThrowable(throwable: ${throwable.show})"

    case NotFound(httpRequest: HttpRequest, httpResponse: HttpResponse) =>
      s"NotFound(httpRequest: ${httpRequest.show}, httpResponse: ${httpResponse.show})"

    case BadRequest(httpRequest: HttpRequest, httpResponse: HttpResponse) =>
      s"BadRequest(httpRequest: ${httpRequest.show}, httpResponse: ${httpResponse.show})"

    case InternalServerError(httpResponse: HttpResponse) =>
      s"InternalServerError(httpResponse: ${httpResponse.show})"

    case RequestTimeout(httpResponse: HttpResponse) =>
      s"RequestTimeout(httpResponse: ${httpResponse.show})"

    case Forbidden(httpRequest: HttpRequest, httpResponse: HttpResponse) =>
      s"Forbidden(httpRequest: ${httpRequest.show}, httpResponse: ${httpResponse.show})"

    case UnprocessableContent(httpRequest: HttpRequest, httpResponse: HttpResponse) =>
      s"UnprocessableContent(httpRequest: ${httpRequest.show}, httpResponse: ${httpResponse.show})"

    case MethodUnsupportedForMultipart(httpRequest: HttpRequest) =>
      s"MethodUnsupportedForMultipart(httpRequest: ${httpRequest.show})"

    case MethodUnsupportedForFileUpload(httpRequest: HttpRequest) =>
      s"MethodUnsupportedForFileUpload(httpRequest: ${httpRequest.show})"

    case Unauthorized(httpRequest: HttpRequest, httpResponse: HttpResponse) =>
      s"Unauthorized(httpRequest: ${httpRequest.show}, httpResponse: ${httpResponse.show})"
  }

  def recoverFromOptional404[A](httpError: HttpError): Either[HttpError, Option[A]] =
    httpError match {
      case HttpError.FailedResponse(
            HttpResponse(HttpResponse.Status(HttpResponse.Status.Code(404), _), _, _)
          ) =>
        none[A].asRight[HttpError]
      case error =>
        error.asLeft[Option[A]]
    }

  def toOptionIfNotFound[A](httpErrorOrA: Either[HttpError, Option[A]]): Either[HttpError, Option[A]] =
    httpErrorOrA match {
      case Right(a) =>
        a.asRight[HttpError]

      case Left(HttpError.NotFound(_, _)) =>
        none[A].asRight[HttpError]

      case Left(_) =>
        httpErrorOrA
    }

  def toEmptyListIfNotFound[A](httpErrorOrA: Either[HttpError, List[A]]): Either[HttpError, List[A]] =
    httpErrorOrA match {
      case Right(a) =>
        a.asRight[HttpError]

      case Left(HttpError.NotFound(_, _)) =>
        List.empty[A].asRight[HttpError]

      case Left(_) =>
        httpErrorOrA
    }

  implicit final class EitherHttpErrorOps[A](
    val httpError: Either[HttpError, Option[A]]
  ) extends AnyVal {
    def toOptionIfNotFound: Either[HttpError, Option[A]] =
      HttpError.toOptionIfNotFound(httpError)
  }

  implicit final class EitherHttpErrorListOps[A](
    val httpError: Either[HttpError, List[A]]
  ) extends AnyVal {
    def toEmptyListIfNotFound: Either[HttpError, List[A]] =
      HttpError.toEmptyListIfNotFound(httpError)
  }

}
