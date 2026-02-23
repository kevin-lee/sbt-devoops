package devoops

import cats.*
import cats.effect.{IO, Temporal}
import cats.instances.all.*
import cats.syntax.all.*
import devoops.data.SbtTaskResult.SbtTaskHistory
import devoops.data.*
import effectie.core.*
import effectie.syntax.all.*
import extras.scala.io.syntax.truecolor.rgb.*
import extras.scala.io.truecolor.Rgb
import just.semver.SemVer
import kevinlee.git.Git
import kevinlee.git.Git.{BranchName, Repository, TagName}
import kevinlee.github.data.*
import kevinlee.github.{GitHubApi, GitHubTask}
import kevinlee.http.HttpClient
import kevinlee.sbt.SbtCommon.messageOnlyException
import kevinlee.sbt.io.{CaseSensitivity, Io}

import loggerf.logger.{CanLog, SbtLogger}
import org.http4s.ember.client.EmberClientBuilder
import sbt.Keys.*
import sbt.{AutoPlugin, File, PluginTrigger, Plugins, Setting}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*

/** @author Kevin Lee
  * @since 2019-01-01
  */
object DevOopsGitHubReleasePlugin extends AutoPlugin {

  // $COVERAGE-OFF$
  override def requires: Plugins      = empty
  override def trigger: PluginTrigger = noTrigger

  object autoImport extends GitHubReleaseKeys with GitHubReleaseOps

  import autoImport.*
  import cats.effect.unsafe.implicits.global

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    devOopsLogLevel := DevOopsLogLevel.info.render,
    devOopsGitTagFrom := "main",
    devOopsGitTagDescription := None,
    devOopsGitTagName := decideVersion(version.value, v => s"v${SemVer.render(SemVer.parseUnsafe(v))}"),
    devOopsGitTagPushRepo := "origin",
    devOopsGitTag := {
      lazy val basePath       = baseDirectory.value
      lazy val tagFrom        = BranchName(devOopsGitTagFrom.value)
      lazy val tagName        = TagName(devOopsGitTagName.value)
      lazy val tagDesc        = devOopsGitTagDescription.value
      lazy val pushRepo       = Repository(devOopsGitTagPushRepo.value)
      lazy val projectVersion = version.value

      implicit val devOopsLogLevelValue: DevOopsLogLevel = DevOopsLogLevel.fromStringUnsafe(devOopsLogLevel.value)

      import effectie.instances.ce3.fx.*

      val run1: IO[(SbtTaskHistory, Either[SbtTaskError, Unit])] =
        getTagVersion[IO](basePath, tagFrom, tagName, tagDesc, pushRepo, projectVersion)
          .value
          .run

      SbtTask[IO]
        .handleSbtTask(run1)
        .unsafeRunSync()

    },
    devOopsCiDir := "ci",
    devOopsArtifactNamePrefix := name.value,
    devOopsPackagedArtifacts := {
      val filenamePrefix = devOopsArtifactNamePrefix.value
      List(
        s"target/scala-*/${filenamePrefix}*.jar",
        s"*/target/scala-*/${filenamePrefix}*.jar",
        s"*/*/target/scala-*/${filenamePrefix}*.jar",
        s"*/*/*/target/scala-*/${filenamePrefix}*.jar",
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
    devOopsChangelogLocation := "changelogs",
    devOopsGitHubAuthTokenEnvVar := "GITHUB_TOKEN",
    devOopsGitHubAuthTokenFile :=
      Some(new File(Io.getUserHome, ".github")),
    devOopsGitHubRequestTimeout := 2.minutes,
    devOopsWhenGitTagExistsInRelease := WhenGitTagExistsInRelease.failTagCreation,
    devOopsWhenGitHubReleaseExistsInRelease := WhenGitHubReleaseExistsInRelease.updateReleaseNote,
    devOopsGitHubRelease := {
      lazy val tagName         = TagName(devOopsGitTagName.value)
      lazy val authTokenEnvVar = devOopsGitHubAuthTokenEnvVar.value
      lazy val authTokenFile   = devOopsGitHubAuthTokenFile.value
      lazy val baseDir         = baseDirectory.value
      lazy val requestTimeout  = devOopsGitHubRequestTimeout.value

      lazy val whenGitHubReleaseExistsInRelease = devOopsWhenGitHubReleaseExistsInRelease.value

      implicit val devOopsLogLevelValue: DevOopsLogLevel = DevOopsLogLevel.fromStringUnsafe(devOopsLogLevel.value)

      import effectie.instances.ce3.fx.*

      implicit val log: CanLog = SbtLogger.sbtLoggerCanLog(streams.value.log)
      val git                  = Git[IO]
      val sbtTask              = SbtTask[IO]

      val result: IO[(SbtTaskHistory, Either[SbtTaskError, Unit])] = EmberClientBuilder
        .default[IO]
        .withIdleConnectionTime(requestTimeout)
        .withTimeout(requestTimeout)
        .build
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
              oauth <- sbtTask.eitherTWithWriter(
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
                           whenGitHubReleaseExistsInRelease,
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
    devOopsGitTagAndGitHubRelease := {
      lazy val tagName                          = TagName(devOopsGitTagName.value)
      lazy val tagDesc                          = devOopsGitTagDescription.value
      lazy val tagFrom                          = BranchName(devOopsGitTagFrom.value)
      lazy val authTokenEnvVar                  = devOopsGitHubAuthTokenEnvVar.value
      lazy val authTokenFile                    = devOopsGitHubAuthTokenFile.value
      lazy val baseDir                          = baseDirectory.value
      lazy val pushRepo                         = Repository(devOopsGitTagPushRepo.value)
      lazy val projectVersion                   = version.value
      lazy val requestTimeout                   = devOopsGitHubRequestTimeout.value
      lazy val whenGitTagExistsInRelease        = devOopsWhenGitTagExistsInRelease.value
      lazy val whenGitHubReleaseExistsInRelease = devOopsWhenGitHubReleaseExistsInRelease.value

      implicit val devOopsLogLevelValue: DevOopsLogLevel = DevOopsLogLevel.fromStringUnsafe(devOopsLogLevel.value)

      implicit val log: CanLog = SbtLogger.sbtLoggerCanLog(streams.value.log)

      import effectie.instances.ce3.fx.*

      EmberClientBuilder
        .default[IO]
        .withIdleConnectionTime(requestTimeout)
        .withTimeout(requestTimeout)
        .build
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
              _     <- getTagVersionInReleaseTask[IO](
                         baseDir,
                         tagFrom,
                         tagName,
                         tagDesc,
                         pushRepo,
                         projectVersion,
                         whenGitTagExistsInRelease,
                       )
              _     <- SbtTask[IO].handleGitHubTask(
                         runGitHubRelease(
                           tagName,
                           baseDir,
                           GitHub.ChangelogLocation(devOopsChangelogLocation.value),
                           pushRepo,
                           whenGitHubReleaseExistsInRelease,
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

      @SuppressWarnings(Array("org.wartremover.warts.GlobalExecutionContext"))
      implicit val ec: ExecutionContext = ExecutionContext.global

      implicit val devOopsLogLevelValue: DevOopsLogLevel = DevOopsLogLevel.fromStringUnsafe(devOopsLogLevel.value)

      import effectie.instances.ce3.fx.*

      implicit val log: CanLog = SbtLogger.sbtLoggerCanLog(streams.value.log)
      val git                  = Git[IO]
      val sbtTask              = SbtTask[IO]

      val result: IO[(SbtTaskHistory, Either[SbtTaskError, Unit])] = EmberClientBuilder
        .default[IO]
        .withIdleConnectionTime(requestTimeout)
        .withTimeout(requestTimeout)
        .build
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

  private def getTagVersion[F[?]: Fx: Monad](
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
      _ <- SbtTask[F].fromGitTask(Git[F].fetchTags(basePath))
      _ <- SbtTask[F].fromGitTask(
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
      _ <- SbtTask[F].fromGitTask(Git[F].pushTag(pushRepo, tagName, basePath))
    } yield ()

  private def getTagVersionInReleaseTask[F[?]: Fx: Monad](
    basePath: File,
    tagFrom: BranchName,
    tagName: TagName,
    gitTagDescription: Option[String],
    pushRepo: Repository,
    projectVersion: String,
    whenGitTagExistsInRelease: WhenGitTagExistsInRelease,
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

      _    <- SbtTask[F].toLeftWhen(
                currentBranchName.value =!= tagFrom.value,
                SbtTaskError.gitTaskError(s"current branch does not match with $tagFrom"),
              )
      _    <- SbtTask[F].fromGitTask(Git[F].fetchTags(basePath))
      tags <- SbtTask[F].fromGitTask(Git[F].getTag(basePath))
      _    <-
        if (tags.contains(tagName.value)) {
          whenGitTagExistsInRelease match {
            case WhenGitTagExistsInRelease.LogAndContinue =>
              succeedSbtTaskWithMessage(
                s"Tag ${tagName.value} already exists. Skip tag creation because devOopsWhenGitTagExistsInRelease is ${WhenGitTagExistsInRelease
                    .render(whenGitTagExistsInRelease)}."
              )
            case WhenGitTagExistsInRelease.FailTagCreation =>
              failSbtTask(
                SbtTaskError.tagAlreadyExistsInRelease(tagName.value)
              )
          }
        } else {
          for {
            _ <- SbtTask[F].fromGitTask(
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
            _ <- SbtTask[F].fromGitTask(Git[F].pushTag(pushRepo, tagName, basePath))
          } yield ()
        }
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
  private def runGitHubRelease[F[?]: Fx: CanCatch: Monad: Temporal](
    tagName: TagName,
    baseDir: File,
    changelogLocation: GitHub.ChangelogLocation,
    gitTagPushRepo: Repository,
    whenGitHubReleaseExistsInRelease: WhenGitHubReleaseExistsInRelease,
    oAuthToken: GitHub.OAuthToken,
    gitHubApi: GitHubApi[F],
  ): GitHubTask.GitHubTaskResult[F, Option[GitHubRelease.Response]] =
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

      repoWithAuth = GitHub.GitHubRepoWithAuth(
                       GitHub.Repo(
                         GitHub.Repo.Org(repo.org.org),
                         GitHub.Repo.Name(repo.name.name),
                       ),
                       GitHub.GitHubRepoWithAuth.AccessToken(oAuthToken.token).some,
                     )

      _                           <- liftEffectToGitHubTask(
                                       GitHubApi.githubWithAbuseRateLimit[F]()
                                     )
      gitHubReleaseAndUpdateState <-
        createOrUpdateGitHubRelease(
          tagName,
          changelog,
          repoWithAuth,
          whenGitHubReleaseExistsInRelease,
          gitHubApi,
        )
      _                           <- logGitHubReleaseSummary(tagName, gitHubReleaseAndUpdateState)
    } yield gitHubReleaseAndUpdateState._1

  private def createOrUpdateGitHubRelease[F[?]: Fx: Monad](
    tagName: TagName,
    changelog: GitHub.Changelog,
    repoWithAuth: GitHub.GitHubRepoWithAuth,
    whenGitHubReleaseExistsInRelease: WhenGitHubReleaseExistsInRelease,
    gitHubApi: GitHubApi[F],
  ): GitHubTask.GitHubTaskResult[F, (Option[GitHubRelease.Response], ReleaseCreationOrUpdate)] = {
    val releaseName          = GitHubRelease.ReleaseName(tagName.value).some
    val changelogDescription = GitHubRelease.Description(changelog.changelog).some
    val createParams         = GitHubRelease.CreateRequestParams(
      tagName,
      releaseName,
      changelogDescription,
      GitHubRelease.Draft.no,
      GitHubRelease.Prerelease.no,
    )

    for {
      _            <- logGitHubReleaseStep(
                        s"Try to create a GitHub release with tag: ${tagName.value}"
                      )
      createResult <- liftEffectToGitHubTask(
                        gitHubApi.createRelease(createParams, repoWithAuth)
                      )
      result       <- createResult match {
                        case Right(maybeRelease) =>
                          for {
                            _ <-
                              logGitHubReleaseStep(
                                maybeRelease.fold(
                                  s"Create release API returned empty response for tag: ${tagName.value}"
                                )(release =>
                                  s"Create release API succeeded for release id: ${release.id.id} (tag: ${release.tagName.value})"
                                )
                              )
                          } yield (maybeRelease, ReleaseCreationOrUpdate.created)

                        case Left(createError) if GitHubError.isReleaseTagNameAlreadyExists(createError) =>
                          whenGitHubReleaseExistsInRelease match {
                            case WhenGitHubReleaseExistsInRelease.UpdateReleaseNote =>
                              recoverAndUpdateGitHubReleaseWhenExists(
                                tagName,
                                repoWithAuth,
                                releaseName,
                                changelogDescription,
                                createError,
                                gitHubApi,
                              )

                            case WhenGitHubReleaseExistsInRelease.LogAndContinue =>
                              for {
                                _ <-
                                  logGitHubReleaseStep(
                                    s"Release with tag already exists. Skip changelog update because devOopsWhenGitHubReleaseExistsInRelease is ${WhenGitHubReleaseExistsInRelease
                                        .render(whenGitHubReleaseExistsInRelease)}."
                                  )
                              } yield (none[GitHubRelease.Response], ReleaseCreationOrUpdate.skipped)

                            case WhenGitHubReleaseExistsInRelease.FailRelease =>
                              for {
                                _      <-
                                  logGitHubReleaseStep(
                                    s"Release with tag already exists. Stop task because devOopsWhenGitHubReleaseExistsInRelease is ${WhenGitHubReleaseExistsInRelease
                                        .render(whenGitHubReleaseExistsInRelease)}."
                                  )
                                failed <- failGitHubTask[
                                            F,
                                            (
                                              Option[GitHubRelease.Response],
                                              ReleaseCreationOrUpdate,
                                            ),
                                          ](
                                            GitHubError.releaseCreationError(
                                              releaseAlreadyExistsInReleaseFailureMessage(
                                                tagName,
                                                whenGitHubReleaseExistsInRelease,
                                              )
                                            )
                                          )
                              } yield failed
                          }

                        case Left(createError) =>
                          for {
                            _      <- logGitHubReleaseStep(
                                        s"Create release API failed for tag: ${tagName.value}"
                                      )
                            failed <- failGitHubTask[
                                        F,
                                        (
                                          Option[GitHubRelease.Response],
                                          ReleaseCreationOrUpdate,
                                        ),
                                      ](createError)
                          } yield failed
                      }
    } yield result
  }

  private def recoverAndUpdateGitHubReleaseWhenExists[F[?]: Fx: Monad](
    tagName: TagName,
    repoWithAuth: GitHub.GitHubRepoWithAuth,
    releaseName: Option[GitHubRelease.ReleaseName],
    changelogDescription: Option[GitHubRelease.Description],
    createError: GitHubError,
    gitHubApi: GitHubApi[F],
  ): GitHubTask.GitHubTaskResult[F, (Option[GitHubRelease.Response], ReleaseCreationOrUpdate)] =
    for {
      _          <-
        logGitHubReleaseStep(
          s"Release with tag already exists. Try to find the existing release by tag: ${tagName.value}"
        )
      findResult <- liftEffectToGitHubTask(
                      gitHubApi.findReleaseByTagName(tagName, repoWithAuth)
                    )
      recovered  <- findResult match {
                      case Right(Some(release)) =>
                        val updateParams = GitHubRelease.UpdateRequestParams(
                          tagName,
                          GitHubRelease.ReleaseId(release.id.id),
                          releaseName,
                          changelogDescription,
                          none[GitHubRelease.Draft],
                          none[GitHubRelease.Prerelease],
                        )
                        for {
                          _            <-
                            logGitHubReleaseStep(
                              s"Found existing release id: ${release.id.id}. Try to update release body."
                            )
                          updateResult <- liftEffectToGitHubTask(
                                            gitHubApi.updateRelease(updateParams, repoWithAuth)
                                          )
                          updated      <- updateResult match {
                                            case Right(updatedRelease) =>
                                              for {
                                                _ <-
                                                  logGitHubReleaseStep(
                                                    updatedRelease.fold(
                                                      s"Update release API returned empty response for tag: ${tagName.value}"
                                                    )(release =>
                                                      s"Update release API succeeded for release id: ${release.id.id} (tag: ${release.tagName.value})"
                                                    )
                                                  )
                                              } yield (updatedRelease, ReleaseCreationOrUpdate.updated)

                                            case Left(updateError) =>
                                              for {
                                                _      <- logGitHubReleaseStep(
                                                            s"Update release API failed for tag: ${tagName.value}"
                                                          )
                                                failed <-
                                                  failGitHubTask[
                                                    F,
                                                    (
                                                      Option[GitHubRelease.Response],
                                                      ReleaseCreationOrUpdate,
                                                    ),
                                                  ](updateError)
                                              } yield failed
                                          }
                        } yield updated

                      case Right(None) =>
                        for {
                          _      <-
                            logGitHubReleaseStep(
                              s"Find release by tag returned no release for tag: ${tagName.value}. Return the original create error."
                            )
                          failed <- failGitHubTask[
                                      F,
                                      (
                                        Option[GitHubRelease.Response],
                                        ReleaseCreationOrUpdate,
                                      ),
                                    ](createError)
                        } yield failed

                      case Left(findError) =>
                        for {
                          _      <- logGitHubReleaseStep(
                                      s"Find release by tag failed for tag: ${tagName.value}"
                                    )
                          failed <- failGitHubTask[
                                      F,
                                      (
                                        Option[GitHubRelease.Response],
                                        ReleaseCreationOrUpdate,
                                      ),
                                    ](findError)
                        } yield failed
                    }
    } yield recovered

  private def releaseAlreadyExistsInReleaseFailureMessage(
    tagName: TagName,
    whenGitHubReleaseExistsInRelease: WhenGitHubReleaseExistsInRelease,
  ): String = {
    val lhsColor   = Rgb.unsafeFromHexString("#F6D58E")
    val rhsColor   = Rgb.unsafeFromHexString("#7DBBFF")
    val redColor   = Rgb.unsafeFromInt(0xf67280)
    val greenColor = Rgb.unsafeFromInt(0xc1e1c1)

    val currentBehavior =
      s"${"WhenGitHubReleaseExistsInRelease".rgbed(greenColor)}." +
        s"${WhenGitHubReleaseExistsInRelease.render(whenGitHubReleaseExistsInRelease).rgbed(redColor)}"

    val releaseExistsLhs         = "devOopsWhenGitHubReleaseExistsInRelease".rgbed(lhsColor)
    val releaseExistsUpdateRhs   =
      s"${"WhenGitHubReleaseExistsInRelease".rgbed(greenColor)}.${"UpdateReleaseNote".rgbed(rhsColor)}"
    val releaseExistsContinueRhs =
      s"${"WhenGitHubReleaseExistsInRelease".rgbed(greenColor)}.${"LogAndContinue".rgbed(rhsColor)}"

    s"""|  The GitHub release for ${tagName.value} already exists.
        |  This task stopped because devOopsWhenGitHubReleaseExistsInRelease is set to $currentBehavior.
        |
        |  To update the existing release note with changelog:
        |  ${releaseExistsLhs} := ${releaseExistsUpdateRhs}
        |
        |  To continue without updating the existing release note:
        |  ${releaseExistsLhs} := ${releaseExistsContinueRhs}
        |""".stripMargin
  }

  private def logGitHubReleaseSummary[F[?]: Fx: Monad](
    tagName: TagName,
    gitHubReleaseAndUpdateState: (Option[GitHubRelease.Response], ReleaseCreationOrUpdate)
  ): GitHubTask.GitHubTaskResult[F, Unit] =
    logGitHubReleaseSteps(
      gitHubReleaseAndUpdateState match {
        case (_, ReleaseCreationOrUpdate.Skipped) =>
          /* `Skipped` means release already exists and changelog update is intentionally skipped. */
          List(ReleaseCreationOrUpdate.releaseResultMessage(ReleaseCreationOrUpdate.Skipped, tagName))

        case (
              Some(release),
              releaseCreationOrUpdate @ (ReleaseCreationOrUpdate.Created | ReleaseCreationOrUpdate.Updated)
            ) =>
          /* Release success with new release creation. */
          List[String](
            ReleaseCreationOrUpdate.releaseResultMessage(releaseCreationOrUpdate, release.tagName),
            release
              .body
              .description
              .split("\n")
              .mkString("Changelog uploaded:\n    ", "\n    ", "\n"),
          )

        case (None, releaseCreationOrUpdate @ (ReleaseCreationOrUpdate.Created | ReleaseCreationOrUpdate.Updated)) =>
          /* Release failure in creation or update path. */
          List(ReleaseCreationOrUpdate.releaseFailureMessage(releaseCreationOrUpdate))

      }
    )

  private def logGitHubReleaseStep[F[?]: Fx: Monad](message: String): GitHubTask.GitHubTaskResult[F, Unit] =
    logGitHubReleaseSteps(List(message))

  private def logGitHubReleaseSteps[F[?]: Fx: Monad](messages: List[String]): GitHubTask.GitHubTaskResult[F, Unit] =
    SbtTask[F].eitherTWithWriter(
      pureOf(().asRight[GitHubError])
    )(_ => messages)

  private def liftEffectToGitHubTask[F[?]: Fx: Monad, A](fa: F[A]): GitHubTask.GitHubTaskResult[F, A] =
    SbtTask[F].eitherTWithWriter(
      fa.map(_.asRight[GitHubError])
    )(_ => List.empty[String])

  private def failGitHubTask[F[?]: Fx: Monad, A](error: GitHubError): GitHubTask.GitHubTaskResult[F, A] =
    SbtTask[F].eitherTWithWriter(
      pureOf(error.asLeft[A])
    )(_ => List.empty[String])

  private def succeedSbtTaskWithMessage[F[?]: Fx: Monad](message: String): SbtTask.Result[F, Unit] =
    SbtTask[F].eitherTWithWriter(
      pureOf(().asRight[SbtTaskError])
    )(_ => List(SbtTaskResult.nonSbtTaskResult(message)))

  private def failSbtTask[F[?]: Fx: Monad](error: SbtTaskError): SbtTask.Result[F, Unit] =
    SbtTask[F].eitherTWithWriter(
      pureOf(error.asLeft[Unit])
    )(_ => List.empty[SbtTaskResult])

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  private def runUploadAssetsToGitHubRelease[F[?]: Fx: CanCatch: Monad](
    tagName: TagName,
    assets: Vector[File],
    baseDir: File,
    gitTagPushRepo: Repository,
    oAuthToken: GitHub.OAuthToken,
    gitHubApi: GitHubApi[F],
  )(implicit ec: ExecutionContext, timer: Temporal[F]): GitHubTask.GitHubTaskResult[F, List[GitHubRelease.Asset]] =
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
    } yield gitHubRelease

  private sealed trait ReleaseCreationOrUpdate
  private object ReleaseCreationOrUpdate {
    case object Created extends ReleaseCreationOrUpdate
    case object Updated extends ReleaseCreationOrUpdate
    case object Skipped extends ReleaseCreationOrUpdate

    def created: ReleaseCreationOrUpdate = Created
    def updated: ReleaseCreationOrUpdate = Updated
    def skipped: ReleaseCreationOrUpdate = Skipped

    def releaseResultMessage(
      releaseCreationOrUpdate: ReleaseCreationOrUpdate,
      tagName: TagName,
    ): String =
      releaseCreationOrUpdate match {
        case Created =>
          s"GitHub release: ${tagName.value}"
        case Updated =>
          s"GitHub release updated: ${tagName.value}"
        case Skipped =>
          s"GitHub release already exists. Changelog update skipped: ${tagName.value}"
      }

    def releaseFailureMessage(releaseCreationOrUpdate: ReleaseCreationOrUpdate): String =
      releaseCreationOrUpdate match {
        case Created =>
          "Release has failed."
        case Updated =>
          "Release update has failed."
        case Skipped =>
          "Release note update has been skipped."
      }
  }

  // $COVERAGE-ON$

}
