package net.sourceforge.ondex.ovtk2.ui.gds;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import net.sourceforge.ondex.config.OndexJAXBContextRegistry;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.tools.data.Protein3dStructure;
import net.sourceforge.ondex.tools.data.Protein3dStructureHolder;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolViewer;

/**
 * GDSEditor for PDBStructure information. Displays the PDB structure in a JMOL.
 * 
 * @author peschr, taubertj
 */
public class PDBStructureEditor extends JPanel implements GDSEditor {

	private class GDSTableModel extends AbstractTableModel {

		// generated
		private static final long serialVersionUID = 1176410895403511752L;

		private Attribute attribute = null;

		private Object o = null;

		private Protein3dStructure structure;

		/**
		 * Constructor sets Attribute.
		 * 
		 * @param attribute
		 *            Attribute
		 */
		public GDSTableModel(Attribute attribute) {
			this.attribute = attribute;
			this.o = attribute.getValue();
			if (this.o instanceof Protein3dStructure)
				this.structure = (Protein3dStructure) o;
		}

		public Class<?> getColumnClass(int c) {
			return String.class;
		}

		public int getColumnCount() {
			return 1;
		}

		public String getColumnName(int col) {
			return "PDB id";
		}

		public int getRowCount() {
			return 1;
		}

		public Object getValueAt(int row, int col) {
			if (structure != null && row == 0)
				return structure.getAccessionNr();
			else
				return o.toString();
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			if (structure != null && row == 0) {
				structure.setAccessionNr((String) value);
				attribute.setValue(structure);
				fireTableCellUpdated(row, col);
			}
		}
	}

	static class JmolPanel extends JPanel {

		/**
		 * generated
		 */
		private static final long serialVersionUID = 1370685975286067202L;

		private final Dimension currentSize = new Dimension();

		private final Rectangle rectClip = new Rectangle(); // ignored by Jmol
		JmolViewer viewer;

		JmolPanel() {
			viewer = JmolViewer.allocateViewer(this, new SmarterJmolAdapter(),
					null, null, null, null, null);
		}

		@Override
		public void paint(Graphics g) {
			getSize(currentSize);
			g.getClipBounds(rectClip);
			viewer.renderScreenImage(g, currentSize, rectClip);
		}
	}

	// generated
	private static final long serialVersionUID = -2583865453004709414L;

	static {
		// comes with its own marshaller etc
		OndexJAXBContextRegistry jaxbRegistry = OndexJAXBContextRegistry
				.instance();
		jaxbRegistry.addClassBindings(Protein3dStructureHolder.class);
		jaxbRegistry.addHolder(Protein3dStructure.class,
				Protein3dStructureHolder.class);
		System.out.println("adding to OndexJAXBContextRegistry");
	}

	private JTable table;

	/**
	 * starts the PDBViewer in a JInternalFrame
	 * 
	 * @param attribute
	 *            attribute values with stored the PDB-Filename
	 */
	public PDBStructureEditor(Attribute attribute) {

		setLayout(new BorderLayout());

		if (attribute != null) {

			// JTable for value
			table = new JTable(new GDSTableModel(attribute));
			this.add(table, BorderLayout.NORTH);

			JmolPanel jmolPanel = new JmolPanel();
			this.add(jmolPanel, BorderLayout.CENTER);

			// load PDB file
			String pdbFile = table.getValueAt(0, 0).toString();
			String strError = jmolPanel.viewer
					.openFile("http://www2.rcsb.org/pdb/files/" + pdbFile
							+ ".pdb1.gz");

			if (strError == null)
				jmolPanel.viewer
						.script("set measurementUnits ANGSTROMS; select all;spacefill off; wireframe off; backbone off; cartoon on; color cartoon structure; color structure; select ligand;wireframe 0.16;spacefill 0.5; color cpk ; select all; model 0;set antialiasDisplay true; spin on; save STATE state_1");
			else
				JOptionPane.showInternalMessageDialog(OVTK2Desktop
						.getInstance().getDesktopPane(), strError,
						"Error while loading...", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void flushChanges() {
		TableCellEditor ce = table.getCellEditor();
		if (ce != null) {
			ce.stopCellEditing();
		}
	}

	/**
	 * returns the default value
	 * 
	 * @return Object always null
	 */
	public Object getDefaultValue() {
		return null;
	}
}
