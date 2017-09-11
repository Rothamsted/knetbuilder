# Revision History

## v1.2.1-SNAPSHOT
  * OWL Parser
    * Internals: complete review of the generic parser architecture (TODO: link), which now has clear separation between 
      data source decomposition (TODO: Scanner) and mapping (TODO: Mapper, PairMapper). 
      The OWL Parser (TODO:link) was changed accordingly. Generic packages (ie, the generic parser library and a few utilities
      TODO: link) were migrated from the owl parser module to proper places.
  * Internal components to index data based on Lucene
  	* Alle ONDEX code reviewed and migrated to Lucene 6.0.0.

## v1.2
  * OWL parser
    * Some debugging and improvements in parsing GO, TO, FYPO ontologies
    * Options to customise accession prefixes in the final ONDEX graph
  * UniProt parser
    * GO terms added from accessions file [itself](https://github.com/Rothamsted/ondex-knet-builder/commit/b07c6469c7631a82bce65a46226abcaa0d3a2a00)
    * obo file [excluded](https://github.com/Rothamsted/ondex-knet-builder/commit/6c383b8d2be4455be0c132b1065947af40c715e0)
  * Graph-merge options [added to the tab-parser-2](https://github.com/Rothamsted/ondex-knet-builder/commit/c0d907b099999635ecf577f32fec9fb8e0310e48) to the tabular parser
