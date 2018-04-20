#!/bin/bash
WORKFLOW=$1
shift

PLUGIN_ARGS=""
until [ -z $1 ]
do
  PLUGIN_ARGS="$PLUGIN_ARGS -P$1"
  shift
done

if [ "$JAVA_TOOL_OPTIONS" == "" ]; then
  # So, let's set default JVM options here, unless you already have them from the outside
  # Note that this variable is part of standard Java (https://goo.gl/rrmXEX), so we don't need
  # to pass it to the java command below and possible further JVM invocations get it automatically too
  export JAVA_TOOL_OPTIONS="-Xmx2G"
fi

# Enable the debugger, this is sometimes needed by developers
#JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Xdebug -Xnoagent -Djava.compiler=NONE"
#JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

java -Dondex.dir=./data -jar lib/ondex-mini-${project.version}.jar \
     -ubla -ptest -w$WORKFLOW $PLUGIN_ARGS
