set -e
if [ "$1" == "--help" ]; then

  cat <<EOT
  
  
    ./$(dirname $0)
    
Makes the Downloads.md file, setting the links to download the required versions.

EOT

  exit 1
fi

#IS_DEBUG=true
function debug
{
  [[ -z "$IS_DEBUG" ]] && return
  echo "$1" >&2	
}

# A simple client to the Nexus API to search info about a Maven artifact 
# This returns API JSON and might contain multiple results per query (eg, multiple versions),
# which are sorted by version number (Nexus applies a sensible order relation like 3.2.1 > 3.2) 
# 
# See the list of parameter below
#
function nexus_asset_search
{
	repo="$1"
	group="$2"
	artifact="$3"
	classifier="$4"
	ext="$5"
	version="$6" # This is the only one that is optional, last versions are fetched if omitted
	
	url="https://knetminer.org/artifactory"
	url="$url/service/rest/v1/search/assets?sort=version&direction=desc"
	
	url="$url&repository=$repo"
	url="$url&maven.groupId=$group"
	url="$url&maven.artifactId=$artifact"
	url="$url&maven.classifier=$classifier"
	url="$url&maven.extension=$ext"
	[[ -z "$version" ]] || url="$url&maven.baseVersion=$version"
	
  debug "--- URL: $url" 		

	curl -X GET "$url" -H "accept: application/json"
}

# Expects the Nexus search result coming from nexus_asset_search and extracts the first 'downloadUrl' field
# that is in that JSON, so the last version requested.
# 
# It has no parameters, since it processes its standard input and returns a result through the
# stdout.
#
function js_2_download_url
{
	egrep --max-count 1 '"downloadUrl" :' | sed -E s/'.*"downloadUrl" : "([^"]+)".*'/'\1'/
}

# Expects an .md template in the standard input and replaces placeholders for Maven module links, resolving
# them through the functions above. Returns the result through stdout, multiple invocations (one per module/placeholder)
# can be piped. See below the param details. See further below for usage examples. 
# 
function make_doc
{
	# Artifact coordinates for the Nexus API, same as nexus_asset_search()
	repo="$1"
	group="$2"
	artifact="$3"
	classifier="$4"
	ext="$5"
  # eg, 'ondexSnapUrl', will replace '%ondexSnapUrl%' with the URL found by Nexus to download
  # Ondex (as long as you passed the right artifact coordinates).
  #  
  placeholder="%$6%" 
  version="$7" # Optional, as above

	debug "--- make_doc '$repo' '$group' '$artifact' '$classifier' '$ext' '$placeholder' '$version'"

	download_url=$(nexus_asset_search \
  	$repo $group $artifact "$classifier" "$ext" "$version" |js_2_download_url)
  cat | sed -E s"|$placeholder|$download_url|g"
}


wdir="$(pwd)"
cd "$(dirname "$0")"
mydir="$(pwd)"
cd "$wdir"


# Gets all the download links by chaining multiple invocations of make_doc()/Nexus-API
#

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
  	packaged-distro zip ondexSnapUrl 3.0.1-SNAPSHOT \
| make_doc \
    maven-snapshots net.sourceforge.ondex.apps ondex-mini \
    packaged-distro zip miniSnapUrl 3.0.1-SNAPSHOT \
| make_doc \
    maven-snapshots net.sourceforge.ondex.modules rdf-export-2-cli \
    '' zip rdfExporterSnapUrl 3.0.1-SNAPSHOT \
| make_doc \
    maven-releases net.sourceforge.ondex.apps installer \
  	packaged-distro zip ondexRelUrl \
| make_doc \
    maven-releases net.sourceforge.ondex.apps ondex-mini \
    packaged-distro zip miniRelUrl \
| make_doc \
    maven-releases net.sourceforge.ondex.modules rdf-export-2-cli \
    '' zip rdfExporterRelUrl
    