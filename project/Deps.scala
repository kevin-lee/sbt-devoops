import sbt._

object Deps {
  val hedgehogVersion: String = "d74f5bb31f26d3e3b7f7d0198b6e768a1ed20669"

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

  val kevinsRepo: Resolver = "Kevin's Repo" at "https://dl.bintray.com/kevinlee/maven"

  val justFp: ModuleID = "kevinlee" %% "just-fp" % "0.1.0"

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
