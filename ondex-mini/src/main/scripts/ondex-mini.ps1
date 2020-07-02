$WORKDIR = split-path -parent $MyInvocation.MyCommand.Definition
Push-Location $WORKDIR
$MYDIR = Get-Location
Pop-Location

$WORKFLOW, $args = $args

$PLUGIN_ARGS = ""
while ( $args.count -gt 0 )
{
  $vardef, $args = $args
  $PLUGIN_ARGS = "$PLUGIN_ARGS -P$vardef"
}

$jar_path = Get-ChildItem $MYDIR/lib -Filter 'ondex-mini-*.jar'
$jar_path = "lib\$jar_path"

if ( "$JAVA_TOOL_OPTIONS" -eq "" ) {
  # So, let's set default JVM options here, unless you already have them from the outside
  # Note that this variable is part of standard Java (https://goo.gl/rrmXEX), so we don't need
  # to pass it to the java command below and possible further JVM invocations get it automatically too
  $env:JAVA_TOOL_OPTIONS = "-Xmx2G"
}

# Enable the debugger, this is sometimes needed by developers
# $env:JAVA_TOOL_OPTIONS = "env:$JAVA_TOOL_OPTIONS -Xdebug -Xnoagent -Djava.compiler=NONE
#	$env:JAVA_TOOL_OPTIONS = "env:$JAVA_TOOL_OPTIONS -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# JMX connection to be used via SSH (map both ports) and with client tools like jvisualvm
#JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS  -Dcom.sun.management.jmxremote.ssl=false
#$env:JAVA_TOOL_OPTIONS = "env:$JAVA_TOOL_OPTIONS -Dcom.sun.management.jmxremote.authenticate=false"
#$env:JAVA_TOOL_OPTIONS = "env:$JAVA_TOOL_OPTIONS -Dcom.sun.management.jmxremote.port=9010"
#$env:JAVA_TOOL_OPTIONS = "env:$JAVA_TOOL_OPTIONS -Dcom.sun.management.jmxremote.rmi.port=9011"
#$env:JAVA_TOOL_OPTIONS = "env:$JAVA_TOOL_OPTIONS  -Djava.rmi.server.hostname=localhost"
#$env:JAVA_TOOL_OPTIONS = "env:$JAVA_TOOL_OPTIONS -Dcom.sun.management.jmxremote.local.only=false"

$jcmd = "-Dondex.dir=$MYDIR/data -jar $jar_path -ubla -ptest -w$WORKFLOW $PLUGIN_ARGS"
#echo $jcmd
Start-Process -FilePath java -NoNewWindow -Wait -ArgumentList $jcmd
