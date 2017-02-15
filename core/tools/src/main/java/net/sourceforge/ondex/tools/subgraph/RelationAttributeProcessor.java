package net.sourceforge.ondex.tools.subgraph;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author lysenkoa
 */
@SuppressWarnings("unchecked")
public abstract class RelationAttributeProcessor {
    protected static final Map<String, RelationAttributeProcessorFactory> typeToChecker = new HashMap<String, RelationAttributeProcessorFactory>();

    static {
        typeToChecker.put(DefConst.DEFCC, new RelationAttributeProcessorFactory() {
            //(DEFCC, pos)
            public RelationAttributeProcessor getAttributeProcessor(AttributePrototype ap, ONDEXGraph graph) {
                return new RelationAttributeProcessor(ap, graph) {
                    private RelationType cc;

                    public void setSource(ONDEXRelation r) {
                        cc = r.getOfType();
                    }

                    public Set getMatches(ONDEXRelation r) {
                        Set result = new HashSet();
                        if (r.getOfType().equals(cc))
                            result.add(cc);
                        return result;
                    }

                    public boolean completeMatch(ONDEXRelation r) {
                        if (cc != null)
                            return r.getOfType().equals(cc);
                        return false;
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFEVIDENCE, new RelationAttributeProcessorFactory() {
            //(DEFEVIDENCE, pos);
            public RelationAttributeProcessor getAttributeProcessor(AttributePrototype ap, ONDEXGraph graph) {
                return new RelationAttributeProcessor(ap, graph) {
                    private Set<EvidenceType> ev = new HashSet<EvidenceType>(0);

                    public void setSource(ONDEXRelation r) {
                        ev = getMatches(r);
                    }

                    public Set getMatches(ONDEXRelation r) {
                        Set result = new HashSet();
                        String[] def = ap.getDef();
                        if (def[1] != null) {
                            for (EvidenceType ev : r.getEvidence()) {
                                if (ev.getId().equals(def[1]))
                                    result.add(ev);
                            }
                        } else {
                            Set<EvidenceType> ait = r.getEvidence();
                            result.addAll(ait);
                        }
                        return result;
                    }

                    public boolean completeMatch(ONDEXRelation r) {
                        for (EvidenceType et : r.getEvidence()) {
                            if (ev.contains(et)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFATTR, new RelationAttributeProcessorFactory() {
            //(DEFATTR, attributeName, TEXT, pos);
            public RelationAttributeProcessor getAttributeProcessor(AttributePrototype ap, ONDEXGraph graph) {
                return new RelationAttributeProcessor(ap, graph) {
                    private Set<Attribute> attribute;

                    public void setSource(ONDEXRelation r) throws NullValueException, AccessDeniedException, EmptyStringException {
                        attribute = getMatches(r);
                    }

                    public Set getMatches(ONDEXRelation r) throws NullValueException, AccessDeniedException, EmptyStringException {
                        Set result = new HashSet();
                        String[] def = ap.getDef();
                        AttributeName att = null;
                        if (def[1] != null && def[2] != null) {
                            att = createAttName(meta, def[1], def[2]);
                            Attribute attribute = r.getAttribute(att);
                            if (def[3] != null) {
                                if (attribute.getValue().getClass().equals(String.class)) {
                                    if (def[3].equals(attribute.getValue()))
                                        result.add(attribute);
                                }
                                if (def[3].equals(attribute.toString()))
                                    result.add(attribute);
                            }
                        } else if (def[1] != null) {
                            for (Attribute attribute : r.getAttributes()) {
                                if (attribute.getOfType().getId().equals(def[1])) {
                                    if (def[3] != null) {
                                        if (attribute.getValue().getClass().equals(String.class)) {
                                            if (def[3].equals(attribute.getValue()))
                                                result.add(attribute);
                                        }
                                        if (def[3].equals(attribute.toString()))
                                            result.add(attribute);
                                    }
                                    result.add(attribute);
                                }
                            }
                        } else if (def[2] != null) {
                            for (Attribute attribute : r.getAttributes()) {
                                if (attribute.getValue().getClass().equals(convertToClass(def[2]))) {
                                    if (def[3] != null) {
                                        if (attribute.getValue().getClass().equals(String.class)) {
                                            if (def[3].equals(attribute.getValue()))
                                                result.add(attribute);
                                        }
                                        if (def[3].equals(attribute.toString()))
                                            result.add(attribute);
                                    }
                                    result.add(attribute);
                                }
                            }
                        } else {
                            Set<Attribute> ait = r.getAttributes();
                            result.addAll(ait);
                        }
                        return result;
                    }

                    public boolean completeMatch(ONDEXRelation r) {
                        for (Attribute g : r.getAttributes()) {
                            if (attribute.contains(g)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
            }
        });
    }

    public interface RelationAttributeProcessorFactory {
        public RelationAttributeProcessor getAttributeProcessor(AttributePrototype ap, ONDEXGraph graph);
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

    public static RelationAttributeProcessor getRelationProcessor(AttributePrototype ap, ONDEXGraph graph) {
        RelationAttributeProcessorFactory tmp = typeToChecker.get(ap.getType());
        if (tmp == null) return null;
        return tmp.getAttributeProcessor(ap, graph);
    }

    protected final AttributePrototype ap;
    protected final ONDEXGraphMetaData meta;

    protected RelationAttributeProcessor(AttributePrototype ap, ONDEXGraph graph) {
        this.ap = ap;
        this.meta = graph.getMetaData();
    }

    abstract public void setSource(ONDEXRelation r) throws NullValueException, AccessDeniedException, EmptyStringException;

    abstract public Set getMatches(ONDEXRelation r) throws NullValueException, AccessDeniedException, EmptyStringException;

    abstract public boolean completeMatch(ONDEXRelation r);
}