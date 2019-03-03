package kevinlee.sbt.devoops

import kevinlee.git.Git
import kevinlee.git.Git.{BranchName, Repository, TagName}

import kevinlee.sbt.devoops.data.{SbtTask, SbtTaskError}
import kevinlee.sbt.io.{CaseSensitivity, Io}

import kevinlee.semver.SemanticVersion

import sbt.Keys._
import sbt.MessageOnlyException
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

    lazy val devOopsCiDir: SettingKey[String] = settingKey[String]("The ci directory which contains the files created in build to upload to GitHub release (e.g. packaged jar files) It can be either an absolute or relative path. (default: ci)")

    lazy val devOopsCopyReleasePackages: TaskKey[Vector[File]] =
      taskKey[Vector[File]](s"task to copy packaged artifacts to the location specified (default: target/scala-*/$${name.value}*.jar to PROJECT_HOME/$${devOopsCiDir.value}/dist")

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
  )

  // $COVERAGE-ON$

}