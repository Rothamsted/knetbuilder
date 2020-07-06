package net.sourceforge.ondex.parser.genericobo;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * 
 * @author hoekmanb
 *
 */
public abstract class ReferenceContainer {

	protected String xRefString;//the line string
	protected String xRef; //the acutal xRef
	protected DataSource DataSourceAccession;
	protected boolean ambigous;
	protected boolean write;
	protected ONDEXGraph graph;

	public ReferenceContainer(ONDEXGraph graph) {
		this.graph = graph;
	}

	protected void setXrefString(String xRefString) {
		this.xRefString = xRefString;
	}

	protected DataSource getDataSourceAccession() {
		return DataSourceAccession;
	}

	protected boolean getAmbigous() {
		return ambigous;
	}

	protected String getXref() {
		return xRef;
	}

	public boolean writeXRef() {
		return write;
	}

	public abstract void analyseXRef();
}
