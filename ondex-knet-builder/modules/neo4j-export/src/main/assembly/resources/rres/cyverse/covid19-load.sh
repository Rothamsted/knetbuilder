# Updates the specific CoViD datasets
# See https://github.com/Rothamsted/covid19-kg
#
export NEO4J_HOME=/opt/software/neo4j-covid19
export NEO_TAR_OPTS="--strip-components=4 covid19neo/neo4j/data/databases/graph.db"

set -e
cd "$(dirname $0)"
./neo4j-load.sh $1 $2
