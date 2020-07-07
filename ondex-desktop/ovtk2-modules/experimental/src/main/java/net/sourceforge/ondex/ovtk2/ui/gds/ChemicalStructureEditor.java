//package net.sourceforge.ondex.ovtk2.ui.gds;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Rectangle;
//import java.io.IOException;
//import java.io.StringReader;
//import java.io.StringWriter;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.swing.JPanel;
//import javax.swing.JTable;
//import javax.swing.table.AbstractTableModel;
//import javax.swing.table.TableCellEditor;
//
//import net.sourceforge.ondex.config.OndexJAXBContextRegistry;
//import net.sourceforge.ondex.core.Attribute;
//import net.sourceforge.ondex.ovtk2.util.ErrorDialog;
//import net.sourceforge.ondex.ovtk2.util.chemical.StructureUtils;
//import net.sourceforge.ondex.tools.data.ChemicalStructure;
//import net.sourceforge.ondex.tools.data.ChemicalStructureHolder;
//
//import org.openscience.cdk.Molecule;
//import org.openscience.cdk.MoleculeSet;
//import org.openscience.cdk.exception.CDKException;
//import org.openscience.cdk.interfaces.IAtomContainer;
//import org.openscience.cdk.io.MDLV2000Reader;
//import org.openscience.cdk.io.SMILESReader;
//import org.openscience.cdk.io.SMILESWriter;
//import org.openscience.cdk.renderer.AtomContainerRenderer;
//import org.openscience.cdk.renderer.font.AWTFontManager;
//import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
//import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
//import org.openscience.cdk.renderer.generators.IGenerator;
//import org.openscience.cdk.renderer.generators.RingGenerator;
//import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
//
///**
// * Attribute renderer plug-in for chemical structures
// * 
// * @author taubertj
// * 
// */
//public class ChemicalStructureEditor extends JPanel implements GDSEditor {
//
//	private class GDSTableModel extends AbstractTableModel {
//
//		// generated
//		private static final long serialVersionUID = 1176410895403511742L;
//
//		private Attribute attribute = null;
//
//		private Object o = null;
//
//		private ChemicalStructure structure = null;
//
//		/**
//		 * Constructor sets Attribute.
//		 * 
//		 * @param attribute
//		 *            Attribute
//		 */
//		public GDSTableModel(Attribute attribute) {
//			this.attribute = attribute;
//			this.o = attribute.getValue();
//			if (this.o instanceof ChemicalStructure)
//				this.structure = (ChemicalStructure) o;
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
//			if (structure != null && row == 0) {
//				return structure.toString();
//			} else
//				return o.toString();
//		}
//
//		public boolean isCellEditable(int row, int col) {
//			return true;
//		}
//
//		public void setValueAt(Object value, int row, int col) {
//			if (structure != null && row == 0) {
//				structure.setSMILES((String) value);
//				attribute.setValue(structure);
//				fireTableCellUpdated(row, col);
//			}
//		}
//	}
//
//	/**
//	 * generated
//	 */
//	private static final long serialVersionUID = 565152003637900042L;
//
//	static {
//		// comes with its own marshaller etc
//		OndexJAXBContextRegistry jaxbRegistry = OndexJAXBContextRegistry
//				.instance();
//		jaxbRegistry.addClassBindings(ChemicalStructureHolder.class);
//		jaxbRegistry.addHolder(ChemicalStructure.class,
//				ChemicalStructureHolder.class);
//		System.out.println("adding to OndexJAXBContextRegistry");
//	}
//
//	private JTable table;
//
//	public static class RendererPanel extends JPanel {
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
//	 *            attribute values with stored structure
//	 */
//	public ChemicalStructureEditor(Attribute attribute) {
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
//			ChemicalStructure cs = (ChemicalStructure) attribute.getValue();
//			IAtomContainer molecule = new Molecule();
//			String mol = cs.getMOL();
//			if (mol != null && mol.length() > 0) {
//
//				// try to read in molecule from mol file
//				MDLV2000Reader mdl = new MDLV2000Reader(new StringReader(mol));
//				try {
//					mdl.read(molecule);
//				} catch (CDKException e) {
//					ErrorDialog.show(e);
//				}
//
//				// construct SMILE string for molecule
//				if (cs.getSMILES().length() == 0) {
//					StringWriter smilesString = new StringWriter();
//					SMILESWriter sw = new SMILESWriter(smilesString);
//					try {
//						sw.write(molecule);
//						sw.close();
//						cs.setSMILES(smilesString.toString());
//					} catch (CDKException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//
//			} else {
//
//				// construct molecule from SMILES
//				if (cs.getSMILES().length() > 0) {
//					SMILESReader sr = new SMILESReader(new StringReader(
//							cs.getSMILES()));
//					MoleculeSet ms = new MoleculeSet();
//					try {
//						sr.read(ms);
//						// not sure this is right, but I assume there is only
//						// one molecule represented by a SMILE in our case
//						molecule = ms.getMolecule(0);
//					} catch (CDKException e) {
//						ErrorDialog.show(e);
//					}
//				}
//			}
//
//			// build structure diagram
//			molecule = StructureUtils.layoutMolecule(molecule);
//
//			// render molecule
//			this.add(new RendererPanel(molecule), BorderLayout.CENTER);
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
