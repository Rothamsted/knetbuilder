#
# We use this to populate the Neo4j instances on our Cyverse servers, starting from dump files uploaded on line
# (currently, on OneDrive).
#
# TODO: comment the code
#

set -e
dataset="$1"
release="v$2"

export NEO4J_HOME=${NEO4J_HOME:=/opt/software/neo4j}
# You can also use NEO_TAR_OPTS

echo -e "\n\n\tUpgrading $dataset $release\n"

cd "$NEO4J_HOME"

echo -e "\n---- Stopping Neo4j"
./bin/neo4j stop

cd data/databases
rm -Rf graph.db
drive_path="brandizi_rres_onedrive:knetminer-pub-data/$release/$dataset/$dataset-neo4j.tar.bz2"
echo -e "\n---- Getting new DB from '$drive_path'"
rclone cat "$drive_path" |tar xv --bzip2 $NEO_TAR_OPTS

echo -e "\n---- Restarting Neo4j"
cd ../..
ulimit -n 40000
./bin/neo4j start

echo -e "\n\n\tThe End\n"
