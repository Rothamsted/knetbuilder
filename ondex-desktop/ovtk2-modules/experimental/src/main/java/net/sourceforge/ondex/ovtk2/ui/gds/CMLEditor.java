//package net.sourceforge.ondex.ovtk2.ui.gds;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Rectangle;
//import java.io.ByteArrayInputStream;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.swing.JPanel;
//import javax.swing.JTable;
//import javax.swing.table.AbstractTableModel;
//import javax.swing.table.TableCellEditor;
//
//import net.sourceforge.ondex.core.Attribute;
//import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
//import net.sourceforge.ondex.ovtk2.util.chemical.StructureUtils;
//
//import org.openscience.cdk.ChemFile;
//import org.openscience.cdk.exception.CDKException;
//import org.openscience.cdk.interfaces.IAtomContainer;
//import org.openscience.cdk.interfaces.IChemFile;
//import org.openscience.cdk.io.CMLReader;
//import org.openscience.cdk.renderer.AtomContainerRenderer;
//import org.openscience.cdk.renderer.font.AWTFontManager;
//import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
//import org.openscience.cdk.renderer.generators.BasicBondGenerator;
//import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
//import org.openscience.cdk.renderer.generators.IGenerator;
//import org.openscience.cdk.renderer.generators.RingGenerator;
//import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
//
///**
// * Attribute renderer plug-in for chemical structures in CML
// * 
// * @author taubertj
// * 
// */
//public class CMLEditor extends JPanel implements GDSEditor {
//
//	private class GDSTableModel extends AbstractTableModel {
//
//		// generated
//		private static final long serialVersionUID = 1176410895403511742L;
//
//		private Attribute attribute = null;
//
//		private String cml = null;
//
//		/**
//		 * Constructor sets Attribute.
//		 * 
//		 * @param attribute
//		 *            Attribute
//		 */
//		public GDSTableModel(Attribute attribute) {
//			this.attribute = attribute;
//			this.cml = (String) attribute.getValue();
//		}
//
//		public Class<?> getColumnClass(int c) {
//			return String.class;
//		}
//
//		public int getColumnCount() {
//			return 1;
//		}
//
//		public String getColumnName(int col) {
//			return "Chemical structure representation";
//		}
//
//		public int getRowCount() {
//			return 1;
//		}
//
//		public Object getValueAt(int row, int col) {
//			return cml.toString();
//		}
//
//		public boolean isCellEditable(int row, int col) {
//			return true;
//		}
//
//		public void setValueAt(Object value, int row, int col) {
//			cml = (String) value;
//			attribute.setValue(cml);
//			fireTableCellUpdated(row, col);
//		}
//	}
//
//	/**
//	 * generated
//	 */
//	private static final long serialVersionUID = 56515200363790032L;
//
//	private JTable table;
//
//	static class RendererPanel extends JPanel {
//
//		/**
//		 * generated
//		 */
//		private static final long serialVersionUID = -1862912144266791331L;
//
//		private AtomContainerRenderer renderer;
//
//		private IAtomContainer molecule;
//
//		public RendererPanel(IAtomContainer molecule) {
//			this.molecule = molecule;
//
//			// generators make the image elements
//			List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
//			generators.add(new BasicSceneGenerator());
//			//generators.add(new BasicBondGenerator());
//			generators.add(new RingGenerator()); 
//			generators.add(new BasicAtomGenerator());
//
//			// the renderer needs to have a toolkit-specific font manager
//			renderer = new AtomContainerRenderer(generators,
//					new AWTFontManager());
//
//			// the call to 'setup' only needs to be done on the first paint
//			renderer.setup(molecule, this.getBounds());
//		}
//
//		@Override
//		public void paint(Graphics g) {
//			Rectangle bounds = this.getBounds();
//			Rectangle smaller = new Rectangle();
//			smaller.setBounds(bounds.x + 5, bounds.y - 5, bounds.width - 10,
//					bounds.height - 10);
//
//			// paint the background
//			Graphics2D g2 = (Graphics2D) g;
//			g2.setColor(Color.WHITE);
//			g2.fillRect(0, 0, bounds.width, bounds.height);
//
//			// the paint method also needs a toolkit-specific renderer
//			renderer.paint(molecule, new AWTDrawVisitor(g2), smaller, true);
//		}
//	}
//
//	/**
//	 * starts the JChemPaint in a JInternalFrame
//	 * 
//	 * @param attribute
//	 *            attribute values with stored CML
//	 */
//	public CMLEditor(Attribute attribute) {
//
//		setLayout(new BorderLayout());
//
//		if (attribute != null) {
//
//			// JTable for value
//			table = new JTable(new GDSTableModel(attribute));
//			this.add(table, BorderLayout.NORTH);
//
//			// get molecule data out of chemical structure
//			String value = (String) attribute.getValue();
//			if (value != null && value.length() > 0) {
//
//				// try to read in molecule from cml
//				CMLReader cml = new CMLReader(new ByteArrayInputStream(
//						value.getBytes()));
//				IChemFile chemFile = new ChemFile();
//				try {
//					cml.read(chemFile);
//				} catch (CDKException e) {
//					ErrorDialog.show(e);
//				}
//
//				// get first molecule etc.
//				IAtomContainer molecule = chemFile.getChemSequence(0)
//						.getChemModel(0).getMoleculeSet().getMolecule(0);
//
//				// build structure diagram
//				molecule = StructureUtils.layoutMolecule(molecule);
//
//				// render molecule
//				this.add(new RendererPanel(molecule), BorderLayout.CENTER);
//			}
//		}
//	}
//
//	public void flushChanges() {
//		TableCellEditor ce = table.getCellEditor();
//		if (ce != null) {
//			ce.stopCellEditing();
//		}
//	}
//
//	/**
//	 * returns the default value
//	 * 
//	 * @return Object always null
//	 */
//	public Object getDefaultValue() {
//		return null;
//	}
//
//}
