#!/usr/bin/env bash
WORKDIR=$(pwd)
cd $(dirname $0)
MYDIR=$(pwd)
cd "$WORKDIR"

# TODO: this a provisional experiment, improve and comment.
 
# See runme.sh
[[ -z "$JAVA_TOOL_OPTIONS" ]] && export JAVA_TOOL_OPTIONS="-Xmx2G"

# TODO: get rid of these deps, they're relics now superseded by the SDK
black_lst='(sjsxp-1.0.2|stax-api-1.0|xml-apis-1.4.01)'

for j in lib/*.jar plugins/*.jar
do
	[[ "$j" =~ ${black_lst}\.jar ]] && continue
  [[ -z "$cp" ]] || cp="$cp:"
  cp="$cp$j"
done

groovysh -cp $cp

