package devoops

import sbt._
import sbt.Keys._
import cats.Eq
import cats.syntax.eq.catsSyntaxEq

/** @author Kevin Lee
  * @since 2021-04-10
  */
object DevOopsSbtExtraPlugin extends AutoPlugin {

  override def requires: Plugins      = empty
  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    implicit val projectRefEq: Eq[ProjectRef] = Eq.fromUniversalEquals[ProjectRef]

    val rootProjectRef: Def.Initialize[ProjectRef] = Def.setting {
      val rootBuildUri  = Keys.loadedBuild.value.root
      val rootProjectId = Keys.loadedBuild.value.units(rootBuildUri).root
      ProjectRef(rootBuildUri, rootProjectId)
    }

    val currentProjectRef: Def.Initialize[Task[ProjectRef]] = Def.task(Project.current(Keys.state.value))

    val isRootProject: SettingKey[Boolean] = settingKey[Boolean]("Check if this project is the root project.")

    val isCurrentProject: TaskKey[Boolean] = taskKey[Boolean](
      "Check if this project is currently selected project."
    )

    val isCurrentProjectRoot: TaskKey[Boolean] = taskKey[Boolean](
      "Check if this project is the root project as well as currently selected."
    )

    val commonSettings: SettingsDefinition = Def.settings(
      isRootProject := Keys.thisProjectRef.value === rootProjectRef.value,
      isCurrentProject := Keys.thisProjectRef.value === currentProjectRef.value,
      isCurrentProjectRoot := isCurrentProject.value && isRootProject.value,
    )

    lazy val noPublish: SettingsDefinition = Seq(
      publish := {},
      publishM2 := {},
      publishLocal := {},
      publishArtifact := false,
      sbt.Keys.`package` / skip := true,
      packagedArtifacts / skip := true,
      publish / skip := true,
    )

    lazy val noDoc: SettingsDefinition = Seq(
      Compile / doc / sources := Seq.empty[File],
      Compile / packageDoc / publishArtifact := false,
    )
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = commonSettings

}
