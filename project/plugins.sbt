logLevel := sbt.Level.Warn

addSbtPlugin("com.github.sbt"   % "sbt-ci-release"            % "1.5.10")
addSbtPlugin("org.wartremover"  % "sbt-wartremover"           % "3.0.6")
addSbtPlugin("org.scoverage"    % "sbt-scoverage"             % "2.0.6")
addSbtPlugin("org.scoverage"    % "sbt-coveralls"             % "1.3.2")
addSbtPlugin("org.lyranthe.sbt" % "partial-unification"       % "1.1.2")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"               % "0.6.3")
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.2.16")

addSbtPlugin("com.github.reibitto" % "sbt-welcome"  % "0.2.2")
addSbtPlugin("io.kevinlee"         % "sbt-docusaur" % "0.13.0")

val sbtDevOopsVersion = "2.24.0"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
