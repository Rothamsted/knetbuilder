# Revision History

*This file was last reviewed on 2025-06-18*. **Please, keep this note up to date**.

## 7.0.2-SNAPSHOT
* **IMPORTANT**: RDF Exporter and components relying on URI generation for
  concepts and relations: URIs are now built using the ONDEX internal integer ID.
* Neo4j exporter: ondexId added to the indexes.
* Migration to JDK 21. **Older JDK not supported anymore**
* Various dependencies upgraded:
  * java2rdf (implying Jena for the RDF exporter)
  * rdf2neo
  * jutils

## 7.0.1
* Various dependencies upgraded.

## 7.0
* Spring migrated to version 6. **No backward compatibility guaranteed**
* Addition to [`jaxb.properties`](ondex-knet-builder/modules/oxl/src/main/resources/net/sourceforge/ondex/export/oxl/jaxb.properties), to cope with older Jakarta bugs.

## 6.0
* Migrated to Java 17. **WARNING: no backward compatibility guaranteed**.

## 5.2

### Bugfixes
* Fixing problem (via dependency upgrade) with rdfutils and null literals.


## 5.1
* Metadata descriptor (still experimental).
* Text mining plug-in:
  * stop words added to the text mining plug-in, [#65][i65].
  * various bugfixes.
* `GraphLabelsUtils`, case for selecting names with specie prefix added and used for the Cytoscape exporter [#681][i681]
* CytoscapeJS exporter:
  * Various bugfixes (eg, [#61][i61], [#66][i66], [#67][i67]).
* (Hopefully) all logging messages sent to the logger, not to `System.out` anymore.
* Indexing functionality added to `OndexPluginUtils`.
* Various dependency upgrades (eg, Jena, RDF utils)
  
[i681]: https://github.com/Rothamsted/knetminer/issues/681
[i65]: https://github.com/Rothamsted/knetbuilder/issues/65
[i61]: https://github.com/Rothamsted/knetbuilder/issues/61
[i66]: https://github.com/Rothamsted/knetbuilder/issues/66
[i67]: https://github.com/Rothamsted/knetbuilder/issues/67

### Internal changes
* KnetMiner initialiser module added from code in KnetMiner ([#51][i51]).
* Lucene module: bugfixing for certain [non-working queries][i53].
* Maven Assembly plug-in upgraded to latest version, many related files fixed.
* `javax.xml.bind` + `jaxb-runtime` replaced with `jakarta.xml.bind-api` + `jaxb-impl`, due to problems with J17 compatibility 
(moreover, the previous modules are a dead projects).


[i51]: https://github.com/Rothamsted/knetbuilder/issues/51
[i53]: https://github.com/Rothamsted/knetbuilder/issues/53

## 5.0
* Big changes to migrate to Java 11.
* New version of rd2neo integrated into the neo4j-export component.
* GraphSamplingPlugIn, better criteria to sample a representative graph.
* Code changes:
  * git submodules eliminated, we went back to a single repository codebase
  * POMs cleaning/refactoring
  * Some code cleaning/refactoring
  * Graph manipulation and access utilities added
  * CI Migration to GitHub Actions, common CI script from rdf-common
* Bugfixes and improvements to tab parser
* Bugfixes and improvements to Lucene indexing module
* Miscellanea:
  * Windows scripts updated
  * Some diagnostic messages added to the OXL parser
  * FASTA importer: AA sequence no longer imported
  * JSON exporter: minor changes to the output, to reflect Knetminer needs
 
## 3.0
* Various bugfixes in the components:
  * Neo4j-export
* Internal changes
  * Maven artifactory migrated to [our new Nexus](https://knetminer.org/artifactory/).
  * Maven POM linked to 
  * Cleaning and improvement of both code and Maven files

## 2.1
  * Improvements to OWL/Ondex mappings ([example](https://github.com/Rothamsted/ondex-knet-builder/commit/eff609d09550cc96f2ed877a91b45764aa6528e6)). 
  * Minor changes to the launching scripts ([example](https://github.com/Rothamsted/ondex-desktop/commit/0b2f5145207fb21553b682c78f81752b553eed09)).
  * Fixing problems with OXL Import/Export ([#12](https://github.com/Rothamsted/ondex-knet-builder/issues/12), [#14](https://github.com/Rothamsted/ondex-knet-builder/issues/14)).
  * Fixing issues with the FASTA parser ([1](https://github.com/Rothamsted/ondex-knet-builder/commit/3795afd8c10c3000bbc6f443dd0b33b5cd309f5a), [2](https://github.com/Rothamsted/ondex-knet-builder/commit/dbea4cd20bbbcfe2140a284c187d5fd4b66a5add)).
  * Internal changes:  
    * Improvements in the parser API ([1](https://github.com/Rothamsted/ondex-base/commit/4e3d238111a3367c7531b4815c0a777b1261ed6f), [2](https://github.com/Rothamsted/ondex-base/commit/7b3406761162ef0aa44f2706a349f341d3d8a9a1), [3](https://github.com/Rothamsted/ondex-base/commit/ff79d961a4f0fcf5c3a15d8ec7be99e694660419)).  
    * Minor review of the common type definitions (see [ondex_metadata.xml](https://github.com/Rothamsted/ondex-base/blob/master/datadir/src/main/resources/xml/ondex_metadata.xml)).  
    * Minor fixes to fasta-gff3 Parser and JSON Export plugins
    * Minor [review of some logging messages](https://github.com/Rothamsted/ondex-base/commit/38238b3fb0460a7d2e8417610b03309dfa5dfa74).  
  

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
  * All ONDEX code reviewed and migrated to Lucene 6.0.0.  
  * Berkeley support removed from the main build.
  * Bugfixing and Enhancements:  
  	 * Text mining module (to support new PubMed XML, [#12](https://github.com/Rothamsted/ondex-knet-builder/issues/12), [#13](https://github.com/Rothamsted/ondex-knet-builder/issues/12)).
  	 * New Concept Classes and and relation types added.


## 1.2
  * OWL parser
    * Some debugging and improvements in parsing GO, TO, FYPO ontologies
    * Options to customise accession prefixes in the final ONDEX graph
  * UniProt parser
    * GO terms added from accessions file [itself](https://github.com/Rothamsted/ondex-knet-builder/commit/b07c6469c7631a82bce65a46226abcaa0d3a2a00)
    * obo file [excluded](https://github.com/Rothamsted/ondex-knet-builder/commit/6c383b8d2be4455be0c132b1065947af40c715e0)
  * Graph-merge options [added to the tab-parser-2](https://github.com/Rothamsted/ondex-knet-builder/commit/c0d907b099999635ecf577f32fec9fb8e0310e48) to the tabular parser
