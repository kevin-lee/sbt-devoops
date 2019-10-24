#!/bin/bash -e

set -x

if [ "$#" -ne 2 ]
  then
    echo "Scala version and sbt version are missing. Please enter the Scala and sbt versions."
    echo "sbt-build.sh 2.12.10 1.3.3"
    exit 1
else
  SCALA_VERSION=$1
  SBT_VERSION=$2
  echo "============================================"
  echo "Build projects"
  echo "--------------------------------------------"
  echo ""
  if [[ "$CI_BRANCH" == "master" || "$CI_BRANCH" == "release" ]]
  then
    sbt -J-Xmx2048m "; ++${SCALA_VERSION}!; ^^${SBT_VERSION}; clean; coverage; test; coverageReport; coverageAggregate; packagedArtifacts"
  else
    sbt -J-Xmx2048m "; ++${SCALA_VERSION}!; ^^${SBT_VERSION}; clean; coverage; test; coverageReport; coverageAggregate; package"
  fi
  sbt -J-Xmx2048m "; ++${SCALA_VERSION}!; ^^${SBT_VERSION}; coveralls"


  echo "============================================"
  echo "Building projects: Done"
  echo "============================================"
fi
