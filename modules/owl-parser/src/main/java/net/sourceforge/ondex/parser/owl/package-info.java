/**
 * Implementation of an OWL parser for ONDEX, focused on ontology files. This is based on the 
 * net.sourceforge.ondex.parser framework.
 * 
 * {@link net.sourceforge.ondex.parser.owl.OWLMapper} is the entry point to to parse/map an OWL file. It typically 
 * bootstraps the file parsing by invoking a set of {@link net.sourceforge.ondex.parser.owl.OwlSubClassRelMapper}, one
 * per top OWL class, and then these mappers visit the ontology tree and invoking {@link net.sourceforge.ondex.parser.owl.OWLConceptMapper}
 * for each met OWL subclass. The latter maps an OWL class to and ONDEX concept (and uses {@link net.sourceforge.ondex.parser.owl.OWLConceptClassMapper}
 * to map the ancestor OWL top class to a concept class), as well as various other OWL elements to ONDEX concept attributes
 * (eg, {@link net.sourceforge.ondex.parser.owl.OWLAccessionsMapper}, {@link net.sourceforge.ondex.parser.owl.OWLNamesMapper})
 * further direct relations, such as 'part-of' or 'regulates' (using {@link net.sourceforge.ondex.parser.owl.OWLSimpleConceptRelMapper}.
 * 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
package net.sourceforge.ondex.parser.owl;
