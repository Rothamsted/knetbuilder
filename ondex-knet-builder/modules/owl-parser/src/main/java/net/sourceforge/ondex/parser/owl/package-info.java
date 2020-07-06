/**
 * <p>Implementation of an OWL parser for ONDEX, focused on ontology files. This is based on the 
 * net.sourceforge.ondex.parser framework.</p>
 * 
 * <p>{@link net.sourceforge.ondex.parser.owl.OWLMapper} is the entry point to parse/map an 
 * {@link org.apache.jena.ontology.OntModel OWL ontology}, for instance one loaded from an .owl file. This is 
 * an {@link net.sourceforge.ondex.parser.ExploringMapper}, which means it is able to follow relations like 
 * {@code owl:subClassOf}, as in {@link net.sourceforge.ondex.parser.owl.OWLSubClassScanner} and to build 
 * a corresponding ONDEX graph with the OWL classes that it meets. Other relations it is able to follow are 
 * {@link net.sourceforge.ondex.parser.owl.OWLSomeScanner} and {@link net.sourceforge.ondex.parser.owl.OWLEqIntersctScanner}, 
 * which are mappers useful for relations like part-of or participates-in, which usually are represented by means
 * of OWL axioms that cobine owl:someValuesFrom and owl:equivalentClass.</p>
 * 
 * <p>OWL parsers are configured by means of Spring Beans XML files. This allows for defining details about the particular
 * ontology that is being imported in ONDEX. See examples (in the parser's unit tests and distribution) 
 * and documentation (TODO).</p> 
 * 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
package net.sourceforge.ondex.parser.owl;
