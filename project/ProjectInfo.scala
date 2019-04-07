import wartremover.{Wart, Warts}

/**
  * @author Kevin Lee
  * @since 2018-05-21
  */
object ProjectInfo {

  val ProjectScalaVersion: String = "2.12.7"
  val CrossScalaVersions: Seq[String] = Seq("2.10.7", ProjectScalaVersion)

  val CrossSbtVersions: Seq[String] = Seq("0.13.17", "1.2.6")

  val ProjectVersion: String = "0.2.0"

  val commonScalacOptions: Seq[String] = Seq(
      "-deprecation"
    , "-unchecked"
    , "-feature"
    , "-Ywarn-value-discard"
    , "-Yno-adapted-args"
    , "-Xlint"
    , "-Xfatal-warnings"
    , "-Ywarn-dead-code"
    , "-Ywarn-inaccessible"
    , "-Ywarn-nullary-unit"
    , "-Ywarn-nullary-override"
    , "-language:higherKinds"
    , "-encoding", "UTF-8"
  )

  val commonWarts: Seq[Wart] =
    Warts.allBut(
        Wart.DefaultArguments
      , Wart.Overloading
      , Wart.Any
      , Wart.Nothing
      , Wart.NonUnitStatements
    )

}
