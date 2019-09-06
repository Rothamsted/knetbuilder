#
# Downloads relevant ontology files, which you typically want to upload into Neo together with data.
#

target_dir=${1:=data}


function get_onto ()
{
	title="$1"
	url="$2"
	f_name="$3"
	
	echo -e "\t$title"
	wget "$url" -O "$target_dir/$f_name"
}

get_onto 'BioKNO main file' \
  https://raw.githubusercontent.com/Rothamsted/bioknet-onto/master/bioknet.owl bioknet.owl
get_onto 'BioKNO Ondex mappings' \
  https://raw.githubusercontent.com/Rothamsted/bioknet-onto/master/bk_ondex.owl bk_ondex.owl
get_onto 'Mappings to external ontologies' \
	https://raw.githubusercontent.com/Rothamsted/bioknet-onto/master/bk_mappings.ttl data/bk_mappings.ttl

echo -e "\n---- Additional ontologies, to be used for the triple store"

mkdir --parents "$target_dir/ext"

get_onto "schema.org" http://schema.org/version/latest/schema.ttl ext/schema.ttl

# TODO: bioschemas!
get_onto "BioPAX" http://www.biopax.org/release/biopax-level3.owl ext/biopax-level3.owl
get_onto "SIO" http://semanticscience.org/ontology/sio.owl ext/sio.owl
get_onto "dcterms:" http://dublincore.org/2012/06/14/dcterms.rdf ext/dcterms.rdf
get_onto "dc:" http://dublincore.org/2012/06/14/dcelements.rdf ext/dcelements.rdf
get_onto "SKOS" http://www.w3.org/TR/skos-reference/skos.rdf ext/skos.rdf
  

