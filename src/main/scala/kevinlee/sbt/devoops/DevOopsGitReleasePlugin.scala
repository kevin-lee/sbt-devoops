package kevinlee.sbt.devoops

import java.io.FileInputStream

import kevinlee.git.Git
import kevinlee.git.Git.{BranchName, RepoUrl, Repository, TagName}
import kevinlee.github.data._
import kevinlee.sbt.devoops.data.{SbtTask, SbtTaskError}
import kevinlee.sbt.io.{CaseSensitivity, Io}
import kevinlee.semver.SemanticVersion
import kevinlee.CommonPredef._
import kevinlee.github.{GitHubApi, GitHubTask}

import sbt.Keys._
import sbt.{AutoPlugin, File, MessageOnlyException, PluginTrigger, Plugins, Setting, SettingKey, TaskKey, settingKey, taskKey}

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object DevOopsGitReleasePlugin extends AutoPlugin {

  // $COVERAGE-OFF$
  override def requires: Plugins = empty
  override def trigger: PluginTrigger = noTrigger

  object autoImport {
    lazy val gitTagFrom: SettingKey[String] =
      settingKey[String]("The name of branch to tag from. [Default: release]")

    lazy val gitTagDescription: SettingKey[Option[String]] =
      settingKey[Option[String]]("description for git tagging")

    lazy val gitTagName: TaskKey[String] =
      taskKey[String]("""git tag name (default: parse the project version as semantic version and render with the prefix 'v'. e.g.) version := "1.0.0" / gitTagName := "v1.0.0"""")

    lazy val gitTagPushRepo: TaskKey[String] =
      taskKey[String]("The name of Git repo to push the tag (default: origin)")


    lazy val gitTag: TaskKey[Unit] =
      taskKey[Unit]("task to create a git tag from the branch set in gitTagFrom")

    lazy val devOopsCiDir: SettingKey[String] = settingKey[String]("The ci directory which contains the files created in build to upload to GitHub release (e.g. packaged jar files) It can be either an absolute or relative path. (default: ci)")

    lazy val devOopsCopyReleasePackages: TaskKey[Vector[File]] =
      taskKey[Vector[File]](s"task to copy packaged artifacts to the location specified (default: target/scala-*/$${name.value}*.jar to PROJECT_HOME/$${devOopsCiDir.value}/dist")

    lazy val changelogLocation: SettingKey[String] =
      settingKey[String]("The location of changelog file. (default: PROJECT_HOME/changelogs)")

    lazy val gitHubAuthTokenFile: SettingKey[File] =
      settingKey[File]("The path to GitHub OAuth token file. The file should contain oauth=OAUTH_TOKEN (default: $USER/.github) If you want to get the file in user's home, use new File(Io.getUserHome, \".github\")")

    lazy val gitHubRelease: TaskKey[Unit] =
      taskKey[Unit]("Release the current version meaning upload the packaged files and changelog to GitHub.")

    def decideVersion(projectVersion: String, decide: String => String): String =
      decide(projectVersion)

    def copyFiles(
      caseSensitivity: CaseSensitivity
    , projectBaseDir: File
    , filePaths: List[String]
    , targetDir: File
    ): Either[SbtTaskError, Vector[File]] = {
      val files = Io.findAllFiles(
          caseSensitivity
        , projectBaseDir
        , filePaths
      )
      if (files.isEmpty) {
        Left(SbtTaskError.noFileFound("copying files", filePaths))
      } else {
        val copied = Io.copy(files, targetDir)
        println(
          s""">> copyPackages - Files copied from:
             |${files.mkString("  - ",  "\n  - ", "\n")}
             |  to
             |${copied.mkString("  - ",  "\n  - ", "\n")}
             |""".stripMargin)
        Right(copied)
      }
    }

    def readOAuthToken(file: File): Either[GitHubError, OAuthToken] = {
      val props = new java.util.Properties()
      props.load(new FileInputStream(file))
      Option(props.getProperty("oauth"))
        .fold[Either[GitHubError, OAuthToken]](Left(GitHubError.noCredential))(token => Right(OAuthToken(token)))
    }

    def getRepoFromUrl(repoUrl: RepoUrl): Either[GitHubError, Repo] = {
      val names =
        if (repoUrl.repoUrl.startsWith("http"))
          repoUrl.repoUrl.split("/")
        else
          repoUrl.repoUrl.split(":").last.split("/")
      names.takeRight(2) match {
        case Array(org, name) =>
          Right(Repo(RepoOrg(org), RepoName(name.stripSuffix(".git"))))
        case _ =>
          Left(GitHubError.invalidGitHubRepoUrl(repoUrl))
      }
    }

    def getChangelog(dir: File, tagName: TagName): Either[GitHubError, Changelog] = {
      val changelogName = s"${tagName.value.stripPrefix("v")}.md"
      // baseDirectory.value, changelogLocation.value)
      val changelog = new File(dir, changelogName)
      if (!changelog.exists) {
        Left(GitHubError.changelogNotFound(changelog.getCanonicalPath, tagName))
      } else {
        val log = scala.io.Source.fromFile(changelog).getLines().mkString("\n")
        Right(Changelog(log))
      }
    }

  }

  import SbtTask.{toLeftWhen, fromGitTask, eitherTWithHistoryStrings}
  import autoImport._


  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    gitTagFrom := "release"

  , gitTagName := decideVersion(version.value, v => s"v${SemanticVersion.parseUnsafe(v).render}")
  , gitTagPushRepo := "origin"
  , gitTag := {
      val basePath = baseDirectory.value
      val tagFrom = BranchName(gitTagFrom.value)
      val tagName = TagName(gitTagName.value)
      val pushRepo = Repository(gitTagPushRepo.value)
      SbtTask.handleSbtTask(
        (for {
          currentBranchName <- fromGitTask(Git.currentBranchName(basePath))
          _ <- toLeftWhen(
            currentBranchName.value !== tagFrom.value
          , SbtTaskError.gitTaskError(s"current branch does not match with $tagFrom")
          )
          fetchResult <- fromGitTask(Git.fetchTags(basePath))
          tagResult <-
            fromGitTask(
              gitTagDescription.value
                .fold(
                  Git.tag(tagName, basePath)
                ) { desc =>
                  Git.tagWithDescription(
                      tagName
                    , Git.Description(desc)
                    , baseDirectory.value
                  )
                }
            )
          pushResult <- fromGitTask(Git.pushTag(pushRepo, tagName, basePath))
        } yield ()).run.run
      )
    }
  , devOopsCiDir := "ci"
  , devOopsCopyReleasePackages := {
      @SuppressWarnings(Array("org.wartremover.warts.Throw"))
      val result: Vector[File] = copyFiles(
          CaseSensitivity.caseSensitive
        , baseDirectory.value
        , List(s"target/scala-*/${name.value}*.jar")
        , new File(new File(devOopsCiDir.value), "dist")
        ) match {
          case Left(error) =>
            throw new MessageOnlyException(SbtTaskError.render(error))
          case Right(files) =>
            files
        }
      result
    }
  , changelogLocation := "changelogs"
  , gitHubAuthTokenFile :=
      new File(Io.getUserHome, ".github")
  , gitHubRelease := {
      val tagName = TagName(gitTagName.value)
      val assets = devOopsCopyReleasePackages.value
      gitTag.value
      SbtTask.handleGitHubTask(
        (for {
          changelog <- eitherTWithHistoryStrings(
              getChangelog(new File(baseDirectory.value, changelogLocation.value), tagName)
            )(_ => List("Get changelog"))
          url <- GitHubTask.fromGitTask(
              Git.getRemoteUrl(Repository(gitTagPushRepo.value), baseDirectory.value)
            )
          repo <- eitherTWithHistoryStrings(getRepoFromUrl(url)
            )(r => List(s"Get GitHub repo org and name: ${Repo.repoNameString(r)}"))
          oauth <- eitherTWithHistoryStrings(
              readOAuthToken(gitHubAuthTokenFile.value)
            )(_ => List("Get GitHub OAuth token"))
          gitHub <- eitherTWithHistoryStrings(
              GitHubApi.connectWithOAuth(oauth)
            )(_ => List("Connect GitHub with OAuth"))
          gitHubRelease <- eitherTWithHistoryStrings(
              GitHubApi.release(
                gitHub
              , repo
              , tagName
              , changelog
              , assets
              )
            )(release =>
              List[String](
                  s"GitHub release: ${release.tagName.value}"
                , release.releasedFiles.mkString("Files uploaded:\n    - ", "\n    - ", "")
                , release.changelog.changelog.split("\n").mkString("Changelog uploaded:\n    ", "\n    ", "\n")
              )
            )
        } yield ()).run.run
      )
    }
  )

  // $COVERAGE-ON$

}