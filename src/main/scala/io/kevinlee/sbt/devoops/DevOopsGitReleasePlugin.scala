package io.kevinlee.sbt.devoops

import io.kevinlee.git.{Git, GitCommandResult, GitCommandError}
import io.kevinlee.git.Git.{BranchName, TagName}
import io.kevinlee.semver.SemanticVersion
import sbt.Keys._
import sbt.{AutoPlugin, PluginTrigger, Plugins, Setting, SettingKey, TaskKey, settingKey, taskKey}

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
      settingKey[Option[String]]("")

    lazy val gitTagName: TaskKey[String] =
      taskKey[String]("")

    lazy val gitTag: TaskKey[Unit] =
      taskKey[Unit]("task to create a git tag from the branch set in gitTagFrom")

    def decideVersion(projectVersion: String, decide: String => String): String =
      decide(projectVersion)

  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    gitTagFrom := "release"

  , gitTagName := decideVersion(version.value, v => s"v${SemanticVersion.parseUnsafe(v)}")
  , gitTag := (
      // FIXME: it should tag if the current branch is the same as gitTagFrom.value instead of doing checkout.
      Git.checkout(BranchName(gitTagFrom.value), baseDirectory.value) match {
      case Right(_) =>
        (gitTagDescription.value match {
          case Some(desc) =>
            Git.tagWithDescription(
              TagName(gitTagName.value)
            , Git.Description(desc)
            , baseDirectory.value
            )
          case None =>
            Git.tag(TagName(gitTagFrom.value), baseDirectory.value)
        }) match {
          case Right(result) =>
          println(GitCommandResult.render(result))
          case Left(error) =>
          sys.error(GitCommandError.render(error))
        }
      case Left(error )=>
        sys.error(GitCommandError.render(error))

    })
  )

  // $COVERAGE-ON$

}