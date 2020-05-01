
cd "$(dirname $0)"

gh_url_base='https://raw.githubusercontent.com/Rothamsted/ondex-knet-builder/master/modules/neo4j-export/src/main/assembly/resources/rres/cyverse'
host="$(hostname)"

[[ "$host" == 'brandizim-1' ]] && files='neo4j-load.sh covid19-load.sh'
[[ "$host" == 'brandizim-2' ]] && files='neo4j-load.sh'
[[ "$host" == 'brandizim-3' ]] && files='3store-load.sh'

for file in $files update-scripts.sh
do
	echo "-- $file"
	wget "$gh_url_base/$file" -O $file
	[[ "$file" =~ '\.sh' ]] && chmod +x $file
done
