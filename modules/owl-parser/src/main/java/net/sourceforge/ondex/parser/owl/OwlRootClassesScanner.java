package net.sourceforge.ondex.parser.owl;

import java.util.stream.Stream;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

import info.marcobrandizi.rdfutils.jena.JenaGraphUtils;
import net.sourceforge.ondex.parser.ExploringMapper;
import net.sourceforge.ondex.parser.Scanner;

/**
 * A {@link Scanner} that gets all the top classes in an OWL ontology (ie, all declared classes that haven't
 * any subclass but owl:Thing). This is provided mainly for the
 * {@link ExploringMapper#getRootsScanner() rootScanner property} of the {@link OWLMapper}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Jun 2017</dd></dl>
 *
 */
public class OwlRootClassesScanner implements Scanner<OntModel, OntClass>
{
	@Override
	public Stream<OntClass> scan ( OntModel model )
	{
		return JenaGraphUtils.JENAUTILS.toStream ( model.listHierarchyRootClasses () );
	}
}