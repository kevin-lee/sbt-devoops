logLevel := sbt.Level.Warn

addSbtPlugin("com.github.sbt"   % "sbt-ci-release"      % "1.5.10")
addSbtPlugin("org.wartremover"  % "sbt-wartremover"     % "2.3.7")
addSbtPlugin("org.scoverage"    % "sbt-scoverage"       % "1.5.1")
addSbtPlugin("org.scoverage"    % "sbt-coveralls"       % "1.2.7")
addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.2")
addSbtPlugin("io.kevinlee"      % "sbt-docusaur"        % "0.8.1")

val sbtDevOopsVersion = "2.14.0"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
