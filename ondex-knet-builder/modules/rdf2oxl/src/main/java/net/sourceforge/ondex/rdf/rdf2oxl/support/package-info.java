/**
 * <p>The OXL/RDF converter works by means of a set of {@link net.sourceforge.ondex.rdf.rdf2oxl.support.QueryProcessor}s. 
 * Each of them is usually setup with a SPARQL query, it collects
 * {@link org.apache.jena.query.QuerySolution query result bindings} and it distributes chunk of them to some 
 * {@link net.sourceforge.ondex.rdf.rdf2oxl.support.QuerySolutionHandler}.</p>
 * 
 * <p>Such results are usually pointers of resources, such as concept or relation URIs, and the query solution handler 
 * is responsible for running a further 'graph query', which is a SPARQL CONSTRUCT query getting details for the 
 * corresponding URIs (eg, concept or relation details). Thus, such query is parameterised on a list of URIS, by means
 * of a placeholder inside a VALUE clause (see examples in src/main/resources/oxl_templates). The graph achieved from 
 * such CONSTRUCT query is fetched as JSON-LD format (via Jena) and then the @graph object in JSON-LS is passed to 
 * {@link net.sourceforge.ondex.rdf.rdf2oxl.support.freemarker.FreeMarkerHelper} as a map, together with a proper XML/OXL
 * template (eg., to render the XML corresponding to a concept) and eventually the OXL is output by the FreeMarker
 * engine.</p>
 * 
 * <p>Some minor processing happens in between, see {@link net.sourceforge.ondex.rdf.rdf2oxl.support.QuerySolutionHandler}
 * for details.</p>
 * 
 * <p>There are multiple instances of processors and handlers, plus other configuration parameters, which are put together
 * via Spring, the main file for doing so is {@code default_beans.xml} in {@code src/main/resources}. Among other things,
 * this defines a list of @ItemConfiguration(s), which is injected into 
 * {@link net.sourceforge.ondex.rdf.rdf2oxl.Rdf2OxlConverter}, mainly to let it know the combinations of 
 * processor + handler + SPARQL selector/graph queries + OXL template to be used to process each resource type in OXL
 * (i.e., there's one {@link net.sourceforge.ondex.rdf.rdf2oxl.support.ItemConfiguration} for Concept, another for Relation, 
 * etc.).</p>
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Jul 2018</dd></dl>
 *
 */
package net.sourceforge.ondex.rdf.rdf2oxl.support;