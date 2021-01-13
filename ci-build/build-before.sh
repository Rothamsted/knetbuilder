export JAVA_TOOL_OPTIONS="-Xms1G -Xmx4G -Dsun.net.client.defaultConnectTimeout=600000 -Dsun.net.client.defaultReadTimeout=600000" 
# Let's make it less verbose
export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO"
