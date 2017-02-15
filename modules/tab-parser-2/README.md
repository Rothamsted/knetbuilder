#Tabular Parser 2

A plug-in for the Integrator, which uses a simple XML to map from a particular TSV/CSV file structure 
(i.e., list of headers) to ONDEX entities. See the [mapping XML Schema](src/main/resources/tab_parser.xsd) and
its [auto-generated documentation](doc/index.html) for details. 
the plug-in receives a parameter pointing to an XML instance of such schema, which defines how your particular tabular
file (or set of tabular files having the same structure) defines ONDEX entities such as concepts and relations.
