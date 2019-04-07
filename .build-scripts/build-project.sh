#!/bin/bash -e

echo "============================================"
echo "Build projects"
echo "--------------------------------------------"
echo ""
echo "============================================"
echo "Run: sbt clean ^coverage ^test ^coverageReport ^coverageAggregate ^package"
echo "--------------------------------------------"
if [[ "$BRANCH_NAME" == "rc" ]]
  then
  if sbt clean ^coverage ^test ^coverageReport ^coverageAggregate ; then
    echo "Done: sbt clean ^coverage ^test ^coverageReport ^coverageAggregate"
    echo "============================================"
  else
    echo "Failed: sbt clean ^coverage ^test ^coverageReport ^coverageAggregate" 1>&2
    echo "============================================"
    exit 1
  fi
  if sbt ^packageBin ^packageSrc ^packageDoc ; then
    echo "Done: sbt ^packageBin ^packageSrc ^packageDoc"
    echo "============================================"
  else
    echo "Failed: sbt ^packageBin ^packageSrc ^packageDoc" 1>&2
    echo "============================================"
    exit 1
  fi
elif sbt clean ^coverage ^test ^coverageReport ^coverageAggregate ^package ; then
  echo "Done: sbt clean ^coverage ^test ^coverageReport ^coverageAggregate ^package"
  echo "============================================"
else
  echo "Failed: sbt clean ^coverage ^test ^coverageReport ^coverageAggregate ^package" 1>&2
  echo "============================================"
  exit 1
fi

echo ""

echo "============================================"
echo "Run: sbt ^coveralls"
echo "--------------------------------------------"
if sbt ^coveralls ; then
  echo "Done: sbt ^coveralls"
  echo "============================================"
else
  echo "Failed: sbt ^coveralls" 1>&2
  echo "============================================"
  exit 1
fi
echo ""
echo "============================================"
echo "Building projects: Done"
echo "============================================"
