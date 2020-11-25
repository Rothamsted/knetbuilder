
# TODO: factorise this fragment into knetminer-common
#
if [[ "$TRAVIS_EVENT_TYPE" == "cron" ]]; then
	
	# Travis's cron doesn't consider if there have been changes or not, so we rely on 
	# git commits to check that. This isn't perfect (eg, last build could have failed due to network problems,
	# not necessarily the code itself), but good enough in most cases. 
	# TODO: see if the date of the last successful build can be get from the Travis API.
	#
	nchanges=$(git log --since '24 hours ago' --format=oneline | wc -l)
	if [[ $(($nchanges)) == 0  ]]; then
		cat <<EOT


	This is a cron-triggered build and the code didn't change since the latest build, so we're not rebuilding.
	This is based on github logs (--since '24 hours ago'). Please, launch a new build manually if I didn't get it right.
	
EOT
	exit
	fi
fi

if [[ "$IS_SIMPLE_BUILD" == 'true' ]]; then
  echo "Building the ondex-knet-builder subtree only"
  cd ondex-knet-builder
fi

export JAVA_TOOL_OPTIONS="-Xms1G -Xmx4G -Dsun.net.client.defaultConnectTimeout=600000 -Dsun.net.client.defaultReadTimeout=600000" 
# We need to reduce Maven verbosity, Travis has an out limit
export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO"

# More output shrinking via filtering
ignore_re='\[DEBUG\]| DEBUG |already added\, skipping|Copying|Adding|Loading|Installing'
ignore_re="$ignore_re|Visibility index built on|javadoc: warning"
ignore_re="($ignore_re)"

mvn deploy --no-transfer-progress --batch-mode --settings maven-settings.xml\
 | egrep --ignore-case --invert-match "$ignore_re"

exit_code=${PIPESTATUS[0]} # we don't care about the grep exit code, we want mvn result!

if [[ "$exit_code" == 0 ]]; then
  # Jenkins will do internal stuff, such as updating download links and deploying
  # on our servers.
  # The API URL below is provided by this plug-in: https://plugins.jenkins.io/build-token-root/
	# The token in configured in the job, in the Build Triggers section.
  job='ondex-knet-builder_Update_Downloads_Links'
	curl -X POST "http://ondex.rothamsted.ac.uk/build/buildByToken/build?job=$job&token=$KNET_JENKINS_TOKEN"  
else
	exit $exit_code
fi


