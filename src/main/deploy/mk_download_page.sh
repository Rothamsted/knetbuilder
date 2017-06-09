ver="$1"
snap_ver="$2"

if [ "$snap_ver" == "" ]; then

  cat <<EOT
  
  
    ./$(dirname $0) <stable-version> <snapshot-version> 
    
Makes the Downloads.md file, setting the links to download the required versions.

EOT

  exit 1
fi


function get_tag
{
  text="$1"
  tag="$2"

  result=$(echo -e "$text" | grep "<$tag>.*</$tag>" | sed -r s/"^\\s*<$tag>(.*)<\/$tag>\\s*\$"/'\1'/)
  echo $result
}

function get_snapshot_tail 
{
  url_prefix="$1"
  snap_ver="$2"
  
  url="$url_prefix/$snap_ver/maven-metadata.xml"
  xml=$(wget -O - --quiet "$url")
  
  ts=$(get_tag "$xml" timestamp)
  build=$(get_tag "$xml" buildNumber)
  
  echo "$ts-$build"
}

function make_doc
{
  snap_url_prefix="$1"
  ver="$2"
  snap_ver="$3"
  snap_tail_var="$4"

  snap_tail=$(get_snapshot_tail "$snap_url_prefix" "$snap_ver")
  snap_ver_no=$(echo "$snap_ver" | sed s/'-SNAPSHOT'/''/)

  cat \
    | sed s/"\%version%"/"$ver"/g \
    | sed s/'\%snapVersionNo\%'/"$snap_ver_no"/g \
    | sed s/"\%$snap_tail_var\%"/"$snap_tail"/g 
}


wdir="$(pwd)"
cd "$(dirname "$0")"
mydir="$(pwd)"
cd "$wdir"

cat "$mydir/Downloads_template.md" \
| make_doc \
	'http://ondex.rothamsted.ac.uk/nexus/content/groups/public-snapshots/net/sourceforge/ondex/apps/installer' \
	"$ver" \
	"$snap_ver" \
	snapTailOndex \
| make_doc \
	'http://ondex.rothamsted.ac.uk/nexus/content/groups/public-snapshots/net/sourceforge/ondex/apps/ondex-mini' \
	"$ver" \
	"$snap_ver" \
	snapTailMini \
>"$mydir/../../../Downloads.md"
	


