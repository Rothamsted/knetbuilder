#!/bin/bash
# TODO: remove? We use the scripts in ovtk2 instead
#
DATA=data/
echo "Running OVTK on top of '$DATA'"

# See ondex-mini/src/main/scripts/runme.sh for details on why we include plugins/
# 
for J in lib/*.jar plugins/*.jar
do
  CLASSPATH=$CLASSPATH":"$J
done

if [ "$JAVA_TOOL_OPTIONS" == "" ]; then
  # So, let's set default JVM options here, unless you already have them from the outside
  # Note that this variable is part of standard Java (https://goo.gl/rrmXEX), so we don't need
  # to pass it to the java command below and possible further JVM invocations get it automatically too
  export JAVA_TOOL_OPTIONS="-Xmx2G"
fi

# Enable the debugger, this is sometimes needed by developers
JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Xdebug -Xnoagent -Djava.compiler=NONE
									 -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# JMX connection to be used via SSH (map both ports) and with client tools like jvisualvm
#JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS  -Dcom.sun.management.jmxremote.ssl=false
#                    -Dcom.sun.management.jmxremote.authenticate=false
#                    -Dcom.sun.management.jmxremote.port=9010
#                    -Dcom.sun.management.jmxremote.rmi.port=9011
#                    -Djava.rmi.server.hostname=localhost
#                    -Dcom.sun.management.jmxremote.local.only=false"

#echo "Classpath: " $CLASSPATH
java -Dondex.dir=$DATA -Dplugin.scan.lib=false -classpath $CLASSPATH \
     net.sourceforge.ondex.workflow2.gui.Main $*
