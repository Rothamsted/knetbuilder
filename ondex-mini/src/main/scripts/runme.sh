#!/bin/bash
WORKFLOW=$1
shift

PLUGIN_ARGS=""
until [ -z $1 ]
do
  PLUGIN_ARGS="$PLUGIN_ARGS -P$1"
  shift
done

# Enable the debugger, this is sometimes needed by developers 
#DEBUG_OPTS='-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005'

java $DEBUG_OPTS -Xmx2G -Dondex.dir=./data -jar lib/ondex-mini-${project.version}.jar \
     -ubla -ptest -w$WORKFLOW $PLUGIN_ARGS
