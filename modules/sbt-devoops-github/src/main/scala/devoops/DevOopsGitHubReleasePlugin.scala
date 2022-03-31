package devoops

import cats._
import cats.effect.{ContextShift, IO, Timer}
import cats.instances.all._
import cats.syntax.all._
import devoops.data.SbtTaskResult.SbtTaskHistory
import devoops.data._
import effectie.syntax.all._
import effectie.core._
import just.semver.SemVer
import kevinlee.git.Git
import kevinlee.git.Git.{BranchName, Repository, TagName}
import kevinlee.github.data._
import kevinlee.github.{GitHubApi, GitHubTask}
import kevinlee.http.HttpClient
import kevinlee.sbt.SbtCommon.messageOnlyException
import kevinlee.sbt.io.{CaseSensitivity, Io}
import loggerf.logger.{CanLog, SbtLogger}
import loggerf.cats.instances._
import org.http4s.blaze.client.BlazeClientBuilder
import sbt.Keys._
import sbt.{AutoPlugin, File, PluginTrigger, Plugins, Setting}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/** @author Kevin Lee
  * @since 2019-01-01
  */
object DevOopsGitHubReleasePlugin extends AutoPlugin {

  // $COVERAGE-OFF$
  override def requires: Plugins      = empty
  override def trigger: PluginTrigger = noTrigger

  object autoImport extends GitHubReleaseKeys with GitHubReleaseOps

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    devOopsLogLevel            := DevOopsLogLevel.info.render,
    devOopsGitTagFrom          := "main",
    devOopsGitTagDescription   := None,
    devOopsGitTagName          := decideVersion(version.value, v => s"v${SemVer.render(SemVer.parseUnsafe(v))}"),
    devOopsGitTagPushRepo      := "origin",
    devOopsGitTag              := {
      lazy val basePath       = baseDirectory.value
      lazy val tagFrom        = BranchName(devOopsGitTagFrom.value)
      lazy val tagName        = TagName(devOopsGitTagName.value)
      lazy val tagDesc        = devOopsGitTagDescription.value
      lazy val pushRepo       = Repository(devOopsGitTagPushRepo.value)
      lazy val projectVersion = version.value

      implicit val devOopsLogLevelValue: DevOopsLogLevel = DevOopsLogLevel.fromStringUnsafe(devOopsLogLevel.value)

      import effectie.cats.fx._

      val run1: IO[(SbtTaskHistory, Either[SbtTaskError, Unit])] =
        getTagVersion[IO](basePath, tagFrom, tagName, tagDesc, pushRepo, projectVersion)
          .value
          .run

      SbtTask[IO]
        .handleSbtTask(run1)
        .unsafeRunSync()

    },
    devOopsCiDir               := "ci",
    devOopsArtifactNamePrefix  := name.value,
    devOopsPackagedArtifacts   := {
      val filenamePrefix = devOopsArtifactNamePrefix.value
      List(
        s"target/scala-*/${filenamePrefix}*.jar",
        s"*/target/scala-*/${filenamePrefix}*.jar",
        s"*/*/target/scala-*/${filenamePrefix}*.jar",
      )
    },
    devOopsCopyReleasePackages := {

      implicit val devOopsLogLevelValue: DevOopsLogLevel = DevOopsLogLevel.fromStringUnsafe(devOopsLogLevel.value)

      val result: Vector[File] =
        copyFiles(
          "devOopsCopyReleasePackages",
          CaseSensitivity.caseSensitive,
          baseDirectory.value,
          devOopsPackagedArtifacts.value,
          new File(new File(devOopsCiDir.value), "dist"),
        ) match {
          case Left(error) =>
            messageOnlyException(SbtTaskError.render(error))
          case Right(files) =>
            files
        }
      result
    },
    devOopsChangelogLocation            := "changelogs",
    devOopsGitHubAuthTokenEnvVar        := "GITHUB_TOKEN",
    devOopsGitHubAuthTokenFile          :=
      Some(new File(Io.getUserHome, ".github")),
    devOopsGitHubRequestTimeout         := 2.minutes,
    devOopsGitHubRelease                := {
      lazy val tagName                  = TagName(devOopsGitTagName.value)
      lazy val authTokenEnvVar          = devOopsGitHubAuthTokenEnvVar.value
      lazy val authTokenFile            = devOopsGitHubAuthTokenFile.value
      lazy val baseDir                  = baseDirectory.value
      lazy val requestTimeout           = devOopsGitHubRequestTimeout.value
      implicit val ec: ExecutionContext = ExecutionContext.global
      implicit val cs: ContextShift[IO] = IO.contextShift(ec)
      implicit val timer: Timer[IO]     = IO.timer(ec)

      implicit val devOopsLogLevelValue: DevOopsLogLevel = DevOopsLogLevel.fromStringUnsafe(devOopsLogLevel.value)

      import effectie.cats.fx._

      implicit val log: CanLog = SbtLogger.sbtLoggerCanLog(streams.value.log)
      val git                  = Git[IO]
      val sbtTask              = SbtTask[IO]

      val result: IO[(SbtTaskHistory, Either[SbtTaskError, Unit])] = BlazeClientBuilder[IO](ec)
        .withIdleTimeout(requestTimeout)
        .withRequestTimeout(requestTimeout)
        .withConnectTimeout(requestTimeout)
        .resource
        .use { client =>
          val r: SbtTask.Result[IO, Unit] =
            for {
              _     <- sbtTask.fromGitTask(git.fetchTags(baseDir))
              tags  <- sbtTask.fromGitTask(git.getTag(baseDir))
              _     <- sbtTask.toLeftWhen(
                         !tags.contains(tagName.value),
                         SbtTaskError.gitTaskError(
                           s"tag ${tagName.value} does not exist. tags: ${tags.mkString("[", ",", "]")}"
                         ),
                       )
              oauth <-
                sbtTask.eitherTWithWriter(
                  effectOf[IO](
                    getGitHubAuthToken(authTokenEnvVar, authTokenFile)
                      .leftMap(SbtTaskError.gitHubTaskError)
                  )
                )(_ => List(SbtTaskResult.gitHubTaskResult("Get GitHub OAuth token")))
              _     <- sbtTask.handleGitHubTask(
                         runGitHubRelease(
                           tagName,
                           baseDir,
                           GitHub.ChangelogLocation(devOopsChangelogLocation.value),
                           Repository(devOopsGitTagPushRepo.value),
                           oauth,
                           GitHubApi[IO](HttpClient[IO](client)),
                         )
                       )
            } yield ()
          r.value.run
        }
      sbtTask
        .handleSbtTask(result)
        .unsafeRunSync()
    },
    devOopsGitTagAndGitHubRelease       := {
      lazy val tagName         = TagName(devOopsGitTagName.value)
      lazy val tagDesc         = devOopsGitTagDescription.value
      lazy val tagFrom         = BranchName(devOopsGitTagFrom.value)
      lazy val authTokenEnvVar = devOopsGitHubAuthTokenEnvVar.value
      lazy val authTokenFile   = devOopsGitHubAuthTokenFile.value
      lazy val baseDir         = baseDirectory.value
      lazy val pushRepo        = Repository(devOopsGitTagPushRepo.value)
      lazy val projectVersion  = version.value
      lazy val requestTimeout  = devOopsGitHubRequestTimeout.value

      implicit val ec: ExecutionContext = ExecutionContext.global
      implicit val cs: ContextShift[IO] = IO.contextShift(ec)
      implicit val timer: Timer[IO]     = IO.timer(ec)

      implicit val devOopsLogLevelValue: DevOopsLogLevel = DevOopsLogLevel.fromStringUnsafe(devOopsLogLevel.value)

      implicit val log: CanLog = SbtLogger.sbtLoggerCanLog(streams.value.log)

      import effectie.cats.fx._

      BlazeClientBuilder[IO](ec)
        .withIdleTimeout(requestTimeout)
        .withRequestTimeout(requestTimeout)
        .withConnectTimeout(requestTimeout)
        .resource
        .use { client =>
          SbtTask[IO].handleSbtTask(
            (for {
              oauth <-
                SbtTask[IO].eitherTWithWriter(
                  effectOf[IO](
                    getGitHubAuthToken(authTokenEnvVar, authTokenFile)
                      .leftMap(SbtTaskError.gitHubTaskError)
                  )
                )(_ => List(SbtTaskResult.gitHubTaskResult("Get GitHub OAuth token")))
              _     <- getTagVersion[IO](baseDir, tagFrom, tagName, tagDesc, pushRepo, projectVersion)
              _     <- SbtTask[IO].handleGitHubTask(
                         runGitHubRelease(
                           tagName,
                           baseDir,
                           GitHub.ChangelogLocation(devOopsChangelogLocation.value),
                           pushRepo,
                           oauth,
                           GitHubApi[IO](HttpClient[IO](client)),
                         )
                       )
            } yield ()).value.run
          )
        }
        .unsafeRunSync()
    },
    devOopsGitHubReleaseUploadArtifacts := {
      lazy val tagName         = TagName(devOopsGitTagName.value)
      lazy val assets          = devOopsCopyReleasePackages.value
      lazy val authTokenEnvVar = devOopsGitHubAuthTokenEnvVar.value
      lazy val authTokenFile   = devOopsGitHubAuthTokenFile.value
      lazy val baseDir         = baseDirectory.value
      lazy val artifacts       = devOopsPackagedArtifacts.value
      lazy val requestTimeout  = devOopsGitHubRequestTimeout.value

      implicit val ec: ExecutionContext = ExecutionContext.global
      implicit val cs: ContextShift[IO] = IO.contextShift(ec)
      implicit val timer: Timer[IO]     = IO.timer(ec)

      implicit val devOopsLogLevelValue: DevOopsLogLevel = DevOopsLogLevel.fromStringUnsafe(devOopsLogLevel.value)

      import effectie.cats.fx._

      implicit val log: CanLog = SbtLogger.sbtLoggerCanLog(streams.value.log)
      val git                  = Git[IO]
      val sbtTask              = SbtTask[IO]

      val result: IO[(SbtTaskHistory, Either[SbtTaskError, Unit])] = BlazeClientBuilder[IO](ec)
        .withIdleTimeout(requestTimeout)
        .withRequestTimeout(requestTimeout)
        .withConnectTimeout(requestTimeout)
        .resource
        .use { client =>
          val r: SbtTask.Result[IO, Unit] =
            for {
              _     <- sbtTask.fromGitTask(git.fetchTags(baseDir))
              tags  <- sbtTask.fromGitTask(git.getTag(baseDir))
              _     <- sbtTask.toLeftWhen(
                         !tags.contains(tagName.value),
                         SbtTaskError.gitTaskError(
                           s"tag ${tagName.value} does not exist. tags: ${tags.mkString("[", ",", "]")}"
                         ),
                       )
              _     <- sbtTask.toLeftWhen(
                         assets.isEmpty,
                         SbtTaskError.noFileFound(
                           "devOopsCopyReleasePackages",
                           artifacts,
                         ),
                       )
              oauth <-
                sbtTask.eitherTWithWriter(
                  effectOf[IO](
                    getGitHubAuthToken(authTokenEnvVar, authTokenFile)
                      .leftMap(SbtTaskError.gitHubTaskError)
                  )
                )(_ => List(SbtTaskResult.gitHubTaskResult("Get GitHub OAuth token")))
              _     <- sbtTask.handleGitHubTask(
                         runUploadAssetsToGitHubRelease(
                           tagName,
                           assets,
                           baseDir,
                           Repository(devOopsGitTagPushRepo.value),
                           oauth,
                           GitHubApi[IO](HttpClient[IO](client)),
                         )
                       )
            } yield ()
          r.value.run
        }
      sbtTask
        .handleSbtTask(result)
        .unsafeRunSync()
    },
  )

  private def getTagVersion[F[_]: Fx: Monad](
    basePath: File,
    tagFrom: BranchName,
    tagName: TagName,
    gitTagDescription: Option[String],
    pushRepo: Repository,
    projectVersion: String,
  ): SbtTask.Result[F, Unit] =
    for {
      projectVersion <-
        SbtTask[F].fromNonSbtTask(
          effectOf(
            SemVer
              .parse(projectVersion)
              .leftMap(SbtTaskError.semVerFromProjectVersionParseError(projectVersion, _))
          )
        )(semVer =>
          List(
            SbtTaskResult.nonSbtTaskResult(
              s"The semantic version from the project version has been parsed. version: ${SemVer.render(semVer)}"
            )
          )
        )

      _ <- SbtTask[F].toLeftWhen(
             projectVersion.pre.isDefined || projectVersion.buildMetadata.isDefined,
             SbtTaskError.versionNotEligibleForTagging(projectVersion),
           )

      currentBranchName <- SbtTask[F].fromGitTask(Git[F].currentBranchName(basePath))

      _ <- SbtTask[F].toLeftWhen(
             currentBranchName.value =!= tagFrom.value,
             SbtTaskError.gitTaskError(s"current branch does not match with $tagFrom"),
           )

      fetchResult <- SbtTask[F].fromGitTask(Git[F].fetchTags(basePath))
      tagResult   <- SbtTask[F].fromGitTask(
                       gitTagDescription
                         .fold(
                           Git[F].tag(tagName, basePath)
                         ) { desc =>
                           Git[F].tagWithDescription(
                             tagName,
                             Git.Description(desc),
                             basePath,
                           )
                         }
                     )
      pushResult  <- SbtTask[F].fromGitTask(Git[F].pushTag(pushRepo, tagName, basePath))
    } yield ()

  private def getGitHubAuthToken(
    envVarName: String,
    authTokenFile: Option[File],
  ): Either[GitHubError, GitHub.OAuthToken] =
    sys
      .env
      .get(envVarName)
      .fold(readOAuthToken(authTokenFile))(token => GitHub.OAuthToken(token).asRight)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  private def runGitHubRelease[F[_]: Fx: CanCatch: Monad: Timer](
    tagName: TagName,
    baseDir: File,
    changelogLocation: GitHub.ChangelogLocation,
    gitTagPushRepo: Repository,
    oAuthToken: GitHub.OAuthToken,
    gitHubApi: GitHubApi[F],
  ): GitHubTask.GitHubTaskResult[F, Unit] =
    for {
      changelog <-
        SbtTask[F].eitherTWithWriter(
          effectOf[F](getChangelog(new File(baseDir, changelogLocation.changeLogLocation), tagName))
        )(_ => List("Get changelog"))

      url  <- GitHubTask[F].fromGitTask(
                Git[F].getRemoteUrl(gitTagPushRepo, baseDir)
              )
      repo <-
        SbtTask[F].eitherTWithWriter(
          effectOf(getRepoFromUrl(url))
        )(r => List(s"Get GitHub repo org and name: ${r.toRepoNameString}"))

      gitHubRelease <-
        SbtTask[F].eitherTWithWriter(
          GitHubApi.githubWithAbuseRateLimit[F]() >> gitHubApi.createRelease(
            GitHubRelease.CreateRequestParams(
              tagName,
              GitHubRelease.ReleaseName(tagName.value).some,
              GitHubRelease.Description(changelog.changelog).some,
              GitHubRelease.Draft.no,
              GitHubRelease.Prerelease.no,
            ),
            GitHub.GitHubRepoWithAuth(
              GitHub.Repo(
                GitHub
                  .Repo
                  .Org(
                    repo.org.org
                  ),
                GitHub
                  .Repo
                  .Name(
                    repo.name.name
                  ),
              ),
              GitHub.GitHubRepoWithAuth.AccessToken(oAuthToken.token).some,
            ),
          )
        ) {
          case Some(release) =>
            List[String](
              s"GitHub release: ${release.tagName.value}",
              release
                .body
                .description
                .split("\n")
                .mkString("Changelog uploaded:\n    ", "\n    ", "\n"),
            )
          case None =>
            List("Release has failed.")
        }
    } yield ()

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  private def runUploadAssetsToGitHubRelease[F[_]: Fx: CanCatch: Monad](
    tagName: TagName,
    assets: Vector[File],
    baseDir: File,
    gitTagPushRepo: Repository,
    oAuthToken: GitHub.OAuthToken,
    gitHubApi: GitHubApi[F],
  )(implicit ec: ExecutionContext, timer: Timer[F]): GitHubTask.GitHubTaskResult[F, Unit] =
    for {
      url <- GitHubTask[F].fromGitTask(
               Git[F].getRemoteUrl(gitTagPushRepo, baseDir)
             )

      repo <-
        SbtTask[F].eitherTWithWriter(
          effectOf(getRepoFromUrl(url))
        )(r => List(s"Get GitHub repo org and name: ${r.toRepoNameString}"))

      repoWithAuth = GitHub.GitHubRepoWithAuth(
                       GitHub.Repo(
                         GitHub.Repo.Org(repo.org.org),
                         GitHub.Repo.Name(repo.name.name),
                       ),
                       GitHub.GitHubRepoWithAuth.AccessToken(oAuthToken.token).some,
                     )
      maybeRelease  <-
        SbtTask[F].eitherTWithWriter(
          gitHubApi.findReleaseByTagName(tagName, repoWithAuth)
        )(_ => List(s"try to find a GitHub release with the given tag: ${tagName.value}"))
      release       <- GitHubTask[F].toLeftIfNone(
                         maybeRelease,
                         GitHubError.releaseNotFoundByTagName(tagName),
                       )
      gitHubRelease <-
        SbtTask[F].eitherTWithWriter(
          gitHubApi.uploadAllAssetsToRelease(
            GitHubRelease.ReleaseId(release.id.id),
            repoWithAuth,
            assets.toList,
          )
        )(assets =>
          List[String](
            s"GitHub release: ${release.tagName.value}",
            if (assets.isEmpty)
              "No files to upload"
            else
              assets
                .map { asset =>
                  s"${asset.name.name} @ ${asset.browserDownloadUrl.browserDownloadUrl}"
                }
                .mkString("Files uploaded:\n    - ", "\n    - ", ""),
          )
        )
    } yield ()

  // $COVERAGE-ON$
}
