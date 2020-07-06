cd $(dirname "$0")
     
cd src/main/assembly/resources
./update-ontologies.sh

wget https://raw.githubusercontent.com/Rothamsted/bioknet-onto/master/examples/bmp_reg_human/bkout/all.ttl \
     -O examples/bmp-reg-human.ttl
