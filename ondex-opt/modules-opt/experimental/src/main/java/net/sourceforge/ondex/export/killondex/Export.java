package net.sourceforge.ondex.export.killondex;

import net.sourceforge.ondex.annotations.IncludeType;
import net.sourceforge.ondex.annotations.Webservice;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.export.ONDEXExport;

/**
 * Terminates the execution of an Ondex workflow. This is kind of a hack to make
 * sure Ondex-mini terminates really.
 * 
 * @author taubertj
 * 
 */
@Webservice(description = "Applicable only to OndexMini and worse a HACK! August 2011 Christian",
    include = IncludeType.NEVER)
public class Export extends ONDEXExport {

	@Override
	public String getId() {
		return "killondex";
	}

	@Override
	public String getName() {
		return "Terminate Ondex";
	}

	@Override
	public String getVersion() {
		return "20.07.2011";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[0];
	}

	@Override
	public void start() throws Exception {
		this.fireEventOccurred(new GeneralOutputEvent(
				"Exiting to system on request!", ONDEXExport
						.getCurrentMethodName()));
		System.exit(0);
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
