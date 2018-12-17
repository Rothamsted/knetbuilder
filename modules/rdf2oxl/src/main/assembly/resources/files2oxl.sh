#!/bin/bash
export WORK_DIR="$(pwd)"

# Parse Params 

iarg=1
while [[ $iarg -lt $# ]]
do
  key="${!iarg}" # variable indirection, equivalent to $i, where i is the value of iarg
  if [[ "$key" == '-t' ]] || [[ "$key" == '--tdb' ]]; then 
  	((iarg++))
  	tdb_path="${!iarg}";
  	((iarg++))
	fi
  if [[ "$key" == '-h' ]] || [[ "$key" == '--help' ]]; then
  	help_flag=1 
  	((iarg++))
	fi
	if [[ $key != -* ]]; then break; fi
done


if  [[ $# -lt 2 ]] || [[ $flag_help ]]; then
				cat <<EOT
	
	
	*** rdf2oxl, the RDF-to-OXL converter ***
	
	$(basename $0) [rdf2oxl.sh options] <OXL-FILE> <RDF-FILE>...
	
	Loads the files into the TDB triple store set by --tdb or TDB_PATH (uses a default in /tmp if none is set),
	then invokes rdf2oxl.sh passing this TDB and the -c option.
		
	Requires JENA_HOME to be set.	
	
EOT
  exit 1
fi

if [ "$JENA_HOME" == '' ]; then
	echo -e "\n\n  Please set JENA_HOME to the path of the Jena binaries, which includes bin/ utilities\n"
	exit 1
fi

if [ "$tdb_path" == "" ]; then
	export tdb_path=/tmp/rdf2neo_tdb
	echo "Generating new TDB at '$tdb_path'"
  rm -Rf "$tdb_path"
  mkdir "$tdb_path"
else
	tdb_flag=1
fi

ifiles=$(($iarg )) # The index of the first RDF file arg
args=( "$@" ) # Save args 
shift $ifiles # RDF files are now the only args

# See here for an explanation about ${1+"$@"} :
# http://stackoverflow.com/questions/743454/space-in-java-command-line-arguments 

echo -e "\n\n  Invoking tdbloader\n"
"$JENA_HOME/bin/tdbloader" --loc="$tdb_path" ${1+"$@"}


echo -e "\n\n  Invoking rdf2oxl.sh"
set -- ${args[@]::$ifiles} # Back to the original arguments minus the RDF files
if [[ $tdb_flag ]]; then
	./rdf2oxl.sh ${1+"$@"}
else
	./rdf2oxl.sh --tdb "$tdb_path" ${1+"$@"}
fi

excode=$?
echo -e "\n\n  files2oxl.sh finished"
exit $excode
