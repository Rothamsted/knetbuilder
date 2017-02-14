package net.sourceforge.ondex.parser.pdb.transformer;

import java.util.Iterator;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.parser.pdb.MetaData;
import net.sourceforge.ondex.parser.pdb.Parser;
import net.sourceforge.ondex.parser.pdb.sink.DbLink;
import net.sourceforge.ondex.parser.pdb.sink.ProteinProtoType;
import net.sourceforge.ondex.tools.data.Protein3dStructure;

/**
 * transformers a protein object into a concept
 * 
 * @author peschr
 * 
 */
public class ProteinTransformer {

	private ConceptClass ccProtein3DStructure;

	private DataSource dataSourceGenBank;

	private DataSource dataSourcePDB;

	private DataSource dataSourceUniProt;

	private EvidenceType etIMPD;

	private ONDEXGraph graph;

	private AttributeName pdb;

	/**
	 * @param graph
	 */
	public ProteinTransformer(ONDEXGraph graph) {
		this.graph = graph;

		pdb = graph.getMetaData().getFactory().createAttributeName(
				"Protein3dStructure", Protein3dStructure.class);
		ccProtein3DStructure = graph.getMetaData().getConceptClass(
				MetaData.CC_Protein3DStructure);
		dataSourcePDB = graph.getMetaData().getDataSource(MetaData.CV_PDB);
		dataSourceUniProt = graph.getMetaData().getDataSource(MetaData.CV_UniProt);
		dataSourceGenBank = graph.getMetaData().getDataSource(MetaData.CV_GenBank);
		etIMPD = graph.getMetaData().getEvidenceType("IMPD");
	}

	/**
	 * main transformer method.
	 * 
	 * @param protein
	 * @throws ToBigPDFFileException
	 */
	public void transform(ProteinProtoType protein) {
		ONDEXConcept concept = graph.getFactory().createConcept(
				protein.getAccessionNr(), dataSourcePDB, ccProtein3DStructure, etIMPD);
		if (protein.getAccessionNr() == null) {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID())
					.fireEventOccurred(
							new GeneralOutputEvent(
									"Structure without accession ("
											+ protein.getPdbFileName(),
									Parser.class.toString()));
			return;
		}
		concept.createConceptName(protein.getAccessionNr(), true);
		concept.createConceptAccession(protein.getAccessionNr(), dataSourcePDB, false);

		// Iterates over all database links
		Iterator<DbLink> i = protein.getDblinks().iterator();
		while (i.hasNext()) {
			DataSource dataSource;
			DbLink dbLink = i.next();
			if (dbLink.getDbName().equals(MetaData.CV_UniProt)) {
				dataSource = this.dataSourceUniProt;
			} else {
				dataSource = this.dataSourceGenBank;
			}
			for (int a = 0; a < dbLink.getAccession().length; a++)
				concept.createConceptAccession(dbLink.getAccession()[a], dataSource,
						false);
			i.next();
		}
		// saves the path to the source pdb file, so it can be loaded within
		// OVTK
		if (protein.getAccessionNr() != null) {
			Protein3dStructure ps = new Protein3dStructure();
			ps.setAccessionNr(protein.getAccessionNr());
			concept.createAttribute(pdb, ps, false);
		}
	}
}
