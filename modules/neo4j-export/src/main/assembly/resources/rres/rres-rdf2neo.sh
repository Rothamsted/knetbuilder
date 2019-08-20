set -e

if [ "$1" == '--backup' ]; then
	is_backup='true'; shift
fi

cfg_name="$1"
release="$2"


wdir=$(pwd)
cd $(dirname $0)
mydir=$(pwd)

cd ../..
sw_home=$(pwd)

cd "$mydir/.."
export RDF2NEO=$(pwd)

releases_dir=/var/lib/neo4j/data/db-dumps/releases
my_release_dir="$releases_dir/$release/$cfg_name"

cfg_path="$mydir/${cfg_name}-cfg.sh"
echo -e "\n\n\tRunning with the configuration at '$cfg_path'\n"
. "$cfg_path"

# rdf2neo options
#

export JENA_HOME="$sw_home/jena"
export RDF2NEO_TDB="$my_release_dir/rdf2neo-tdb"


export OPTS="-Dneo4j.boltUrl=bolt://localhost:$CFG_NEO_PORT"
export OPTS="$OPTS -Dneo4j.user=rouser -Dneo4j.password=rouser"

# Enables JMX monitoring (visualvm, etc)
export OPTS="$OPTS -Dcom.sun.management.jmxremote.port=5010 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
export OPTS="$OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$RDF2NEO/logs/jvm.dump"

export JAVA_TOOL_OPTIONS="-Xmx20G"


echo "--- Deleting existing TDB '$RDF2NEO_TDB'"
rm -Rf "$RDF2NEO_TDB"

echo "--- Stopping Neo4j"
sudo systemctl stop ${CFG_NEO_SERVICE_NAME}.service
if [ "$is_backup" == 'true' ]; then
	bkp_path="$my_release_dir/neo4j-bkp.db"
	echo "--- backing-up existing DB to '$bkp_path'"
	rm -rf "$bkp_path"
	mv --no-target-directory "$CFG_NEO_GRAPH_PATH" "$bkp_path"
else
	echo "Deleting existing DB '$CFG_NEO_GRAPH_PATH'"
	rm -Rf "$CFG_NEO_GRAPH_PATH"
fi
echo "--- Restarting empty Neo4j DB"
sudo systemctl start ${CFG_NEO_SERVICE_NAME}.service


if [ ! -e "data" ]; then
  echo "--- Getting common ontologies"
  ./get_ontologies.sh  
fi

rm -Rf "$my_release_dir/rdf/data"
cp -R data "$my_release_dir/rdf"

rdf_path="$my_release_dir/rdf/${cfg_name}.ttl"

echo -e "\n\n\tRunning rdf2neo on '$rdf_path'"
./ondex2neo.sh "$my_release_dir/rdf/data/"* "$rdf_path"

echo -e "\n\n\t$(basename $0), The end\n"
