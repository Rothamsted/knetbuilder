package net.sourceforge.ondex.parser.genericobo.po;

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
public class POReferenceContainer extends ReferenceContainer {

	public POReferenceContainer(ONDEXGraph graph) {
		super(graph);
	}

	@Override
	public void analyseXRef() {
		String[] xrefSplit = xRefString.split(":");

		if (xrefSplit.length == 2 && xrefSplit[0].equalsIgnoreCase("CL")) {
			ambigous = false;
			DataSourceAccession = graph.getMetaData().getDataSource(MetaData.cvCL);
			write = true;
			xRef = xRefString;
		} else {
			ONDEXEventHandler.getEventHandlerForSID(graph.getSID()).fireEventOccurred(new DataSourceMissingEvent("Database \""
					+ xRefString
					+ "\" referenced from xref field in OBO file unknown!",
					Parser.getCurrentMethodName()));
		}
	}
}
