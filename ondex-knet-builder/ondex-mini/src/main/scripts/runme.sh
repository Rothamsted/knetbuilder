#!/bin/bash
WORKDIR=$(pwd)
cd $(dirname $0)
MYDIR=$(pwd)
cd "$WORKDIR"

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
#JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Xdebug -Xnoagent -Djava.compiler=NONE
#									 -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# JMX connection to be used via SSH (map both ports) and with client tools like jvisualvm
#JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS  -Dcom.sun.management.jmxremote.ssl=false
#                    -Dcom.sun.management.jmxremote.authenticate=false
#                    -Dcom.sun.management.jmxremote.port=9010
#                    -Dcom.sun.management.jmxremote.rmi.port=9011
#                    -Djava.rmi.server.hostname=localhost
#                    -Dcom.sun.management.jmxremote.local.only=false"

java -Dondex.dir="$MYDIR/data" -jar "$MYDIR"/lib/ondex-mini-*.jar \
     -ubla -ptest -w$WORKFLOW $PLUGIN_ARGS