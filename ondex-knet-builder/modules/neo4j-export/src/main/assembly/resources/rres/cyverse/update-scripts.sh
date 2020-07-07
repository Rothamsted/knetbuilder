# Updates scripts on Cyverse, downloading from GH
# WARNING! This updates itself too!
#
cd "$(dirname $0)"

gh_url_base='https://raw.githubusercontent.com/Rothamsted/ondex-knet-builder/master/modules/neo4j-export/src/main/assembly/resources/rres/cyverse'

# Download only what we need on this server
host="$(hostname)"
base_neo4j_files='neo4j-load.sh reset-neo4j.sh'
[[ "$host" == 'brandizim-1' ]] && files="$base_neo4j_files covid19-load.sh"
[[ "$host" == 'brandizim-2' ]] && files="$base_neo4j_files"
[[ "$host" == 'brandizim-3' ]] && files='3store-load.sh'

files="$files update-scripts.sh"

for file in $files update-scripts.sh
do
	echo "-- $file"
	# We need options to disable any caching
	wget --no-cache "$gh_url_base/$file?x=$(date |tr ' ' '_')" -O $file
	[[ "$file" =~ \.sh ]] && chmod +x $file
done
