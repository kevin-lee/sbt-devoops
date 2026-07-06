import BuildTools._

ThisBuild / scalaVersion := props.ProjectScalaVersion
ThisBuild / crossScalaVersions := props.CrossScalaVersions
ThisBuild / developers := List(
  Developer(
    props.GitHubUsername,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUsername}"),
  ),
)
ThisBuild / homepage := url(s"https://github.com/${props.GitHubUsername}/${props.ProjectName}").some
ThisBuild / scmInfo :=
  ScmInfo(
    url(s"https://github.com/${props.GitHubUsername}/${props.ProjectName}"),
    s"git@github.com:${props.GitHubUsername}/${props.ProjectName}.git",
  ).some
ThisBuild / licenses := List("MIT" -> url("http://opensource.org/licenses/MIT"))
ThisBuild / startYear := 2018.some
ThisBuild / testFrameworks ~=
  (frameworks => (TestFramework("hedgehog.sbt.Framework") +: frameworks).distinct)

lazy val sbtDevOops = Project(props.ProjectName, file("."))
  .enablePlugins(SbtPlugin)
  .enablePlugins(DevOopsGitHubReleasePlugin, DocusaurPlugin)
  .settings(
    organization := props.Org,
    name := props.ProjectName,
    description := "DevOops - DevOps tool for GitHub",
    /* The root project is the build aggregator + docs/release driver (not a published
     * plugin itself in the sbt 2 sense); keep it on Scala 2.12 / sbt 1 so it doesn't try
     * to resolve scripted-sbt_3 etc. on the Scala 3 axis. Individual plugin modules are
     * what sbt 2 users depend on.
     */
    crossScalaVersions := List(props.ProjectScalaVersion),
    (pluginCrossBuild / sbtVersion) := props.Sbt1Version,
    writeVersion := versionWriter(Def.spaceDelimited("filename").parsed)(version.value),
    writeCurrentVersion := {
      val latestVersion = {
        import sys.process.*
        "git fetch --tags".!
        val tag = "git rev-list --tags --max-count=1".!!.trim
        s"git describe --tags $tag".!!.trim.stripPrefix("v")
      }
      val websiteDir    = docusaurDir.value

      val latestVersionFile = websiteDir / "latestVersion.json"
      val latestVersionJson = s"""{"version":"$latestVersion"}"""
      IO.write(latestVersionFile, latestVersionJson)
    },
    docusaurDir := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir := docusaurDir.value / "build",
    gitHubPagesOrgName := props.GitHubUsername,
    gitHubPagesRepoName := props.ProjectName,
    publishMavenStyle := true,
  )
  .dependsOn(
    sbtDevOopsCommon,
    sbtDevOopsScala,
    sbtDevOopsSbtExtra,
    sbtDevOopsGitHub,
  )
  .aggregate(
    sbtDevOopsCommon,
    sbtDevOopsScala,
    sbtDevOopsSbtExtra,
    sbtDevOopsHttpCore,
    sbtDevOopsGitHubCore,
    sbtDevOopsStarter,
    sbtDevOopsGitHub,
    sbtDevOopsReleaseVersionPolicy,
    sbtDevOopsJava,
  )

lazy val sbtDevOopsCommon = subProject(props.SubProjectNameCommon)
  .enablePlugins(SbtPlugin)
  .settings(
    libraryDependencies ++= List(
      libs.semVer,
      libs.commonsIo,
      libs.cats,
    ) ++ libs.hedgehogLibs ++ (
      if (scalaBinaryVersion.value == "2.12")
        List(libs.newtype % Test)
      else
        libs.refined4s.map(_ % Test)
    ),
  )

lazy val sbtDevOopsScala = subProject(props.SubProjectNameScala)
  .enablePlugins(SbtPlugin)
  .settings(
    addSbtPlugin(libs.sbtTpolecat)
  )
  .dependsOn(sbtDevOopsCommon)

lazy val sbtDevOopsSbtExtra = subProject(props.SubProjectNameSbtExtra)
  .enablePlugins(SbtPlugin)
  .settings(
    addSbtPlugin(libs.sbt2Compat)
  )

lazy val sbtDevOopsHttpCore = subProject(props.SubProjectNameHttpCore)
  .enablePlugins(SbtPlugin)
  .settings(
    libraryDependencies ++= List(
      libs.catsEffect,
      libs.effectie,
      libs.justSysprocess,
      libs.extrasCats,
    ) ++ libs.loggerF ++ libs.circe ++ libs.refined ++ libs.http4sClient ++ libs.javaxActivation212 ++ (
      if (scalaBinaryVersion.value == "2.12")
        List(libs.newtype)
      else
        libs.refined4s
    ),
  )
  .dependsOn(sbtDevOopsCommon % props.IncludeTest)

lazy val sbtDevOopsGitHubCore = subProject(props.SubProjectNameGitHubCore)
  .enablePlugins(SbtPlugin)
  .settings(
    libraryDependencies ++= libs.hedgehogLibs ++ List(libs.extrasHedgehogCatsEffect3) ++ (
      if (scalaBinaryVersion.value == "2.12") List.empty else libs.refined4s
    ),
  )
  .dependsOn(sbtDevOopsCommon, sbtDevOopsHttpCore)

lazy val sbtDevOopsStarter = subProject(props.SubProjectNameStarter)
  .enablePlugins(SbtPlugin)
  .settings(
    addSbtPlugin(libs.sbtScalafmt),
    addSbtPlugin(libs.sbtScalafix),
    addSbtPlugin(libs.sbtWelcome),
    libraryDependencies ++= List(libs.extrasScalaIo)
  )
  .dependsOn(sbtDevOopsScala, sbtDevOopsSbtExtra, sbtDevOopsHttpCore, sbtDevOopsGitHubCore)

lazy val sbtDevOopsGitHub = subProject(props.SubProjectNameGitHub)
  .enablePlugins(SbtPlugin)
  .settings(
    addSbtPlugin(libs.sbt2Compat),
    libraryDependencies ++= List(
      libs.extrasScalaIo,
    )
  )
  .dependsOn(sbtDevOopsCommon, sbtDevOopsGitHubCore)

lazy val sbtDevOopsReleaseVersionPolicy = subProject(props.SubProjectNameReleaseVersionPolicy)
  .enablePlugins(SbtPlugin)
  .settings(
    addSbtPlugin(libs.sbtRelease),
    addSbtPlugin(libs.sbtVersionPolicy),
    addSbtPlugin(libs.sbt2Compat),
    libraryDependencies ++= List(
      libs.extrasScalaIo,
    )
  )
  .dependsOn(sbtDevOopsCommon)

lazy val sbtDevOopsJava = subProject(props.SubProjectNameJava)
  .enablePlugins(SbtPlugin)

// scalafmt: off
def prefixedProjectName(name: String) = s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"
// scalafmt: on

def subProject(projectName: String): Project = {
  val prefixedName = prefixedProjectName(projectName)
  Project(projectName, file(s"modules/$prefixedName"))
    .settings(
      organization := props.Org,
      name := prefixedName,
      crossScalaVersions := props.CrossScalaVersions,
      (pluginCrossBuild / sbtVersion) := {
        scalaBinaryVersion.value match {
          case "2.12" => props.Sbt1Version
          case _      => props.Sbt2Version
        }
      },
      libraryDependencies ++= (
        if (scalaBinaryVersion.value == "2.12")
          List(
            compilerPlugin("org.scalamacros" % "paradise"       % "2.1.1" cross CrossVersion.full),
            compilerPlugin("org.typelevel"   % "kind-projector" % "0.13.4" cross CrossVersion.full),
          )
        else
          List.empty
      ),
      scalacOptions ++= (
        if (scalaBinaryVersion.value == "2.12") List.empty
        else
          List(
            "-Xkind-projector",
            // Scala 3 migration warning for explicit application of implicit params (e.g. Source.fromInputStream(in)(Codec.UTF8)).
            "-Wconf:msg=Implicit parameters should be provided with a `using` clause:s",
            // sbt 2 plugin classpath pulls the Scala 3 stdlib transitively; the "several versions of the
            // Scala standard library" / duplicate `caps` object+package notice is a classpath artifact, not our code.
            "-Wconf:msg=package scala contains object and package with same name:s",
            "-Wconf:msg=several versions of the Scala standard library:s",
            // Scala 3 migration-style notices on code that must remain valid on Scala 2.12 too.
            "-Wconf:msg=The syntax `private\\[this\\]` will be deprecated:s",
            "-Wconf:msg=is eta-expanded even though:s",
            // Scala 3 -Wunused flags context-bound type classes (used only via implicit machinery) as unused.
            "-Wconf:msg=unused implicit parameter:s",
            // `final case object` is a pervasive style here; the redundant-final notice is Scala-3-only noise.
            "-Wconf:msg=Modifier final is redundant:s",
            // `Seq[Setting[_]]`-style wildcards must stay `_` for the shared Scala 2.12 sources.
            "-Wconf:msg=`_` is deprecated for wildcard arguments of types:s",
            // sbt DSL uses alphanumeric methods (e.g. `m cross CrossVersion.full`) as infix; Scala 3 warns on this.
            "-Wconf:msg=is not declared infix:s",
            // Scala 3 -Wunused over-eagerly flags intentional exclusion imports (e.g. `import sbt.{some as _, *}`).
            "-Wconf:msg=unused import:s",
            // Discarded values (e.g. `mkdirs(): Boolean`, expression statements) — mostly in tests.
            "-Wconf:msg=unused value of type:s",
            "-Wconf:msg=discarded non-Unit value:s",
          )
      ),
//      scalacOptions ++= List("-Xsource:3"),
      Compile / console / scalacOptions := scalacOptions.value diff List("-Ywarn-unused-import", "-Xfatal-warnings"),
      licenses := List("MIT" -> url("http://opensource.org/licenses/MIT")),
      publishMavenStyle := true,
      coverageHighlighting := (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) =>
          false
        case _ =>
          true
      }),
      scriptedLaunchOpts := {
        scriptedLaunchOpts.value ++
          Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
      },
      scriptedBufferLog := false,
    )
}

lazy val props =
  new {

    private val GitHubRepo = findRepoOrgAndName

    val Org            = "io.kevinlee"
    val GitHubUsername = GitHubRepo.fold("Kevin-Lee")(_.orgToString)
    val RepoName       = GitHubRepo.fold("sbt-devoops")(_.nameToString)

    val ProjectName = RepoName

    val SubProjectNameCommon               = "common"
    val SubProjectNameScala                = "scala"
    val SubProjectNameSbtExtra             = "sbt-extra"
    val SubProjectNameStarter              = "starter"
    val SubProjectNameHttpCore             = "http-core"
    val SubProjectNameGitHubCore           = "github-core"
    val SubProjectNameGitHub               = "github"
    val SubProjectNameReleaseVersionPolicy = "release-version-policy"
    val SubProjectNameJava                 = "java"

    val ProjectScalaVersion = "2.12.18"
    val ProjectScala3Version = "3.8.4"
    val CrossScalaVersions  = List(ProjectScalaVersion, ProjectScala3Version).distinct

    /* sbt versions targeted by each Scala axis when building/publishing the plugins.
     * Scala 2.12 -> sbt 1.x, Scala 3 -> sbt 2.x (see pluginCrossBuild / sbtVersion in subProject).
     */
    val Sbt1Version = "1.11.7"
    val Sbt2Version = "2.0.1"

    val hedgehogVersion = "0.13.1"

    val newtypeVersion = "0.4.4"

    val refined4sVersion = "1.18.0"

    val catsVersion       = "2.13.0"
    val catsEffectVersion = "3.7.0"

    val extrasVersion = "0.53.0"

    val effectieVersion = "2.3.0"

    /* logger-f-sbt-logging was split out of logger-f in 2.11.0 and is now versioned
     * independently. logger-f (core/cats) stays on 2.11.0; logger-f-sbt-logging is 2.11.1+
     * and provides the sbt 2 (util-logging_3) build needed for the Scala 3 axis.
     */
    val loggerFVersion           = "2.11.0"
    val loggerFSbtLoggingVersion = "2.11.1"

    val refinedVersion = "0.11.3"

    val circeVersion        = "0.14.15"
    val circeRefinedVersion = "0.15.1"

    val http4sVersion = "0.23.34"

    val justSemVerVersion = "1.3.0"

    val justSysprocessVersion = "1.0.0"

    val commonsIoVersion = "2.22.0"

    val activationVersion    = "1.1.1"
    val activationApiVersion = "1.2.0"

    val SbtTpolecatVersion = "0.5.7"

    val SbtVersionPolicyVersion = "3.3.0"
    val SbtReleaseVersion       = "1.5.0"

    /* Compatibility bridge from the sbt team so shared plugin sources compile on both
     * sbt 1 and sbt 2 (e.g. Def.uncached is native in sbt 2 and a no-op enrichment in sbt 1).
     */
    val Sbt2CompatVersion = "0.1.0"

    val SbtScalafmtVersion = "2.6.1"
    val SbtScalafixVersion = "0.14.7"

    val SbtWelcomeVersion = "0.6.0"

    val IncludeTest = "compile->compile;test->test"
  }

lazy val libs =
  new {

    lazy val hedgehogLibs = List(
      "qa.hedgehog" %% "hedgehog-core"   % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-sbt"    % props.hedgehogVersion % Test,
    )

    lazy val newtype = "io.estatico" %% "newtype" % props.newtypeVersion

    /* refined4s replaces io.estatico.newtype on the Scala 3 axis (newtype has no Scala 3 build). */
    lazy val refined4s = List(
      "io.kevinlee" %% "refined4s-core"  % props.refined4sVersion,
      "io.kevinlee" %% "refined4s-circe" % props.refined4sVersion,
    )

    lazy val refined = Seq(
      "eu.timepit" %% "refined"      % props.refinedVersion,
      "eu.timepit" %% "refined-cats" % props.refinedVersion,
    )

    lazy val cats       = "org.typelevel" %% "cats-core"   % props.catsVersion
    lazy val catsEffect = "org.typelevel" %% "cats-effect" % props.catsEffectVersion

    lazy val extrasCats                = "io.kevinlee" %% "extras-cats"     % props.extrasVersion
    lazy val extrasScalaIo             = "io.kevinlee" %% "extras-scala-io" % props.extrasVersion
    lazy val extrasHedgehogCatsEffect3 =
      "io.kevinlee" %% "extras-hedgehog-ce3" % props.extrasVersion % Test

    lazy val effectie = "io.kevinlee" %% "effectie-cats-effect3" % props.effectieVersion

    lazy val loggerF = List(
      "io.kevinlee" %% "logger-f-cats"        % props.loggerFVersion,
      "io.kevinlee" %% "logger-f-sbt-logging" % props.loggerFSbtLoggingVersion,
    )

    lazy val http4sClient = List(
      "org.http4s" %% "http4s-dsl"          % props.http4sVersion,
      "org.http4s" %% "http4s-ember-client" % props.http4sVersion,
      "org.http4s" %% "http4s-circe"        % props.http4sVersion,
    )

    lazy val circe = List(
      "io.circe" %% "circe-generic" % props.circeVersion,
      "io.circe" %% "circe-parser"  % props.circeVersion,
    ) ++ List(
      "io.circe" %% "circe-refined" % props.circeRefinedVersion
    )

    lazy val semVer = "io.kevinlee" %% "just-semver" % props.justSemVerVersion

    lazy val justSysprocess = "io.kevinlee" %% "just-sysprocess" % props.justSysprocessVersion

    lazy val commonsIo = "commons-io" % "commons-io" % props.commonsIoVersion

    lazy val javaxActivation212 = List(
      "javax.activation" % "activation" % props.activationVersion,
    )

    lazy val sbtTpolecat = "org.typelevel" % "sbt-tpolecat" % props.SbtTpolecatVersion

    lazy val sbtVersionPolicy = "ch.epfl.scala"  % "sbt-version-policy" % props.SbtVersionPolicyVersion
    lazy val sbtRelease       = "com.github.sbt" % "sbt-release"        % props.SbtReleaseVersion

    lazy val sbt2Compat = "com.github.sbt" % "sbt2-compat" % props.Sbt2CompatVersion

    lazy val sbtScalafmt = "org.scalameta" % "sbt-scalafmt" % props.SbtScalafmtVersion
    lazy val sbtScalafix = "ch.epfl.scala" % "sbt-scalafix" % props.SbtScalafixVersion

    lazy val sbtWelcome = "com.github.reibitto" % "sbt-welcome" % props.SbtWelcomeVersion

    def all(scalaVersion: String) = crossVersionProps(
      List(
        commonsIo,
        semVer,
        newtype,
        cats,
        catsEffect,
        effectie,
        justSysprocess,
        extrasScalaIo,
        extrasHedgehogCatsEffect3,
      ) ++
        hedgehogLibs ++
        loggerF ++
        http4sClient ++
        circe,
      scalaVersion,
    ) {
      case Some((2, 12)) =>
        javaxActivation212
      case Some((2, 10)) =>
        Seq.empty
    }
  }

lazy val writeVersion        = inputKey[Unit]("Write Version in File'")
lazy val writeCurrentVersion = inputKey[Unit]("Write the current version at ${docusaurDir.value}/latestVersion.json")

import scala.{Console => sConsole}
logo :=
  raw"""
       |       __   __      ___           ____
       |  ___ / /  / /_____/ _ \___ _  __/ __ \___  ___  ___
       | (_-</ _ \/ __/___/ // / -_) |/ / /_/ / _ \/ _ \(_-<
       |/___/_.__/\__/   /____/\__/|___/\____/\___/ .__/___/
       |                                         /_/
       |
       |${sConsole.BLUE}${name.value}${sConsole.RESET} v${sConsole.BLUE}${version.value}${sConsole.RESET}
       |${sConsole.YELLOW}Scala ${scalaVersion.value}${sConsole.RESET}
       |-----------------------------------------------------
       |""".stripMargin

import sbtwelcome._

val aliasFormatter: String => String =
  _ + s"${scala.io.AnsiColor.RESET}: "

usefulTasks := Seq(
  UsefulTask("reload", "Run reload").alias("r"),
  UsefulTask("clean", "Run clean").alias("cln"),
  UsefulTask("compile", "Run compile").alias("c"),
  UsefulTask("+compile", "Run +compile").alias("cc"),
  UsefulTask("Test/compile", "Run Test/compile").alias("tc"),
  UsefulTask("+Test/compile", "Run +Test/compile").alias("ctc"),
  UsefulTask("test", "Run test").alias("t"),
  UsefulTask("+test", "Run +test").alias("ct"),
  UsefulTask("scripted", "Run scripted for sbt-test").alias("st"),
  UsefulTask("+scripted", "Run +scripted for sbt-test").alias("cst"),
  UsefulTask("scalafmtCheckAll", "Run scalafmtCheckAll").alias("fmtchk"),
  UsefulTask("scalafmtAll", "Run scalafmtAll").alias("fmt"),
  UsefulTask("publishLocal", "Run publishLocal").alias("pl"),
  UsefulTask("dependencyUpdates", "Run dependencyUpdates").alias("du"),
  UsefulTask("unusedCompileDependencies", "Run unusedCompileDependencies").alias("uud"),
  UsefulTask("undeclaredCompileDependencies", "Run undeclaredCompileDependencies").alias("udd"),
).map(_.formatAlias(aliasFormatter))

logoColor := sConsole.MAGENTA
