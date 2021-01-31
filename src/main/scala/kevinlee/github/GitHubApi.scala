package kevinlee.github

import cats.Monad
import cats.data.{EitherT, NonEmptyList}
import cats.syntax.all._
import effectie.cats.EffectConstructor
import effectie.cats.Effectful._
import kevinlee.git.Git
import kevinlee.github.data._
import kevinlee.http.{HttpClient, HttpRequest}

import java.io.File
import scala.concurrent.ExecutionContext

/** @author Kevin Lee
  * @since 2021-01-14
  */
trait GitHubApi[F[_]] {

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  def release(
    repo: GitHubRepoWithAuth,
    tagName: Git.TagName,
    changelog: Changelog,
    assets: List[File],
  )(implicit ec: ExecutionContext): F[Either[GitHubError, GitHubRelease.Response]]

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

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  def uploadAssetToRelease(
    params: GitHubRelease.UploadAssetParams,
    repo: GitHubRepoWithAuth,
  )(implicit ec: ExecutionContext): F[Either[GitHubError, (File, Option[GitHubRelease.Asset])]]

}

object GitHubApi {

  def apply[F[_]: Monad: EffectConstructor](httpClient: HttpClient[F]): GitHubApi[F] =
    new GitHubApiF[F](httpClient)

  final class GitHubApiF[F[_]: Monad: EffectConstructor](
    val httpClient: HttpClient[F]
  ) extends GitHubApi[F] {
    // TODO: make it configurable
    val baseUrl: String       = "https://api.github.com"
    val baseUploadUrl: String = "https://uploads.github.com"

    val DefaultAccept: String = "application/vnd.github.v3+json"

    val contentTypeMap: javax.activation.MimetypesFileTypeMap = {
      val map = new javax.activation.MimetypesFileTypeMap()
      map.addMimeTypes("application/zip jar zip")
      map.addMimeTypes("application/xml pom xml")
      map
    }

    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    override def release(
      repo: GitHubRepoWithAuth,
      tagName: Git.TagName,
      changelog: Changelog,
      assets: List[File],
    )(implicit ec: ExecutionContext): F[Either[GitHubError, GitHubRelease.Response]] = (for {
      maybeExistingRelease <- EitherT(
                                findReleaseByTagName(
                                  tagName,
                                  repo,
                                )
                              )

      maybeRelease <- EitherT(
                        maybeExistingRelease match {
                          case Some(_) =>
                            pureOf(
                              GitHubError
                                .releaseAlreadyExists(tagName)
                                .asLeft[Option[GitHubRelease.Response]]
                            )

                          case None =>
                            this.createRelease(
                              GitHubRelease.CreateRequestParams(
                                tagName,
                                GitHubRelease.ReleaseName(tagName.value).some,
                                GitHubRelease.Description(changelog.changelog).some,
                                GitHubRelease.Draft.no,
                                GitHubRelease.Prerelease.no,
                              ),
                              repo,
                            )
                        }
                      )

      release <- EitherT(
                   maybeRelease match {
                     case Some(release) =>
                       assets
                         .traverse { file =>
                           val contentType = contentTypeMap.getContentType(file)
                           val filename    = file.getName
                           EitherT(
                             uploadAssetToRelease(
                               GitHubRelease.UploadAssetParams(
                                 GitHubRelease.ReleaseId(release.id.id),
                                 GitHubRelease.UploadAssetParams.AssetName(filename),
                                 none[GitHubRelease.UploadAssetParams.AssetLabel],
                                 GitHubRelease
                                   .UploadAssetParams
                                   .AssetFile(
                                     file,
                                     List(GitHubRelease.Asset.ContentType(contentType)),
                                   ),
                               ),
                               repo,
                             )
                           ).transform {
                             case Left(err) =>
                               GitHubRelease
                                 .Asset
                                 .FailedAssetUpload(file, err.some)
                                 .asLeft[GitHubRelease.Asset]

                             case Right((file, Some(asset))) =>
                               asset.asRight[GitHubRelease.Asset.FailedAssetUpload]

                             case Right((file, None)) =>
                               GitHubRelease
                                 .Asset
                                 .FailedAssetUpload(file, none[GitHubError])
                                 .asLeft[GitHubRelease.Asset]

                           }.value
                         }
                         .map { results =>
                           results.partitionBifold(identity) match {
                             case ((Nil, assets)) =>
                               release
                                 .copy(assets = release.assets ++ assets)
                                 .asRight[GitHubError]

                             case ((failed :: rest, succeeded)) =>
                               GitHubError
                                 .assetUploadFailure(
                                   NonEmptyList(failed, rest),
                                   succeeded,
                                 )
                                 .asLeft[GitHubRelease.Response]
                           }
                         }

                     case None =>
                       pureOf(GitHubError.noReleaseCreated.asLeft[GitHubRelease.Response])
                   }
                 )

    } yield release).value

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

    @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
    override def uploadAssetToRelease(
      params: GitHubRelease.UploadAssetParams,
      repo: GitHubRepoWithAuth,
    )(implicit ec: ExecutionContext): F[Either[GitHubError, (File, Option[GitHubRelease.Asset])]] = {
      val url         =
        s"$baseUploadUrl/repos/${repo.gitHubRepo.org.org}/${repo.gitHubRepo.repo.repo}/releases/${params.releaseId.releaseId}/assets"
      val httpRequest = HttpRequest
        .withHeadersParamsAndFileBody(
          HttpRequest.Method.post,
          HttpRequest.Uri(url),
          repo
            .accessToken
            .toHeaderList,
          List(
            HttpRequest.Param(
              "name" -> params.name.assetName
            )
          ) ++ params
            .label
            .map(assetLabel =>
              HttpRequest.Param(
                "label" -> assetLabel.assetLabel
              )
            )
            .toList,
          params.assetFile.assetFile,
        )
      httpClient
        .request[Option[GitHubRelease.Asset]](httpRequest)
        .map(
          _.toOptionIfNotFound
            .leftMap(GitHubError.fromHttpError)
            .flatMap(res => (params.assetFile.assetFile, res).asRight[GitHubError])
        )
    }
  }

}
