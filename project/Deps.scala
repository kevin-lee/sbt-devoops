import sbt._

object Deps {
  val hedgehogVersion: String = "6dba7c9ba065e423000e9aa2b6981ce3d70b74cb"

  val hedgehogRepo: Resolver =
      "bintray-scala-hedgehog" at "https://dl.bintray.com/hedgehogqa/scala-hedgehog"

  val hedgehogLibs: Seq[ModuleID] = Seq(
      "hedgehog" %% "hedgehog-core" % hedgehogVersion % Test
    , "hedgehog" %% "hedgehog-runner" % hedgehogVersion % Test
    , "hedgehog" %% "hedgehog-sbt" % hedgehogVersion % Test
  )

  val justFp: ModuleID = "io.kevinlee" %% "just-fp" % "1.3.4"

  val commonsIo: ModuleID = "commons-io" % "commons-io" % "2.1"

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
