//package net.sourceforge.ondex.ovtk2.util.chemical;
//
//import org.openscience.cdk.AtomContainer;
//import org.openscience.cdk.DefaultChemObjectBuilder;
//import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
//import org.openscience.cdk.exception.CDKException;
//import org.openscience.cdk.geometry.GeometryTools;
//import org.openscience.cdk.geometry.Projector;
//import org.openscience.cdk.graph.ConnectivityChecker;
//import org.openscience.cdk.interfaces.IAtomContainer;
//import org.openscience.cdk.interfaces.IMolecule;
//import org.openscience.cdk.interfaces.IMoleculeSet;
//import org.openscience.cdk.layout.StructureDiagramGenerator;
//import org.openscience.cdk.tools.CDKHydrogenAdder;
//import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
//
//public class StructureUtils {
//
//	// ///////
//	// FIELDS
//	// ///////
//
//	private static final StructureDiagramGenerator sdg = new StructureDiagramGenerator();
//
//	// ////////
//	// METHODS
//	// ////////
//
//	/**
//	 * Lays out a 2D structure diagram.
//	 * 
//	 * @param ac
//	 *            the molecule to layout.
//	 * @return A new molecule laid out in 2D. If the molecule already has 2D
//	 *         coordinates then it is returned unchanged. If layout fails then
//	 *         null is returned.
//	 */
//	public static IAtomContainer layout2D(final IAtomContainer ac) {
//		// Generate 2D coordinates?
//		if (GeometryTools.has2DCoordinates(ac)) {
//			return ac; // already has 2D coordinates.
//		} else {
//			// Generate 2D structure diagram (for each connected component).
//			final IAtomContainer ac2d = new AtomContainer();
//			final IMoleculeSet som = ConnectivityChecker
//					.partitionIntoMolecules(ac);
//			for (int n = 0; n < som.getMoleculeCount(); n++) {
//				synchronized (sdg) {
//					IMolecule mol = som.getMolecule(n);
//					sdg.setMolecule(mol, true);
//					try {
//						// Generate 2D coords for this molecule.
//						sdg.generateCoordinates();
//						mol = sdg.getMolecule();
//					} catch (final Exception e) {
//						// Use projection instead.
//						Projector.project2D(mol);
//					}
//
//					ac2d.add(mol); // add 2D molecule.
//				}
//			}
//
//			return GeometryTools.has2DCoordinates(ac2d) ? ac2d : null;
//		}
//	}
//
//	/**
//	 * Layout molecule structure diagram.
//	 * 
//	 * @param mol
//	 *            the molecule to layout.
//	 * @return Laid out molecule.
//	 */
//	public static IAtomContainer layoutMolecule(final IAtomContainer mol) {
//		// Layout structure.
//		final IAtomContainer ac = makeHydrogensImplicit(mol);
//
//		// Aromaticity.
//		try {
//			CDKHueckelAromaticityDetector.detectAromaticity(ac);
//		} catch (final CDKException e) {
//			// failed.
//		}
//		ac.setProperties(mol.getProperties());
//
//		return layout2D(ac);
//	}
//
//	/**
//	 * Implicitly represent the hydrogens in a structure.
//	 * 
//	 * @param mol
//	 *            The structure to convert.
//	 * @return The molecule(s) with implicit hydrogens.
//	 */
//	public static IAtomContainer makeHydrogensImplicit(final IAtomContainer mol) {
//		// Explicit -> Implicit H: addExH then removeH is better than
//		// removeH then addImpH.
//
//		final IMolecule m2 = DefaultChemObjectBuilder.getInstance()
//				.newInstance(IMolecule.class, mol);
//		try {
//
//			CDKHydrogenAdder.getInstance(m2.getBuilder()).addImplicitHydrogens(
//					m2);
//		} catch (final Throwable e) {
//			// failed.
//		}
//
//		// Explicit -> Implicit H.
//		return AtomContainerManipulator
//				.removeHydrogensPreserveMultiplyBonded(m2);
//	}
//
//	// /////////////
//	// CONSTRUCTORS
//	// /////////////
//
//	// Static utility class - can't construct.
//
//	private StructureUtils() {
//		// do nothing.
//	}
//}
