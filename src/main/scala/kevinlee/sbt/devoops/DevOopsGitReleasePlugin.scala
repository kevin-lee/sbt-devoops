package kevinlee.sbt.devoops

import kevinlee.git.Git
import kevinlee.git.Git.{BranchName, Repository, TagName}
import kevinlee.sbt.devoops.data.SbtTask
import kevinlee.sbt.io.{CaseSensitivity, Io}
import kevinlee.semver.SemanticVersion
import sbt.Keys._
import sbt.{AutoPlugin, File, PluginTrigger, Plugins, Setting, SettingKey, TaskKey, settingKey, taskKey}

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

    lazy val copyPackages: TaskKey[Seq[File]] =
      taskKey[Seq[File]](s"task to copy packaged artifacts to the location specified (default: target/scala-*/$${name.value}*.jar to PROJECT_HOME/cd")

    def decideVersion(projectVersion: String, decide: String => String): String =
      decide(projectVersion)

  }

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
      SbtTask.handleGitCommandTask(
        for {
          currentBranchCheckResults <- Git.checkIfCurrentBranchIsSame(tagFrom, basePath).right
          fetchResult <- Git.fetchTags(basePath).right
          tagResult <- gitTagDescription.value
                        .fold(
                          Git.tag(tagName, basePath)
                        ) { desc =>
                          Git.tagWithDescription(
                            tagName
                            , Git.Description(desc)
                            , baseDirectory.value
                          )
                        }.right
          pushResult <- Git.pushTag(pushRepo, tagName, basePath).right
        } yield currentBranchCheckResults :+ fetchResult :+ tagResult :+ pushResult
      )
    }
  , copyPackages := {
      val projectBaseDir = baseDirectory.value
      val files = Io.findAllFiles(
          CaseSensitivity.caseSensitive
        , projectBaseDir
        , List(
            s"target/scala-*/${name.value}*.jar"
          )
      )
      if (files.isEmpty) {
        println("No files were found so nothing has been copied.")
        Vector.empty[File]
      } else {
        val copied = Io.copy(files, new File(projectBaseDir, "cd"))
        println(
          s""">> copyPackages - Files copied from:
             |${files.mkString("  - ",  "\n  - ", "\n")}
             |  to
             |${copied.mkString("  - ",  "\n  - ", "\n")}
             |""".stripMargin)
        copied
      }
    }
  )

  // $COVERAGE-ON$

}