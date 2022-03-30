package devoops.data

import sbt._

import scala.concurrent.duration.FiniteDuration

/** @author Kevin Lee
  * @since 2021-02-14
  */
trait GitHubReleaseKeys {

  lazy val devOopsLogLevel: SettingKey[String] = settingKey(
    "Log level for DevOops tasks. It can be one of debug, info, warn and error (default: info)"
  )

  lazy val devOopsGitTagFrom: SettingKey[String] = settingKey[String]("The name of branch to tag from. (Default: main)")

  lazy val devOopsGitTagDescription: SettingKey[Option[String]] = settingKey[Option[String]](
    "description for git tagging (Default: None)"
  )

  lazy val devOopsGitTagName: TaskKey[String] = taskKey[String](
    """git tag name (default: parse the project version as semantic version and render with the prefix 'v'. e.g.) version := "1.0.0" / gitTagName := "v1.0.0""""
  )

  lazy val devOopsGitTagPushRepo: TaskKey[String] = taskKey[String](
    "The name of Git repo to push the tag (default: origin)"
  )

  lazy val devOopsGitTag: TaskKey[Unit] = taskKey[Unit]("task to create a git tag from the branch set in gitTagFrom")

  lazy val devOopsCiDir: SettingKey[String] = settingKey[String](
    "The ci directory which contains the files created in build to upload to GitHub release (e.g. packaged jar files) It can be either an absolute or relative path. (default: ci)"
  )

  lazy val devOopsArtifactNamePrefix: SettingKey[String] = settingKey[String](
    "The artifact filename prefix used in the default devOopsPackagedArtifacts (default: devOopsArtifactNamePrefix := name.value)"
  )

  lazy val devOopsPackagedArtifacts: TaskKey[List[String]] = taskKey(
    s"""A list of packaged artifacts to be copied to PROJECT_HOME/$${devOopsCiDir.value}/dist
       |  - default:
       |     List(
       |       s"target/scala-*/$${devOopsArtifactNamePrefix.value}*.jar", // root project
       |       s"*/target/scala-*/$${devOopsArtifactNamePrefix.value}*.jar", // sub-projects
       |       s"*/*/target/scala-*/$${devOopsArtifactNamePrefix.value}*.jar", // Scala.js projects (sub/js/target/..., sub/jvm/target/...)
       |     )
       |""".stripMargin
  )

  lazy val devOopsCopyReleasePackages: TaskKey[Vector[File]] = taskKey[Vector[File]](
    s"task to copy packaged artifacts to the location specified (default: devOopsPackagedArtifacts.value to PROJECT_HOME/$${devOopsCiDir.value}/dist"
  )

  lazy val devOopsChangelogLocation: SettingKey[String] = settingKey[String](
    "The location of changelog file. (default: PROJECT_HOME/changelogs)"
  )

  lazy val devOopsGitHubAuthTokenEnvVar: SettingKey[String] = settingKey[String](
    "The environment variable name for GitHub auth token (default: GITHUB_TOKEN)"
  )

  lazy val devOopsGitHubAuthTokenFile: SettingKey[Option[File]] = settingKey[Option[File]](
    "The path to GitHub OAuth token file. The file should contain oauth=OAUTH_TOKEN (default: Some($USER/.github)) If you want to get the file in user's home, do Some(new File(Io.getUserHome, \".github\"))"
  )

  lazy val devOopsGitHubRequestTimeout: TaskKey[FiniteDuration] = taskKey[FiniteDuration](
    "Timeout value for any request sent to GitHub (default: 2.minutes)"
  )

  lazy val devOopsGitHubRelease: TaskKey[Unit] = taskKey[Unit](
    "Release the current version without creating a tag. It also uploads the changelog to GitHub."
  )

  lazy val devOopsGitTagAndGitHubRelease: TaskKey[Unit] = taskKey[Unit](
    "Release the current version. It creates a tag with the project version and uploads the changelog to GitHub."
  )

  lazy val devOopsGitHubReleaseUploadArtifacts: TaskKey[Unit] = taskKey[Unit](
    "Upload the packaged files to the GitHub release with the current version. The tag with the project version and the GitHub release of it should exist to run this task."
  )
}
