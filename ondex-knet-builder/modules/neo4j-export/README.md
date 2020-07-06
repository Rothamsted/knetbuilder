# The Ondex-to-Neo4j Export Tool

This command line tool is used to populate a [Neo4j graph database](https://neo4j.com) with the RDF obtained from 
the [Ondex RDF Exporter](https://github.com/Rothamsted/ondex-knet-builder/tree/master/modules/rdf-export-2).

This is based on the [rdf2neo command line tool](https://github.com/Rothamsted/rdf2neo/tree/master/rdf2neo-cli), 
which maps RDF to Neo4j using a set of mapping files based on SPARQL queries and Spring. 

Technically, this Ondex tool is a simple wrapper that invokes rdf2neo using a configuration specific to the RDF 
yield from the Ondex RDF Exporter. 

This is done by the [ondex2neo.sh](src/main/assembly/resources/ondex2neo.sh) entry point script, which passes the 
[Ondex-specific mappings](https://github.com/Rothamsted/ondex-knet-builder/tree/master/modules/neo4j-export/src/main/assembly/resources/ondex_config) to the  [rdf2neo.sh](https://github.com/Rothamsted/ondex-knet-builder/blob/master/modules/neo4j-export/src/main/assembly/resources/ondex2neo.sh) script provided by rdf2neo.

**Note that technically this is not an Ondex plug-in.** You cannot invoke this tool from within the [workflow 
engine](https://github.com/Rothamsted/ondex-knet-builder/wiki/Downloads), because it requires to load its input 
from RDF files, so it wouldn't be useful to integrate this with Ondex. If you're obtaining RDF from a workflow,
write a script that invokes Ondex mini and the Neo4j export too.
