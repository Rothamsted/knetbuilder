package net.sourceforge.ondex.tools.subgraph;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;

import java.util.*;

import static net.sourceforge.ondex.tools.subgraph.DefConst.PositionProcessor;

/**
 * @author lysenkoa
 */
@SuppressWarnings("unchecked")
public class ConceptPrototype extends GraphEntityPrototype {
    private final List<String[]> evValues = new LinkedList<String[]>();
    private final List<String[]> nameValues = new LinkedList<String[]>();
    private final List<String[]> accValues = new LinkedList<String[]>();
    private final List<String[]> gdsValues = new LinkedList<String[]>();

    private final List<String[]>[] listSets = new List[]{evValues, nameValues, accValues, gdsValues};

    private ONDEXGraph graph;
    private ONDEXGraphMetaData meta;
    private String id = "";
    private DataSource elementOf;
    private ConceptClass ofType;
    private EvidenceType evidence;
    private List<AttributePrototype> attributePrototypes;
    private ONDEXConcept currentValue = null;

    private final Map<String, net.sourceforge.ondex.tools.subgraph.DefConst.PositionProcessor> typeToProcessor = new HashMap<String, PositionProcessor>();

    public ConceptPrototype(ONDEXGraph graph, AttributePrototype... aps) throws NullValueException, EmptyStringException {
        this.attributePrototypes = new LinkedList<AttributePrototype>(Arrays.asList(aps));
        this.graph = graph;
        this.meta = graph.getMetaData();
        this.elementOf = createDataSource(meta, "UC");
        this.ofType = createCC(meta, "Thing");
        this.evidence = createEvidence(meta, "IMPD");

        typeToProcessor.put(DefConst.DEFATTR, new PositionProcessor() {
            public void process(AttributePrototype ap) {
                List<String[]> newProts = ap.getValue();
                for (String[] newProt : newProts) {
                    for (String s : newProt)
                        if (s == null) return;
                    listSets[3].add(newProt);
                }

            }
        });
        typeToProcessor.put(DefConst.DEFEVIDENCE, new net.sourceforge.ondex.tools.subgraph.DefConst.PositionProcessor() {
            public void process(AttributePrototype ap) {
                List<String[]> newProts = ap.getValue();
                for (String[] newProt : newProts) {
                    for (String s : newProt)
                        if (s == null) return;
                    listSets[0].add(newProt);
                }
            }
        });
        typeToProcessor.put(DefConst.DEFNAME, new PositionProcessor() {
            public void process(AttributePrototype ap) {
                List<String[]> newProts = ap.getValue();
                for (String[] newProt : newProts) {
                    for (String s : newProt)
                        if (s == null) return;
                    listSets[1].add(newProt);
                }
            }
        });
        typeToProcessor.put(DefConst.DEFACC, new PositionProcessor() {
            public void process(AttributePrototype ap) {
                List<String[]> newProts = ap.getValue();
                for (String[] newProt : newProts) {
                    for (String s : newProt)
                        if (s == null) return;
                    listSets[2].add(newProt);
                }
            }
        });
        typeToProcessor.put(DefConst.DEFDATASOURCE, new PositionProcessor() {
            public void process(AttributePrototype ap) throws NullValueException, EmptyStringException {
                List<String[]> newProts = ap.getValue();
                for (String[] newProt : newProts) {
                    for (String s : newProt)
                        if (s == null) return;
                    elementOf = createDataSource(meta, newProt[newProt.length - 1]);
                }
            }
        });
        typeToProcessor.put(DefConst.DEFCC, new PositionProcessor() {
            public void process(AttributePrototype ap) throws NullValueException, EmptyStringException {
                List<String[]> newProts = ap.getValue();
                for (String[] newProt : newProts) {
                    for (String s : newProt) {
                        if (s == null) {
                            ofType = null;
                            return;
                        }
                    }
                    ofType = createCC(meta, newProt[newProt.length - 1]);
                }
            }
        });
        typeToProcessor.put(DefConst.DEFPID, new PositionProcessor() {
            public void process(AttributePrototype ap) {
                List<String[]> newProts = ap.getValue();
                for (String[] newProt : newProts) {
                    for (String s : newProt)
                        if (s == null) return;
                    id = newProt[newProt.length - 1];
                }
            }
        });
    }

    public void addAttributes(AttributePrototype... aps) {
        Collections.addAll(attributePrototypes, aps);
    }

    public ONDEXConcept getValue() {
        return currentValue;
    }

    public ONDEXConcept parse(String[] vector) throws NullValueException, AccessDeniedException, EmptyStringException {
        currentValue = null;

        for (AttributePrototype ap : attributePrototypes) {
            ap.parse(vector);
            typeToProcessor.get(ap.getType()).process(ap);
        }

        if (elementOf == null) {
            System.err.println("Error!");
            throw new NullValueException("Could not create concept - no valid type specifed.");
        }
        currentValue = graph.getFactory().createConcept(id, elementOf, ofType, evidence);

        /*evValues*/
        for (String[] prot : listSets[0])
            currentValue.addEvidenceType(createEvidence(meta, prot[prot.length - 1]));

        /*nameValues*/
        for (String[] prot : listSets[1]) {
            if (prot[2].equals("") || prot[2] == null)
                continue;
            try {
                boolean isPreferred = Boolean.valueOf(prot[1]);
                currentValue.createConceptName(prot[2], isPreferred);
            } catch (Exception e) {
                currentValue.createConceptName(prot[2], false);
            }
        }

        /*accValues*/
        for (String[] prot : listSets[2]) {
            if (prot[3].equals("") || prot[3] == null)
                continue;
            try {
                boolean isAmbiguous = Boolean.valueOf(prot[1]);
                currentValue.createConceptAccession(prot[3].trim().toUpperCase(), createDataSource(meta, prot[2]), isAmbiguous);
            } catch (Exception e) {
                currentValue.createConceptAccession(prot[3].trim().toUpperCase(), createDataSource(meta, prot[2]), true);
            }
        }

        /*gdsValues*/
        for (String[] prot : listSets[3]) {
            Class<?> cls = null;
            if (prot[1].equals("") || prot[1] == null || prot[3].equals("") || prot[3] == null)
                continue;
            boolean index = false;
            if(prot.length > 4 && prot[4] != null){
            	try{
            		index = prot[4].equalsIgnoreCase("true");
            	}
            	catch(Exception e){
            		 
            	}
            }
            if (prot[2].equalsIgnoreCase(DefConst.NUMBER)) {
      				try{
      					cls = java.lang.Double.class;
      					
      					AttributeName attName = createAttName(meta, prot[1], cls);
      					// Marco Brandizi: this is to fix the fact that PVALUE is declared as float but PathParser always
      					// gets double values from input
      					// TODO: this occurs in many places, we need to factorise
      					Number value; 
      					if ( attName.getDataType ().equals ( Float.class ) ) value = Float.valueOf ( prot [ 3 ] );
      					else value = Double.valueOf ( prot [ 3 ] );

      					currentValue.createAttribute( attName, value, index);
      				}
      				catch(Exception e){
      					System.err.println ( String.format ( 
      						"%s while parsing %s: %s", e.getClass ().getSimpleName (), Arrays.toString ( prot ), e.getMessage () 
      					));
      				}
            } 
            
            else if (prot[2].equalsIgnoreCase(DefConst.INTEGER)) {
                cls = java.lang.Integer.class;
                try {
                    currentValue.createAttribute(createAttName(meta, prot[1], cls), Integer.valueOf(prot[3]), index);
                }
                catch (Exception e) {
        					System.err.println ( String.format ( 
        						"%s while parsing %s: %s", e.getClass ().getSimpleName (), Arrays.toString ( prot ), e.getMessage () 
        					));
                }
            } 
            
            else if(prot[2].equalsIgnoreCase(DefConst.SMILES)){
				cls = net.sourceforge.ondex.tools.data.ChemicalStructure.class;
				net.sourceforge.ondex.tools.data.ChemicalStructure cs = new net.sourceforge.ondex.tools.data.ChemicalStructure();
				cs.setSMILES(prot[3]);
				currentValue.createAttribute(createAttName(meta, prot[1], cls), cs, index);
			}
            
            else {
                cls = java.lang.String.class;
                if(prot[3] == null){
                	System.err.println("Skipped value");
                	continue;
                }
                currentValue.createAttribute(createAttName(meta, prot[1], cls), prot[3], index);
            }
        }
        for (List<String[]> dataList : listSets) dataList.clear();
        return currentValue;
    }
}
