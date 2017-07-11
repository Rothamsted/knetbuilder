#!/bin/sh

# These are passed to the JVM. they're appended, so that you can predefine it from the shell
OPTS="$OPTS -Xms2G -Xmx4G"

# We always work with universal text encoding.
OPTS="$OPTS -Dfile.encoding=UTF-8"

# Monitoring with jconsole (end-user doesn't usually need this)
#OPTS="$OPTS 
# -Dcom.sun.management.jmxremote.port=5010
# -Dcom.sun.management.jmxremote.authenticate=false
# -Dcom.sun.management.jmxremote.ssl=false"
       
# Used for invoking a command in debug mode (end user doesn't usually need this)
#OPTS="$OPTS -Xdebug -Xnoagent"
#OPTS="$OPTS -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# You shouldn't need to change the rest
#
###

# These are Integrator default options, they can be overridden from the invoker (export CMD_OPTS='...')
CMD_OPTS='--plugins plugins --data data'
: ${CMD_OPTS:=--plugins plugins --data data}; 

 
cd "$(dirname $0)"
MYDIR="$(pwd)"

# Additional .jar files or other CLASSPATH directories can be set with this.
# (see http://kevinboone.net/classpath.html for details)  
export CLASSPATH="$CLASSPATH:$MYDIR:$MYDIR/lib/*"

# See here for an explanation about ${1+"$@"} :
# http://stackoverflow.com/questions/743454/space-in-java-command-line-arguments 

java \
  $OPTS net.sourceforge.ondex.OndexLauncherAll $CMD_OPTS ${1+"$@"}

EXCODE=$?

echo Java Finished. Quitting the Shell Too.
echo
exit $EXCODE
