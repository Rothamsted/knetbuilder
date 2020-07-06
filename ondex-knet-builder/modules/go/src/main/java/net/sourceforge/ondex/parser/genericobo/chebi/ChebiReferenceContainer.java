package net.sourceforge.ondex.parser.genericobo.chebi;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.event.ONDEXEventHandler;
import net.sourceforge.ondex.event.type.DataSourceMissingEvent;
import net.sourceforge.ondex.parser.genericobo.MetaData;
import net.sourceforge.ondex.parser.genericobo.Parser;
import net.sourceforge.ondex.parser.genericobo.ReferenceContainer;

/**
 * 
 * @author hoekmanb
 *
 */
public class ChebiReferenceContainer extends ReferenceContainer {

	private static final String CHEBI_ACC = "CHEBI:";
	private static final String MSD_ACC = "MSDCHEM:";

	public ChebiReferenceContainer(ONDEXGraph graph) {
		super(graph);
	}

	@Override
	public void analyseXRef() {

		xRefString = xRefString.trim();
		
		if (xRefString.startsWith("ChemIDplus:")
				&& xRefString.contains("CAS Registry Number")) { // make sure
																	// it is a
																	// cas
																	// number
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.casCV);
			write = true;
			xRef = xRefString.substring(11, xRefString
					.indexOf("\"CAS Registry Number\""));
			xRef = xRef.trim();
		} else if (xRefString.startsWith("ChemIDplus:")
				&& xRefString.contains("\"chemPDB\"")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.chemPDBCV);
			xRef = xRefString.substring(11, xRefString.indexOf("\"chemPDB\""));
			xRef = xRef.trim();
			write = true;
		} else if (xRefString.startsWith("ChemIDplus:")
				&& xRefString.contains("Beilstein Registry Number")) {
			write = false;
		} else if (xRefString.startsWith("NIST Chemistry WebBook:")
				&& xRefString.contains("CAS Registry Number")) { // make sure
																	// it is a
																	// cas
																	// number
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.casCV);
			write = true;
			xRef = xRefString.substring(23, xRefString
					.indexOf("\"CAS Registry Number\""));
		} else if (xRefString.startsWith("PDB:")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.PDB);
			xRef = xRefString.substring(4, xRefString.indexOf("\"PDB\""));
			xRef = xRef.trim();
			write = true;
		} else if (xRefString.startsWith("chemPDB:")
				&& xRefString.contains("\"chemPDB\"")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(
					MetaData.chemPDBCV);
			xRef = xRefString.substring(8, xRefString.indexOf("\"chemPDB\""));
			xRef = xRef.trim();
			write = true;
		} else if (xRefString.startsWith("KEGG DRUG:")
				&& xRefString.contains("\"KEGG DRUG\"")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.KEGG);
			xRef = xRefString
					.substring(10, xRefString.indexOf("\"KEGG DRUG\""));
			xRef = xRef.trim();
			write = true;
		} else if (xRefString.startsWith("KEGG DRUG:")
				&& xRefString.contains("\"CAS Registry Number\"")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.KEGG);
			xRef = xRefString.substring(10, xRefString
					.indexOf("\"CAS Registry Number\""));
			xRef = xRef.trim();
			write = true;
		} else if (xRefString.startsWith("KEGG COMPOUND:")
				&& xRefString.contains("\"KEGG COMPOUND\"")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.KEGG);
			xRef = xRefString.substring(14, xRefString
					.indexOf("\"KEGG COMPOUND\""));
			xRef = xRef.trim();
			write = true;
		} else if (xRefString.startsWith("KEGG COMPOUND:")
				&& xRefString.contains("CAS Registry Number")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.casCV);
			xRef = xRefString.substring(14, xRefString
					.indexOf("\"CAS Registry Number\""));
			xRef = xRef.trim();
			write = true;
		} else if (xRefString.startsWith("KEGG GLYCAN:")
				&& xRefString.contains("\"KEGG GLYCAN\"")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.KEGG);
			xRef = xRefString.substring(12, xRefString
					.indexOf("\"KEGG GLYCAN\""));
			xRef = xRef.trim();
			write = true;
		} else if (xRefString.toUpperCase().startsWith(CHEBI_ACC)
				&& xRefString.contains("CAS Registry Number")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.casCV);
			xRef = xRefString.substring(CHEBI_ACC.length(), xRefString
					.indexOf("\"CAS Registry Number\""));
			xRef = xRef.trim();
			write = true;
		} else if (xRefString.toUpperCase().startsWith(CHEBI_ACC)
				&& xRefString.contains("KEGG COMPOUND")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.KEGG);
			xRef = xRefString.substring(CHEBI_ACC.length(), xRefString
					.indexOf("\"KEGG COMPOUND\""));
			xRef = xRef.trim();
			write = true;
		} else if (xRefString.toUpperCase().startsWith(CHEBI_ACC)
				&& xRefString.contains("chemPDB")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.chemPDBCV);
			xRef = xRefString.substring(CHEBI_ACC.length(), xRefString.indexOf("\"chemPDB\""));
			xRef = xRef.trim();
			write = true;
		} else if (xRefString.toUpperCase().startsWith(MSD_ACC)
				&& xRefString.contains("MSDchem")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.msdChem);
			xRef = xRefString.substring(MSD_ACC.length(), xRefString.indexOf("\"MSDchem\""));
			xRef = xRef.trim();
			write = true;
		}
		// THE following database-accession are excluded because of coverage and
		// familarity.
		else if (xRefString.toUpperCase().startsWith(CHEBI_ACC)
				&& xRefString.contains("UM-BBD compID")) {
			write = false;
		} else if (xRefString.startsWith("Gmelin:")) {
			write = false;
		} else if (xRefString.startsWith("COMe:")) {
			write = false;
		} else if (xRefString.startsWith("MolBase:")) {
			write = false;
		} else if (xRefString.startsWith("WebElements:")) {
			write = false;
		} else if (xRefString.startsWith("RESID:")) {
			write = false;
		} else if (xRefString.startsWith("UM-BBD:")) {
			write = false;
		} else if (xRefString.startsWith("Beilstein:")) {
			write = false;
		} else if (xRefString.startsWith("LIPID MAPS:")) {
			write = false;
		} else if (xRefString.startsWith("Patent:")) {
			write = false;
		} else if (xRefString.startsWith("DrugBank:")) {
			write = false;
		} else {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new DataSourceMissingEvent("Database \""
					+ xRefString
					+ "\" referenced from xref field in CHEBI file unknown!",
					Parser.getCurrentMethodName()));
		}

	}

}
