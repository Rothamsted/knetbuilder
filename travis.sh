export JAVA_TOOL_OPTIONS="-Xms1G -Xmx4G -Dsun.net.client.defaultConnectTimeout=600000 -Dsun.net.client.defaultReadTimeout=600000" 
# We need to reduce Maven verbosity, Travis has an out limit
export JAVA_TOOL_OPTIONS="$JAVA_TOOL_OPTIONS -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO"

# More output shrinking via filtering
ignore_re='\[DEBUG\]| DEBUG |already added\, skipping|Copying|Adding|Loading|Installing'
ignore_re="$ignore_re|Visibility index built on|javadoc: warning"
ignore_re="($ignore_re)"

mvn clean --no-transfer-progress --batch-mode --settings settings.xml\
 | egrep --ignore-case --invert-match "$ignore_re"

exit ${PIPESTATUS[0]} # we don't care about the grep exit code, we want mvn result!
