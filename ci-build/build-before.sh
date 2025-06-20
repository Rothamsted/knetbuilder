export MAVEN_BUILD_ARGS="$MAVEN_BUILD_ARGS --no-transfer-progress"
# Note: -Xms1G has problems with Neo4j 4.3
export JAVA_TOOL_OPTIONS="-Xmx4G -Dsun.net.client.defaultConnectTimeout=600000 -Dsun.net.client.defaultReadTimeout=600000" 
# Let's make it less verbose
export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO"



[[ "$IS_ACT_TOOL" == 'true' ]] || return 0

printf "\n\n\tInstalling Maven\n\n"
apt update
apt install -y maven
printf "\n\nMaven installed\n\n"
