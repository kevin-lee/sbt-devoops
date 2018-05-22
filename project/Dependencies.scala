import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"

  lazy val dependencyLibs2_12 = Seq(
    "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
  )
  lazy val dependencyLibs2_10 = Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.4" % Test
  )
}
