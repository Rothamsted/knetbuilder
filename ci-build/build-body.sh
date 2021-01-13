maven_goal="$1"

# The output is used to decide actions in build-after.sh
mvn $maven_goal -N --settings "ci-build/maven-settings.xml" $MAVEN_ARGS | tee /tmp/build.out
