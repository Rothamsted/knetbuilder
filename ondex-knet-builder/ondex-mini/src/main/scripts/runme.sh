#!/usr/bin/env bash
workdir="`pwd`"
cd "`dirname $0`"
mydir="`pwd`"
cd "$workdir"

workflow=$1
shift

plugin_args=""
until [ -z $1 ]
do
  plugin_args="$plugin_args -P$1"
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

# Note: the JARS loaded by the plugin engine don't work anymore in recent J11 versions.
#
# So, we need to put everything in the classpath and then the plugin loader will just
# read the plugin descriptors. This seems to be an operation without side effects. 
# For the moment, there isn't an easy way to avoid this redundancy.
#
for jar in lib/*.jar plugins/*.jar
do
  [[ -z "$clspath" ]] || clspath="$clspath:"
  clspath="$clspath$mydir/$jar"
done

java -Dondex.dir="$mydir/data" -classpath "$clspath" net.sourceforge.ondex.OndexMiniMain \
     -ubla -ptest -w$workflow $plugin_args
