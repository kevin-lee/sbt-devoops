package io.kevinlee.sbt.devoops

import io.kevinlee.git.Git
import io.kevinlee.git.Git.{BranchName, TagName}
import io.kevinlee.sbt.devoops.data.SbtTask
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
      val tagFrom = BranchName(gitTagFrom.value)
      SbtTask.handleGitCommandTask(
        Git.doIfCurrentBranchMatches(tagFrom, basePath) {
          val tagName = TagName(gitTagName.value)
          Git.fetchTags(basePath)
            .right.map(r => Vector(r))
            .right.flatMap { resultAcc =>
              gitTagDescription.value
                .fold(
                  Git.tag(tagName, basePath)
                ) { desc =>
                  Git.tagWithDescription(
                    tagName
                  , Git.Description(desc)
                  , baseDirectory.value
                  )
                }.right.map(r => resultAcc :+ r)
            }
        }
      )
    }
  )

  // $COVERAGE-ON$

}