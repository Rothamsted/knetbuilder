# OWL Parser

An ONDEX parser module and plug-in that parses OWL ontology files.

You can customise the way OWL constructs are mapped to ONDEX entities by means of Spring XML configuration files. 

This is documented [here][0].  See also
[examples and pre-existing configurations](src/main/assembly/resources), as well as 
[tests](src/test/resources) and [code][1], for details.

[0]: https://github.com/Rothamsted/ondex-knet-builder/wiki/%5BOWL-Parser%5D---Mapping-OWL-Ontologies-to-ONDEX
[1]: src/main/java/net/sourceforge/ondex/parser/owl/package-info.java


These same examples/configurations are available in the ONDEX distribution
 ([snapshots][2], [releases][3]) and in the ONDEX Mini distribution ([snapshots][4], [releases][5]), under the `config/owl-parser` and `example/owl-parser` directories.

[2]: http://ondex.rothamsted.ac.uk/nexus/content/groups/public-snapshots/net/sourceforge/ondex/apps/installer
[3]: http://ondex.rothamsted.ac.uk/nexus/content/groups/public/net/sourceforge/ondex/apps/installer/
[4]: http://ondex.rothamsted.ac.uk/nexus/content/groups/public-snapshots/net/sourceforge/ondex/apps/ondex-mini
[5]: http://ondex.rothamsted.ac.uk/nexus/content/groups/public/net/sourceforge/ondex/apps/ondex-mini/