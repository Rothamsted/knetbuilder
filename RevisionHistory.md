# Revision History

## 2.0.1-SNAPSHOT
  * The next snapshot version

## 2.0
  * Graph Databases and Linked Data support:
    * [New RDF Exporter](modules/rdf-export-2) plug-in.
    * [Tool to export ONDEX RDF to Neo4j](modules/neo4j-export).
  * OWL Parser and base components for developers:
    * Complete review of the [generic parser](https://github.com/Rothamsted/ondex-base/tree/master/core/parser-api) architecture, 
      which now has clear separation between [data source decomposition](https://github.com/Rothamsted/ondex-base/blob/master/core/parser-api/src/main/java/net/sourceforge/ondex/parser/Scanner.java) 
      and [mapping](https://github.com/Rothamsted/ondex-base/blob/master/core/parser-api/src/main/java/net/sourceforge/ondex/parser/Mapper.java). 
    * The [OWL Parser](https://github.com/Rothamsted/ondex-knet-builder/tree/master/modules/owl-parser/src/main/java/net/sourceforge/ondex/parser/owl) 
      was changed accordingly.
    * Generic packages (ie, the generic parser library and a few [utilities](https://github.com/Rothamsted/ondex-base/tree/master/core/base/src/main/java/net/sourceforge/ondex/core/util))
      were migrated from the owl parser module to proper places.
    * Several mappings for common ontologies added.
  * Internal components to index data based on Lucene  
  	* All ONDEX code reviewed and migrated to Lucene 6.0.0.  
  	* Berkeley support removed from the main build.
  	* Bugfixing and Enhancements:  
  	  * Text mining module (to support new PubMed XML, [#12](https://github.com/Rothamsted/ondex-knet-builder/issues/12), [#13](https://github.com/Rothamsted/ondex-knet-builder/issues/12)).
  	  * New Concept Classes and and relation types added.

## v1.2
  * OWL parser
    * Some debugging and improvements in parsing GO, TO, FYPO ontologies
    * Options to customise accession prefixes in the final ONDEX graph
  * UniProt parser
    * GO terms added from accessions file [itself](https://github.com/Rothamsted/ondex-knet-builder/commit/b07c6469c7631a82bce65a46226abcaa0d3a2a00)
    * obo file [excluded](https://github.com/Rothamsted/ondex-knet-builder/commit/6c383b8d2be4455be0c132b1065947af40c715e0)
  * Graph-merge options [added to the tab-parser-2](https://github.com/Rothamsted/ondex-knet-builder/commit/c0d907b099999635ecf577f32fec9fb8e0310e48) to the tabular parser
