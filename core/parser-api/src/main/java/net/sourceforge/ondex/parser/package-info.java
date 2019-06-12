/**
 * <p>Generic parser components. This package attempts to define a general architecture that most of the ONDEX parsers 
 * might based on. The idea is that a parser starts by decomposing a data document into smaller data items, by means of
 * a {@link net.sourceforge.ondex.parser.Scanner}, then each item is mapped onto ONDEX elements, such as concepts, 
 * accessions or relations, by means of {@link net.sourceforge.ondex.parser.Mapper mappers}. This mechanism can be
 * further decomposed, so that, for instance, a data source item used to spawn an 
 * {@link net.sourceforge.ondex.core.ONDEXConcept} might be passed to other scanners and mappers, to produce 
 * components like the concept's {@link net.sourceforge.ondex.core.ConceptAccession}s and names (see 
 * {@link net.sourceforge.ondex.parser.DefaultConceptMapper}, where this approach is actually implemented).</p>
 *
 * <p>An example of parser might be a parser to convert some XML data into ONDEX elements. In such a case, there will likely
 * be a scanner that would get top-level XML elements such as Protein or Gene and would pass them to concept mappers.</p>
 * 
 * <p>Another example would be a CSV parser, where a scanner would go through the rows of the file and would map each row
 * to a {@link net.sourceforge.ondex.parser.ConceptBasedRelMapper}, which, in turn, would be equipped with 
 * {@link net.sourceforge.ondex.parser.ConceptMapper}s.</p>
 * 
 * <p>Another example is the <a href = "https://github.com/Rothamsted/ondex-knet-builder/tree/master/modules/owl-parser">OWL parser</a>.</p>
 * 
 * <p>The components defined for the parser API are designed to allow for the configuration of a parser by means of a 
 * Spring Bean configuration file. The OWL parser is a good starting point to see that in action.</p> 
 *
 * <p>TODO: we still need to define the skeleton component to realise a parser that scans pairs of concepts/relations, such 
 * as a CSV parser.</p>
 *  
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 May 2017</dd></dl>
 *
 */
package net.sourceforge.ondex.parser;