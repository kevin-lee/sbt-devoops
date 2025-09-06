logLevel := sbt.Level.Warn

addSbtPlugin("com.github.sbt"   % "sbt-ci-release"            % "1.11.2")
addSbtPlugin("org.wartremover"  % "sbt-wartremover"           % "3.1.3")
addSbtPlugin("org.scoverage"    % "sbt-scoverage"             % "2.3.1")
addSbtPlugin("org.scoverage"    % "sbt-coveralls"             % "1.3.15")
addSbtPlugin("org.lyranthe.sbt" % "partial-unification"       % "1.1.2")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"               % "0.6.3")
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.3.1")

addSbtPlugin("com.github.reibitto" % "sbt-welcome" % "0.5.0")

addSbtPlugin("io.kevinlee" % "sbt-docusaur" % "0.17.0")

val sbtDevOopsVersion = "3.2.1"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
