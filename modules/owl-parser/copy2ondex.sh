target="$1"

if [ "$target" == "" ]; then
	
	cat <<EOT
	
	
	$(basename $0) <ONDEX binary path>
	
	Updates an ONDEX installation with the last build of the OWL parser plug-in. The parameter is the directory where
	the installer was exploded. The ONDEX and ONDEX mini applications are auto-updated during build, this script might
	be useful during debugging and alike.

EOT

  exit 1
fi

export TRNSF='cp --recursive --no-dereference --preserve --verbose'

cd "$(dirname $0)" 
cd target
unzip owl-parser-*-bundle.zip
cd owl-parser-plugin

$TRNSF config/* "$target/config/owl-parser"
$TRNSF examples/* "$target/data/examples/owl-parser"
$TRNSF lib/* "$target/lib"
