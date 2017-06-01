/**
 * <h1>The ONDEX Mapping Framework</h1>
 * 
 * <p>This package defines a generic framework to map external structures (e.g., a data format) to ONDEX structures like
 * concepts and concept classes.</p>
 *
 * <p>The idea is that, when you define a new mapper or parser, let's say to import a data format, you start from an 
 * implementation of {@link net.sourceforge.ondex.parser.GraphMapper}, which typically would produce a graph from an 
 * input source by composing other {@link net.sourceforge.ondex.parser.ONDEXMapper mappers}, such as 
 * {@link net.sourceforge.ondex.parser.ConceptMapper} or {@link net.sourceforge.ondex.parser.RelationMapper} (or 
 * helper components like {@link net.sourceforge.ondex.parser.RelationBuilder}.</p> 
 * 
 * <p>Components are designed to be used with a <a href = "http://harrewijnen.net/how-to-anxiety/">IoC</a> framework 
 * such as <a href = "https://docs.spring.io/spring/docs/current/spring-framework-reference/html/beans.html">Spring Beans</a>,
 * which is the one we use.</p>
 * 
 * <p>You can see an example of how this can be used in the implementation of the OWL Parser.</p>
 *
 * @author brandizi
 * 
 * <dl><dt>Date:</dt><dd>4 Apr 2017</dd></dl>
 *
 */
package net.sourceforge.ondex.parser;