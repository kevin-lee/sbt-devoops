package kevinlee.sbt.devoops

import kevinlee.sbt.SbtCommon._
import kevinlee.semver.{Major, Minor, SemanticVersion}

import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbt.{AutoPlugin, CircularDependencyLevel, Compile, PluginTrigger, Setting, SettingKey, plugins, settingKey}

/**
  * @author Kevin Lee
  * @since 2018-12-29
  */
object DevOopsScalaPlugin extends AutoPlugin {

  // $COVERAGE-OFF$
  override def requires: JvmPlugin.type = plugins.JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    val essentialOptions: Seq[String] = Seq(
      "-language:higherKinds"             // Allow higher-kinded types
    , "-encoding", "UTF-8"                // Specify character encoding used by source files.
    )

    val defaultOptions: Seq[String] = Seq(
      "-deprecation"                      // Emit warning and location for usages of deprecated APIs.
    , "-unchecked"                        // Enable additional warnings where generated code depends on assumptions.
    , "-feature"                          // Emit warning and location for usages of features that should be imported explicitly.
    , "-Ywarn-value-discard"              // Warn when non-Unit expression results are unused.
    , "-Yno-adapted-args"                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    , "-Xfatal-warnings"                  // Fail the compilation if there are any warnings.
    , "-Ywarn-dead-code"                  // Warn when dead code is identified.
    , "-Ywarn-inaccessible"               // Warn about inaccessible types in method signatures.
    , "-Ywarn-nullary-override"           // Warn when non-nullary `def f()' overrides nullary `def f'.
    , "-Ywarn-numeric-widen"              // Warn when numerics are widened.
    )

    val scalacOptionsHigherThan2_11: Seq[String] = Seq(
      "-Xlint:constant"                   // Evaluation of a constant arithmetic expression results in an error.
    )

    val scalacOptionsHigherThan2_10: Seq[String] = Seq(
      "-Xlint:adapted-args"               // Warn if an argument list is modified to match the receiver.
    , "-Xlint:by-name-right-associative"  // By-name parameter of right associative operator.
    , "-Xlint:delayedinit-select"         // Selecting member of DelayedInit.
    , "-Xlint:doc-detached"               // A Scaladoc comment appears to be detached from its element.
    , "-Xlint:inaccessible"               // Warn about inaccessible types in method signatures.
    , "-Xlint:infer-any"                  // Warn when a type argument is inferred to be `Any`.
    , "-Xlint:missing-interpolator"       // A string literal appears to be missing an interpolator id.
    , "-Xlint:nullary-override"           // Warn when non-nullary `def f()' overrides nullary `def f'.
    , "-Xlint:nullary-unit"               // Warn when nullary methods return Unit.
    , "-Xlint:option-implicit"            // Option.apply used implicit view.
    , "-Xlint:package-object-classes"     // Class or object defined in package object.
    , "-Xlint:poly-implicit-overload"     // Parameterized overloaded implicit methods are not visible as view bounds.
    , "-Xlint:private-shadow"             // A private field (or class parameter) shadows a superclass field.
    , "-Xlint:stars-align"                // Pattern sequence wildcard must align with sequence component.
    , "-Xlint:type-parameter-shadow"      // A local type parameter shadows a type already in scope.
    , "-Xlint:unsound-match"              // Pattern match may not be typesafe.
    , "-Ywarn-unused-import"
    )

    val aggressiveScalacOptionsForHigherThan2_11: Seq[String] = Seq(
    )

    val aggressiveScalacOptionsForHigherThan2_10: Seq[String] = Seq(
      "-explaintypes"                     // Explain type errors in more detail.
    , "-language:existentials"            // Existential types (besides wildcard types) can be written and inferred
    , "-language:experimental.macros"     // Allow macro definition (besides implementation and application)
    , "-Xcheckinit"                       // Wrap field accessors to throw an exception on uninitialized access.
    , "-Xfuture"                          // Turn on future language features.
    , "-Ypartial-unification"             // Enable partial unification in type constructor inference
    , "-Xlint:_"
    , "-Ywarn-extra-implicit"             // Warn when more than one implicit parameter section is defined.
    , "-Ywarn-infer-any"                  // Warn when a type argument is inferred to be `Any`.
    , "-Ywarn-nullary-unit"               // Warn when nullary methods return Unit.
    , "-Ywarn-unused:implicits"           // Warn if an implicit parameter is unused.
    , "-Ywarn-unused:imports"             // Warn if an import selector is not referenced.
    , "-Ywarn-unused:locals"              // Warn if a local definition is unused.
    , "-Ywarn-unused:params"              // Warn if a value parameter is unused.
    , "-Ywarn-unused:patvars"             // Warn if a variable bound in a pattern is unused.
    , "-Ywarn-unused:privates"            // Warn if a private member is unused.
    , "-Ywarn-unused-import"
    )

    lazy val useAggressiveScalacOptions: SettingKey[Boolean] = settingKey("The flag to add aggressive scalac options")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
      useAggressiveScalacOptions := false
    , scalacOptions ++= crossVersionProps(
        essentialOptions ++ defaultOptions
      , SemanticVersion.parseUnsafe(scalaVersion.value)
      ) {
        case (Major(2), Minor(10)) =>
          Seq.empty

        case (Major(2), Minor(11)) =>
          if (useAggressiveScalacOptions.value) {
            aggressiveScalacOptionsForHigherThan2_10
          } else {
            scalacOptionsHigherThan2_10
          }

        case _ =>
          if (useAggressiveScalacOptions.value) {
            aggressiveScalacOptionsForHigherThan2_10 ++ aggressiveScalacOptionsForHigherThan2_11
          } else {
            scalacOptionsHigherThan2_10 ++ scalacOptionsHigherThan2_11
          }
      }
    , scalacOptions in (Compile, console) --= crossVersionProps(
        defaultOptions
      , SemanticVersion.parseUnsafe(scalaVersion.value)
      ) {
        case (Major(2), Minor(10)) =>
          Seq.empty

        case (Major(2), Minor(11)) =>
          if (useAggressiveScalacOptions.value) {
            aggressiveScalacOptionsForHigherThan2_10
          } else {
            scalacOptionsHigherThan2_10
          }

        case _ =>
          if (useAggressiveScalacOptions.value) {
            aggressiveScalacOptionsForHigherThan2_10 ++ aggressiveScalacOptionsForHigherThan2_11
          } else {
            scalacOptionsHigherThan2_10 ++ scalacOptionsHigherThan2_11
          }
      }
    , updateOptions := updateOptions.value.withCircularDependencyLevel(CircularDependencyLevel.Error)

  )

  // $COVERAGE-ON$
}
