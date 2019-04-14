#!/bin/bash -e

set -x

if [ -z "$1" ]
  then
    echo "sbt version is missing. Please enter the sbt version."
    echo "build-project.sh 1.2.8"
else
  sbt_version=$1
  echo "============================================"
  echo "Build projects"
  echo "--------------------------------------------"
  echo ""
  if [[ "$BRANCH_NAME" == "rc" ]]
  then
    sbt -d -J-Xmx2048m "; ^^ ${sbt_version}; clean; test; packagedArtifacts"

  else
    sbt -d -J-Xmx2048m "; ^^ ${sbt_version}; clean; test; package"
  fi

  echo ""

  echo "============================================"
  echo "Building projects: Done"
  echo "============================================"

fi