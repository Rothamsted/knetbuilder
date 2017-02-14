/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.ondex.merger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.exception.type.PluginConfigurationException;
import org.apache.log4j.Logger;

/**
 *
 * @author jweile
 */
public class ConfigReader {

    private ONDEXGraph graph;

    public ConfigReader(ONDEXGraph graph) {
        this.graph = graph;
    }

    

    public Configuration readConfiguration(String cfgString) throws PluginConfigurationException {

        Pattern global = Pattern.compile("(.+)\\((.+)\\)");
        Matcher globalMatcher = global.matcher(cfgString);
        if (globalMatcher.matches() && globalMatcher.groupCount() == 2) {
            String type = globalMatcher.group(1).trim();
            if (type.equals("linkByAccession")) {
                LinkByAccession cfg = new LinkByAccession();
                Map<String,String> args = parseArgs(globalMatcher.group(2));
                if (args.size() == 4) {
                    for (String key :args.keySet()) {
                        if (key.equals("fromCC")) {
                            ConceptClass fromCC = graph.getMetaData().getConceptClass(args.get(key));
                            if (fromCC != null) {
                                cfg.setFromCC(fromCC);
                            } else {
                                throw new PluginConfigurationException("Unknown \"fromCC\" ID: "+args.get(key));
                            }
                        } else if (key.equals("toCC")) {
                            ConceptClass toCC = graph.getMetaData().getConceptClass(args.get(key));
                            if (toCC != null) {
                                cfg.setToCC(toCC);
                            } else {
                                throw new PluginConfigurationException("Unknown \"toCC\" ID: "+args.get(key));
                            }
                        } else if (key.equals("rt")) {
                            RelationType rt = graph.getMetaData().getRelationType(args.get(key));
                            if (rt != null) {
                                cfg.setRt(rt);
                            } else {
                                throw new PluginConfigurationException("Unknown \"rt\" ID: "+args.get(key));
                            }
                        } else if (key.equals("ns")) {
                            DataSource ns = graph.getMetaData().getDataSource(args.get(key));
                            if (ns != null) {
                                cfg.setNs(ns);
                            } else {
                                throw new PluginConfigurationException("Unknown \"ns\" ID; "+args.get(key));
                            }
                        } else {
                            throw new PluginConfigurationException("Invalid argument "+key+" in mode "+type);
                        }
                    }
                } else {
                    throw new PluginConfigurationException("Invalid number of arguments for mode "+type);
                }
                return cfg;
            } else if (type.equals("mergeByAccession")) {
                MergeByAccession cfg = new MergeByAccession();
                Map<String,String> args = parseArgs(globalMatcher.group(2));
                 if (args.size() >= 2) {
                    for (String key :args.keySet()) {
                        if (key.equals("targetCC")) {
                            ConceptClass targetCC = graph.getMetaData().getConceptClass(args.get(key));
                            if (targetCC != null) {
                                cfg.setTargetCC(targetCC);
                            } else {
                                throw new PluginConfigurationException("Unknown \"targetCC\" ID: "+args.get(key));
                            }
                        } else if (key.equals("ns")) {
                            DataSource ns = graph.getMetaData().getDataSource(args.get(key));
                            if (ns != null) {
                                cfg.setNs(ns);
                            } else {
                                throw new PluginConfigurationException("Unknown \"ns\" ID; "+args.get(key));
                            }
                        } else if (key.equals("setBasedMatching")) {
                            boolean setBasedMatching = parseBoolean(args.get(key));
                            cfg.setSetBasedMatching(setBasedMatching);
                        } else if (key.equals("allowMultiples")) {
                            boolean allowMultiples = parseBoolean(args.get(key));
                            cfg.setAllowMultiples(allowMultiples);
                        } else if (key.equals("allowMissing")) {
                            boolean allowMissing = parseBoolean(args.get(key));
                            cfg.setAllowMissing(allowMissing);
                        } else {
                            throw new PluginConfigurationException("Invalid argument "+key+" in mode "+type);
                        }
                    }
                } else {
                    throw new PluginConfigurationException("Invalid number of arguments for mode "+type);
                }
                return cfg;
            } else if (type.equals("mergeByNeighbourhood")) {
                MergeByNeighbourhood cfg = new MergeByNeighbourhood();
                Map<String,String> args = parseArgs(globalMatcher.group(2));
                if (args.size() == 2) {
                    for (String key :args.keySet()) {
                        if (key.equals("targetCC")) {
                            ConceptClass targetCC = graph.getMetaData().getConceptClass(args.get(key));
                            if (targetCC != null) {
                                cfg.setTargetCC(targetCC);
                            } else {
                                throw new PluginConfigurationException("Unknown \"targetCC\" ID: "+args.get(key));
                            }
                        } else if (key.equals("neighbourCC")) {
                            ConceptClass neighbourCC = graph.getMetaData().getConceptClass(args.get(key));
                            if (neighbourCC != null) {
                                cfg.setNeighbourCC(neighbourCC);
                            } else {
                                throw new PluginConfigurationException("Unknown \"neighbourCC\" ID: "+args.get(key));
                            }
                        } else {
                            throw new PluginConfigurationException("Invalid argument "+key+" in mode "+type);
                        }
                    }
                } else {
                    throw new PluginConfigurationException("Invalid number of arguments for mode "+type);
                }
                return cfg;
            } else {
                throw new PluginConfigurationException("Unknown mode: "+type);
            }
        } else {
            throw new PluginConfigurationException("Invalid configuration: "+cfgString);
        }

    }

    /**
     * Parses an argument string and in the form
     *
     * foo="foo bar",bar=baz
     *
     * resulting in the hashmap with the values
     * "foo" -&gt; "foo bar"
     * "bar" -&gt; "baz"
     *
     * @param argString
     * @return
     */
    private Map<String, String> parseArgs(String argString) {
        Pattern quotes = Pattern.compile("\"(.+)\"");
        Map<String,String> args = new HashMap<String, String>();
        for (String arg : argString.split(",")) {
            String[] keyVal = arg.split("=");
            if (keyVal.length == 2) {
                String key = keyVal[0].trim();
                Matcher quotesMatcher = quotes.matcher(keyVal[1]);
                String value = quotesMatcher.matches() ? quotesMatcher.group(1) : keyVal[1];
                args.put(key,value);
            }
        }
        return args;
    }

    private boolean parseBoolean(String s) {
        if (s.equalsIgnoreCase("true")) {
            return true;
        } else if (s.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new NumberFormatException("String \""+s+"\" is not a boolean.");
        }
    }

    public interface Configuration {
        void print(Logger logger);
    }

    public class LinkByAccession implements Configuration {
        public static final String HELP = "LinkbyAccession(ConceptClass fromCC, ConceptClass toCC, RelationType rt, DataSource ns)";

        private ConceptClass fromCC, toCC;
        private RelationType rt;
        private DataSource ns;

        public ConceptClass getFromCC() {
            return fromCC;
        }

        public void setFromCC(ConceptClass fromCC) {
            this.fromCC = fromCC;
        }

        public DataSource getNs() {
            return ns;
        }

        public void setNs(DataSource ns) {
            this.ns = ns;
        }

        public RelationType getRt() {
            return rt;
        }

        public void setRt(RelationType rt) {
            this.rt = rt;
        }

        public ConceptClass getToCC() {
            return toCC;
        }

        public void setToCC(ConceptClass toCC) {
            this.toCC = toCC;
        }

        @Override
        public void print(Logger logger) {
            logger.info("Configuration: Link by accession\n----------------" +
                    "\nfrom concept class: "+fromCC.getFullname() +
                    "\nto concept clas: "+toCC.getFullname() +
                    "\naccession namespace: "+ns.getFullname() +
                    "\nconnecting relation type: "+rt.getFullname());
        }
    }

    public class MergeByAccession implements Configuration {
        public static final String HELP = "MergeByAccession(ConceptClass targetCC, DataSource ns, boolean setBasedMatching, boolean allowMultiples, boolean allowMissing)";

        private ConceptClass targetCC;
        private DataSource ns;
        private boolean setBasedMatching, allowMultiples, allowMissing;

        public boolean isAllowMissing() {
            return allowMissing;
        }

        public void setAllowMissing(boolean allowMissing) {
            this.allowMissing = allowMissing;
        }

        public boolean isAllowMultiples() {
            return allowMultiples;
        }

        public void setAllowMultiples(boolean allowMultiples) {
            this.allowMultiples = allowMultiples;
        }

        public DataSource getNs() {
            return ns;
        }

        public void setNs(DataSource ns) {
            this.ns = ns;
        }

        public boolean isSetBasedMatching() {
            return setBasedMatching;
        }

        public void setSetBasedMatching(boolean setBasedMatching) {
            this.setBasedMatching = setBasedMatching;
        }

        public ConceptClass getTargetCC() {
            return targetCC;
        }

        public void setTargetCC(ConceptClass targetCC) {
            this.targetCC = targetCC;
        }

        @Override
        public void print(Logger logger) {
            logger.info("Configuration: Merge by accession\n----------------" +
                    "\ntarget concept class: "+targetCC.getFullname() +
                    "\naccession namespace: "+ns.getFullname() +
                    "\nset-based matching: "+setBasedMatching +
                    "\nallow missing accessions: "+allowMissing+
                    "\nallow multiple accessions: "+allowMultiples);
        }
    }

    public class MergeByNeighbourhood implements Configuration {
        public static final String HELP = "MergeByNeighbourhood(ConceptClass targetCC, ConceptClass neighbourCC)";

        private ConceptClass targetCC, neighbourCC;

        public ConceptClass getNeighbourCC() {
            return neighbourCC;
        }

        public void setNeighbourCC(ConceptClass neighbourCC) {
            this.neighbourCC = neighbourCC;
        }

        public ConceptClass getTargetCC() {
            return targetCC;
        }

        public void setTargetCC(ConceptClass targetCC) {
            this.targetCC = targetCC;
        }
        
        @Override
        public void print(Logger logger) {
            logger.info("Configuration: Merge by neighbourhood" +
                    "\n----------------" +
                    "\ntarget concept class: "+targetCC.getFullname() +
                    "\nneighbouring concept class: "+neighbourCC.getFullname());
        }
    }
}
