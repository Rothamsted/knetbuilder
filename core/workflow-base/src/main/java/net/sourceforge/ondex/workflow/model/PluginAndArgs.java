package net.sourceforge.ondex.workflow.model;

import net.sourceforge.ondex.ONDEXPlugin;
import net.sourceforge.ondex.ONDEXPluginArguments;

/**
 * @author hindlem
 *         Created 06-Apr-2010 15:00:14
 */
public class PluginAndArgs
{

    private ONDEXPlugin plugin;

    private ONDEXPluginArguments arguments;

    public PluginAndArgs()
    {
    }

    public PluginAndArgs(ONDEXPlugin plugin, ONDEXPluginArguments arguments)
    {
        this.arguments = arguments;
        this.plugin = plugin;
    }

    public ONDEXPluginArguments getArguments() {
        return arguments;
    }

    public ONDEXPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(ONDEXPlugin plugin) {
        this.plugin = plugin;
    }

    public void setArguments(ONDEXPluginArguments args) {
        this.arguments = args;
    }
}
