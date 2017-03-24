#!/bin/bash
WORKFLOW=$1
shift

PLUGIN_ARGS=""
until [ -z $1 ]
do
  PLUGIN_ARGS="$PLUGIN_ARGS -P$1"
  shift
done

for J in lib/*jar
do
  CLASSPATH=$CLASSPATH":"$J
done

java -Xmx2G -Dondex.dir=./data -jar lib/ondex-mini-${project.version}.jar -ubla -ptest -w$WORKFLOW $PLUGIN_ARGS
