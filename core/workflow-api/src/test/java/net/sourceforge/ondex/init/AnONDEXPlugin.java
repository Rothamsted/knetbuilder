package net.sourceforge.ondex.init;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.ArgumentDefinition;

import java.util.Collection;
import java.util.Collections;
import net.sourceforge.ondex.event.ONDEXListener;

public class AnONDEXPlugin implements ONDEXPlugin
{

	@Override
	public ArgumentDefinition[] getArgumentDefinitions() {
		return null;
	}

	@Override
	public ONDEXPluginArguments getArguments() {
		return null;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getVersion() {
		return null;
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return null;
	}

	@Override
	public void setArguments(ONDEXPluginArguments args)
			throws InvalidPluginArgumentException {
	}

	@Override
	public void start() throws Exception {
		
	}

    @Override
    public Collection<ArgumentDescription> getArgumentDescriptions(int position)
    {
        ArgumentDescription ab = new ArgumentDescription();
        ab.setCls("net.sourceforge.ondex.core.AbstractONDEXGraph");
        ab.setName("Graph id");
        ab.setInteranlName("graphId");
        ab.setInputId(position);
        ab.setParser("standartArgument");
        ab.setDescription("Graph that will be operated on by this plugin.");
        ab.setIsRequired(true);
        ab.setIsInputObject(true);
        return Collections.singleton(ab);
    }

    @Override
    public void addONDEXListener(ONDEXListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeONDEXListener(ONDEXListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ONDEXListener[] getONDEXListeners() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
