#!/bin/bash -e

set -x

if [ "$#" -lt 2 ]
  then
    echo "Scala version and sbt version are missing. Please enter the Scala and sbt versions, and optionally whether to send coveralls."
    echo "sbt-build.sh 2.12.10 1.3.3 coveralls"
    exit 1
else
  SCALA_VERSION=$1
  SBT_VERSION=$2
  coveralls=${3:-}
  echo "============================================"
  echo "Build projects"
  echo "--------------------------------------------"
  echo ""
  if [[ "$CI_BRANCH" == "main" || "$CI_BRANCH" == "release" ]]
  then
    if [[ "$coveralls" == "coveralls" ]]
    then
      sbt -J-Xmx2048m \
        ++${SCALA_VERSION}! \
        ^^${SBT_VERSION} \
        clean \
        coverage \
        test \
        coverageReport \
        coverageAggregate \
        packagedArtifacts
    else
      sbt -J-Xmx2048m \
        ++${SCALA_VERSION}! \
        ^^${SBT_VERSION} \
        clean \
        test \
        packagedArtifacts
    fi
  else
    if [[ "$coveralls" == "coveralls" ]]
    then
      sbt -J-Xmx2048m \
        ++${SCALA_VERSION}! \
        ^^${SBT_VERSION} \
        clean \
        coverage \
        test \
        coverageReport \
        coverageAggregate \
        package
    else
      sbt -J-Xmx2048m \
        ++${SCALA_VERSION}! \
        ^^${SBT_VERSION} \
        clean \
        test \
        package
    fi
  fi

  if [[ "$coveralls" == "coveralls" ]]
  then
    sbt -J-Xmx2048m \
      ++${SCALA_VERSION}! \
      ^^${SBT_VERSION} \
      coveralls
  fi


  echo "============================================"
  echo "Building projects: Done"
  echo "============================================"
fi
