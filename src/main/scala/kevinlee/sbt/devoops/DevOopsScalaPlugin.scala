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
    , "-Xfatal-warnings"                  // Fail the compilation if there are any warnings.
    )

    val defaultOptionsBefore2_13: Seq[String] = Seq(
      "-Ywarn-dead-code"                  // Warn when dead code is identified.
    , "-Ywarn-value-discard"              // Warn when non-Unit expression results are unused.
    , "-Yno-adapted-args"                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    , "-Ywarn-inaccessible"               // Warn about inaccessible types in method signatures.
    , "-Ywarn-nullary-override"           // Warn when non-nullary `def f()' overrides nullary `def f'.
    , "-Ywarn-numeric-widen"              // Warn when numerics are widened.
    )

    val defaultOptions2_13: Seq[String] = Seq(
        "-Wdead-code"               // Warn when dead code is identified.
      , "-Wvalue-discard"           // Warn when non-Unit expression results are unused.
      , "-Xlint:adapted-args"       // Warn if an argument list is modified to match the receiver.
      , "-Xlint:inaccessible"       // Warn about inaccessible types in method signatures.
      , "-Xlint:nullary-override"   // Warn when non-nullary def f()' overrides nullarydef f’.
      , "-Wnumeric-widen"           // Warn when numerics are widened.
    )

    val scalacOptions2_13: Seq[String] = Seq(
        "-Wunused:imports"                // Warn if an import selector is not referenced.
      , "-Xlint:nullary-unit"             // Warn when nullary methods return Unit.
      , "-Xlint:infer-any"                // Warn when a type argument is inferred to be Any.
      , "-Xlint:missing-interpolator"     // A string literal appears to be missing an interpolator id.
      , "-Xlint:doc-detached"             // A Scaladoc comment appears to be detached from its element.
      , "-Xlint:private-shadow"           // A private field (or class parameter) shadows a superclass field.
      , "-Xlint:type-parameter-shadow"    // A local type parameter shadows a type already in scope.
      , "-Xlint:poly-implicit-overload"   // Parameterized overloaded implicit methods are not visible as view bounds.
      , "-Xlint:option-implicit"          // Option.apply used implicit view.
      , "-Xlint:delayedinit-select"       // Selecting member of DelayedInit.
      , "-Xlint:package-object-classes"   // Class or object defined in package object.
      , "-Xlint:stars-align"              // Pattern sequence wildcard must align with sequence component.
      , "-Xlint:constant"                 // Evaluation of a constant arithmetic expression results in an error.
    )

    val scalacOptions2_12: Seq[String] = Seq(
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
      , "-Xlint:constant"                   // Evaluation of a constant arithmetic expression results in an error.
    )

    val scalacOptions2_11: Seq[String] = Seq(
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

    val aggressiveScalacOptions2_13: Seq[String] = Seq(
        "-Wextra-implicit"    // Warn when more than one implicit parameter section is defined.
      , "-Woctal-literal"     // Warn on obsolete octal syntax.
      // Is it necessary?
      //          , "-Wself-implicit"  // Warn when an implicit resolves to an enclosing self-definition.
      , "-Wunused:imports"    // Warn if an import selector is not referenced.
      , "-Wunused:patvars"    // Warn if a variable bound in a pattern is unused.
      , "-Wunused:privates"   // Warn if a private member is unused.
      , "-Wunused:locals"     // Warn if a local definition is unused.
      , "-Wunused:explicits"  // Warn if an explicit parameter is unused.
      , "-Wunused:implicits"  // Warn if an implicit parameter is unused.
      , "-Wunused:params"     // Enable -Wunused:explicits,implicits.
      , "-Wunused:linted"     // -Xlint:unused.
      , "-Xlint:_"            // Enable lint
    )

    val aggressiveScalacOptions2_11: Seq[String] = Seq(
      "-explaintypes"                     // Explain type errors in more detail.
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

    val aggressiveScalacOptions2_12: Seq[String] = aggressiveScalacOptions2_11

    lazy val useAggressiveScalacOptions: SettingKey[Boolean] = settingKey("The flag to add aggressive scalac options")
  }

  import autoImport._

  def versionSpecificScalacOptions(useAggressiveScalacOptions: Boolean): PartialFunction[(Major, Minor), Seq[String]] = {
      case (Major(2), Minor(10)) =>
        defaultOptionsBefore2_13

      case (Major(2), Minor(11)) =>
        if (useAggressiveScalacOptions) {
          defaultOptionsBefore2_13 ++ aggressiveScalacOptions2_11
        } else {
          defaultOptionsBefore2_13 ++ scalacOptions2_11
        }

      case (Major(2), Minor(12)) =>
        if (useAggressiveScalacOptions) {
          defaultOptionsBefore2_13 ++ aggressiveScalacOptions2_12
        } else {
          defaultOptionsBefore2_13 ++ scalacOptions2_12
        }

      case (Major(2), Minor(13)) =>
        if (useAggressiveScalacOptions) {
          defaultOptions2_13 ++ aggressiveScalacOptions2_13
        } else {
          defaultOptions2_13 ++ scalacOptions2_13
        }

      case _ =>
        if (useAggressiveScalacOptions) {
          defaultOptions2_13 ++ aggressiveScalacOptions2_13
        } else {
          defaultOptions2_13 ++ scalacOptions2_13
        }
    }

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
      useAggressiveScalacOptions := false
    , scalacOptions ++= crossVersionProps(
        essentialOptions ++ defaultOptions
      , SemanticVersion.parseUnsafe(scalaVersion.value)
      )(versionSpecificScalacOptions(useAggressiveScalacOptions.value))
    , scalacOptions in (Compile, console) := essentialOptions
    , updateOptions := updateOptions.value.withCircularDependencyLevel(CircularDependencyLevel.Error)

  )

  // $COVERAGE-ON$
}
