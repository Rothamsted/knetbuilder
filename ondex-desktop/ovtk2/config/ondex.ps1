$mydir = split-path -parent $MyInvocation.MyCommand.Definition
Push-Location $mydir

$DATA = "data"
$OVTK_DATA = "config"

echo "Running Ondex on data dir '$DATA', config on '$OVTK_DATA'"
 
$CLASSPATH = ""
$jars = Get-ChildItem lib -Filter '*.jar'
foreach ($jar in $jars)
{
  $jar = Split-Path $jar -leaf
  if ( ! "$CLASSPATH" -eq "" ) {
    $CLASSPATH = "${CLASSPATH};"
  }
  $CLASSPATH = "${CLASSPATH}lib\$jar"
}

$jars = Get-ChildItem plugins -Filter '*.jar'
foreach ($jar in $jars)
{
  $jar = Split-Path $jar -leaf
  if ( ! "$CLASSPATH" -eq "" ) {
    $CLASSPATH = "${CLASSPATH};"
  }
  $CLASSPATH = "${CLASSPATH}plugins\$jar"
}


if ( "$env:JAVA_TOOL_OPTIONS" -eq "" ) {
  # So, let's set default JVM options here, unless you already have them from the outside
  # Note that this variable is part of standard Java (https://goo.gl/rrmXEX), so we don't need
  # to pass it to the java command below and possible further JVM invocations get it automatically too
  $env:JAVA_TOOL_OPTIONS = "-Xmx2G"
}

# Enable the debugger, this is sometimes needed by developers
# $env:JAVA_TOOL_OPTIONS = "$env:JAVA_TOOL_OPTIONS -Xdebug -Xnoagent -Djava.compiler=NONE
#	$env:JAVA_TOOL_OPTIONS = "$env:JAVA_TOOL_OPTIONS -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# JMX connection to be used via SSH (map both ports) and with client tools like jvisualvm
#$env:JAVA_TOOL_OPTIONS="$env:JAVA_TOOL_OPTIONS  -Dcom.sun.management.jmxremote.ssl=false
#$env:JAVA_TOOL_OPTIONS = "$env:JAVA_TOOL_OPTIONS -Dcom.sun.management.jmxremote.authenticate=false"
#$env:JAVA_TOOL_OPTIONS = "$env:JAVA_TOOL_OPTIONS -Dcom.sun.management.jmxremote.port=9010"
#$env:JAVA_TOOL_OPTIONS = "$env:JAVA_TOOL_OPTIONS -Dcom.sun.management.jmxremote.rmi.port=9011"
#$env:JAVA_TOOL_OPTIONS = "$env:JAVA_TOOL_OPTIONS  -Djava.rmi.server.hostname=localhost"
#$env:JAVA_TOOL_OPTIONS = "$env:JAVA_TOOL_OPTIONS -Dcom.sun.management.jmxremote.local.only=false"

$jcmd = "-Dondex.dir=$DATA -Dovtk.dir=$OVTK_DATA -Dplugin.scan.lib=false --class-path $CLASSPATH net.sourceforge.ondex.ovtk2.Main"
#echo $jcmd
Start-Process -FilePath java -ArgumentList $jcmd
