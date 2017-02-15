package net.sourceforge.ondex.tools.subgraph;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author lysenkoa
 */
public class ConceptAttributeChecker {

    protected final AttributePrototype ap;
    protected final ONDEXGraphMetaData meta;

    protected static final Map<String, ConceptAttributeCheckerFactory> typeToChecker = new HashMap<String, ConceptAttributeCheckerFactory>();

    static {
        typeToChecker.put(DefConst.DEFCC, new ConceptAttributeCheckerFactory() {
            //(DEFCC, pos)
            public ConceptAttributeChecker getAttributeChecker(AttributePrototype ap, ONDEXGraph graph) {
                return new ConceptAttributeChecker(ap, graph) {
                    public boolean check(ONDEXConcept c) {
                        return c.getOfType().getId().equals(this.ap.getDef()[1]);
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFACC, new ConceptAttributeCheckerFactory() {
            //(DEFACC, "false", cv, pos)
            public ConceptAttributeChecker getAttributeChecker(AttributePrototype ap, ONDEXGraph graph) {
                return new ConceptAttributeChecker(ap, graph) {
                    public boolean check(ONDEXConcept c) throws NullValueException, EmptyStringException, AccessDeniedException {
                        String[] def = ap.getDef();
                        Boolean isAmbiguous = null;
                        if (def[1] != null) {
                            if (def[1].equalsIgnoreCase("true"))
                                isAmbiguous = true;
                            else if (def[1].equalsIgnoreCase("false"))
                                isAmbiguous = false;
                        }
                        DataSource dataSource = null;
                        if (def[2] != null) {
                            try {
                                dataSource = GraphEntityPrototype.createDataSource(meta, def[2]);
                            } catch (Exception e) {
                            }
                        }
                        if (def[3] != null) {
                            if (dataSource != null) {
                            	System.err.println("c:"+c);
                            	System.err.println("def[3]:"+def[3]);
                            	System.err.println("dataSource:"+dataSource);
                                ConceptAccession ca = c.getConceptAccession(def[3], dataSource);
                                if (ca != null && (isAmbiguous == null || isAmbiguous.equals(ca.isAmbiguous()))) {
                                    return true;
                                }
                                return false;
                            }
                            for (ConceptAccession ca : c.getConceptAccessions()) {
                                if (ca.getAccession().equals(def[3])
                                        && (isAmbiguous == null || isAmbiguous.equals(ca.isAmbiguous()))) {
                                    return true;
                                }
                            }
                            return false;
                        } else if (dataSource != null) {
                            for (ConceptAccession ca : c.getConceptAccessions()) {
                                if (ca.getElementOf().equals(dataSource)
                                        && (isAmbiguous == null || isAmbiguous.equals(ca.isAmbiguous()))) {
                                    return true;
                                }
                            }
                            return false;
                        } else if (isAmbiguous != null) {
                            for (ConceptAccession ca : c.getConceptAccessions()) {
                                if (isAmbiguous.equals(ca.isAmbiguous())) {
                                    return true;
                                }
                            }
                            return false;
                        }
                        Set<ConceptAccession> ait = c.getConceptAccessions();
                        boolean result = ait.size() > 0;
                        return result;
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFNAME, new ConceptAttributeCheckerFactory() {
            public ConceptAttributeChecker getAttributeChecker(AttributePrototype ap, ONDEXGraph graph) {
                //(DEFNAME,"true", pos);
                return new ConceptAttributeChecker(ap, graph) {
                    public boolean check(ONDEXConcept c) throws NullValueException, EmptyStringException, AccessDeniedException {
                        String[] def = ap.getDef();
                        Boolean isPreferred = null;
                        if (def[1] != null) {
                            if (def[1].equalsIgnoreCase("true"))
                                isPreferred = true;
                            else if (def[1].equalsIgnoreCase("false"))
                                isPreferred = false;
                        }
                        if (def[2] != null) {
                            ConceptName n = c.getConceptName(def[2]);
                            return (n != null && (isPreferred == null || isPreferred.equals(n.isPreferred())));
                        }
                        Set<ConceptName> ait = c.getConceptNames();
                        boolean result = ait.size() > 0;
                        return result;
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFEVIDENCE, new ConceptAttributeCheckerFactory() {
            //(DEFEVIDENCE, pos);
            public ConceptAttributeChecker getAttributeChecker(AttributePrototype ap, ONDEXGraph graph) {
                return new ConceptAttributeChecker(ap, graph) {
                    public boolean check(ONDEXConcept c) {
                        String[] def = ap.getDef();
                        if (def[1] != null) {
                            for (EvidenceType ev : c.getEvidence()) {
                                if (ev.getId().equals(def[1]))
                                    return true;
                            }
                            return false;
                        }
                        return true;
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFDATASOURCE, new ConceptAttributeCheckerFactory() {
            //(DEFDATASOURCE, pos);
            public ConceptAttributeChecker getAttributeChecker(AttributePrototype ap, ONDEXGraph graph) {
                return new ConceptAttributeChecker(ap, graph) {
                    public boolean check(ONDEXConcept c) {
                        String[] def = ap.getDef();
                        if (def[1] != null) {
                            return c.getElementOf().getId().equals(def[1]);
                        }
                        return true;
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFPID, new ConceptAttributeCheckerFactory() {
            //(DEFPID, pos);
            public ConceptAttributeChecker getAttributeChecker(AttributePrototype ap, ONDEXGraph graph) {
                return new ConceptAttributeChecker(ap, graph) {
                    public boolean check(ONDEXConcept c) {
                        String[] def = ap.getDef();
                        if (def[1] != null) {
                            return c.getPID().equals(def[1]);
                        }
                        return true;
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFATTR, new ConceptAttributeCheckerFactory() {
            //(DEFATTR, attributeName, TEXT, pos);
            public ConceptAttributeChecker getAttributeChecker(AttributePrototype ap, ONDEXGraph graph) {
                return new ConceptAttributeChecker(ap, graph) {
                    public boolean check(ONDEXConcept c) throws NullValueException, AccessDeniedException, EmptyStringException {
                        String[] def = ap.getDef();
                        AttributeName att = null;
                        if (def[1] != null && def[2] != null) {
                            att = createAttName(meta, def[1], def[2]);
                            Attribute attribute = c.getAttribute(att);
                            if (def[3] != null) {
                                if (attribute.getValue().getClass().equals(String.class)) {
                                    return def[3].equals(attribute.getValue());
                                }
                                return def[3].equals(attribute.toString());
                            }
                        } else if (def[1] != null) {
                            for (Attribute attribute : c.getAttributes()) {
                                if (attribute.getOfType().getId().equals(def[1])) {
                                    if (def[3] != null) {
                                        if (attribute.getValue().getClass().equals(String.class)) {
                                            return def[3].equals(attribute.getValue());
                                        }
                                        return def[3].equals(attribute.toString());
                                    }
                                    return true;
                                }
                            }
                            return false;
                        } else if (def[2] != null) {
                            for (Attribute attribute : c.getAttributes()) {
                                if (attribute.getValue().getClass().equals(convertToClass(def[2]))) {
                                    if (def[3] != null) {
                                        if (attribute.getValue().getClass().equals(String.class)) {
                                            return def[3].equals(attribute.getValue());
                                        }
                                        return def[3].equals(attribute.toString());
                                    }
                                    return true;
                                }
                            }
                            return false;
                        }
                        Set<Attribute> ait = c.getAttributes();
                        boolean result = ait.size() > 0;
                        return result;
                    }
                };
            }
        });
    }

    private interface ConceptAttributeCheckerFactory {
        public ConceptAttributeChecker getAttributeChecker(AttributePrototype ap, ONDEXGraph graph);
    }

    public static Class<?> convertToClass(String strCls) {
        Class<?> result = null;
        if (strCls.equals(AttributePrototype.NUMBER) || strCls.equals(AttributePrototype.DOUBLE)) {
            result = java.lang.Double.class;
        } else if (strCls.equals(AttributePrototype.TEXT)) {
            result = java.lang.String.class;
        } else if (strCls.equals(AttributePrototype.INTEGER)) {
            result = java.lang.Integer.class;
        } else if (strCls.equals(AttributePrototype.SMILES)) {
            result = net.sourceforge.ondex.tools.data.ChemicalStructure.class;
        } else if (strCls.equals(AttributePrototype.OBJECT)) {
            result = java.lang.Object.class;
        } else {
            try {
                result = Class.forName(strCls);
            }
            catch (Exception e) {
                result = java.lang.Object.class;
            }
        }
        return result;
    }

    protected static AttributeName createAttName(ONDEXGraphMetaData meta, String type, String clsId) throws NullValueException, EmptyStringException {
        return GraphEntityPrototype.createAttName(meta, type, convertToClass(clsId));
    }

    public static ConceptAttributeChecker getConceptChecker(AttributePrototype ap, ONDEXGraph graph) {
        ConceptAttributeCheckerFactory tmp = typeToChecker.get(ap.getType());
        if (tmp == null) return null;
        return tmp.getAttributeChecker(ap, graph);
    }

    protected ConceptAttributeChecker(AttributePrototype ap, ONDEXGraph graph) {
        this.ap = ap;
        this.meta = graph.getMetaData();
    }

    public boolean check(ONDEXConcept c) throws NullValueException, EmptyStringException, AccessDeniedException {
        return false;
    }
}
