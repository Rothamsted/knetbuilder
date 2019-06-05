package net.sourceforge.ondex.parser.owl;

import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.rdf.model.Resource;

public class OWLAllFromScanner extends OWLRestrictionScanner<AllValuesFromRestriction> 
{
	@Override
	protected AllValuesFromRestriction asRestriction ( Restriction ontRestriction )
	{
		if ( !ontRestriction.isAllValuesFromRestriction () ) return null;
		return ontRestriction.asAllValuesFromRestriction ();
	}

	@Override
	protected Resource getRestrictionClass ( AllValuesFromRestriction someRestr )
	{
		return someRestr.getAllValuesFrom ();
	}
}
