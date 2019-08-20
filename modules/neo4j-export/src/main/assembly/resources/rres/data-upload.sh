set -e

cfg_name="$1"
release="$2"

cd $(dirname $0)
mydir=$(pwd)

releases_dir=/var/lib/neo4j/data/db-dumps/releases
my_release_dir="$releases_dir/$release/$cfg_name"

cd "$my_release_dir"


echo -e "\n\n\tUploading '$(pwd)'\n"

echo "--- Creating RDF tarball"
tar cv --bzip2 -f "$cfg_name"-rdf.tar.bz2 rdf

echo "--- Dumping Neo4j (Server needs to be stopped)"
. "$mydir/$cfg_name"-cfg.sh
cd "$CFG_NEO_GRAPH_PATH/.."
sudo systemctl stop ${CFG_NEO_SERVICE_NAME}.service
tar cv --bzip2 -f "$my_release_dir/$cfg_name"-neo4j.tar.bz2 graph.db 
sudo systemctl start ${CFG_NEO_SERVICE_NAME}.service

echo "--- Uploading to OneDrive"
cd "$my_release_dir"
rclone_dir="onedrive:knetminer-pub-data/$release/$cfg_name"
rclone mkdir "$rclone_dir"

for tail in '.oxl' '-rdf.tar.bz2' '-neo4j.tar.bz2' 
do
	src="$cfg_name$tail"
	echo "--- Uploading '$src'"
	rclone copy "$src" "$rclone_dir"
done 

echo -e "\n\n\tThe End."
