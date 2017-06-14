#!/bin/bash
MEMORY=2G
DATA=data/
OVTK_DATA=config/
echo "Running OVTK with $MEMORY and data dir $DATA edit runme.sh to change this amount"

for J in lib/*jar
do
  CLASSPATH=$CLASSPATH":"$J
done

# Enable the debugger, this is sometimes needed by developers 
#DEBUG_OPTS='-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005'

#echo "Classpath: " $CLASSPATH
java $DEBUG_OPTS -Xmx$MEMORY -Dondex.dir=$DATA -Dovtk.dir=$OVTK_DATA -Dplugin.scan.lib=false -classpath $CLASSPATH \
     net.sourceforge.ondex.ovtk2.Main 