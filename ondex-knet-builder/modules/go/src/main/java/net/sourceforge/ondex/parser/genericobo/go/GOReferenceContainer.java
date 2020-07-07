package net.sourceforge.ondex.parser.genericobo.go;

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
public class GOReferenceContainer extends ReferenceContainer {

	public GOReferenceContainer(ONDEXGraph graph) {
		super(graph);
	}

	@Override
	public void analyseXRef() {
		if (xRefString.startsWith("EC:")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.cvEC);
			write = true;
			xRef = xRefString.substring(3);
			xRef = StringMod.fillEC(xRef);
		} else if (xRefString.startsWith("TC:")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.cvTC);
			write = true;
			xRef = xRefString.substring(3, xRefString.length());
		} else if (xRefString.startsWith("MetaCyc:")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.cvMC);
			write = true;
			xRef = xRefString.substring(8, xRefString.length());
		} else if (xRefString.startsWith("RESID:")) {
			ambigous = true;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.cvRESID);
			write = true;
			xRef = xRefString.substring(6, xRefString.length());
		} else if (xRefString.startsWith("UM-BBD_enzymeID:")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.cvUME);
			write = true;
			xRef = xRefString.substring(16, xRefString.length());
		} else if (xRefString.startsWith("UM-BBD_pathwayID:")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.cvUMP);
			write = true;
			xRef = xRefString.substring(17, xRefString.length());
		} else if (xRefString.startsWith("Reactome:")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.cvREAC);
			write = true;
			xRef = xRefString.substring(9, xRefString.length());
		} else {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new DataSourceMissingEvent("Database \""
					+ xRefString
					+ "\" referenced from xref field in GO file unknown!",
					Parser.getCurrentMethodName()));
		}
	}
}
