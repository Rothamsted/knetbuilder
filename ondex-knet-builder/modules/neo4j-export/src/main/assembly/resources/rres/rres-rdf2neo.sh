# TODO: Comment me!
#
set -e # Stop upon error

while true
do
	case "$1" in
		--backup) is_backup='true'; shift;;
		--tdb) is_tdb_mode='true'; shift;;
		*) break;;
	esac
done

cfg_name="$1"
release="v$2"


wdir=$(pwd)
cd $(dirname $0)
mydir=$(pwd)

cd ../..
sw_home=$(pwd)


cd "$mydir/.."
# TODO: remove
#export RDF2PG_HOME=$(pwd)

releases_dir=/var/lib/neo4j/data/db-dumps/releases
my_release_dir="$releases_dir/$release/$cfg_name"

cfg_path="$mydir/${cfg_name}-cfg.sh"
echo -e "\n\n\tRunning with the configuration at '$cfg_path'\n"
. "$cfg_path"

# rdf2neo options
#

export JENA_HOME="$sw_home/jena"
tdb_path="$my_release_dir/rdf2neo-tdb"


export OPTS="-Dneo4j.boltUrl=bolt://localhost:$CFG_NEO_PORT"
export OPTS="$OPTS -Dneo4j.user=rouser -Dneo4j.password=rouser"

# Enables JMX monitoring (visualvm, etc)
#export OPTS="$OPTS -Dcom.sun.management.jmxremote.port=5010 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
#export OPTS="$OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$RDF2NEO/logs/jvm.dump"

export JAVA_TOOL_OPTIONS="-Xmx20G"

if [ "$is_tdb_mode" != 'true' ]; then
	echo "--- Deleting existing TDB '$tdb_path'"
	rm -Rf "$tdb_path"
fi

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

if [ "$is_tdb_mode" != 'true' ]; then
	
	echo "--- Getting common ontologies"
	
	rm -Rf "$my_release_dir/rdf/ontologies"
	mkdir "$my_release_dir/rdf/ontologies"
  ./get_ontologies.sh "$my_release_dir/rdf/ontologies"  


	rdf_path="$my_release_dir/rdf/${cfg_name}.ttl"

	echo -e "\n\n\tRunning rdf2neo on '$rdf_path'"
	./ondex2neo.sh \
		"$my_release_dir/rdf/ontologies/"*.* \
		"$my_release_dir/rdf/ontologies/ext/"*.* \
		"$rdf_path"
else
	sleep 10 # Neo4j needs time to restart
	./tdb2neo.sh --config ondex_config/config.xml --tdb "$tdb_path"
fi
	
echo -e "\n\n\t$(basename $0), The end\n"

