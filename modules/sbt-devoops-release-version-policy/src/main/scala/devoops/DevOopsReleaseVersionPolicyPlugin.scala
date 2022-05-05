package devoops

import extras.scala.io.syntax.color._
import kevinlee.sbt.SbtCommon._
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport._
import sbtversionpolicy.SbtVersionPolicyPlugin
import sbtversionpolicy.SbtVersionPolicyPlugin.autoImport._

/** @author Kevin Lee
  * @since 2022-05-01
  */
object DevOopsReleaseVersionPolicyPlugin extends AutoPlugin {
  override def requires: Plugins      = SbtVersionPolicyPlugin && ReleasePlugin
  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val CompatibilityFilename: String = "compatibility.sbt"

    val CompatibilityFileContent: String =
      "ThisBuild / versionPolicyIntention := Compatibility.BinaryAndSourceCompatible\n"

    val CompatibilityFileAdditionalContentForFirstRelease: String =
      """>> If this project has never been released, use the following one instead.
        |ThisBuild / versionPolicyIntention := Compatibility.None
        |""".stripMargin

    val DefaultCompatibilityResetGitCommitMessage: String = "Reset compatibility intention"

    lazy val compatibilityResetGitCommitMessage: SettingKey[String] = settingKey[String](
      s"A message used to commit the compatibility intention reset. (default: $DefaultCompatibilityResetGitCommitMessage)"
    )

    lazy val setAndCommitNextCompatibilityIntention: TaskKey[Unit] =
      taskKey[Unit]("Set versionPolicyIntention to Compatibility.BinaryAndSourceCompatible, and commit the change")

  }

  import autoImport._

  private def errorWithCompatibilityFileSetupInstruction(): Nothing =
    messageOnlyException(s""">> versionPolicyIntention is not set. To set it,
                            |>> please add the '${CompatibilityFilename.blue}' file to the root of the project.
                            |>> The content of the file should be the following line
                            |${CompatibilityFileContent.blue}
                            |$CompatibilityFileAdditionalContentForFirstRelease
                            |""".stripMargin)

  override lazy val buildSettings: Seq[Setting[_]] = Seq(
    versionPolicyIntention                 := (ThisBuild / versionPolicyIntention)
      .?
      .value
      .getOrElse(errorWithCompatibilityFileSetupInstruction()),
    compatibilityResetGitCommitMessage     := DefaultCompatibilityResetGitCommitMessage,
    setAndCommitNextCompatibilityIntention := {
      val log           = streams.value.log
      val intention     = (ThisBuild / versionPolicyIntention)
        .?
        .value
        .getOrElse(errorWithCompatibilityFileSetupInstruction())
      val commitMessage = (ThisBuild / compatibilityResetGitCommitMessage).value
      intention match {
        case Compatibility.BinaryAndSourceCompatible =>
          log.info("Not changing compatibility intention because it is already set to BinaryAndSourceCompatible")

        case Compatibility.BinaryCompatible | Compatibility.None =>
          log.info("Reset compatibility intention to BinaryAndSourceCompatible")
          IO.write(
            new File(CompatibilityFilename),
            CompatibilityFileContent,
          )
          val gitAddExitValue = sys
            .process
            .Process(s"git add $CompatibilityFilename")
            .run(log)
            .exitValue()
          @SuppressWarnings(Array("org.wartremover.warts.Equals"))
          val gitAddSuccess   = gitAddExitValue == 0
          assertOrMessageOnlyException(gitAddSuccess, s"Command failed with exit status $gitAddExitValue")

          val gitCommitExitValue =
            sys
              .process
              .Process(
                List(
                  "git",
                  "commit",
                  "-m",
                  commitMessage,
                )
              )
              .run(log)
              .exitValue()
          @SuppressWarnings(Array("org.wartremover.warts.Equals"))
          val gitCommitSuccess   = gitCommitExitValue == 0
          assertOrMessageOnlyException(gitCommitSuccess, s"Command failed with exit status $gitCommitExitValue")
      }
    },
  )

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    releaseVersion := {
      val maybeBump: Option[sbtrelease.Version.Bump] = versionPolicyIntention
        .?
        .value
        .getOrElse(errorWithCompatibilityFileSetupInstruction()) match {
        case Compatibility.None => Some(sbtrelease.Version.Bump.Major)
        case Compatibility.BinaryCompatible => Some(sbtrelease.Version.Bump.Minor)
        /* No need to bump the patch version, because it has already been bumped when sbt-release set the next release version */
        case Compatibility.BinaryAndSourceCompatible => None
      }

      { (currentVersion: String) =>
        val versionWithoutQualifier =
          sbtrelease
            .Version(currentVersion)
            .getOrElse(sbtrelease.versionFormatError(currentVersion))
            .withoutQualifier
        (maybeBump match {
          case Some(bump) => versionWithoutQualifier.bump(bump)
          case None => versionWithoutQualifier
        }).string
      }
    },
    /* Custom release process: run `versionCheck` after we have set the release version, and
     * reset compatibility intention to `Compatibility.BinaryAndSourceCompatible` after the release.
     */
    releaseProcess := {
      Seq[ReleaseStep](
        ReleaseTransformations.checkSnapshotDependencies,
        ReleaseTransformations.inquireVersions,
        ReleaseTransformations.runClean,
        ReleaseTransformations.runTest,
        ReleaseTransformations.setReleaseVersion,
        releaseStepCommand("versionCheck"), // Run task `versionCheck` after the release version is set
        ReleaseTransformations.commitReleaseVersion,
        ReleaseTransformations.tagRelease,
        ReleaseTransformations.publishArtifacts, // Publish locally for our tests only, in practice you will publish to Sonatype
        ReleaseTransformations.setNextVersion,
        ReleaseTransformations.commitNextVersion,
        releaseStepTask(
          /* Reset compatibility intention to `Compatibility.BinaryAndSourceCompatible` */
          setAndCommitNextCompatibilityIntention
        ),
        ReleaseTransformations.pushChanges,
      )
    },
  )
}
