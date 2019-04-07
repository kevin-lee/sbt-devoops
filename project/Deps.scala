import sbt._

object Deps {
  val hedgehogVersion: String = "55d9828dc6bcdc85ba3ebb31efd541d0a14423bf"

  val hedgehogRepo: Resolver =
    Resolver.url(
      "bintray-scala-hedgehog",
      url("https://dl.bintray.com/hedgehogqa/scala-hedgehog")
    )(Resolver.ivyStylePatterns)

  val hedgehogLibs: Seq[ModuleID] = Seq(
      "hedgehog" %% "hedgehog-core" % hedgehogVersion % Test
    , "hedgehog" %% "hedgehog-runner" % hedgehogVersion % Test
    , "hedgehog" %% "hedgehog-sbt" % hedgehogVersion % Test
  )

  val commonsIo: ModuleID = "commons-io" % "commons-io" % "2.1"

  val wartRemover: ModuleID = "org.wartremover" % "sbt-wartremover" % "2.2.1"

  val scoverage: ModuleID = "org.scoverage" % "sbt-scoverage" % "1.5.1"

  val bintray: ModuleID = "org.foundweekends" % "sbt-bintray" % "0.5.4"

  val githubApi: ModuleID = "org.kohsuke" % "github-api" % "1.95"

  val javaxActivation210: List[ModuleID] = List(
      "javax.activation" % "activation" % "1.1.1"
    , "javax.activation" % "javax.activation-api" % "1.2.0"
    , "com.google.code.findbugs" % "jsr305" % "3.0.2"
  )

  val javaxActivation212: List[ModuleID] = List(
      "javax.activation" % "activation" % "1.1.1"
    , "javax.activation" % "javax.activation-api" % "1.2.0"
  )
}
