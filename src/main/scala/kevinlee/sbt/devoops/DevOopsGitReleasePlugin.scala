package kevinlee.sbt.devoops

import cats._
import cats.effect.{ContextShift, IO}
import cats.instances.all._
import cats.syntax.all._
import effectie.cats.Effectful._
import effectie.cats._
import just.semver.SemVer
import kevinlee.git.Git
import kevinlee.git.Git.{BranchName, RepoUrl, Repository, TagName}
import kevinlee.github.data._
import kevinlee.github.{GitHubApi, GitHubTask, OldGitHubApi}
import kevinlee.http.HttpClient
import kevinlee.sbt.SbtCommon.messageOnlyException
import kevinlee.sbt.devoops.data.SbtTaskResult.SbtTaskHistory
import kevinlee.sbt.devoops.data.{SbtTask, SbtTaskError, SbtTaskResult}
import kevinlee.sbt.io.{CaseSensitivity, Io}
import loggerf.logger.{CanLog, SbtLogger}
import org.http4s.client.blaze.BlazeClientBuilder
import sbt.Keys._
import sbt.{AutoPlugin, File, PluginTrigger, Plugins, Setting, SettingKey, TaskKey, settingKey, taskKey}

import scala.concurrent.duration._
import java.io.FileInputStream
import scala.concurrent.ExecutionContext

/** @author Kevin Lee
  * @since 2019-01-01
  */
object DevOopsGitReleasePlugin extends AutoPlugin {

  def TheGitHubApi[F[_]: EffectConstructor: CanCatch: Monad]: OldGitHubApi[F] = OldGitHubApi[F]

  // $COVERAGE-OFF$
  override def requires: Plugins      = empty
  override def trigger: PluginTrigger = noTrigger

  object autoImport {
    lazy val gitTagFrom: SettingKey[String] = settingKey[String]("The name of branch to tag from. (Default: main)")

    lazy val gitTagDescription: SettingKey[Option[String]] = settingKey[Option[String]](
      "description for git tagging (Default: None)"
    )

    lazy val gitTagName: TaskKey[String] = taskKey[String](
      """git tag name (default: parse the project version as semantic version and render with the prefix 'v'. e.g.) version := "1.0.0" / gitTagName := "v1.0.0""""
    )

    lazy val gitTagPushRepo: TaskKey[String] = taskKey[String]("The name of Git repo to push the tag (default: origin)")

    lazy val gitTag: TaskKey[Unit] = taskKey[Unit]("task to create a git tag from the branch set in gitTagFrom")

    lazy val devOopsCiDir: SettingKey[String] = settingKey[String](
      "The ci directory which contains the files created in build to upload to GitHub release (e.g. packaged jar files) It can be either an absolute or relative path. (default: ci)"
    )

    lazy val devOopsPackagedArtifacts: TaskKey[List[String]] = taskKey(
      s"""A list of packaged artifacts to be copied to PROJECT_HOME/$${devOopsCiDir.value}/dist (default: List(s"target/scala-*/$${name.value}*.jar") )"""
    )

    lazy val devOopsCopyReleasePackages: TaskKey[Vector[File]] = taskKey[Vector[File]](
      s"task to copy packaged artifacts to the location specified (default: devOopsPackagedArtifacts.value to PROJECT_HOME/$${devOopsCiDir.value}/dist"
    )

    lazy val changelogLocation: SettingKey[String] = settingKey[String](
      "The location of changelog file. (default: PROJECT_HOME/changelogs)"
    )

    lazy val gitHubAuthTokenEnvVar: SettingKey[String] = settingKey[String](
      "The environment variable name for GitHub auth token (default: GITHUB_TOKEN)"
    )

    lazy val gitHubAuthTokenFile: SettingKey[Option[File]] = settingKey[Option[File]](
      "The path to GitHub OAuth token file. The file should contain oauth=OAUTH_TOKEN (default: Some($USER/.github)) If you want to get the file in user's home, do Some(new File(Io.getUserHome, \".github\"))"
    )

    lazy val gitHubRequestTimeout: TaskKey[FiniteDuration] = taskKey[FiniteDuration](
      "Timeout value for any request sent to GitHub (default: 2.minutes)"
    )

    lazy val gitHubRelease: TaskKey[Unit] = taskKey[Unit](
      "Release the current version without creating a tag. It also uploads the changelog to GitHub."
    )

    lazy val gitTagAndGitHubRelease: TaskKey[Unit] = taskKey[Unit](
      "Release the current version. It creates a tag with the project version and uploads the changelog to GitHub."
    )

    lazy val gitHubReleaseUploadArtifacts: TaskKey[Unit] = taskKey[Unit](
      "Upload the packaged files to the GitHub release with the current version. The tag with the project version and the GitHub release of it should exist to run this task."
    )

    def decideVersion(projectVersion: String, decide: String => String): String =
      decide(projectVersion)

    def copyFiles(
      taskName: String,
      caseSensitivity: CaseSensitivity,
      projectBaseDir: File,
      filePaths: List[String],
      targetDir: File,
    ): Either[SbtTaskError, Vector[File]] =
      scala.util.Try {
        val files  = Io.findAllFiles(
          caseSensitivity,
          projectBaseDir,
          filePaths,
        )
        val copied = Io.copy(files, targetDir)
        println(s""">> copyPackages - Files copied from:
                   |${files.mkString("  - ", "\n  - ", "\n")}
                   |  to
                   |${copied.mkString("  - ", "\n  - ", "\n")}
                   |""".stripMargin)
        copied
      } match {
        case scala.util.Success(files) =>
          files.asRight
        case scala.util.Failure(error) =>
          SbtTaskError.ioError(taskName, error).asLeft
      }

    def readOAuthToken(maybeFile: Option[File]): Either[GitHubError, OAuthToken] =
      maybeFile match {
        case Some(file) =>
          val props = new java.util.Properties()
          props.load(new FileInputStream(file))
          Option(props.getProperty("oauth"))
            .fold[Either[GitHubError, OAuthToken]](GitHubError.noCredential.asLeft)(token => OAuthToken(token).asRight)
        case None       =>
          GitHubError.noCredential.asLeft
      }

    def getRepoFromUrl(repoUrl: RepoUrl): Either[GitHubError, Repo] = {
      val names =
        if (repoUrl.repoUrl.startsWith("http"))
          repoUrl.repoUrl.split("/")
        else
          repoUrl.repoUrl.split(":").last.split("/")
      names.takeRight(2) match {
        case Array(org, name) =>
          Repo(RepoOrg(org), RepoName(name.stripSuffix(".git"))).asRight
        case _                =>
          GitHubError.invalidGitHubRepoUrl(repoUrl).asLeft
      }
    }

    def getChangelog(dir: File, tagName: TagName): Either[GitHubError, Changelog] = {
      val changelogName = s"${tagName.value.stripPrefix("v")}.md"
      val changelog     = new File(dir, changelogName)
      if (!changelog.exists) {
        GitHubError.changelogNotFound(changelog.getCanonicalPath, tagName).asLeft
      } else {
        lazy val changelogSource = scala.io.Source.fromFile(changelog)
        try {
          val log = changelogSource.getLines().mkString("\n")
          Changelog(log).asRight
        } finally {
          changelogSource.close()
        }
      }
    }

  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    gitTagFrom := "main",
    gitTagDescription := None,
    gitTagName := decideVersion(version.value, v => s"v${SemVer.render(SemVer.parseUnsafe(v))}"),
    gitTagPushRepo := "origin",
    gitTag := {
      lazy val basePath       = baseDirectory.value
      lazy val tagFrom        = BranchName(gitTagFrom.value)
      lazy val tagName        = TagName(gitTagName.value)
      lazy val tagDesc        = gitTagDescription.value
      lazy val pushRepo       = Repository(gitTagPushRepo.value)
      lazy val projectVersion = version.value

      val run1: IO[(SbtTaskHistory, Either[SbtTaskError, Unit])] =
        getTagVersion[IO](basePath, tagFrom, tagName, tagDesc, pushRepo, projectVersion)
          .value
          .run

      SbtTask[IO]
        .handleSbtTask(run1)
        .unsafeRunSync()

    },
    devOopsCiDir := "ci",
    devOopsPackagedArtifacts := List(s"target/scala-*/${name.value}*.jar"),
    devOopsCopyReleasePackages := {
      val result: Vector[File] =
        copyFiles(
          "devOopsCopyReleasePackages",
          CaseSensitivity.caseSensitive,
          baseDirectory.value,
          devOopsPackagedArtifacts.value,
          new File(new File(devOopsCiDir.value), "dist"),
        ) match {
          case Left(error)  =>
            messageOnlyException(SbtTaskError.render(error))
          case Right(files) =>
            files
        }
      result
    },
    changelogLocation := "changelogs",
    gitHubAuthTokenEnvVar := "GITHUB_TOKEN",
    gitHubAuthTokenFile :=
      Some(new File(Io.getUserHome, ".github")),
    gitHubRequestTimeout := 2.minutes,
    gitHubRelease := {
      lazy val tagName                  = TagName(gitTagName.value)
      lazy val authTokenEnvVar          = gitHubAuthTokenEnvVar.value
      lazy val authTokenFile            = gitHubAuthTokenFile.value
      lazy val baseDir                  = baseDirectory.value
      lazy val requestTimeout           = gitHubRequestTimeout.value
      implicit val ec: ExecutionContext = ExecutionContext.global
      implicit val cs: ContextShift[IO] = IO.contextShift(ec)

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
                           ChangelogLocation(changelogLocation.value),
                           Repository(gitTagPushRepo.value),
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
    gitTagAndGitHubRelease := {
      lazy val tagName         = TagName(gitTagName.value)
      lazy val tagDesc         = gitTagDescription.value
      lazy val tagFrom         = BranchName(gitTagFrom.value)
      lazy val authTokenEnvVar = gitHubAuthTokenEnvVar.value
      lazy val authTokenFile   = gitHubAuthTokenFile.value
      lazy val baseDir         = baseDirectory.value
      lazy val pushRepo        = Repository(gitTagPushRepo.value)
      lazy val projectVersion  = version.value
      lazy val requestTimeout  = gitHubRequestTimeout.value

      implicit val ec: ExecutionContext = ExecutionContext.global
      implicit val cs: ContextShift[IO] = IO.contextShift(ec)

      implicit val log: CanLog = SbtLogger.sbtLoggerCanLog(streams.value.log)

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
                           ChangelogLocation(changelogLocation.value),
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
    gitHubReleaseUploadArtifacts := {
      lazy val tagName         = TagName(gitTagName.value)
      lazy val assets          = devOopsCopyReleasePackages.value
      lazy val authTokenEnvVar = gitHubAuthTokenEnvVar.value
      lazy val authTokenFile   = gitHubAuthTokenFile.value
      lazy val baseDir         = baseDirectory.value
      lazy val artifacts       = devOopsPackagedArtifacts.value
      lazy val requestTimeout  = gitHubRequestTimeout.value

      implicit val ec: ExecutionContext = ExecutionContext.global
      implicit val cs: ContextShift[IO] = IO.contextShift(ec)

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
                           Repository(gitTagPushRepo.value),
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

  private def getTagVersion[F[_]: EffectConstructor: CanCatch: Monad](
    basePath: File,
    tagFrom: BranchName,
    tagName: TagName,
    gitTagDescription: Option[String],
    pushRepo: Repository,
    projectVersion: String,
  ): SbtTask.Result[F, Unit] =
    for {
      projectVersion    <-
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
      _                 <- SbtTask[F].toLeftWhen(
                             projectVersion.pre.isDefined || projectVersion.buildMetadata.isDefined,
                             SbtTaskError.versionNotEligibleForTagging(projectVersion),
                           )
      currentBranchName <- SbtTask[F].fromGitTask(Git[F].currentBranchName(basePath))
      _                 <- SbtTask[F].toLeftWhen(
                             currentBranchName.value =!= tagFrom.value,
                             SbtTaskError.gitTaskError(s"current branch does not match with $tagFrom"),
                           )
      fetchResult       <- SbtTask[F].fromGitTask(Git[F].fetchTags(basePath))
      tagResult         <- SbtTask[F].fromGitTask(
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
      pushResult        <- SbtTask[F].fromGitTask(Git[F].pushTag(pushRepo, tagName, basePath))
    } yield ()

  private def getGitHubAuthToken(
    envVarName: String,
    authTokenFile: Option[File],
  ): Either[GitHubError, OAuthToken] =
    sys
      .env
      .get(envVarName)
      .fold(readOAuthToken(authTokenFile))(token => OAuthToken(token).asRight)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  private def runGitHubRelease[F[_]: EffectConstructor: CanCatch: Monad](
    tagName: TagName,
    baseDir: File,
    changelogLocation: ChangelogLocation,
    gitTagPushRepo: Repository,
    oAuthToken: OAuthToken,
    gitHubApi: GitHubApi[F],
  ): GitHubTask.GitHubTaskResult[F, Unit] =
    for {
      changelog     <-
        SbtTask[F].eitherTWithWriter(
          effectOf[F](getChangelog(new File(baseDir, changelogLocation.changeLogLocation), tagName))
        )(_ => List("Get changelog"))
      url           <- GitHubTask[F].fromGitTask(
                         Git[F].getRemoteUrl(gitTagPushRepo, baseDir)
                       )
      repo          <-
        SbtTask[F].eitherTWithWriter(
          effectOf(getRepoFromUrl(url))
        )(r => List(s"Get GitHub repo org and name: ${Repo.repoNameString(r)}"))
      gitHubRelease <-
        SbtTask[F].eitherTWithWriter(
          gitHubApi.createRelease(
            GitHubRelease.CreateRequestParams(
              tagName,
              GitHubRelease.ReleaseName(tagName.value).some,
              GitHubRelease.Description(changelog.changelog).some,
              GitHubRelease.Draft.no,
              GitHubRelease.Prerelease.no,
            ),
            GitHubRepoWithAuth(
              GitHubRepo(
                GitHubRepo.Org(
                  repo.repoOrg.org
                ),
                GitHubRepo.Repo(
                  repo.repoName.name
                ),
              ),
              GitHubRepoWithAuth.AccessToken(oAuthToken.token).some,
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
          case None          =>
            List("Release has failed.")
        }
    } yield ()

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  private def runUploadAssetsToGitHubRelease[F[_]: EffectConstructor: CanCatch: Monad](
    tagName: TagName,
    assets: Vector[File],
    baseDir: File,
    gitTagPushRepo: Repository,
    oAuthToken: OAuthToken,
    gitHubApi: GitHubApi[F],
  )(implicit ec: ExecutionContext): GitHubTask.GitHubTaskResult[F, Unit] =
    for {
      url <- GitHubTask[F].fromGitTask(
               Git[F].getRemoteUrl(gitTagPushRepo, baseDir)
             )

      repo <-
        SbtTask[F].eitherTWithWriter(
          effectOf(getRepoFromUrl(url))
        )(r => List(s"Get GitHub repo org and name: ${Repo.repoNameString(r)}"))

      repoWithAuth   = GitHubRepoWithAuth(
                         GitHubRepo(
                           GitHubRepo.Org(repo.repoOrg.org),
                           GitHubRepo.Repo(repo.repoName.name),
                         ),
                         GitHubRepoWithAuth.AccessToken(oAuthToken.token).some,
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
