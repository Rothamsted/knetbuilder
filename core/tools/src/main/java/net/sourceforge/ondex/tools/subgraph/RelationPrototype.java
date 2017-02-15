package net.sourceforge.ondex.tools.subgraph;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.tools.subgraph.DefConst.PositionProcessor;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author lysenkoa
 */
@SuppressWarnings("unchecked")
public class RelationPrototype extends GraphEntityPrototype {
    private final List<String[]> evValues = new ArrayList<String[]>();
    private final List<String[]> gdsValues = new ArrayList<String[]>();

    private final Map<String, PositionProcessor> typeToProcessor = new HashMap<String, PositionProcessor>();
    private final List<String[]>[] listSets = new List[]{evValues, gdsValues};

    private ONDEXGraph graph;
    private ONDEXGraphMetaData meta;
    private RelationType ofTypeSet;
    private EvidenceType evidence;
    private List<AttributePrototype> attributePrototypes;
    private ONDEXRelation currentValue = null;

    public RelationPrototype(ONDEXGraph graph, AttributePrototype... aps) throws NullValueException, EmptyStringException {
        this.graph = graph;
        this.attributePrototypes = new LinkedList<AttributePrototype>(Arrays.asList(aps));
        meta = graph.getMetaData();
        ofTypeSet = createRT(meta, "r");
        evidence = createEvidence(meta, "IMPD");

        typeToProcessor.put(DefConst.DEFATTR, new PositionProcessor() {
            public void process(AttributePrototype ap) {
                List<String[]> newProts = ap.getValue();
                for (String[] newProt : newProts) {
                    for (String s : newProt)
                        if (s == null) return;
                    listSets[1].add(newProt);
                }
            }
        });
        typeToProcessor.put(DefConst.DEFEVIDENCE, new PositionProcessor() {
            public void process(AttributePrototype ap) {
                List<String[]> newProts = ap.getValue();
                for (String[] newProt : newProts) {
                    for (String s : newProt)
                        if (s == null) return;
                    listSets[0].add(newProt);
                }
            }
        });
        typeToProcessor.put(DefConst.DEFRT, new PositionProcessor() {
            public void process(AttributePrototype ap) throws NullValueException, EmptyStringException {
                List<String[]> newProts = ap.getValue();
                for (String[] newProt : newProts) {
                    for (String s : newProt) {
                        if (s == null) {
                            ofTypeSet = null;
                            return;
                        }
                    }
                    ofTypeSet = createRT(meta, newProt[newProt.length - 1]);
                }
            }
        });
    }

    public void addAttributes(AttributePrototype... aps) {
        Collections.addAll(attributePrototypes, aps);
    }

    public ONDEXRelation getValue() {
        return currentValue;
    }

    public ONDEXRelation parse(ConceptPrototype source, ConceptPrototype target, String... vector) throws NullValueException, EmptyStringException {
        return parse(source.getValue(), target.getValue(), vector);
    }

    public ONDEXRelation parse(ONDEXConcept source, ConceptPrototype target, String... vector) throws NullValueException, EmptyStringException {
        return parse(source, target.getValue(), vector);
    }

    public ONDEXRelation parse(ConceptPrototype source, ONDEXConcept target, String... vector) throws NullValueException, EmptyStringException {
        return parse(source.getValue(), target, vector);
    }

    public ONDEXRelation parse(ONDEXConcept source, ONDEXConcept target, String... vector) throws NullValueException, EmptyStringException {
        currentValue = null;
        if (target == null || source == null) return null;

        for (AttributePrototype ap : attributePrototypes) {
            ap.parse(vector);
            typeToProcessor.get(ap.getType()).process(ap);
        }

        if (ofTypeSet == null)
            return null;
        
        currentValue = graph.getFactory().createRelation(source, target, ofTypeSet, evidence);

        /*evValues*/
        for (String[] prot : listSets[0])
            currentValue.addEvidenceType(createEvidence(meta, prot[prot.length - 1]));

        /*gdsValues*/
        for (String[] prot : listSets[1]) {
            Class<?> cls = null;
            if (prot[1].equals("") || prot[1] == null || prot[3].equals("") || prot[3] == null)
                continue;
            if (prot[2].equalsIgnoreCase(DefConst.TEXT)) {
                cls = java.lang.String.class;
                currentValue.createAttribute(createAttName(meta, prot[1], cls), prot[3], false);
            } 
            
            else if (prot[2].equalsIgnoreCase(DefConst.NUMBER)) {
      				try{
      					cls = java.lang.Double.class;
      					
      					AttributeName attName = createAttName(meta, prot[1], cls);
      					// Marco Brandizi: this is to fix the fact that PVALUE is declared as float but PathParser always
      					// gets double values from input
      					Number value; 
      					if ( attName.getDataType ().equals ( Float.class ) ) value = Float.valueOf ( prot [ 3 ] );
      					else value = Double.valueOf ( prot [ 3 ] );

      					currentValue.createAttribute( attName, value, false);
      				}
      				catch(Exception e){
      					System.err.println ( String.format ( 
      						"%s while parsing %s: %s", e.getClass ().getSimpleName (), Arrays.toString ( prot ), e.getMessage () 
      					));
      				}
            } 
            
            else if (prot[2].equalsIgnoreCase(DefConst.INTEGER)) {
                cls = java.lang.Double.class;
                try {
                    currentValue.createAttribute(createAttName(meta, prot[1], cls), Integer.valueOf(prot[3]), false);
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
				currentValue.createAttribute(createAttName(meta, prot[1], cls), cs, false);
			}
            
            else if (prot[2].equalsIgnoreCase(DefConst.COLOR)) {
                String[] rgb = prot[3].split(", ");
                if (rgb.length != 3) {
                    rgb = prot[3].split(" ");
                }
                if (rgb.length == 3) {
                    try {
                        Color c = new Color(Integer.valueOf(rgb[0]), Integer.valueOf(rgb[1]), Integer.valueOf(rgb[2]));
                        currentValue.createAttribute(createAttName(meta, prot[1], Color.class), c, false);
                    }
                    catch (Exception e) {
                        System.err.println("Unparsable colour value!");
                    }
                } else {
                    System.err.println("Unparsable colour value!");
                }
            } else {
                cls = java.lang.String.class;
                currentValue.createAttribute(createAttName(meta, prot[1], cls), prot[3], false);
            }
        }
        for (List<String[]> dataList : listSets) dataList.clear();
        return currentValue;
    }
}
