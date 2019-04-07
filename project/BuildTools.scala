import sbt.Defaults.sbtPluginExtra
import sbt.Keys._
import sbt.{CrossVersion, IO, _}

/**
  * @author Kevin Lee
  * @since 2018-05-21
  */
object BuildTools {

  type VersionSpecificFunction[T] = PartialFunction[Option[(Int, Int)], T]

  def envVar: String => Option[String] = sys.env.get

  def crossVersionProps[T](commonProps: Seq[T],
                           scalaVersion: String)(
                           versionSpecific: VersionSpecificFunction[Seq[T]]): Seq[T] =
    commonProps ++ versionSpecific(CrossVersion.partialVersion(scalaVersion))

  def crossVersionSbtPlugin(organization: String,
                            name: String)(
                            versionSpecific: VersionSpecificFunction[String]): Setting[Seq[ModuleID]] =
    libraryDependencies +=
      sbtPluginExtra(
        m = organization %% name % versionSpecific(CrossVersion.partialVersion(scalaVersion.value)),
        sbtV = (sbtBinaryVersion in pluginCrossBuild).value,
        scalaV = (scalaBinaryVersion in update).value
      )

  sealed trait Prefix {
    def value: String
    def isEmpty: Boolean
    def fold(defaultVal: => String)(f: String => String): String = if (isEmpty) defaultVal else f(value)
    def +(path: => String): String = fold(path)(prefix => s"$prefix/$path")
  }

  object Prefix {
    final case object NoPrefix extends Prefix { val isEmpty = true; val value = "" }
    final case class PrefixVal(value: String) extends Prefix { val isEmpty = false }

    def apply(value: String): Prefix = Option(value).fold[Prefix](NoPrefix)(PrefixVal)
    def apply(): Prefix = NoPrefix
  }

  def versionWriter(paramsResolver: () => Seq[String])(projectVersion: String, basePath: String = "target"): Unit = {
    println("\n== Writing Version File ==")
    val args: Seq[String] = paramsResolver()
    println(s"The project version is $projectVersion.")

    import IO._

    val filename = args.headOption.map(Prefix(basePath) + _).getOrElse("target/version.tmp")
    val versionFile = new sbt.File(filename)
    println(s"write $projectVersion into the file: $versionFile")

    write(versionFile, projectVersion, utf8, append = false)
    println("Done: Writing Version File\n")
  }

}
