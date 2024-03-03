package devoops

import just.semver.{ParseError, SemVer}
import org.typelevel.sbt.tpolecat.TpolecatPlugin
import sbt.Keys.*
import sbt.*

/** @author Kevin Lee
  * @since 2018-12-29
  */
object DevOopsScalaPlugin extends AutoPlugin {

  // $COVERAGE-OFF$
  override def requires: Plugins = plugins.JvmPlugin && TpolecatPlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport {}

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    updateOptions := updateOptions.value.withCircularDependencyLevel(CircularDependencyLevel.Error),
    scalacOptions := {
      val currentOptions = scalacOptions.value
      (SemVer.parse(scalaVersion.value) match {
        case Right(SemVer(SemVer.Major(2), SemVer.Minor(13), SemVer.Patch(patch), _, _)) if patch >= 3 =>
          (currentOptions ++ Seq("-Ymacro-annotations")).distinct
        case _ =>
          currentOptions
      })
    },
    libraryDependencies ++= (SemVer.parse(scalaVersion.value) match {
      case Right(SemVer(SemVer.Major(2), SemVer.Minor(10), SemVer.Patch(7), _, _)) =>
        Seq("com.milessabin" % "si2712fix-plugin_2.10.7" % "1.2.0" % "plugin->default(compile)")
      case Right(_: SemVer) =>
        Seq.empty[ModuleID]
      case Left(error) =>
        sLog
          .value
          .warn(
            "Parsing scalaVersion failed when setting up partial-unification\n" +
              s"Parse failure info: ${ParseError.render(error)}"
          )
        Seq.empty[ModuleID]
    }),
    libraryDependencies ++= {
      val scalaV = scalaVersion.value
      if (scalaV.startsWith("3")) {
        List.empty[ModuleID]
      } else {
        val semVer = SemVer.parseUnsafe(scalaV)
        ((semVer.major, semVer.minor, semVer.patch) match {
          case (SemVer.Major(2), SemVer.Minor(13), SemVer.Patch(_)) |
              (SemVer.Major(2), SemVer.Minor(11), SemVer.Patch(12)) =>
            List(
              compilerPlugin("org.typelevel" % "kind-projector" % "0.13.3" cross CrossVersion.full),
            )
          case (SemVer.Major(2), SemVer.Minor(12), SemVer.Patch(patch)) if patch >= 8 =>
            List(
              compilerPlugin("org.typelevel" % "kind-projector" % "0.13.3" cross CrossVersion.full),
            )
          case (SemVer.Major(2), SemVer.Minor(10), SemVer.Patch(7)) =>
            List(
              compilerPlugin("org.typelevel" % "kind-projector" % "0.12.0" cross CrossVersion.full),
            )
          case (SemVer.Major(2), SemVer.Minor(13), _) | (SemVer.Major(2), SemVer.Minor(12), _) |
              (SemVer.Major(2), SemVer.Minor(11), _) | (SemVer.Major(2), SemVer.Minor(10), _) =>
            List(
              compilerPlugin("org.typelevel" % "kind-projector" % "0.10.3" cross CrossVersion.binary),
            )
          case (_, _, _) =>
            List.empty[ModuleID]
        }) ++ List(
          compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
        ) ++ (
          (semVer.major, semVer.minor, semVer.patch) match {
            case (SemVer.Major(2), SemVer.Minor(12), SemVer.Patch(patch)) if patch >= 2 =>
              List(
                compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
              )
            case (SemVer.Major(2), SemVer.Minor(11), SemVer.Patch(12)) |
                (SemVer.Major(2), SemVer.Minor(11), SemVer.Patch(11)) |
                (SemVer.Major(2), SemVer.Minor(10), SemVer.Patch(7)) =>
              List(
                compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
              )
            case (SemVer.Major(2), SemVer.Minor(12), SemVer.Patch(1)) |
                (SemVer.Major(2), SemVer.Minor(12), SemVer.Patch(0)) =>
              List(
                compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
              )
            case (SemVer.Major(2), SemVer.Minor(11), SemVer.Patch(patch)) if patch >= 0 && patch <= 10 =>
              List(
                compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
              )
            case (SemVer.Major(2), SemVer.Minor(10), SemVer.Patch(patch)) if patch >= 2 && patch <= 6 =>
              List(
                compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
              )
            case (_, _, _) =>
              List.empty[ModuleID]
          }
        )
      }
    },
  )

  // $COVERAGE-ON$
}
