package net.sourceforge.ondex.workflow2.gui.components;
/**
 * 
 * @author lysenkoa
 *
 */
public interface ListItem extends Comparable<ListItem>{
	public String getInternalName();
	public String getTooltip();
}