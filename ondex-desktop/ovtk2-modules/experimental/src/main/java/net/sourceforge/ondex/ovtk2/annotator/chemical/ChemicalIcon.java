//package net.sourceforge.ondex.ovtk2.annotator.chemical;
//
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Rectangle;
//import java.awt.image.BufferedImage;
//import java.io.IOException;
//import java.io.StringReader;
//import java.io.StringWriter;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.swing.Icon;
//
//import net.sourceforge.ondex.core.Attribute;
//import net.sourceforge.ondex.core.AttributeName;
//import net.sourceforge.ondex.core.ONDEXConcept;
//import net.sourceforge.ondex.ovtk2.util.chemical.StructureUtils;
//import net.sourceforge.ondex.tools.data.ChemicalStructure;
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
//import org.openscience.cdk.renderer.generators.BasicBondGenerator;
//import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
//import org.openscience.cdk.renderer.generators.IGenerator;
//import org.openscience.cdk.renderer.generators.RingGenerator;
//import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
//
///**
// * Draws the actual bar chart on an icon. Positive values are in red, negative
// * in green.
// * 
// * @author taubertj
// * @version 01.03.2012
// */
//public class ChemicalIcon implements Icon {
//
//	// is a real icon?
//	private boolean created = false;
//
//	// rendered chemical drawing
//	private BufferedImage image;
//
//	/**
//	 * Constructor for a graph and in context of a concept.
//	 * 
//	 * @param an
//	 *            AttributeName for chemical attribute
//	 * @param concept
//	 *            in context of this concept
//	 */
//	public ChemicalIcon(AttributeName an, ONDEXConcept concept, int size,
//			Color colour) {
//
//		// set image to given size
//		image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
//
//		// check if concept has chemical attribute
//		Attribute attr = concept.getAttribute(an);
//		if (attr != null) {
//			// get molecule data out of chemical structure
//			ChemicalStructure cs = (ChemicalStructure) attr.getValue();
//			IAtomContainer molecule = new Molecule();
//			String mol = cs.getMOL();
//			if (mol != null && mol.length() > 0) {
//
//				// try to read in molecule from mol file
//				MDLV2000Reader mdl = new MDLV2000Reader(new StringReader(mol));
//				try {
//					mdl.read(molecule);
//				} catch (CDKException e) {
//					e.printStackTrace();
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
//						e.printStackTrace();
//					}
//				}
//			}
//
//			// build structure diagram
//			molecule = StructureUtils.layoutMolecule(molecule);
//
//			// paint the background
//			Graphics2D g2 = (Graphics2D) image.getGraphics();
//			g2.setColor(Color.WHITE);
//			g2.fillRect(0, 0, image.getWidth(), image.getHeight());
//			g2.setColor(colour);
//			g2.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);
//
//			// generators make the image elements
//			List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
//			generators.add(new BasicSceneGenerator());
//			//generators.add(new BasicBondGenerator());
//			generators.add(new RingGenerator()); 
//			generators.add(new BasicAtomGenerator());
//
//			// the renderer needs to have a toolkit-specific font manager
//			AtomContainerRenderer renderer = new AtomContainerRenderer(
//					generators, new AWTFontManager());
//
//			Rectangle smaller = new Rectangle();
//			smaller.setBounds(5, 5, image.getWidth() - 10,
//					image.getHeight() - 10);
//
//			// the call to 'setup' only needs to be done on the first paint
//			renderer.setup(molecule, smaller);
//
//			// the paint method also needs a toolkit-specific renderer
//			renderer.paint(molecule, new AWTDrawVisitor(g2), smaller, true);
//
//			created = true;
//		}
//	}
//
//	/**
//	 * Whether or not there was something drawn.
//	 * 
//	 * @return boolean
//	 */
//	public boolean created() {
//		return created;
//	}
//
//	@Override
//	public int getIconHeight() {
//		if (image != null)
//			return image.getHeight();
//		return 2;
//	}
//
//	@Override
//	public int getIconWidth() {
//		if (image != null)
//			return image.getWidth();
//		return 2;
//	}
//
//	@Override
//	public void paintIcon(Component c, Graphics g, int x, int y) {
//		if (image != null) {
//			g.drawImage(image, x, y, null);
//		} else {
//			g.setColor(Color.BLACK);
//			g.fillRect(x, y, 2, 2);
//		}
//	}
//}
