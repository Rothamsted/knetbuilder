#!/bin/bash
MEMORY=2G
DATA=data/
echo "Running OVTK with $MEMORY and data dir $DATA edit runme.sh to change this amount"

for J in lib/*jar
do
  CLASSPATH=$CLASSPATH":"$J
done

#echo "Classpath: " $CLASSPATH
java -Xmx$MEMORY -Dondex.dir=$DATA -Dplugin.scan.lib=false -classpath $CLASSPATH net.sourceforge.ondex.workflow2.gui.Main $* 