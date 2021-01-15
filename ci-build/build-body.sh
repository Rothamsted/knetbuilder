
# Yes, this is identical to the default, but sometimes we prefix this with 'echo' as a way to quick-test 
mvn $MAVEN_GOAL --settings ci-build/maven-settings.xml $MAVEN_ARGS
