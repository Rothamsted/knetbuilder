//package net.sourceforge.ondex.ovtk2.annotator.chemical;
//
//import java.awt.Color;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.swing.Icon;
//
//import net.sourceforge.ondex.core.AttributeName;
//import net.sourceforge.ondex.core.ONDEXConcept;
//import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint;
//import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
//
//import org.apache.commons.collections15.Transformer;
//
///**
// * A node shape which displays chemical drawings.
// * 
// * @author taubertj
// * @version 16.04.2012
// */
//public class ChemicalNodeIconTransformer implements
//		Transformer<ONDEXConcept, Icon> {
//
//	/**
//	 * chemical structure attribute as pre-filter
//	 */
//	private AttributeName an;
//
//	/**
//	 * Pass-through for drawing size
//	 */
//	private int size;
//
//	/**
//	 * icon storage
//	 */
//	protected Map<ONDEXConcept, Icon> iconMap = new HashMap<ONDEXConcept, Icon>();
//
//	/**
//	 * Translate concept into colour
//	 */
//	private ONDEXNodeFillPaint nodeFillPaint;
//
//	/**
//	 * A pass-through for the Ondex graph.
//	 * 
//	 * @param graph
//	 */
//	public ChemicalNodeIconTransformer(OVTK2PropertiesAggregator viewer,
//			int size, boolean useBorderColour) {
//		an = viewer.getONDEXJUNGGraph().getMetaData()
//				.getAttributeName("ChemicalStructure");
//		if (useBorderColour)
//			nodeFillPaint = viewer.getNodeColors();
//		this.size = size;
//	}
//
//	/**
//	 * Returns the icon storage as a <code>Map</code>.
//	 */
//	public Map<ONDEXConcept, Icon> getIconMap() {
//		return iconMap;
//	}
//
//	/**
//	 * Sets the icon storage to the specified <code>Map</code>.
//	 */
//	public void setIconMap(Map<ONDEXConcept, Icon> iconMap) {
//		this.iconMap = iconMap;
//	}
//
//	@Override
//	public Icon transform(ONDEXConcept input) {
//		// lazy initialisation of icon map
//		if (!iconMap.containsKey(input)) {
//			ChemicalIcon icon;
//			if (nodeFillPaint != null) {
//				Color c = (Color) nodeFillPaint.transform(input);
//				icon = new ChemicalIcon(an, input, size, c);
//			} else
//				icon = new ChemicalIcon(an, input, size, Color.BLACK);
//			if (icon.created())
//				iconMap.put(input, icon);
//			else
//				iconMap.put(input, null);
//		}
//		return iconMap.get(input);
//	}
//}
