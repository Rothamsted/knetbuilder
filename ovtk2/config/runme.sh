#!/bin/bash
MEMORY=1200M
DATA=data/
OVTK_DATA=config/
echo "Running OVTK with $MEMORY and data dir $DATA edit runme.sh to change this amount"

for J in lib/*jar
do
  CLASSPATH=$CLASSPATH":"$J
done

#echo "Classpath: " $CLASSPATH
java -Xmx$MEMORY -Dondex.dir=$DATA -Dovtk.dir=$OVTK_DATA -Dplugin.scan.lib=false -classpath $CLASSPATH net.sourceforge.ondex.ovtk2.Main 