#
# Downloads relevant ontology files, which are needed to make rdf2oxl to work correctly.
#

cd $(dirname "$0")

echo -e "\n  BioKNO main file"
wget https://raw.githubusercontent.com/Rothamsted/bioknet-onto/master/bioknet.owl \
     -O data/bioknet.owl

echo -e "\n  BioKNO Ondex mappings"
wget https://raw.githubusercontent.com/Rothamsted/bioknet-onto/master/bk_ondex.owl \
     -O data/bk_ondex.owl

#echo -e "\n  Mappings to external ontologies"
#wget https://raw.githubusercontent.com/Rothamsted/bioknet-onto/master/bk_mappings.ttl \
#     -O data/bk_mappings.ttl
