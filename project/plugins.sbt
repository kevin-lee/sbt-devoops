logLevel := sbt.Level.Warn

addSbtPlugin("com.github.sbt"   % "sbt-ci-release"            % "1.5.10")
addSbtPlugin("org.wartremover"  % "sbt-wartremover"           % "2.3.7")
addSbtPlugin("org.scoverage"    % "sbt-scoverage"             % "1.5.1")
addSbtPlugin("org.scoverage"    % "sbt-coveralls"             % "1.2.7")
addSbtPlugin("org.lyranthe.sbt" % "partial-unification"       % "1.1.2")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"               % "0.6.3")
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.2.16")

addSbtPlugin("com.github.reibitto" % "sbt-welcome"  % "0.2.2")
addSbtPlugin("io.kevinlee"         % "sbt-docusaur" % "0.11.0")

val sbtDevOopsVersion = "2.20.0"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
