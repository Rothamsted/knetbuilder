package net.sourceforge.ondex.parser.owl;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

/**
 * Returns a list of classes from an OWL data set. This is supposed to be top classes to be used by {@link OwlRecursiveRelMapper}
 * to start its job.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Jun 2017</dd></dl>
 *
 */
@FunctionalInterface
public interface TopClassesProvider extends Function<OntModel, Iterator<OntClass>>
{
}