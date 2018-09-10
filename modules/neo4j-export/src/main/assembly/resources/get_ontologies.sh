#
# Downloads relevant ontology files, which you typically want to upload into Neo together with data.
#
echo "  BioKNO main file"
wget https://raw.githubusercontent.com/Rothamsted/bioknet-onto/master/bioknet.owl \
     -O data/bioknet.owl

echo "  BioKNO Ondex mappings"
wget https://raw.githubusercontent.com/Rothamsted/bioknet-onto/master/bk_ondex.owl \
     -O data/bk_ondex.owl

echo "  Mappings to external ontologies"
wget https://raw.githubusercontent.com/Rothamsted/bioknet-onto/master/bk_mappings.ttl \
     -O data/bk_mappings.ttl
