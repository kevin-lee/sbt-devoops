#!/bin/bash -e

echo "============================================"
echo "Build projects"
echo "--------------------------------------------"
echo ""
echo "============================================"
echo "Run: sbt -d -J-Xmx2048m clean ^coverage ^test ^coverageReport ^coverageAggregate ^package"
echo "--------------------------------------------"
if [[ "$BRANCH_NAME" == "rc" ]]
then
  if sbt -d -J-Xmx2048m clean ^coverage ^test ^coverageReport ^coverageAggregate ; then
    echo "Done: sbt -d -J-Xmx2048m clean ^coverage ^test ^coverageReport ^coverageAggregate"
    echo "============================================"
  else
    echo "Failed: sbt -d -J-Xmx2048m clean ^coverage ^test ^coverageReport ^coverageAggregate" 1>&2
    echo "============================================"
    exit 1
  fi
  if sbt -d -J-Xmx2048m ^packageBin ^packageSrc ^packageDoc ; then
    echo "Done: sbt -d -J-Xmx2048m ^packageBin ^packageSrc ^packageDoc"
    echo "============================================"
  else
    echo "Failed: sbt -d -J-Xmx2048m ^packageBin ^packageSrc ^packageDoc" 1>&2
    echo "============================================"
    exit 1
  fi
elif sbt -d -J-Xmx2048m clean ^coverage ^test ^coverageReport ^coverageAggregate ^package ; then
  echo "Done: sbt -d -J-Xmx2048m clean ^coverage ^test ^coverageReport ^coverageAggregate ^package"
  echo "============================================"
else
  echo "Failed: sbt -d -J-Xmx2048m clean ^coverage ^test ^coverageReport ^coverageAggregate ^package" 1>&2
  echo "============================================"
  exit 1
fi

echo ""

echo "============================================"
echo "Run: sbt -d -J-Xmx2048m ^coveralls"
echo "--------------------------------------------"
if sbt -d -J-Xmx2048m ^coveralls ; then
  echo "Done: sbt -d -J-Xmx2048m ^coveralls"
  echo "============================================"
else
  echo "Failed: sbt -d -J-Xmx2048m ^coveralls" 1>&2
  echo "============================================"
  exit 1
fi
echo ""
echo "============================================"
echo "Building projects: Done"
echo "============================================"
