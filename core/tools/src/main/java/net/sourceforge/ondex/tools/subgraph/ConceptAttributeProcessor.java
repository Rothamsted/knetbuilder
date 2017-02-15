package net.sourceforge.ondex.tools.subgraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;

/**
 * @author lysenkoa
 */
@SuppressWarnings("unchecked")
public abstract class ConceptAttributeProcessor {
    protected static final Map<String, ConceptAttributeProcessorFactory> typeToChecker = new HashMap<String, ConceptAttributeProcessorFactory>();

    static {
        typeToChecker.put(DefConst.DEFCC, new ConceptAttributeProcessorFactory() {
            //(DEFCC, pos)
            public ConceptAttributeProcessor getAttributeProcessor(AttributePrototype ap, ONDEXGraph graph) {
                return new ConceptAttributeProcessor(ap, graph) {
                    private ConceptClass cc;

                    public void setSource(ONDEXConcept c) {
                        cc = c.getOfType();
                    }

                    public Set getMatches(ONDEXConcept c) {
                        Set result = new HashSet();
                        if (c.getOfType().equals(cc))
                            result.add(cc);
                        return result;
                    }

                    public boolean completeMatch(ONDEXConcept c) {
                        if (cc != null)
                            return c.getOfType().equals(cc);
                        return false;
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFACC, new ConceptAttributeProcessorFactory() {

            //(DEFACC, "false", dataSource, pos)
            public ConceptAttributeProcessor getAttributeProcessor(AttributePrototype ap, ONDEXGraph graph) {
                return new ConceptAttributeProcessor(ap, graph) {
                    private Set<ConceptAccession> accs = new HashSet<ConceptAccession>(0);

                    public void setSource(ONDEXConcept c) throws NullValueException, EmptyStringException, AccessDeniedException {
                        accs = getMatches(c);
                    }

                    public Set getMatches(ONDEXConcept c) throws NullValueException, EmptyStringException, AccessDeniedException {
                        Set result = new HashSet();
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
                            dataSource = GraphEntityPrototype.createDataSource(meta, def[2]);
                        }
                        if (def[3] != null) {
                            if (dataSource != null) {
                                ConceptAccession ca = c.getConceptAccession(def[3], dataSource);
                                if (ca != null && (isAmbiguous == null || isAmbiguous.equals(ca.isAmbiguous()))) {
                                    result.add(ca);
                                }
                            }
                            for (ConceptAccession ca : c.getConceptAccessions()) {
                                if (ca.getAccession().equals(def[3])
                                        && (isAmbiguous == null || isAmbiguous.equals(ca.isAmbiguous()))) {
                                    result.add(ca);
                                }
                            }
                        } else if (dataSource != null) {
                            for (ConceptAccession ca : c.getConceptAccessions()) {
                                if (ca.getElementOf().equals(dataSource)
                                        && (isAmbiguous == null || isAmbiguous.equals(ca.isAmbiguous()))) {
                                    result.add(ca);
                                }
                            }
                        } else if (isAmbiguous != null) {
                            for (ConceptAccession ca : c.getConceptAccessions()) {
                                if (isAmbiguous.equals(ca.isAmbiguous())) {
                                    result.add(ca);
                                }
                            }
                        } else {
                            Set<ConceptAccession> ait = c.getConceptAccessions();
                            result.addAll(ait);
                        }
                        return result;
                    }

                    public boolean completeMatch(ONDEXConcept c) {
                        for (ConceptAccession ca : c.getConceptAccessions()) {
                            if (accs.contains(ca)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFNAME, new ConceptAttributeProcessorFactory() {
            public ConceptAttributeProcessor getAttributeProcessor(AttributePrototype ap, ONDEXGraph graph) {
                //(DEFNAME,"true", pos);
                return new ConceptAttributeProcessor(ap, graph) {
                    private Set<ConceptName> names = new HashSet<ConceptName>(0);

                    public void setSource(ONDEXConcept c) throws NullValueException, EmptyStringException, AccessDeniedException {
                        names = getMatches(c);
                    }

                    public Set getMatches(ONDEXConcept c) throws NullValueException, EmptyStringException, AccessDeniedException {
                        Set result = new HashSet();
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
                            if (n != null && (isPreferred == null || isPreferred.equals(n.isPreferred()))) {
                                result.add(n);
                            }
                        } else {
                            Set<ConceptName> ait = c.getConceptNames();
                            result.addAll(ait);
                        }
                        return result;
                    }

                    public boolean completeMatch(ONDEXConcept c) {
                        for (ConceptName cn : c.getConceptNames()) {
                            if (names.contains(cn)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFEVIDENCE, new ConceptAttributeProcessorFactory() {
            //(DEFEVIDENCE, pos);
            public ConceptAttributeProcessor getAttributeProcessor(AttributePrototype ap, ONDEXGraph graph) {
                return new ConceptAttributeProcessor(ap, graph) {
                    private Set<EvidenceType> ev = new HashSet<EvidenceType>(0);

                    public void setSource(ONDEXConcept c) {
                        ev = getMatches(c);
                    }

                    public Set getMatches(ONDEXConcept c) {
                        Set result = new HashSet();
                        String[] def = ap.getDef();
                        if (def[1] != null) {
                            for (EvidenceType ev : c.getEvidence()) {
                                if (ev.getId().equals(def[1]))
                                    result.add(ev);
                            }
                        } else {
                            Set<EvidenceType> ait = c.getEvidence();
                            result.addAll(ait);
                        }
                        return result;
                    }

                    public boolean completeMatch(ONDEXConcept c) {
                        for (EvidenceType et : c.getEvidence()) {
                            if (ev.contains(et)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFDATASOURCE, new ConceptAttributeProcessorFactory() {
            //(DEFDATASOURCE, pos);
            public ConceptAttributeProcessor getAttributeProcessor(AttributePrototype ap, ONDEXGraph graph) {
                return new ConceptAttributeProcessor(ap, graph) {
                    private DataSource dataSource;

                    public void setSource(ONDEXConcept c) {
                        dataSource = c.getElementOf();
                    }

                    public Set getMatches(ONDEXConcept c) {
                        Set result = new HashSet();
                        String[] def = ap.getDef();
                        if (def[1] != null) {
                            if (dataSource.equals(c.getOfType()))
                                result.add(dataSource);
                        } else {
                            result.add(dataSource);
                        }
                        return result;
                    }

                    public boolean completeMatch(ONDEXConcept c) {
                        if (dataSource != null)
                            return c.getElementOf().equals(dataSource);
                        return false;
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFPID, new ConceptAttributeProcessorFactory() {
            //(DEFPID, pos);
            public ConceptAttributeProcessor getAttributeProcessor(AttributePrototype ap, ONDEXGraph graph) {
                return new ConceptAttributeProcessor(ap, graph) {
                    private String pid;

                    public void setSource(ONDEXConcept c) {
                        pid = c.getPID();
                    }

                    public Set getMatches(ONDEXConcept c) {
                        Set result = new HashSet();
                        String[] def = ap.getDef();
                        if (def[1] != null) {
                            if (c.getPID().equals(def[1]))
                                result.add(pid);
                        } else {
                            result.add(pid);
                        }
                        return result;
                    }

                    public boolean completeMatch(ONDEXConcept c) {
                        if (this.pid != null)
                            return this.pid.equals(c.getPID());
                        return false;
                    }
                };
            }
        });
        typeToChecker.put(DefConst.DEFATTR, new ConceptAttributeProcessorFactory() {
            //(DEFATTR, attributeName, TEXT, pos);
            public ConceptAttributeProcessor getAttributeProcessor(AttributePrototype ap, ONDEXGraph graph) {
                return new ConceptAttributeProcessor(ap, graph) {
                    private Set<Attribute> gds;

                    public void setSource(ONDEXConcept c) throws NullValueException, AccessDeniedException, EmptyStringException {
                        gds = getMatches(c);
                    }

                    public Set getMatches(ONDEXConcept c) throws NullValueException, AccessDeniedException, EmptyStringException {
                        Set result = new HashSet();
                        String[] def = ap.getDef();
                        AttributeName att = null;
                        if (def[1] != null && def[2] != null) {
                            att = createAttName(meta, def[1], def[2]);
                            Attribute attribute = c.getAttribute(att);
                            if (def[3] != null) {
                                if (attribute.getValue().getClass().equals(String.class)) {
                                    if (def[3].equals(attribute.getValue()))
                                        result.add(attribute);
                                }
                                if (def[3].equals(attribute.toString()))
                                    result.add(attribute);
                            }
                        } else if (def[1] != null) {
                            for (Attribute attribute : c.getAttributes()) {
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
                            for (Attribute attribute : c.getAttributes()) {
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
                            Set<Attribute> ait = c.getAttributes();
                            result.addAll(ait);
                        }
                        return result;
                    }

                    public boolean completeMatch(ONDEXConcept c) {
                        for (Attribute g : c.getAttributes()) {
                            if (gds.contains(g)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
            }
        });
    }

    public interface ConceptAttributeProcessorFactory {
        public ConceptAttributeProcessor getAttributeProcessor(AttributePrototype ap, ONDEXGraph graph);
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

    public static ConceptAttributeProcessor getConceptProcessor(AttributePrototype ap, ONDEXGraph graph) {
        ConceptAttributeProcessorFactory tmp = typeToChecker.get(ap.getType());
        if (tmp == null) return null;
        return tmp.getAttributeProcessor(ap, graph);
    }

    protected final AttributePrototype ap;
    protected final ONDEXGraphMetaData meta;

    protected ConceptAttributeProcessor(AttributePrototype ap, ONDEXGraph graph) {
        this.ap = ap;
        this.meta = graph.getMetaData();
    }

    abstract public void setSource(ONDEXConcept c) throws NullValueException, EmptyStringException, AccessDeniedException;

    abstract public Set getMatches(ONDEXConcept c) throws NullValueException, EmptyStringException, AccessDeniedException;

    abstract public boolean completeMatch(ONDEXConcept c);
}