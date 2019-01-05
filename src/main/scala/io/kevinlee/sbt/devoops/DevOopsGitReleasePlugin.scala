package io.kevinlee.sbt.devoops

import io.kevinlee.git.{Git, GitCommandError, GitCommandResult}
import io.kevinlee.git.Git.{BranchName, TagName}
import io.kevinlee.sbt.devoops.errors.SbtTaskError
import io.kevinlee.semver.SemanticVersion
import io.kevinlee.CommonPredef._

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

  , gitTagName := decideVersion(version.value, v => s"v${SemanticVersion.parseUnsafe(v).render}")
  , gitTag := {
      val basePath = baseDirectory.value
      Git.currentBranchName(basePath) match {
        case Right(GitCommandResult.GitCurrentBranchName(BranchName(branchName))) =>
          val tagFrom = gitTagFrom.value
          val tagName = gitTagName.value
          if (branchName === tagFrom) {
            (gitTagDescription.value match {
              case Some(desc) =>
                Git.tagWithDescription(
                  TagName(tagName)
                , Git.Description(desc)
                , baseDirectory.value
                )
              case None =>
                Git.tag(TagName(tagName), baseDirectory.value)
            }) match {
              case Right(result) =>
                println(GitCommandResult.render(result))
              case Left(error) =>
                sys.error(GitCommandError.render(error))
            }
          } else {
            sys.error(
              SbtTaskError.render(
                SbtTaskError.gitTaskError(
                s"The current branch is not the same as the release branch set in gitTagFrom: $gitTagFrom"
                )
              )
            )
          }
        case Right(gcr) =>
          sys.error(s"Unexpected result. Expected: GitCurrentBranchName / actual: $gcr")

        case Left(error )=>
          sys.error(GitCommandError.render(error))

      }
    }
  )

  // $COVERAGE-ON$

}