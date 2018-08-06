echo "  Updating BioKNO main file"
wget https://raw.githubusercontent.com/Rothamsted/bioknet-onto/master/bioknet.owl \
     -O src/main/resources/bioknet.owl

echo "  Updating BioKNO Ondex Mappings"
wget https://raw.githubusercontent.com/Rothamsted/bioknet-onto/master/bk_ondex.owl \
     -O src/test/resources/oxl_templates_test/bk_ondex.owl
     
echo "  The End."
