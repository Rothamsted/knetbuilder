#!/bin/bash

export WORK_DIR="$(pwd)"
if [ "$RDF2PG_HOME" == "" ]; then
	cd "$(dirname $0)"
	export RDF2PG_HOME="$(pwd)"
	cd "$WORK_DIR"
fi

if  [ "$1" == '-h' ] || [ "$1" == '--help' ]; then
				cat <<EOT
	
	
*** ONDEX Neo4j Exporter, the ONDEX-to-RDF-to-Neo4j converter ***

	$(basename $0) [rdf2neo options] <RDF-FILE>...

	Invokes rdf2neo.sh to populate a neo4j database with an ONDEX network in RDF format, typically produced by the
	ONDEX RDF Export plug-in (i.e., a BK-Onto instance).

	It uses the configuration in ondex_config/, you need to review config.xml in that directory to link it to your
	Neo4j database.

	Requires JENA_HOME to be set.
	Uses a default Jena TDB in /tmp, possibly redefine it with --tdb (see rdf2neo.sh)
	
EOT
  exit 1
fi

config_path="$RDF2PG_HOME/ondex_config/config.xml"

echo -e "\n\n  rdf2neo.sh\n"
"$RDF2PG_HOME/rdf2neo.sh" --config "file://$config_path" ${1+"$@"} 

excode=$?
echo -e "\n\n  ondex2neo.sh finished"
exit $excode
