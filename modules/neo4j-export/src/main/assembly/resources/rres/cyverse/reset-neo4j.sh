set -e

cd "$(dirname $0)"
. ./last-versions.sh

[[ "$host" == 'brandizim-1' ]] && dataset='arabidopsis'
[[ "$host" == 'brandizim-2' ]] && dataset='wheat'

./neo4j-load.sh $dataset $KNETMINER_VERSION 

if [[ ! -z "$COVID19_VERSION" ]]; then
	echo CoViD-19, version $COVID19_VERSION
	./covid19-load.sh human-covid19 $COVID19_VERSION
fi
