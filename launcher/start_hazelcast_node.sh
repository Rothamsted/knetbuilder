#!/bin/bash

# Hazelcast node launcher script for use with the distributed memory
# implementation of an ONDEX graph. You can use this script to start
# Hazelcast data nodes on different machines in order to spread large
# ONDEX graphs over the resources of a cluster of machines.
#
# Please ensure that you use the same ondex-hazelcast.xml configuration
# file on every machine in the cluster. If you wish to run more than one
# ONDEX instance on a cluster of machines at the same time, then you will
# need to set an appropriate cluster name, username and password in this
# configuration file.
#
# By default, this script starts a Hazelcast data node with 1GB of RAM.
# You can customise the amount of RAM assigned to Hazelcast by altering
# the variable below.
#
# Author: Keith Flanagan

# The Java class to run
EXE_CLASSNAME="net.sourceforge.ondex.core.memorydist.HazelcastNode"
RAM="-Xmx1024M"

# Get the filename of this script
SCRIPT_NAME=$0
SCRIPT_PATH=`which $0`
PROG_HOME=`dirname $SCRIPT_PATH`

#MVN_OPTS="-o"

MVN="mvn $MVN_OPTS"

if [ ! -d $PROG_HOME/target/dependency ] ; then
    # Install dependencies into the program's target directory if necessary
    ( cd $PROG_HOME ; $MVN dependency:copy-dependencies )
fi

# Configure CLASSPATH
# Include program's compiled classes
CLASSPATH=$CLASSPATH:$PROG_HOME/target/classes

# Include .jar dependencies
for LIB in `find $PROG_HOME/target/dependency -name "*.jar"` ; do
    CLASSPATH=$CLASSPATH:$LIB
done

# Finally, start the application
java $RAM -cp $CLASSPATH $EXE_CLASSNAME $@



