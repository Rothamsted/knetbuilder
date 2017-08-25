target="$1"

if [ "$target" == "" ]; then
	
	cat <<EOT
	
	
	$(basename $0) <ONDEX binary path>
	
	Updated an ONDEX installation with the last build of the OWL parser plug-in. The parameter is the directory where
	the installer was exploded. 

EOT

  exit 1
fi

export TRNSF='/bin/cp --recursive --no-dereference --preserve --verbose'
export TRNSF='/bin/cp -R -P -p -v'

cd "$(dirname $0)" 
cd target
unzip owl-parser-*-bundle.zip
cd owl-parser-plugin

$TRNSF config/* "$target/config/owl-parser"
$TRNSF examples/* "$target/data/examples/owl-parser"
$TRNSF lib/* "$target/lib"
