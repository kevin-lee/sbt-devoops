package kevinlee.sbt.devoops

import cats._
import cats.effect.IO
import cats.instances.all._
import cats.syntax.all._
import effectie.cats.Effectful._
import effectie.cats._
import just.semver.SemVer
import kevinlee.git.Git
import kevinlee.git.Git.{BranchName, RepoUrl, Repository, TagName}
import kevinlee.github.data._
import kevinlee.github.{GitHubTask, OldGitHubApi}
import kevinlee.sbt.SbtCommon.messageOnlyException
import kevinlee.sbt.devoops.data.SbtTaskResult.SbtTaskHistory
import kevinlee.sbt.devoops.data.{SbtTask, SbtTaskError, SbtTaskResult}
import kevinlee.sbt.io.{CaseSensitivity, Io}
import sbt.Keys._
import sbt.{AutoPlugin, File, PluginTrigger, Plugins, Setting, SettingKey, TaskKey, settingKey, taskKey}

import java.io.FileInputStream

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object DevOopsGitReleasePlugin extends AutoPlugin {

  def TheGitHubApi[F[_]: EffectConstructor: CanCatch: Monad]: OldGitHubApi[F] = OldGitHubApi[F]

  // $COVERAGE-OFF$
  override def requires: Plugins = empty
  override def trigger: PluginTrigger = noTrigger

  object autoImport {
    lazy val gitTagFrom: SettingKey[String] =
      settingKey[String]("The name of branch to tag from. (Default: master)")

    lazy val gitTagDescription: SettingKey[Option[String]] =
      settingKey[Option[String]]("description for git tagging (Default: None)")

    lazy val gitTagName: TaskKey[String] =
      taskKey[String]("""git tag name (default: parse the project version as semantic version and render with the prefix 'v'. e.g.) version := "1.0.0" / gitTagName := "v1.0.0"""")

    lazy val gitTagPushRepo: TaskKey[String] =
      taskKey[String]("The name of Git repo to push the tag (default: origin)")


    lazy val gitTag: TaskKey[Unit] =
      taskKey[Unit]("task to create a git tag from the branch set in gitTagFrom")

    lazy val devOopsCiDir: SettingKey[String] = settingKey[String]("The ci directory which contains the files created in build to upload to GitHub release (e.g. packaged jar files) It can be either an absolute or relative path. (default: ci)")

    lazy val devOopsPackagedArtifacts: TaskKey[List[String]] =
      taskKey(s"""A list of packaged artifacts to be copied to PROJECT_HOME/$${devOopsCiDir.value}/dist (default: List(s"target/scala-*/$${name.value}*.jar") )""")

    lazy val devOopsCopyReleasePackages: TaskKey[Vector[File]] =
      taskKey[Vector[File]](s"task to copy packaged artifacts to the location specified (default: devOopsPackagedArtifacts.value to PROJECT_HOME/$${devOopsCiDir.value}/dist")

    lazy val changelogLocation: SettingKey[String] =
      settingKey[String]("The location of changelog file. (default: PROJECT_HOME/changelogs)")

    lazy val gitHubAuthTokenEnvVar: SettingKey[String] =
      settingKey[String]("The environment variable name for GitHub auth token (default: GITHUB_TOKEN)")

    lazy val gitHubAuthTokenFile: SettingKey[Option[File]] =
      settingKey[Option[File]]("The path to GitHub OAuth token file. The file should contain oauth=OAUTH_TOKEN (default: Some($USER/.github)) If you want to get the file in user's home, do Some(new File(Io.getUserHome, \".github\"))")

    lazy val artifactsRequiredForGitHubRelease: SettingKey[Boolean] =
      settingKey[Boolean]("Option to upload artifacts to GitHub when doing GitHub release (default: true so upload the files)")

    lazy val gitHubRelease: TaskKey[Unit] =
      taskKey[Unit]("Release the current version without creating a tag. It uploads the packaged files and changelog to GitHub.")

    lazy val gitTagAndGitHubRelease: TaskKey[Unit] =
      taskKey[Unit]("Release the current version. It creates a tag with the project version and uploads the packaged files and changelog to GitHub.")

    def decideVersion(projectVersion: String, decide: String => String): String =
      decide(projectVersion)

    def copyFiles(
      taskName: String
    , caseSensitivity: CaseSensitivity
    , projectBaseDir: File
    , filePaths: List[String]
    , targetDir: File
    ): Either[SbtTaskError, Vector[File]] =
      scala.util.Try {
        val files = Io.findAllFiles(
          caseSensitivity
          , projectBaseDir
          , filePaths
        )
        val copied = Io.copy(files, targetDir)
        println(
          s""">> copyPackages - Files copied from:
             |${files.mkString("  - ",  "\n  - ", "\n")}
             |  to
             |${copied.mkString("  - ",  "\n  - ", "\n")}
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
        case None =>
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
        case _ =>
          GitHubError.invalidGitHubRepoUrl(repoUrl).asLeft
      }
    }

    def getChangelog(dir: File, tagName: TagName): Either[GitHubError, Changelog] = {
      val changelogName = s"${tagName.value.stripPrefix("v")}.md"
      // baseDirectory.value, changelogLocation.value)
      val changelog = new File(dir, changelogName)
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
    gitTagFrom := "master"
  , gitTagDescription := None

  , gitTagName := decideVersion(version.value, v => s"v${SemVer.render(SemVer.parseUnsafe(v))}")
  , gitTagPushRepo := "origin"
  , gitTag := {
      lazy val basePath = baseDirectory.value
      lazy val tagFrom = BranchName(gitTagFrom.value)
      lazy val tagName = TagName(gitTagName.value)
      lazy val tagDesc = gitTagDescription.value
      lazy val pushRepo = Repository(gitTagPushRepo.value)
      lazy val projectVersion = version.value

      val run1: IO[(SbtTaskHistory, Either[SbtTaskError, Unit])] =
        getTagVersion[IO](basePath, tagFrom, tagName, tagDesc, pushRepo, projectVersion)
        .value
        .run

      SbtTask[IO].handleSbtTask(run1)
        .unsafeRunSync()

    }
  , devOopsCiDir := "ci"
  , devOopsPackagedArtifacts := List(s"target/scala-*/${name.value}*.jar")
  , devOopsCopyReleasePackages := {
      val result: Vector[File] = copyFiles(
          "devOopsCopyReleasePackages"
        , CaseSensitivity.caseSensitive
        , baseDirectory.value
        , devOopsPackagedArtifacts.value
        , new File(new File(devOopsCiDir.value), "dist")
        ) match {
          case Left(error) =>
            messageOnlyException(SbtTaskError.render(error))
          case Right(files) =>
            files
        }
      result
    }
  , changelogLocation := "changelogs"
  , gitHubAuthTokenEnvVar := "GITHUB_TOKEN"
  , gitHubAuthTokenFile :=
      Some(new File(Io.getUserHome, ".github"))
  , artifactsRequiredForGitHubRelease := true
  , gitHubRelease := {
      lazy val tagName = TagName(gitTagName.value)
      lazy val uploadArtifacts = artifactsRequiredForGitHubRelease.value
      lazy val assets = devOopsCopyReleasePackages.value
      lazy val authTokenEnvVar = gitHubAuthTokenEnvVar.value
      lazy val authTokenFile = gitHubAuthTokenFile.value
      lazy val baseDir = baseDirectory.value
      lazy val artifacts = devOopsPackagedArtifacts.value
      val git = Git[IO]
      val sbtTask = SbtTask[IO]
      val rslt: SbtTask.Result[IO, Unit] = for {
        _ <- sbtTask.fromGitTask(git.fetchTags(baseDir))
        tags <- sbtTask.fromGitTask(git.getTag(baseDir))
        _ <- sbtTask.toLeftWhen(
          !tags.contains(tagName.value)
          , SbtTaskError.gitTaskError(
            s"tag ${tagName.value} does not exist. tags: ${tags.mkString("[", ",", "]")}"
          )
        )
        _ <- sbtTask.toLeftWhen(
          uploadArtifacts && assets.isEmpty
          , SbtTaskError.noFileFound(
            "devOopsCopyReleasePackages (uploadArtifacts is true)"
            , artifacts
          )
        )
        oauth <- sbtTask.eitherTWithWriter(
          effectOf[IO](
            getGitHubAuthToken(authTokenEnvVar, authTokenFile)
              .leftMap(SbtTaskError.gitHubTaskError
              )
          )
        )(
          _ => List(SbtTaskResult.gitHubTaskResult("Get GitHub OAuth token"))
        )
        _ <- sbtTask.handleGitHubTask(
          runGitHubRelease(
            tagName
            , assets
            , baseDir
            , ChangelogLocation(changelogLocation.value)
            , Repository(gitTagPushRepo.value)
            , oauth
          )
        )
      } yield ()
      sbtTask.handleSbtTask(
        rslt.value.run
      ).unsafeRunSync()
    }
  , gitTagAndGitHubRelease := {
      lazy val tagName = TagName(gitTagName.value)
      lazy val tagDesc = gitTagDescription.value
      lazy val tagFrom = BranchName(gitTagFrom.value)
      lazy val uploadArtifacts = artifactsRequiredForGitHubRelease.value
      lazy val assets = devOopsCopyReleasePackages.value
      lazy val authTokenEnvVar = gitHubAuthTokenEnvVar.value
      lazy val authTokenFile = gitHubAuthTokenFile.value
      lazy val baseDir = baseDirectory.value
      lazy val pushRepo = Repository(gitTagPushRepo.value)
      lazy val projectVersion = version.value

      SbtTask[IO].handleSbtTask(
        (for {
          _ <- SbtTask[IO].toLeftWhen(
                uploadArtifacts && assets.isEmpty
              , SbtTaskError.noFileFound(
                "devOopsCopyReleasePackages (uploadArtifacts is true)"
                , devOopsPackagedArtifacts.value
              )
            )
          oauth <- SbtTask[IO].eitherTWithWriter(
            effectOf[IO](getGitHubAuthToken(authTokenEnvVar, authTokenFile)
              .leftMap(SbtTaskError.gitHubTaskError))
            )(
              _ => List(SbtTaskResult.gitHubTaskResult("Get GitHub OAuth token"))
            )
          _ <- getTagVersion[IO](baseDir, tagFrom, tagName, tagDesc, pushRepo, projectVersion)
          _ <-  SbtTask[IO].handleGitHubTask(
              runGitHubRelease(
                  tagName
                , assets
                , baseDir
                , ChangelogLocation(changelogLocation.value)
                , pushRepo
                , oauth
                )
            )
        } yield ()).value.run
      ).unsafeRunSync()
    }
  )

  private def getTagVersion[F[_]: EffectConstructor: CanCatch: Monad](
      basePath: File
    , tagFrom: BranchName
    , tagName: TagName
    , gitTagDescription: Option[String]
    , pushRepo: Repository
    , projectVersion: String
    ): SbtTask.Result[F, Unit] =
      for {
        projectVersion <- SbtTask[F].fromNonSbtTask(
          effectOf(SemVer.parse(projectVersion)
            .leftMap(SbtTaskError.semVerFromProjectVersionParseError(projectVersion, _)))
        )(
          semVer => List(SbtTaskResult.nonSbtTaskResult(
            s"The semantic version from the project version has been parsed. version: ${SemVer.render(semVer)}")
          )
        )
        _ <- SbtTask[F].toLeftWhen(
          projectVersion.pre.isDefined || projectVersion.buildMetadata.isDefined
          , SbtTaskError.versionNotEligibleForTagging(projectVersion)
        )
        currentBranchName <- SbtTask[F].fromGitTask(Git[F].currentBranchName(basePath))
        _ <- SbtTask[F].toLeftWhen(
          currentBranchName.value =!= tagFrom.value
          , SbtTaskError.gitTaskError(s"current branch does not match with $tagFrom")
        )
        fetchResult <- SbtTask[F].fromGitTask(Git[F].fetchTags(basePath))
        tagResult <- SbtTask[F].fromGitTask(
          gitTagDescription
            .fold(
              Git[F].tag(tagName, basePath)
            ) { desc =>
              Git[F].tagWithDescription(
                tagName
                , Git.Description(desc)
                , basePath
              )
            }
        )
        pushResult <- SbtTask[F].fromGitTask(Git[F].pushTag(pushRepo, tagName, basePath))
      } yield ()

  private def getGitHubAuthToken(
      envVarName: String
    , authTokenFile: Option[File]
    ): Either[GitHubError, OAuthToken] =
      sys.env.get(envVarName)
        .fold(readOAuthToken(authTokenFile))(token => OAuthToken(token).asRight)

  private def runGitHubRelease[F[_]: EffectConstructor: CanCatch: Monad](
      tagName: TagName
    , assets: Vector[File]
    , baseDir: File
    , changelogLocation: ChangelogLocation
    , gitTagPushRepo: Repository
    , oAuthToken: OAuthToken
    ): GitHubTask.GitHubTaskResult[F, Unit] =
      for {
        changelog <- SbtTask[F].eitherTWithWriter(
            effectOf[F](getChangelog(new File(baseDir, changelogLocation.changeLogLocation), tagName))
        )(
            _ => List("Get changelog")
          )
        url <- GitHubTask[F].fromGitTask(
            Git[F].getRemoteUrl(gitTagPushRepo, baseDir)
          )
        repo <- SbtTask[F].eitherTWithWriter(
            effectOf(getRepoFromUrl(url))
          )(
            r => List(s"Get GitHub repo org and name: ${Repo.repoNameString(r)}")
          )
        gitHub <- SbtTask[F].eitherTWithWriter(
            TheGitHubApi[F].connectWithOAuth(oAuthToken)
          )(
            _ => List("Connect GitHub with OAuth")
          )
        gitHubRelease <- SbtTask[F].eitherTWithWriter(
          TheGitHubApi[F].release(
              gitHub
            , repo
            , tagName
            , changelog
            , assets
            ))(
              release =>
                List[String](
                    s"GitHub release: ${release.tagName.value}"
                  , if (release.releasedFiles.isEmpty)
                      "No files to upload"
                    else
                      release.releasedFiles.mkString("Files uploaded:\n    - ", "\n    - ", "")
                  , release.changelog.changelog.split("\n")
                      .mkString("Changelog uploaded:\n    ", "\n    ", "\n")
                  )
            )
      } yield ()

  // $COVERAGE-ON$
}