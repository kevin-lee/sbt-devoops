package kevinlee.github.data

import cats.data.NonEmptyList
import cats.syntax.all.*
import devoops.data.DevOopsLogLevel
import io.circe.parser.*
import kevinlee.git.Git.{RepoUrl, TagName}
import kevinlee.git.GitCommandError
import kevinlee.http.HttpResponse.FailedResponseBodyJson
import kevinlee.http.{HttpError, HttpRequest, HttpResponse}

import java.time.Instant

/** @author Kevin Lee
  * @since 2019-03-09
  */
sealed trait GitHubError

object GitHubError {
  final case object NoCredential extends GitHubError
  final case object InvalidCredential extends GitHubError
  final case class MalformedURL(
    url: String,
    errorMessage: Option[String],
  ) extends GitHubError
  final case class ConnectionFailure(error: String) extends GitHubError
  final case class GitHubServerError(error: String) extends GitHubError
  final case class ReleaseAlreadyExists(tagName: TagName) extends GitHubError
  final case class ReleaseCreationError(message: String) extends GitHubError
  final case class ReleaseNotFoundByTagName(
    tagName: TagName
  ) extends GitHubError
  final case class InvalidGitHubRepoUrl(repoUrl: RepoUrl) extends GitHubError
  final case class ChangelogNotFound(
    changelogLocation: String,
    tagName: TagName,
  ) extends GitHubError
  final case class CausedByGitCommandError(
    cause: GitCommandError
  ) extends GitHubError
  case object NoReleaseCreated extends GitHubError
  final case class AbuseRateLimits(
    message: String,
    documentationUrl: String,
  ) extends GitHubError
  final case class RateLimitExceeded(
    // "X-RateLimit-Limit"
    rateLimit: Option[Int],
    // X-RateLimit-Remaining
    remaining: Option[Int],
    // X-RateLimit-Reset
    reset: Option[Instant],
    message: String,
    docUrl: String,
  ) extends GitHubError
  final case class ForbiddenRequest(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
  ) extends GitHubError
  final case class UnprocessableEntity(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
    responseBodyJson: Option[FailedResponseBodyJson],
  ) extends GitHubError
  final case class AuthFailure(message: String) extends GitHubError
  final case class AssetUploadFailure(
    failed: NonEmptyList[GitHubRelease.Asset.FailedAssetUpload],
    succeeded: List[GitHubRelease.Asset],
  ) extends GitHubError
  final case class UnexpectedFailure(httpError: HttpError) extends GitHubError

  def noCredential: GitHubError = NoCredential

  def malformedURL(url: String, errorMessage: Option[String]): GitHubError = MalformedURL(url, errorMessage)

  def invalidCredential: GitHubError = InvalidCredential

  def connectionFailure(error: String): GitHubError =
    ConnectionFailure(error)

  def gitHubServerError(error: String): GitHubError = GitHubServerError(error)

  def releaseAlreadyExists(tagName: TagName): GitHubError =
    ReleaseAlreadyExists(tagName)

  def releaseCreationError(message: String): GitHubError =
    ReleaseCreationError(message)

  def releaseNotFoundByTagName(tagName: TagName): GitHubError =
    ReleaseNotFoundByTagName(tagName)

  def invalidGitHubRepoUrl(repoUrl: RepoUrl): GitHubError =
    InvalidGitHubRepoUrl(repoUrl)

  def changelogNotFound(changelogLocation: String, tagName: TagName): GitHubError =
    ChangelogNotFound(changelogLocation, tagName)

  def causedByGitCommandError(cause: GitCommandError): GitHubError =
    CausedByGitCommandError(cause)

  def noReleaseCreated: GitHubError = NoReleaseCreated

  def abuseRateLimits(message: String, documentationUrl: String): GitHubError =
    AbuseRateLimits(message, documentationUrl)

  def rateLimitExceeded(
    rateLimit: Option[Int],
    remaining: Option[Int],
    reset: Option[Instant],
    message: String,
    docUrl: String,
  ): GitHubError = RateLimitExceeded(rateLimit, remaining, reset, message, docUrl)

  def forbiddenRequest(httpRequest: HttpRequest, httpResponse: HttpResponse): GitHubError =
    ForbiddenRequest(httpRequest, httpResponse)

  def unprocessableEntity(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
    responseBodyJson: Option[FailedResponseBodyJson],
  ): GitHubError =
    UnprocessableEntity(httpRequest: HttpRequest, httpResponse: HttpResponse, responseBodyJson)

  def authFailure(message: String): GitHubError = AuthFailure(message)

  def assetUploadFailure(
    failed: NonEmptyList[GitHubRelease.Asset.FailedAssetUpload],
    succeeded: List[GitHubRelease.Asset],
  ): GitHubError = AssetUploadFailure(failed, succeeded)

  def unexpectedFailure(httpError: HttpError): GitHubError =
    UnexpectedFailure(httpError)

  def isReleaseTagNameAlreadyExists(gitHubError: GitHubError): Boolean = gitHubError match {
    case GitHubError.UnprocessableEntity(_, _, Some(responseBodyJson)) =>
      responseBodyJson.errors.exists { error =>
        val values = error.value
        values.get("resource").contains("Release") &&
        values.get("field").contains("tag_name") &&
        values.get("code").contains("already_exists")
      }

    case GitHubError.NoCredential | GitHubError.InvalidCredential | GitHubError.MalformedURL(_, _) |
        GitHubError.ConnectionFailure(_) | GitHubError.GitHubServerError(_) | GitHubError.ReleaseAlreadyExists(_) |
        GitHubError.ReleaseCreationError(_) | GitHubError.ReleaseNotFoundByTagName(_) |
        GitHubError.InvalidGitHubRepoUrl(_) | GitHubError.ChangelogNotFound(_, _) |
        GitHubError.CausedByGitCommandError(_) | GitHubError.NoReleaseCreated | GitHubError.AbuseRateLimits(_, _) |
        GitHubError.RateLimitExceeded(_, _, _, _, _) | GitHubError.ForbiddenRequest(_, _) |
        GitHubError.UnprocessableEntity(_, _, None) | GitHubError.AuthFailure(_) |
        GitHubError.AssetUploadFailure(_, _) | GitHubError.UnexpectedFailure(_) =>
      false
  }

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  def render(gitHubError: GitHubError)(implicit sbtLogLevel: DevOopsLogLevel): String = gitHubError match {
    case NoCredential =>
      "No GitHub access credential found - Check out the document for GitHub Auth Token"

    case InvalidCredential =>
      "Invalid GitHub access credential"

    case MalformedURL(url, errorMessage) =>
      s"The given GitHub URL is malformed. URL: $url - error: $errorMessage"

    case ConnectionFailure(error) =>
      s"GitHub API connection failed - error: $error"

    case GitHubServerError(error) =>
      s"GitHub server error - error: $error"

    case ReleaseAlreadyExists(tagName) =>
      s"Error] The release with the given tag name (${tagName.value}) already exists on GitHub."

    case ReleaseCreationError(message) =>
      s"Error] Failed to create GitHub release - reason: $message"

    case ReleaseNotFoundByTagName(tagName) =>
      s"Error] There is no GitHub release with the given tag, ${tagName.value}"

    case InvalidGitHubRepoUrl(repoUrl) =>
      s"Error] Invalid GitHub repository URL: ${repoUrl.repoUrl}"

    case ChangelogNotFound(changelogLocation, tagName) =>
      s"Changelog file does not exist at $changelogLocation for the tag, ${tagName.value}"

    case CausedByGitCommandError(cause) =>
      s"""Error] GitHub task failed due to git command error:
         |  ${GitCommandError.render(cause)}
         |""".stripMargin

    case NoReleaseCreated =>
      "No GitHub release has been created"

    case AbuseRateLimits(message, docUrl) =>
      s"""$message
         |For more details, visit $docUrl
         |""".stripMargin

    case RateLimitExceeded(rateLimit, remaining, reset, message, docUrl) =>
      s"""$message
         |The maximum number of requests per hour: $rateLimit
         |The number of requests remaining in the current rate limit window: $remaining
         |The rate limit window resets at ${reset.fold("\"No reset info provided by GitHub\"")(_.toString)}
         |For more details, visit $docUrl
         |""".stripMargin

    case ForbiddenRequest(httpRequest, httpResponse) =>
      s"""The request has been forbidden by GitHub API.
         |""".stripMargin ++ (
        if (sbtLogLevel.isDebug)
          s"""---
             | Request: ${httpRequest.show}
             |Response: ${httpResponse.show}
             |""".stripMargin
        else
          ""
      )

    case UnprocessableEntity(httpRequest, httpResponse, responseBodyJson) =>
      s"""Unprocessable Entity:
         |responseBody: ${responseBodyJson.fold("")(_.show)}
         |""".stripMargin ++ (
        if (sbtLogLevel.isDebug)
          s"""---
             | Request: ${httpRequest.show}
             |Response: ${httpResponse.show}
             |""".stripMargin
        else
          ""
      )

    case AuthFailure(message) =>
      s"""Authentication to access GitHub API has failed.
         |  - message: $message
         |""".stripMargin

    case AssetUploadFailure(failed, succeeded) =>
      @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
      val failedOnes = failed.toList.map {
        case GitHubRelease.Asset.FailedAssetUpload(file @ _, Some(err: AssetUploadFailure)) =>
          GitHubError.render(err)

        case GitHubRelease.Asset.FailedAssetUpload(file, reason) =>
          s"${file.toString}${reason.fold("")(s => s" Reason: ${GitHubError.render(s)}")}"
      }
      s"""Uploading assets to GitHub release has failed.
         |Failed: ${failedOnes.mkString("\n  - ", "\n  - ", "\n")}
         |Succeeded: ${succeeded.mkString("\n  - ", "\n  - ", "\n")}
         |""".stripMargin

    case UnexpectedFailure(httpError) =>
      s"""Unexpected failure:
         |${httpError.show}
         |""".stripMargin

  }

  def fromHttpError(httpError: HttpError): GitHubError = httpError match {
    case HttpError.Forbidden(httpRequest, httpResponse @ HttpResponse(_, headers @ _, Some(body))) =>
      decode[FailedResponseBodyJson](body.body) match {
        case Right(FailedResponseBodyJson(message, _, Some(docUrl))) =>
          if (
            message.contains("You have triggered an abuse detection mechanism") ||
            docUrl.contains("abuse-rate-limits")
          ) {
            GitHubError.abuseRateLimits(message, docUrl)
          } else if (
            message.contains("API rate limit exceeded") ||
            docUrl.contains("rate-limiting")
          ) {
            val rateLimit = httpResponse
              .findHeaderValueByName(_.equalsIgnoreCase("X-RateLimit-Limit"))
              .map(_.toInt)
            val remaining = httpResponse
              .findHeaderValueByName(_.equalsIgnoreCase("X-RateLimit-Remaining"))
              .map(_.toInt)
            val reset     = httpResponse
              .findHeaderValueByName(_.equalsIgnoreCase("X-RateLimit-Reset"))
              .map(x => Instant.ofEpochSecond(x.toLong))

            GitHubError.rateLimitExceeded(
              rateLimit,
              remaining,
              reset,
              message,
              docUrl,
            )
          } else {
            GitHubError.forbiddenRequest(httpRequest, httpResponse)
          }
        case Right(FailedResponseBodyJson(_, _, None)) =>
          GitHubError.forbiddenRequest(httpRequest, httpResponse)

        case Left(_) =>
          GitHubError.forbiddenRequest(httpRequest, httpResponse)
      }

    case HttpError.UnprocessableContent(request, response) =>
      val responseBodyJson = response.toFailedResponseBodyJson
      GitHubError.unprocessableEntity(request, response, responseBodyJson)

    case HttpError.Unauthorized(_, response) =>
      GitHubError.authFailure(
        response
          .toFailedResponseBodyJson
          .fold("")(_.message)
      )

    case error =>
      GitHubError.unexpectedFailure(error)
  }

  implicit class GitHubErrorOps(private val gitHubError: GitHubError) extends AnyVal {
    def render(implicit L: DevOopsLogLevel): String = GitHubError.render(gitHubError)
  }
}
