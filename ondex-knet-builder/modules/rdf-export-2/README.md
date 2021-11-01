# ONDEX-to-RDF Exporter Plug-in

This is a new RDF exporter, which uses the BK-Onto ontology. Please see details [here](src/main/java/net/sourceforge/ondex/rdf/export/package-info.java).

[Here](src/main/assembly/resources/examples/text_mining_wf.xml) you find an example of invocation from  
ONDEX Mini's workflow.  

This can be downloaded and invoked as a command line tool, available from the [Ondex Downloads Page][10]. 
The CLI interface accepts the same parameters that the [plug-in][20] accepts.

An OXL [URIs addition plug-in][30] is available in the same package. Similarly, this is available both as an Ondex plug-in 
and command line tool.

[10]: https://github.com/Rothamsted/knetbuilder/wiki/Downloads#rdf-exporter
[20]: https://github.com/Rothamsted/knetbuilder/blob/master/ondex-knet-builder/modules/rdf-export-2/src/main/java/net/sourceforge/ondex/rdf/export/RDFExporterPlugin.java
[30]: https://github.com/Rothamsted/knetbuilder/blob/master/ondex-knet-builder/modules/rdf-export-2/src/main/java/net/sourceforge/ondex/rdf/export/URIAdditionPlugin.java
