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
public class RelationAttributeChecker {

    protected final AttributePrototype ap;

    protected final ONDEXGraphMetaData meta;

    protected static final Map<String, RelationAttributeCheckerFactory> typeToChecker = new HashMap<String, RelationAttributeCheckerFactory>();

    static {
        typeToChecker.put(DefConst.DEFCC, new RelationAttributeCheckerFactory() {
            //(DEFCC, pos)
            public RelationAttributeChecker getAttributeChecker(AttributePrototype ap, ONDEXGraph graph) {
                return new RelationAttributeChecker(ap, graph) {
                    public boolean check(ONDEXRelation r) {
                        return r.getOfType().getId().equals(this.ap.getDef()[1]);
                    }
                };
            }
        });

        typeToChecker.put(DefConst.DEFEVIDENCE, new RelationAttributeCheckerFactory() {
            //(DEFEVIDENCE, pos);
            public RelationAttributeChecker getAttributeChecker(AttributePrototype ap, ONDEXGraph graph) {
                return new RelationAttributeChecker(ap, graph) {
                    public boolean check(ONDEXRelation r) {
                        String[] def = ap.getDef();
                        if (def[1] != null) {
                            for (EvidenceType ev : r.getEvidence()) {
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
        typeToChecker.put(DefConst.DEFATTR, new RelationAttributeCheckerFactory() {
            //(DEFATTR, attributeName, TEXT, pos);
            public RelationAttributeChecker getAttributeChecker(AttributePrototype ap, ONDEXGraph graph) {
                return new RelationAttributeChecker(ap, graph) {
                    public boolean check(ONDEXRelation r) throws NullValueException, AccessDeniedException, EmptyStringException {
                        String[] def = ap.getDef();
                        AttributeName att = null;
                        if (def[1] != null && def[2] != null) {
                            att = createAttName(meta, def[1], def[2]);
                            Attribute attribute = r.getAttribute(att);
                            if (def[3] != null) {
                                if (attribute.getValue().getClass().equals(String.class)) {
                                    return def[3].equals(attribute.getValue());
                                }
                                return def[3].equals(attribute.toString());
                            }
                        } else if (def[1] != null) {
                            for (Attribute attribute : r.getAttributes()) {
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
                            for (Attribute attribute : r.getAttributes()) {
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
                        Set<Attribute> ait = r.getAttributes();
                        boolean result = ait.size() > 0;
                        return result;
                    }
                };
            }
        });
    }

    private interface RelationAttributeCheckerFactory {
        public RelationAttributeChecker getAttributeChecker(AttributePrototype ap, ONDEXGraph graph);
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

    public static RelationAttributeChecker getConceptChecker(AttributePrototype ap, ONDEXGraph graph) {
        RelationAttributeCheckerFactory tmp = typeToChecker.get(ap.getType());
        if (tmp == null) return null;
        return tmp.getAttributeChecker(ap, graph);
    }

    protected RelationAttributeChecker(AttributePrototype ap, ONDEXGraph graph) {
        this.ap = ap;
        this.meta = graph.getMetaData();
    }

    public boolean check(ONDEXRelation r) throws NullValueException, AccessDeniedException, EmptyStringException {
        return false;
    }
}