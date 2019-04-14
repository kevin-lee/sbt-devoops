#!/bin/bash -e

alias sbt='sbt -d -J-Xmx2048m'

if [ ! -n "$PROJECT_BUILD_NAME" ]
  then
  echo "NO PROJECT_BUILD_NAME is found so quit!" 1>&2
  exit 1
fi

echo "======================================================"
echo "Release to Bintray"
echo "======================================================"
echo ""
echo "Run: sbt -d -J-Xmx2048m ^publish"
echo "------------------------------------------------------"
if sbt -d -J-Xmx2048m ^publish ; then
  echo "Done: sbt -d -J-Xmx2048m ^publish"
else
  echo "Failed: sbt -d -J-Xmx2048m ^publish" 1>&2
  exit 1
fi
echo "======================================================"


#echo "======================================================"
#echo "Copy packaged files to deploy"
#echo "======================================================"
#echo "ls -l target/scala-*/sbt-*/*.jar"
#ls -l target/scala-*/sbt-*/*.jar
#echo ""
#echo "======================================================"
#if [ -d "target/ci" ]; then
#  echo "Clean up existing target/ci/*"
#  echo "rm -Rf target/ci/*"
#  rm -Rf target/ci/*
#  echo "------------------------------------------------------"
#fi
#echo "Create a folder to put all the binary files."
#echo "------------------------------------------------------"
#echo "mkdir -p target/ci/$PROJECT_BUILD_NAME"
#mkdir -p "target/ci/$PROJECT_BUILD_NAME"
#echo "ls -l target/ci/$PROJECT_BUILD_NAME"
#ls -l "target/ci/$PROJECT_BUILD_NAME"
#
#echo "------------------------------------------------------"
#echo "cp target/scala-*/sbt-*/*.jar target/ci/$PROJECT_BUILD_NAME/"
#cp target/scala-*/sbt-*/*.jar "target/ci/$PROJECT_BUILD_NAME/"
#echo "------------------------------------------------------"
#echo "ls -lR target/ci/$PROJECT_BUILD_NAME/"
#ls -lR "target/ci/$PROJECT_BUILD_NAME"
#echo "------------------------------------------------------"
#echo "Copying all binary files to 'target/ci', Done!"
#echo "======================================================"
