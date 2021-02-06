package kevinlee.sbt.devoops

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

/** @author Kevin Lee
  * @since 2018-12-30
  */
object DevOopsJavaPlugin extends AutoPlugin {
  // $COVERAGE-OFF$

  override def requires: JvmPlugin.type = plugins.JvmPlugin

  /**  To use this plugin, add the following line to `build.sbt`.
    * {{{
    * enablePlugins(DevOopsJavaPlugin)
    * }}}
    */
  override def trigger: PluginTrigger = noTrigger

  object autoImport {

    lazy val javaVersion: SettingKey[String] = settingKey[String]("The Java version for the Java Project")

  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    javaVersion := "1.8"
    /*
     * crossPaths and autoScalaLibrary should be false for Java project.
     */,
    crossPaths := false,
    autoScalaLibrary := false,
    javacOptions ++= Seq(
      "-source",
      javaVersion.value,
      "-encoding",
      "UTF-8",
    ),
    javacOptions in (Compile, compile) ++= Seq(
      "-target",
      javaVersion.value,
      "-Xlint:unchecked",
    ),
  )

  // $COVERAGE-ON$
}
