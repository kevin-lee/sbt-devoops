#!/bin/bash -e

set -x

# Run scripted tests for the given Scala version / sbt series.
#
# sbt 1 plugins are published as _2.12_1.0 (built on Scala 2.12); their scripted tests
# pin sbt 1.x. sbt 2 plugins are published as _sbt2_3 (built on Scala 3); their scripted
# tests pin sbt 2.x and must run on the Scala 3 axis so the _sbt2_3 artifacts are
# publishLocal'd by the scripted pre-publish step.
#
# Usage: sbt-scripted.sh <scala-version> <sbt-series: sbt1|sbt2>

if [ -z "$1" ] || [ -z "$2" ]; then
  echo "Usage: sbt-scripted.sh <scala-version> <sbt-series: sbt1|sbt2>"
  exit 1
fi

scala_version="$1"
sbt_series="$2"

echo "============================================"
echo "Scripted tests: Scala ${scala_version} (${sbt_series})"
echo "--------------------------------------------"

if [ "$sbt_series" == "sbt2" ]; then
  # Scope to the modules that own sbt 2 scripted tests so the scripted pre-publish only
  # publishes _sbt2_3-capable projects (not the Scala-2.12-pinned root).
  sbt \
    "++${scala_version}" \
    -v \
    clean \
    "scala/scripted sbt-devoops-scala/scala-3-sbt2-test" \
    "starter/scripted sbt-devoops-starter/write-default-scalafmt-conf-sbt2-test-new sbt-devoops-starter/write-default-scalafix-conf-scala3-sbt2-test-new" \
    "sbt-local-cache/scripted sbt-devoops-sbt-local-cache/sbt2-local-cache sbt-devoops-sbt-local-cache/sbt2-clean-includes-cache sbt-devoops-sbt-local-cache/sbt2-default-off"
else
  # sbt 1: run the full scripted suite. The sbt-2-pinned tests (sbt.version=2.0.1) are
  # automatically skipped by the scripted runner because their binary sbt version differs
  # from the active sbt 1.x.
  sbt \
    "++${scala_version}" \
    -v \
    clean \
    scripted
fi

echo "============================================"
echo "Scripted tests: Done"
echo "============================================"
