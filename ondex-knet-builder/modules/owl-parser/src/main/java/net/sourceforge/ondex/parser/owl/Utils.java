package net.sourceforge.ondex.parser.owl;

import net.sourceforge.ondex.core.util.prototypes.ConceptClassPrototype;
import net.sourceforge.ondex.core.util.prototypes.DataSourcePrototype;

/**
 * Utilities for the OWL parser.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>3 Aug 2017</dd></dl>
 *
 */
public class Utils
{
	public static final DataSourcePrototype OWL_PARSER_DATA_SOURCE = new DataSourcePrototype ( "owlParser", "The OWL Parser", "" );
	public static final ConceptClassPrototype GENERIC_ONTOLOGY_CONCEPT_CLASS = new ConceptClassPrototype (
		"GENERIC_ONTO_TERM", "Generic Ontology Term", "Ontology term from unknown ontology or top category"
	);
}
