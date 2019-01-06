package io.kevinlee.sbt.devoops

import io.kevinlee.CommonPredef._
import io.kevinlee.git.Git.{BranchName, TagName}
import io.kevinlee.git.{Git, GitCommandResult}
import io.kevinlee.sbt.devoops.data.{SbtTaskError, SbtTaskResult}
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

  , gitTagName := decideVersion(version.value, v => s"v${SemanticVersion.parseUnsafe(v).render}")
  , gitTag := {
      val basePath = baseDirectory.value
      (Git.currentBranchName(basePath) match {
        case Right(GitCommandResult.GitCurrentBranchName(BranchName(branchName))) =>
          // TODO: Probably fetch and merge (pull) before tagging?
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
                Right(SbtTaskResult.gitCommandTaskResult(result))
              case Left(error) =>
                Left(SbtTaskError.gitTaskGitCommandError(error))
            }
          } else {
            Left(SbtTaskError.gitTaskError(
              s"The current branch is not the same as the release branch set in gitTagFrom: $gitTagFrom"
            ))
          }
        case Right(gcr) =>
          Left(SbtTaskError.gitTaskError(s"Unexpected result. Expected: GitCurrentBranchName / actual: ${GitCommandResult.render(gcr)}"))

        case Left(error) =>
          Left(SbtTaskError.gitTaskGitCommandError(error))
      }) match {
        case Right(result) =>
          println(SbtTaskResult.render(result))
        case Left (error) =>
          sys.error(SbtTaskError.render(error))
      }
    }
  )

  // $COVERAGE-ON$

}