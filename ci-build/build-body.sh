
# The output is used to decide actions in build-after.sh
mvn $MAVEN_GOAL --settings "ci-build/maven-settings.xml" $MAVEN_ARGS
