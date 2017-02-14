package net.sourceforge.ondex.parser.carbbank;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;

/**
 * Keeps all information about a publication in one place and allows for direct
 * creation of the corresponding ONDEXConcept.
 * 
 * @author victorlesk
 * 
 */
public class CarbbankCompoundInfo {

	public Set<DBRef> references = new HashSet<DBRef>();

	public Set<String> aglycon               = new HashSet<String>();
	public Set<String> biologicalActivity    = new HashSet<String>();
	public Set<String> biologicalSource      = new HashSet<String>();
	public String ccsdAccession    = "";
	public String molecularFormula = "";
	public String molecularType    = "";
	public Set<String> nonCarbohydrate       = new HashSet<String>();
	public Set<String> proteinAttachmentSite = new HashSet<String>();
	public Set<String> parentMolecule        = new HashSet<String>();
	public Set<String> structureCode         = new HashSet<String>();
	public String structureIndex   = "";
	public Set<String> syntheticTarget       = new HashSet<String>();
	public String structure        = "";
	public Set<String> trivialName           = new HashSet<String>();
	public Set<String> variableRegion        = new HashSet<String>();

	static ONDEXGraph graph;
	static DataSource dataSource;
	static DataSource ccsd;		
	static ConceptClass cc;
	static EvidenceType et;

	static AttributeName aglyconAttributeName;
	static AttributeName biologicalActivityAttributeName;
	static AttributeName biologicalSourceAttributeName;
	static AttributeName molecularFormulaAttributeName;
	static AttributeName molecularTypeAttributeName;
	static AttributeName nonCarbohydrateAttributeName;
	static AttributeName proteinAttachmentSiteAttributeName;
	static AttributeName parentMoleculeAttributeName;
	static AttributeName structureCodeAttributeName;
	static AttributeName structureIndexAttributeName;
	static AttributeName syntheticTargetAttributeName;
	static AttributeName structureAttributeName;
	static AttributeName trivialNameAttributeName;
	static AttributeName variableRegionAttributeName;

	public static void initialiseMetaData(ONDEXGraph g) {
		graph = g;
		ONDEXGraphMetaData metaData = graph.getMetaData();

		dataSource   = metaData.getFactory().createDataSource("CARBBANK", "carbbank");
		ccsd = metaData.getDataSource("CCSD");		
		cc   = metaData.getConceptClass("Comp");
		et   = metaData.getEvidenceType("IMPD");

		aglyconAttributeName = metaData
				.getFactory()
				.createAttributeName(
						"aglycon",
						"biochemical entity to which carbohydrate structure is attached",
						Set.class);
		biologicalActivityAttributeName = metaData.getFactory()
				.createAttributeName("biologicalactivity",
						"biological activity", Set.class);
		biologicalSourceAttributeName = metaData.getFactory()
				.createAttributeName("biologicalsource", "biological source",
						Set.class);
		molecularFormulaAttributeName = metaData.getFactory()
				.createAttributeName("molecularformula", "molecular formula",
						String.class);
		molecularTypeAttributeName = metaData.getFactory().createAttributeName(
				"moleculartype", "molecular type", Set.class);
		nonCarbohydrateAttributeName = metaData.getFactory()
				.createAttributeName("noncarbohydrate",
						"non-carbohydrate moiety in structure", Set.class);
		proteinAttachmentSiteAttributeName = metaData.getFactory()
				.createAttributeName("proteinattachmentsite",
						"protein attachment site", Set.class);
		parentMoleculeAttributeName = metaData.getFactory()
				.createAttributeName("parentmolecule", "parent molecule",
						Set.class);
		structureCodeAttributeName = metaData.getFactory().createAttributeName(
				"structurecode", "structure code", Set.class);
		structureIndexAttributeName = metaData.getFactory()
				.createAttributeName("structureindex", "structure index",
						String.class);
		syntheticTargetAttributeName = metaData.getFactory()
				.createAttributeName("synthetictarget",
						"synthetic target name", Set.class);
		structureAttributeName = metaData.getFactory().createAttributeName(
				"structure", "structure string", String.class);
		trivialNameAttributeName = metaData.getFactory().createAttributeName(
				"trivialname", "trivial name", Set.class);
		variableRegionAttributeName = metaData.getFactory()
				.createAttributeName("variableregion", "variable region",
						Set.class);
	}

	@Override
	public boolean equals(Object obj) {
		return structureIndex.equals(obj);
	}

	@Override
	public int hashCode() {
		return structureIndex.hashCode();
	}

	/**
	 * Creates the corresponding ONDEXConcept on the given graph.
	 * 
	 * @return created publication ONDEXConcept
	 */
	public ONDEXConcept createONDEXConcept() {

		if (!structureIndex.isEmpty()) {
			ONDEXConcept c = graph.getFactory().createConcept(structureIndex, dataSource, cc, et);

			// publication associated Attribute
			c.createAttribute(structureIndexAttributeName, structureIndex, false);
			c.createConceptName(structureIndex, true);

			if (!aglycon               .isEmpty()) { c.createAttribute(aglyconAttributeName,               aglycon               , false);}
			if (!biologicalActivity    .isEmpty()) { c.createAttribute(biologicalActivityAttributeName,    biologicalActivity    , false);}
			if (!biologicalSource      .isEmpty()) { c.createAttribute(biologicalSourceAttributeName,      biologicalSource      , false);}
			if (!ccsdAccession         .isEmpty()) { c.createConceptAccession(ccsdAccession,                ccsd                  , false);}
			if (!molecularFormula      .isEmpty()) { c.createAttribute(molecularFormulaAttributeName,      molecularFormula      , false);}
			if (!molecularType         .isEmpty()) { c.createAttribute(molecularTypeAttributeName,         molecularType         , false);}
			if (!nonCarbohydrate       .isEmpty()) { c.createAttribute(nonCarbohydrateAttributeName,       nonCarbohydrate       , false);}
			if (!proteinAttachmentSite .isEmpty()) { c.createAttribute(proteinAttachmentSiteAttributeName, proteinAttachmentSite , false);}
			if (!parentMolecule        .isEmpty()) { c.createAttribute(parentMoleculeAttributeName,        parentMolecule        , false);}
			if (!structureCode         .isEmpty()) { c.createAttribute(structureCodeAttributeName,         structureCode         , false);}
			if (!syntheticTarget       .isEmpty()) { c.createAttribute(syntheticTargetAttributeName,       syntheticTarget       , false);}
			if (!structure             .isEmpty()) { c.createAttribute(structureAttributeName,             structure             , false);}
			if (!trivialName           .isEmpty()) { c.createAttribute(trivialNameAttributeName,           trivialName           , false);}
			if (!variableRegion        .isEmpty()) { c.createAttribute(variableRegionAttributeName,        variableRegion        , false);}

			// add every occurrence of a references as concept accession
			for (DBRef dbref : references) {
				c.createConceptAccession (dbref.reference, dbref.dataSource, true);
			}

			return c;
		}
		else { throw new RuntimeException("Created without valid structure index tag."); } 
	}
}
