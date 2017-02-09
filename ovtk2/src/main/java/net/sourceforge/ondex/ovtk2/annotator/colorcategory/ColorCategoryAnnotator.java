package net.sourceforge.ondex.ovtk2.annotator.colorcategory;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.ONDEXNodeFillPaint;
import net.sourceforge.ondex.ovtk2.graph.custom.MultiColorNodePaint;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.util.AppearanceSynchronizer;
import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
import net.sourceforge.ondex.ovtk2.util.SpringUtilities;
import net.sourceforge.ondex.ovtk2.util.listmodel.AttributeNameListModel;
import net.sourceforge.ondex.ovtk2.util.renderer.CustomCellRenderer;

/**
 * automatically assigns colors to the values of the selected attribute.
 * 
 * @author Jochen Weile, B.Sc.
 */
public class ColorCategoryAnnotator extends OVTK2Annotator implements ActionListener {

	// ####FIELDS####

	/**
	 * blah.
	 */
	private static final long serialVersionUID = -6100625174161606722L;

	/**
	 * the selected attribute name.
	 */
	private AttributeName an;

	/**
	 * the list containing the fitting attribute names.
	 */
	private JList list;

	/**
	 * the list model for the above list.
	 */
	private AttributeNameListModel anlm;

	/**
	 * the start button.
	 */
	private JButton goButton;

	/**
	 * a checkbox to determine whether blanks are set to white.
	 */
	private JCheckBox setBlanksToWhite;

	/**
	 * a checkbox to enable exclusive mode, e.g. check for duplicated Attribute
	 * :2, :3 ...
	 */
	private JCheckBox exclusive;

	/**
	 * use only black and white colour scale
	 */
	private JCheckBox useBlackAndWhite;

	/**
	 * an ordered set of attribute values.
	 */
	private TreeSet<Comparable<?>> gdsValues;

	/**
	 * a table linking values to colors.
	 */
	private Map<Object, Color> colortable;

	/**
	 * Annotator has been used
	 */
	private boolean used = false;

	public LegendFrame legend;

	// only for use in applet
	public JFrame frame;

	// ####CONSTRUCTOR####

	/**
	 * constructor. sets up and displays the gui.
	 * 
	 * 
	 */
	public ColorCategoryAnnotator(OVTK2PropertiesAggregator viewer) {
		super(viewer);

		setLayout(new SpringLayout());

		anlm = new AttributeNameListModel();

		// The magic button
		goButton = new JButton("Annotate Graph");
		goButton.setEnabled(false);

		setBlanksToWhite = new JCheckBox("Set blanks to white?");
		exclusive = new JCheckBox("Exlusive use of attribute?");
		useBlackAndWhite = new JCheckBox("Use black and white?");

		list = new JList(anlm);
		list.setCellRenderer(new CustomCellRenderer());

		addAttributeNamesToList();

		if (anlm.getSize() == 0) {
			add(new JLabel("There are not any attributes in the graph."));
		} else {
			list.validate();
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					int index = list.getSelectedIndex();
					if (index > -1)
						goButton.setEnabled(true);
				}

			});

			add(new JLabel("Select attribute to annotate concepts with"));
			add(new JScrollPane(list));

			add(setBlanksToWhite);
			add(exclusive);
			add(useBlackAndWhite);

			goButton.addActionListener(this);
			add(goButton);
		}

		SpringUtilities.makeCompactGrid(this, this.getComponentCount(), 1, 5, 5, 5, 5);

	}

	// ####METHODS####

	/**
	 * Returns associated viewer.
	 */
	public OVTK2PropertiesAggregator getViewer() {
		return viewer;
	}

	/**
	 * adds all suitable attribute names to the jlist.
	 */
	private void addAttributeNamesToList() {
		for (AttributeName attn : graph.getMetaData().getAttributeNames()) {
			// should also accept list types now.
			if (Comparable.class.isAssignableFrom(attn.getDataType()) || Collection.class.isAssignableFrom(attn.getDataType())) {
				Set<ONDEXConcept> concepts = graph.getConceptsOfAttributeName(attn);
				if (concepts != null && concepts.size() > 0 && !AppearanceSynchronizer.attr.contains(attn.getId()))
					anlm.addAttributeName(attn);
			}
		}
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int index = list.getSelectedIndex();
		if (index > -1) {
			String attNameName = ((JLabel) list.getModel().getElementAt(index)).getName();
			an = graph.getMetaData().getAttributeName(attNameName);
			registerGDSValues();
			computeColorTable();
			applyColors();
			showLegendWindow();
			used = true;
		}
	}

	/**
	 * shows the color legend frame.
	 */
	private void showLegendWindow() {
		// make sure there is only one legend
		if (legend != null) {
			try {
				legend.setClosed(true);
			} catch (PropertyVetoException e) {
				ErrorDialog.show(e);
			}
		}
		legend = new LegendFrame(this);
		if (Config.isApplet) {
			// close old legend
			if (frame != null) {
				frame.setVisible(false);
				frame.dispose();
			}
			frame = new JFrame("Legend");
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setContentPane(legend.getContentPane());
			frame.pack();
			frame.setVisible(true);
		} else {
			OVTK2Desktop.getInstance().getDesktopPane().add(legend);
			legend.setVisible(true);
		}
	}

	/**
	 * registers the attribute values in the sorted set.
	 */
	private void registerGDSValues() {
		gdsValues = new TreeSet<Comparable<?>>();

		Object value;
		for (ONDEXConcept c : graph.getConcepts()) {
			Attribute attribute = c.getAttribute(an);
			if (attribute != null) {
				value = attribute.getValue();
				if (value instanceof Comparable<?>)
					gdsValues.add((Comparable<?>) value);
				else if (value instanceof Collection<?>) {
					for (Object v : (Collection<?>) value) {
						if (v instanceof Comparable<?>) {
							gdsValues.add((Comparable<?>) v);
						} else {
							break;
						}
					}
				}
			}
		}

	}

	/**
	 * computes the colour table.
	 */
	private void computeColorTable() {
		boolean blackAndWhite = useBlackAndWhite.isSelected();
		colortable = new HashMap<Object, Color>();
		double l = (double) gdsValues.size() + 1;
		double resolution = 1.0 / l;
		double val = 0.0;
		Iterator<Comparable<?>> it = gdsValues.iterator();
		Comparable<?> c;
		while (it.hasNext()) {
			c = it.next();
			if (!blackAndWhite)
				colortable.put(c, new Color(hueToRGB(val)));
			else {
				int scale = (int) Math.rint(255.0 * val);
				colortable.put(c, new Color(scale, scale, scale));
			}
			val += resolution;
		}
	}

	/**
	 * converts a HSV colour representation into an RGB encoded integer.
	 * 
	 * @param h
	 *            hue
	 * @param s
	 *            saturation
	 * @param v
	 *            value
	 * @return
	 */
	private int HSVtoRGB(float h, float s, float v) {
		h *= 6;
		// H is given on [0->6] or -1. S and V are given on [0->1].
		// RGB are each returned on [0->1].

		float m, n, f;
		int i;

		float[] hsv = new float[3];
		float[] rgb = new float[3];

		hsv[0] = h;
		hsv[1] = s;
		hsv[2] = v;

		if (hsv[0] == -1) {
			rgb[0] = rgb[1] = rgb[2] = hsv[2];
		} else {
			i = (int) (Math.floor(hsv[0]));
			f = hsv[0] - i;
			if (i % 2 == 0)
				f = 1 - f; // if i is even
			m = hsv[2] * (1 - hsv[1]);
			n = hsv[2] * (1 - hsv[1] * f);
			switch (i) {
			case 6:
			case 0:
				rgb[0] = hsv[2];
				rgb[1] = n;
				rgb[2] = m;
				break;
			case 1:
				rgb[0] = n;
				rgb[1] = hsv[2];
				rgb[2] = m;
				break;
			case 2:
				rgb[0] = m;
				rgb[1] = hsv[2];
				rgb[2] = n;
				break;
			case 3:
				rgb[0] = m;
				rgb[1] = n;
				rgb[2] = hsv[2];
				break;
			case 4:
				rgb[0] = n;
				rgb[1] = m;
				rgb[2] = hsv[2];
				break;
			case 5:
				rgb[0] = hsv[2];
				rgb[1] = m;
				rgb[2] = n;
				break;
			}
		}

		int r = (int) (rgb[0] * 255.0f);
		int g = (int) (rgb[1] * 255.0f);
		int b = (int) (rgb[2] * 255.0f);

		int out = 0;
		out += r << 16;
		out += g << 8;
		out += b;

		return out;

	}

	/**
	 * creates an RGB INT for the given hue.
	 * 
	 * @param h
	 * @return
	 */
	private int hueToRGB(double h) {
		return HSVtoRGB((float) h, 1.0f, 1.0f);
	}

	/**
	 * applies the colours to the concepts.
	 */
	void applyColors() {

		// set selection mode to manual
		ONDEXNodeFillPaint colorManager = viewer.getNodeColors();
		colorManager.setFillPaintSelection(ONDEXNodeFillPaint.NodeFillPaintSelection.MANUAL);

		// check for duplicated Attribute, e.g. Pheno and Pheno:2
		AttributeName an2 = null;
		if (exclusive.isSelected()) {
			an2 = graph.getMetaData().getAttributeName(an.getId() + ":2");
		}

		for (ONDEXConcept concept : graph.getConcepts()) {
			Attribute attribute = concept.getAttribute(an);
			if (attribute != null) {
				Object o = attribute.getValue();
				// this is check for additional related Attribute
				if (an2 != null) {
					attribute = concept.getAttribute(an2);
					if (attribute != null) {
						Color c = Color.BLACK;
						colorManager.updateColor(concept, c);
						continue;
					}
				}
				if (o instanceof Comparable<?>) {
					Color c = colortable.get(o);
					colorManager.updateColor(concept, c);
				} else if (o instanceof Collection<?>) {
					Collection<?> coll = (Collection<?>) o;
					Vector<Color> colors = new Vector<Color>();
					for (Object v : coll) {
						Color c = colortable.get(v);
						if (c != null) {
							colors.add(c);
						}
					}
					Color[] colorArray = colors.toArray(new Color[colors.size()]);
					colorManager.updateColor(concept, new MultiColorNodePaint(colorArray));
				}
			} else if (setBlanksToWhite.isSelected()) {
				Color c = Color.WHITE;
				colorManager.updateColor(concept, c);
			}
		}

		// update viewer
		viewer.getVisualizationViewer().fireStateChanged();
		viewer.getVisualizationViewer().repaint();
	}

	TreeSet<Comparable<?>> getItemSet() {
		return gdsValues;
	}

	Map<Object, Color> getColorTable() {
		return colortable;
	}

	String getAttributeName() {
		if (an.getFullname() != null)
			return an.getFullname();
		else
			return an.getId();
	}

	/**
	 * @see net.sourceforge.ondex.ovtk2.annotator.OVTK2Annotator#getName()
	 */
	@Override
	public String getName() {
		return Config.language.getProperty("Name.Menu.Annotator.ColorCategory");
	}

	// ####ENCLOSED TYPES####

	@Override
	public boolean hasBeenUsed() {
		return this.used;
	}

}
