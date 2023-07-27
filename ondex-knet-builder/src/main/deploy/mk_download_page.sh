set -e

wget -O /tmp/download-page-utils.sh \
  https://raw.githubusercontent.com/Rothamsted/knetminer-common/master/download-page/download-page-utils.sh

. /tmp/download-page-utils.sh

wdir="`pwd`"
cd "`dirname "$0"`"
mydir="`pwd`"
cd "$wdir"


# Gets all the download links by chaining multiple invocations of make_doc()/Nexus-API
#
cat "$mydir/Downloads_template.md" \
| make_doc \
    maven-snapshots net.sourceforge.ondex.apps installer \
  	packaged-distro zip ondexSnapUrl \
| make_doc \
    maven-snapshots net.sourceforge.ondex.apps ondex-mini \
    packaged-distro zip miniSnapUrl \
| make_doc \
    maven-snapshots net.sourceforge.ondex.modules rdf-export-2-cli \
    '' zip rdfExporterSnapUrl \
| make_doc \
    maven-releases net.sourceforge.ondex.apps installer \
  	packaged-distro zip ondexRelUrl  \
| make_doc \
    maven-releases net.sourceforge.ondex.apps ondex-mini \
    packaged-distro zip miniRelUrl  \
| make_doc \
    maven-releases net.sourceforge.ondex.modules rdf-export-2-cli \
    '' zip rdfExporterRelUrl 
