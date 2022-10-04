import sbt.*

object Dependencies {
  val hedgehogVersion = "0.9.0"

  val hedgehog = List(
    "qa.hedgehog" %% "hedgehog-core" % hedgehogVersion,
    "qa.hedgehog" %% "hedgehog-runner" % hedgehogVersion,
    "qa.hedgehog" %% "hedgehog-sbt" % hedgehogVersion
  ).map(_ % Test)

}
