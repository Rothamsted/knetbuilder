set -e
if [ "$1" == "--help" ]; then

  cat <<EOT
  
  
    ./$(dirname $0)
    
Makes the Downloads.md file, setting the links to download the required versions.

EOT

  exit 1
fi

function nexus_asset_search
{
	repo="$1"
	group="$2"
	artifact="$3"
	classifier="$4"
	ext="$5"
	version="$6"
	
	url="https://knetminer.org/artifactory"
	url="$url/service/rest/v1/search/assets?sort=version&direction=desc"
	
	url="$url&repository=$repo"
	url="$url&maven.groupId=$group"
	url="$url&maven.artifactId=$artifact"
	url="$url&maven.classifier=$classifier"
	url="$url&maven.extension=$ext"
	[[ -z "$version" ]] || url="$url&maven.baseVersion=$version"
		
	curl -X GET "$url" -H "accept: application/json"
}

function js_2_download_url
{
	egrep --max-count 1 '"downloadUrl" :' | sed -E s/'.*"downloadUrl" : "([^"]+)".*'/'\1'/
}

function make_doc
{
	repo="$1"
	group="$2"
	artifact="$3"
	classifier="$4"
	ext="$5"
  placeholder="%$6%"
  version="$7"

	download_url=$(nexus_asset_search \
  	$repo $group $artifact "$classifier" "$ext" "$ver" |js_2_download_url)
  cat | sed -E s"|$placeholder|$download_url|g"
}


wdir="$(pwd)"
cd "$(dirname "$0")"
mydir="$(pwd)"
cd "$wdir"

# TODO: For now, we report both 4-* version (JDK11) and 2-* version (JDK8)
# Later, we will omit the version param and fetch the last snapshot only
cat "$mydir/Downloads_template.md" \
| make_doc \
    maven-snapshots net.sourceforge.ondex.apps installer \
  	packaged-distro zip ondexJ11Url 4.0-SNAPSHOT \
| make_doc \
    maven-snapshots net.sourceforge.ondex.apps ondex-mini \
    packaged-distro zip miniJ11Url 4.0-SNAPSHOT \
| make_doc \
    maven-snapshots net.sourceforge.ondex.modules rdf-export-2-cli \
    '' zip rdfExporterJ11Url 4.0-SNAPSHOT \
| make_doc \
    maven-snapshots net.sourceforge.ondex.apps installer \
  	packaged-distro zip ondexSnapUrl 2.1.2-SNAPSHOT \
| make_doc \
    maven-snapshots net.sourceforge.ondex.apps ondex-mini \
    packaged-distro zip miniSnapUrl 2.1.2-SNAPSHOT \
| make_doc \
    maven-snapshots net.sourceforge.ondex.modules rdf-export-2-cli \
    '' zip rdfExporterSnapUrl 2.1.2-SNAPSHOT
# TODO: Releases not available yet, to be added
    