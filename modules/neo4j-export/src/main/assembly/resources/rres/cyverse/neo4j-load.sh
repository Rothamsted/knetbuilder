# TODO: comment me!

dataset="$1"
release="$2"

echo -e "\n\n\tUpgrading $dataset $release\n"

neo4j_home=/opt/software/neo4j

cd "$neo4j_home"
echo -e "\n---- Stopping Neo4j"
./bin/neo4j stop

cd data/databases
rm -Rf graph.db
drive_path="brandizi_rres_onedrive:knetminer-pub-data/$release/$dataset/$dataset-neo4j.tar.bz2"
echo -e "\n---- Getting new DB from '$drive_path'"
rclone cat "$drive_path" |tar xv --bzip2

echo -e "\n---- Restarting Neo4j"
cd ../..
./bin/neo4j start

echo -e "\n\n\tThe End\n"
