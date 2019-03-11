if [ "$1" == "--help" ]; then

  cat <<EOT
  
  
    ./$(dirname $0)
    
Makes the Downloads.md file, setting the links to download the required versions.

EOT

  exit 1
fi

# Simple (and buggy) function to get the value of a tag from some XML
function get_tag
{
  text="$1"
  tag="$2"

  result=$(echo -e "$text" | grep "<$tag>.*</$tag>" | sed -r s/"^\\s*<$tag>(.*)<\/$tag>\\s*\$"/'\1'/)
  echo $result
}

# Gets the tail about the last version that Nexus append to an artifact in the repo, after name and snapshot version. 
# This is usually a timestamp plus a build number. 
#
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

# Gets the last release of an artifact, wether it is a stable release or a snapshot 
function get_last_release
{
  url_root="$1"
  xml=$(wget -O - --quiet "$url_root/maven-metadata.xml")
  get_tag "$xml" latest
}

# Instantiates a template (sent in via stdin), replacing version-related placeholders with values regarding
# the last version of Mini and Ondex, so that the result will point to the correct URLs on the repo.
#
# $1 is the project path to consider, which is appended to our repo URL url_prefix (see the function body).
# 
# $2 is the placeholder used in the template to refer the URL tail pointing to the last snapshot in the repo
#    (eg, in http://nexus.url/.../installer-2.1.1-20190310.014031-104-packaged-distro.zip, it refers to
#    20190310.014031-104).
# 
# See Downloads_template.md for details.
#
function make_doc
{
  project_path="$1"
  snap_tail_var="$2"

  url_prefix="http://ondex.rothamsted.ac.uk/nexus/content/groups"
  stable_url_root="$url_prefix/public/$project_path"
  snap_url_root="$url_prefix/public-snapshots/$project_path"

  ver=$(get_last_release "$stable_url_root")
  snap_ver=$(get_last_release "$snap_url_root")

  snap_tail=$(get_snapshot_tail "$snap_url_root" "$snap_ver")
  snap_ver_no=$(echo "$snap_ver" | sed s/'-SNAPSHOT'/''/) # It has this shape on Nexus

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
	'net/sourceforge/ondex/apps/installer' \
	snapTailOndex \
| make_doc \
	'net/sourceforge/ondex/apps/ondex-mini' \
	snapTailMini
