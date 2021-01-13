set -e

cd `dirname "$0"`
mydir=`pwd`

export CI_DIR_URL="https://raw.githubusercontent.com/Rothamsted/knetminer-common/master/ci-build"
wget "$CI_DIR_URL/install.sh" -O install.sh
bash ./install.sh

export JAVA_TOOL_OPTIONS="-Xms1G -Xmx4G -Dsun.net.client.defaultConnectTimeout=600000 -Dsun.net.client.defaultReadTimeout=600000" 
# Let's make it less verbose
export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO"

echo bash ./build.sh
cd "$mydir"

# Jenkins will do internal stuff, such as updating download links and deploying
# on our servers.
# The API URL below is provided by this plug-in: https://plugins.jenkins.io/build-token-root/
# The token in configured in the job, in the Build Triggers section.
job='ondex-knet-builder_Update_Downloads_Links'
curl -X POST "https://knetminer.org/build/buildByToken/build?job=$job&token=$KNET_JENKINS_TOKEN" -o -
