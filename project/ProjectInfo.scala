import wartremover.{Wart, Warts}

/** @author Kevin Lee
  * @since 2018-05-21
  */
object ProjectInfo {

  val commonScalacOptions: Seq[String] = Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-Ywarn-value-discard",
    "-Yno-adapted-args",
    "-Xlint",
    "-Xfatal-warnings",
    "-Ywarn-dead-code",
    "-Ywarn-inaccessible",
    "-Ywarn-nullary-unit",
    "-Ywarn-nullary-override",
    "-language:higherKinds",
    "-encoding",
    "UTF-8"
  )

  val commonWarts: Seq[Wart] =
    Warts.allBut(
      Wart.DefaultArguments,
      Wart.Overloading,
      Wart.Any,
      Wart.Nothing,
      Wart.NonUnitStatements,
      Wart.ImplicitParameter
    )

}
