//package net.sourceforge.ondex.ovtk2.ui.popup.items;
//
//import java.awt.Color;
//import java.awt.Component;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//import javax.swing.JOptionPane;
//import net.sourceforge.ondex.core.Attribute;
//import net.sourceforge.ondex.core.AttributeName;
//import net.sourceforge.ondex.core.ConceptClass;
//import net.sourceforge.ondex.core.ONDEXConcept;
//import net.sourceforge.ondex.core.ONDEXGraph;
//import net.sourceforge.ondex.ovtk2.config.Config;
//import net.sourceforge.ondex.ovtk2.ui.popup.EntityMenuItem;
//import net.sourceforge.ondex.ovtk2.util.LayoutNeighbours;
//import net.sourceforge.ondex.ovtk2.util.chemical.SimilarityDocumentFilter;
//import net.sourceforge.ondex.parser.chemblactivity.Parser;
//import net.sourceforge.ondex.parser.chemblactivity.Parser.EXMODE;
//import net.sourceforge.ondex.tools.data.ChemicalStructure;
//import org.apache.log4j.Logger;
//import org.w3c.dom.Document;
//
///**
// * Ondex queries the ChEMBL webservice with the compound(s) SMILE for
// * similarity.
// *
// * @author taubertj
// *
// */
//public class LinkChEMBLCompSimilarityItem extends EntityMenuItem<ONDEXConcept> {
//
//    private Logger log = Logger.getLogger(getClass());
//
//    @Override
//    public boolean accepts() {
//
//        // get meta data
//        ONDEXGraph graph = viewer.getONDEXJUNGGraph();
//        ConceptClass ccComp = graph.getMetaData().getConceptClass("Comp");
//        AttributeName anChemicalStructure = graph.getMetaData()
//                .getAttributeName("ChemicalStructure");
//        if (anChemicalStructure == null) {
//            return false;
//        }
//
//        // look at all selected chemical compounds
//        for (ONDEXConcept c : entities) {
//            if (c.getOfType().equals(ccComp)
//                    && c.getAttribute(anChemicalStructure) != null) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    @Override
//    protected void doAction() {
//
//        // get meta data
//        ONDEXGraph graph = viewer.getONDEXJUNGGraph();
//        ONDEXConcept center = null;
//        ConceptClass ccComp = graph.getMetaData().getConceptClass("Comp");
//        AttributeName anChemicalStructure = graph.getMetaData()
//                .getAttributeName("ChemicalStructure");
//
//        // parse all accessions contained in graph
//        Map<String, Set<ONDEXConcept>> accessions = new HashMap<String, Set<ONDEXConcept>>();
//        for (ONDEXConcept c : entities) {
//            if (c.getOfType().equals(ccComp)
//                    && c.getAttribute(anChemicalStructure) != null) {
//                Attribute attr = c.getAttribute(anChemicalStructure);
//                ChemicalStructure cs = (ChemicalStructure) attr.getValue();
//                String smiles = cs.getSMILES();
//                if (!accessions.containsKey(smiles)) {
//                    accessions.put(smiles, new HashSet<ONDEXConcept>());
//                }
//                accessions.get(smiles).add(c);
//                center = c;
//            }
//        }
//
//        Parser activities = new Parser();
//
//        log.debug("--- parser: " + activities.getClass().getCanonicalName());
//        activities.setONDEXGraph(graph);
//        activities.initMetaData();
//        try {
//            EXMODE mode = EXMODE.CompSimilarity;
//
//            // get XML document from rest service
//            Map<String, Document> docs = activities.retrieveXML(accessions,
//                    mode);
//
//            SimilarityDocumentFilter filter = new SimilarityDocumentFilter(
//                    docs, mode);
//
//            // ask user for filter
//            int option = JOptionPane.showConfirmDialog((Component) viewer,
//                    filter, "Filter on properties",
//                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
//            if (option == JOptionPane.OK_OPTION) {
//
//                docs = filter.getFiltered();
//
//                Set<ONDEXConcept> created = activities.parseActivities(docs,
//                        accessions, mode);
//                System.out
//                        .println("Added " + created.size() + " new concepts.");
//
//                // make new concepts visible
//                viewer.getONDEXJUNGGraph().setVisibility(created, true);
//                for (ONDEXConcept c : created) {
//                    // set something like default attributes
//                    viewer.getNodeColors().updateColor(c,
//                            Config.getColorForConceptClass(c.getOfType()));
//                    viewer.getNodeDrawPaint().updateColor(c, Color.BLACK);
//                    viewer.getNodeShapes().updateShape(c);
//
//                    // make all relations visible
//                    viewer.getONDEXJUNGGraph().setVisibility(
//                            graph.getRelationsOfConcept(c), true);
//                }
//
//                // layout nodes on big circle
//                LayoutNeighbours.layoutNodes(viewer.getVisualizationViewer(),
//                        center, created);
//
//                if (viewer.getMetaGraph() != null) {
//                    viewer.getMetaGraph().updateMetaData();
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    @Override
//    public MENUCATEGORY getCategory() {
//        return MENUCATEGORY.LINK;
//    }
//
//    @Override
//    protected String getMenuPropertyName() {
//        return "Viewer.VertexMenu.LinkChEMBLCompSimilarity";
//    }
//
//    @Override
//    protected String getUndoPropertyName() {
//        return "";
//    }
//
//}
