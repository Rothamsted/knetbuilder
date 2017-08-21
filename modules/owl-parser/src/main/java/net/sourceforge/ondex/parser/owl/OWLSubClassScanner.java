package net.sourceforge.ondex.parser.owl;

import static info.marcobrandizi.rdfutils.jena.JenaGraphUtils.JENAUTILS;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;

import info.marcobrandizi.rdfutils.jena.JenaGraphUtils;
import net.sourceforge.ondex.parser2.Scanner;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Aug 2017</dd></dl>
 *
 */
public class OWLSubClassScanner implements Scanner<OntClass, OntClass>
{
	@Override
	public Stream<OntClass> scan ( OntClass parent )
	{
		return JENAUTILS.toStream ( parent.listSubClasses ( true ), true )
		.filter ( cls -> !cls.isAnon () ); // These are usually other restrictions and we catch them elsewhere
	}
}
