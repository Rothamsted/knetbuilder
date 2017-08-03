//package net.sourceforge.ondex.parser.owl;
//
//import java.util.Iterator;
//
//import org.apache.jena.ontology.OntClass;
//import org.apache.jena.ontology.OntModel;
//
///**
// * A {@link TopClassesProvider} that gets all the top classes in an OWL ontology (ie, all declared classes that haven't
// * any subclass but owl:Thing).
// *
// * @author brandizi
// * <dl><dt>Date:</dt><dd>22 Jun 2017</dd></dl>
// *
// */
//public class OwlRootClassesProvider implements TopClassesProvider
//{
//	@Override
//	public Iterator<OntClass> apply ( OntModel model )
//	{
//		return model.listHierarchyRootClasses ();
//	}
//}