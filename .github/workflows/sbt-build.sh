#!/bin/bash -e

set -x

# Build + test for the given Scala version / sbt series.
#
# On the sbt 2 (Scala 3) axis, the root project is pinned to Scala 2.12 / sbt 1 (it pulls
# scripted-sbt). Forcing the whole aggregate onto ++3.8.4 makes it resolve inter-project
# deps with a malformed _sbt2_2.12 suffix, so the sbt 2 axis tests only the modules that
# actually cross-build to Scala 3.
#
# Usage: sbt-build.sh <scala-version> <sbt-series: sbt1|sbt2>

if [ -z "$1" ] || [ -z "$2" ]; then
  echo "Usage: sbt-build.sh <scala-version> <sbt-series: sbt1|sbt2>"
  exit 1
fi

scala_version="$1"
sbt_series="$2"

echo "============================================"
echo "Build + test: Scala ${scala_version} (${sbt_series})"
echo "--------------------------------------------"
java -version

if [ "$sbt_series" == "sbt2" ]; then
  # Scope to the modules that cross-build to Scala 3 (exclude the Scala-2.12-pinned root).
  sbt \
    "++${scala_version}" \
    -v \
    clean \
    common/Test/compile common/test \
    http-core/Test/compile http-core/test \
    github-core/Test/compile github-core/test \
    sbt-extra/Test/compile \
    scala/Test/compile \
    github/Test/compile \
    starter/Test/compile \
    release-version-policy/Test/compile release-version-policy/test \
    java/Test/compile
else
  # sbt 1: the whole build (including the root) is on Scala 2.12.
  sbt \
    "++${scala_version}" \
    -v \
    clean \
    Test/compile \
    test
fi

echo "============================================"
echo "Build + test: Done"
echo "============================================"
