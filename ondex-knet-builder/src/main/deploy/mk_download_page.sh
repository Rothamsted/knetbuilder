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

# TODO: The 3.0 specified below is to obtain the right release, rather than 3.0-RC
# I've filed a bug to Sonatype about this (https://issues.sonatype.org/browse/NEXUS-24220). 
# We need a more stable solution, like  results filtering.
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
  	packaged-distro zip ondexRelUrl 4.0RC2 \
| make_doc \
    maven-releases net.sourceforge.ondex.apps ondex-mini \
    packaged-distro zip miniRelUrl 4.0RC2 \
| make_doc \
    maven-releases net.sourceforge.ondex.modules rdf-export-2-cli \
    '' zip rdfExporterRelUrl 4.0RC2
