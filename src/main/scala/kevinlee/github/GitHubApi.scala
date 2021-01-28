package kevinlee.github

import cats.Monad
import cats.syntax.all._
import kevinlee.git.Git
import kevinlee.github.data._
import kevinlee.http.{HttpClient, HttpRequest}

/** @author Kevin Lee
  * @since 2021-01-14
  */
trait GitHubApi[F[_]] {

  def findReleaseByTagName(
    tagName: Git.TagName,
    repo: GitHubRepoWithAuth,
  ): F[Either[GitHubError, Option[GitHubRelease.Response]]]

  def createRelease(
    params: GitHubRelease.CreateRequestParams,
    repo: GitHubRepoWithAuth,
  ): F[Either[GitHubError, Option[GitHubRelease.Response]]]

  def updateRelease(
    params: GitHubRelease.UpdateRequestParams,
    repo: GitHubRepoWithAuth,
  ): F[Either[GitHubError, Option[GitHubRelease.Response]]]

  def uploadAssetToRelease(
    params: GitHubRelease.UploadAssetParams,
    repo: GitHubRepoWithAuth,
  ): F[Either[GitHubError, Option[GitHubRelease.Asset]]]

}

object GitHubApi {

  def apply[F[_]: Monad](httpClient: HttpClient[F]): GitHubApi[F] = new GitHubApiF[F](httpClient)

  final class GitHubApiF[F[_]: Monad](val httpClient: HttpClient[F]) extends GitHubApi[F] {
    // TODO: make it configurable
    val baseUrl: String       = "https://api.github.com"
    val baseUploadUrl: String = "https://uploads.github.com"

    val DefaultAccept: String = "application/vnd.github.v3+json"

    def findReleaseByTagName(
      tagName: Git.TagName,
      repo: GitHubRepoWithAuth,
    ): F[Either[GitHubError, Option[GitHubRelease.Response]]] = {
      val url         = s"$baseUrl/repos/${repo.gitHubRepo.org.org}/${repo.gitHubRepo.repo.repo}/releases/tags/${tagName.value}"
      val httpRequest = HttpRequest.withHeaders(
        HttpRequest.Method.get,
        HttpRequest.Uri(url),
        HttpRequest.Header("accept" -> DefaultAccept) ::
          repo
            .accessToken
            .toHeaderList,
      )
      httpClient
        .request[Option[GitHubRelease.Response]](httpRequest)
        .map(
          _.toOptionIfNotFound
            .leftMap(GitHubError.fromHttpError)
            .flatMap(res => res.asRight[GitHubError])
        )
    }

    override def createRelease(
      params: GitHubRelease.CreateRequestParams,
      repo: GitHubRepoWithAuth,
    ): F[Either[GitHubError, Option[GitHubRelease.Response]]] = {
      val url         = s"$baseUrl/repos/${repo.gitHubRepo.org.org}/${repo.gitHubRepo.repo.repo}/releases"
      val httpRequest = HttpRequest
        .withHeadersAndJsonBody[GitHubRelease.CreateRequestParams](
          HttpRequest.Method.post,
          HttpRequest.Uri(url),
          HttpRequest.Header("accept" -> DefaultAccept) ::
            repo
              .accessToken
              .toHeaderList,
          params,
        )
      httpClient
        .request[Option[GitHubRelease.Response]](httpRequest)
        .map(
          _.toOptionIfNotFound
            .leftMap(GitHubError.fromHttpError)
            .flatMap(res => res.asRight[GitHubError])
        )
    }

    def updateRelease(
      params: GitHubRelease.UpdateRequestParams,
      repo: GitHubRepoWithAuth,
    ): F[Either[GitHubError, Option[GitHubRelease.Response]]] = {
      val url         =
        s"$baseUrl/repos/${repo.gitHubRepo.org.org}/${repo.gitHubRepo.repo.repo}/releases/${params.releaseId.releaseId}"
      val httpRequest = HttpRequest
        .withHeadersAndJsonBody[GitHubRelease.UpdateRequestParams](
          HttpRequest.Method.patch,
          HttpRequest.Uri(url),
          HttpRequest.Header("accept" -> DefaultAccept) ::
            repo
              .accessToken
              .toHeaderList,
          params,
        )
      httpClient
        .request[Option[GitHubRelease.Response]](httpRequest)
        .map(
          _.toOptionIfNotFound
            .leftMap(GitHubError.fromHttpError)
            .flatMap(res => res.asRight[GitHubError])
        )
    }

    override def uploadAssetToRelease(
      params: GitHubRelease.UploadAssetParams,
      repo: GitHubRepoWithAuth,
    ): F[Either[GitHubError, Option[GitHubRelease.Asset]]] = {
      val url         =
        s"$baseUploadUrl/repos/${repo.gitHubRepo.org.org}/${repo.gitHubRepo.repo.repo}/releases/${params.releaseId.releaseId}/assets"
      val httpRequest = HttpRequest
        .withHeadersParamsAndMultipartBody(
          HttpRequest.Method.post,
          HttpRequest.Uri(url),
          repo
            .accessToken
            .toHeaderList,
          List(
            HttpRequest.Param(
              "name" -> params.name.assetName
            )
          ) ++ params.label
            .map(assetLabel =>
              HttpRequest.Param(
                "label" -> assetLabel.assetLabel
              )
            )
            .toList,
          params.multipartData,
        )
      httpClient
        .request[Option[GitHubRelease.Asset]](httpRequest)
        .map(
          _.toOptionIfNotFound
            .leftMap(GitHubError.fromHttpError)
            .flatMap(res => res.asRight[GitHubError])
        )
    }
  }

}
