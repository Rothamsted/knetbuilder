package net.sourceforge.ondex.ovtk2.ui;

import java.awt.Container;

import net.sourceforge.ondex.logging.ONDEXLogger;
import net.sourceforge.ondex.ovtk2.ui.toolbars.OVTK2ToolBar;

/**
 * Used internally by the scripting engine
 * 
 * @author lysenkoa
 * 
 */
public interface OVTK2ResourceAssesor {

	public OVTK2PropertiesAggregator[] getAllViewers();

	public ONDEXLogger getLogger();

	public Container getParentPane();

	public OVTK2MetaGraph getSelectedMetagraph();

	public OVTK2PropertiesAggregator getSelectedViewer();

	public OVTK2ToolBar getToolBar();

	public void setSelectedViewer(OVTK2PropertiesAggregator viewer);
}
