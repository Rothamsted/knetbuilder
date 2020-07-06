package net.sourceforge.ondex.export.slotable;

import net.sourceforge.ondex.annotations.IncludeType;
import net.sourceforge.ondex.annotations.Webservice;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.core.util.GraphAdaptor;
import net.sourceforge.ondex.export.ONDEXExport;

@Webservice(description = "Webservices make no direct use of GraphAdapter. August 2011 Christian",
    include = IncludeType.NEVER)
public class Export extends ONDEXExport {
	private static GraphAdaptor ga;
	
	public static void setGraphAdaptor(GraphAdaptor adaptor){
		ga = adaptor;
	}

	@Override
	public String getId() {
		return "slottableAdapter";
	}

	@Override
	public String getName() {
		return "Slottable adapter";
	}

	@Override
	public String getVersion() {
		return "v1.0";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[0];
	}

	@Override
	public void start() throws Exception {
		ga.passGraph(graph);
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

}
