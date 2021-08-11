#!/usr/bin/env bash
set -e

# Consider the SLURM environment (many thanks to slackoverflow:56962129)
if [[ -n "$SLURM_JOB_ID" ]];  then
	mypath=`scontrol show job $SLURM_JOBID |grep 'Command=' |sed -r s/'.*Command=(.+\.sh).*'/'\1'/`
else
  mydir="$0"
fi
cd `dirname "$mydir"`
mydir=`pwd`
cd "$workdir"


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

# See runme.sh for details on why we include plugins/
# 
for jar in lib/*.jar plugins/*.jar
do
  [[ -z "$classpath" ]] || classpath="$classpath:"
  classpath="$classpath$jar"
done

# See here for an explanation about ${1+"$@"} :
# http://stackoverflow.com/questions/743454/space-in-java-command-line-arguments 

java \
	-Dlog4j.configuration="file:$mydir/data/log4j.properties" \
  -classpath "$classpath" \
  net.sourceforge.ondex.mini.MiniPlugInCLI ${1+"$@"}
rval=$?

echo -e "\nJava finished\n\n"
exit $rval
