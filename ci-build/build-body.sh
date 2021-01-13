
# The output is used to decide actions in build-after.sh
mvn $MAVEN_GOAL -N --settings "ci-build/maven-settings.xml" $MAVEN_ARGS | tee /tmp/build.out
