sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("io.kevinlee" % "sbt-devoops-scala" % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}

