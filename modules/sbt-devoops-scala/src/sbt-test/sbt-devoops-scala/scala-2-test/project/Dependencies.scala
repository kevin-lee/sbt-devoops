import sbt._

object Dependencies {
  val hedgehogVersion = "0.13.0"

  val hedgehog = List(
    "qa.hedgehog" %% "hedgehog-core" % hedgehogVersion,
    "qa.hedgehog" %% "hedgehog-runner" % hedgehogVersion,
    "qa.hedgehog" %% "hedgehog-sbt" % hedgehogVersion
  ).map(_ % Test)

}
