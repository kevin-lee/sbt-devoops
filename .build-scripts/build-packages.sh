#!/bin/bash -e

echo "======================================================"
echo "Build packages"
echo "------------------------------------------------------"

sbt clean
sbt writeVersion

echo ""
echo "======================================================"
echo "Run: sbt -d -J-Xmx2048m clean ^packagedArtifacts "
echo "------------------------------------------------------"
if sbt -d -J-Xmx2048m clean ^packagedArtifacts ; then
  echo "Done: sbt -d -J-Xmx2048m clean ^packagedArtifacts "
  echo "======================================================"
else
  echo "Failed: sbt -d -J-Xmx2048m clean ^packagedArtifacts " 1>&2
  echo "======================================================"
  exit 1
fi

echo ""
echo "======================================================"
echo "Building Packages: Done"
echo "======================================================"

sbt writeVersion
