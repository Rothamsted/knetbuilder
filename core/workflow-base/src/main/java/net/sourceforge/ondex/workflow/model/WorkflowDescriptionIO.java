package net.sourceforge.ondex.workflow.model;

import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.init.PluginDescription;
import net.sourceforge.ondex.init.PluginRegistry;
import net.sourceforge.ondex.init.PluginType;
import net.sourceforge.ondex.workflow.engine.Engine;
import org.apache.log4j.Logger;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.Format.TextMode;
import org.jdom.output.XMLOutputter;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for saving and reading Ondex workflow files in a variety of formats
 *
 * @author lysenkoa
 */
public class WorkflowDescriptionIO {
    private static final Logger LOG = Logger.getLogger(WorkflowDescriptionIO.class);

    private WorkflowDescriptionIO() {

    }

    public static WorkflowDescription readFile(File file, PluginRegistry pr) throws IOException, JDOMException, XMLStreamException, PluginType.UnknownPluginTypeException {
        LOG.info("Reading task description file: " + file);
        String version = Engine.getVersion(file.getCanonicalPath());
        LOG.debug("Version: " + version);
        WorkflowDescription result;
        if (version.startsWith("2")) {
            result = parseVersion_2(file, pr);
        } else if (version.startsWith("3")) {
            result = parseVersion_3(file, pr);
        } else {
            result = parseVersion_1(file, pr);
        }
        return result;
    }

    private static WorkflowDescription parseVersion_1(File file, PluginRegistry pr) throws IOException, JDOMException, XMLStreamException, PluginType.UnknownPluginTypeException {
        WorkflowDescription result = new WorkflowDescription();
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(file);
        Element root = doc.getRootElement();
        List<Element> es = root.getChildren();
        String graph = "default";
        for (Element e : es) {
            LOG.debug("processing " + e.getName());
            if (e.getName().equals("DefaultGraph")) {
                PluginDescription pb = pr.getPluginDescription(PluginType.PRODUCER, "memorygraph");
                if(pb == null) throw new RuntimeException("Could not find a PRODUCER plugin of type 'memorygraph'");
                List<BoundArgumentValue> args = new ArrayList<BoundArgumentValue>();
                ArgumentDescription gab = getArgumentDescriptionByName(pb, "GraphId");
                args.add(new BoundArgumentValue(gab, e.getAttributeValue("name")));
                graph = e.getAttributeValue("name");
                result.addPlugin(new WorkflowTask(pb, args));
            } else {
                PluginDescription pb = pr.getPluginDescription(PluginType.getType(e.getName()), e.getAttributeValue("name"));
                if (pb == null) {
                    throw new RuntimeException(e.getAttributeValue("name") + " of type " + PluginType.getType(e.getName()) + " is not registered in plugin registry");
                }
                List<BoundArgumentValue> args = new ArrayList<BoundArgumentValue>();
                List<Element> atts = e.getChildren();
                ArgumentDescription graphId = getArgumentDescriptionByName(pb, "graphId");
                args.add(new BoundArgumentValue(graphId, graph));
                LOG.debug("adding graph id argument: " + graphId + " -> " + graph);
                for (Element att : atts) {
                    String paramName = att.getAttributeValue("name");
                    if (paramName == null) {
                        throw new RuntimeException("name attribute required but not present in element " + att.getName() + " for plugin " + pb.getName());
                    }
                    ArgumentDescription ab = getArgumentDescriptionByName(pb, paramName);
                    if (ab == null) {
                        throw new RuntimeException(paramName + " is not a valid argument for plugin " + pb.getName());
                    }
                    args.add(new BoundArgumentValue(ab, att.getText()));
                }
                result.addPlugin(new WorkflowTask(pb, args));
            }
        }
        return result;
    }

    private static WorkflowDescription parseVersion_2(File file, PluginRegistry pr) throws IOException, JDOMException, XMLStreamException, PluginType.UnknownPluginTypeException {
        final Map<String, PluginDescription> clsToPluginDescription = new HashMap<String, PluginDescription>();
        for (PluginDescription pb : pr.getAllPlugins()) {
            clsToPluginDescription.put(pb.getCls(), pb);
        }
        WorkflowDescription result = new WorkflowDescription();
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(file);
        Element root = doc.getRootElement();
        Map<String, Element> resources = new HashMap<String, Element>();
        List<Element> res = root.getChild("Resources").getChildren();
        for (Element r : res) {
            resources.put(r.getChildText("GlobalId"), r.getChild("Value"));
        }
        List<Element> components = root.getChild("Components").getChildren();
        Map<String, String> map = new HashMap<String, String>();
        for (Element c : components) {
            if (c.getAttributeValue("type").equals("function") && c.getAttributeValue("method").equals("getNewGraph")) {
                PluginDescription pb = pr.getPluginDescription(PluginType.PRODUCER, "newgraph");
                List<BoundArgumentValue> args = new ArrayList<BoundArgumentValue>();
                List<Element> inputs = c.getChild("InputList").getChildren();
                String graphID = "default";
                for (Element e : inputs) {
                    String id = e.getChildText("LocalId").trim();
                    if (id.equals("0")) {
                        ArgumentDescription ab = getArgumentDescriptionByName(pb, "Type");
                        String value = resources.get(e.getChildText("GlobalId")).getText();
                        args.add(new BoundArgumentValue(ab, value));
                    } else if (id.equals("1")) {
                        ArgumentDescription ab = getArgumentDescriptionByName(pb, "GraphId");
                        String value = resources.get(e.getChildText("GlobalId")).getText();
                        graphID = value;
                        args.add(new BoundArgumentValue(ab, value));
                    } else if (id.equals("2")) {
                        ArgumentDescription ab = getArgumentDescriptionByName(pb, "storageFolder");
                        String value = resources.get(e.getChildText("GlobalId")).getText();
                        args.add(new BoundArgumentValue(ab, value));
                    }
                }
                List<Element> outputs = c.getChild("OutputList").getChildren();
                map.put(String.valueOf(outputs.get(0).getChildText("GlobalId")), graphID);
                result.addPlugin(new WorkflowTask(pb, args));
            } else if (c.getAttributeValue("type").equals("plugin")) {
                PluginDescription pb = clsToPluginDescription.get(c.getAttributeValue("class"));
                List<BoundArgumentValue> args = new ArrayList<BoundArgumentValue>();
                String graphName = "default";
                if (pb == null) {
                    String[] ids = c.getChildText("UniqueID").split("\\.");
                    pb = pr.getPluginDescription(PluginType.getType(firstUpper(ids[1])), ids[0]);
                    if (pb == null) {
                        pb = pr.getPluginDescription(PluginType.getType(ids[1]), ids[0]);
                    }
                    if (pb == null) {
                        System.err.println("Missing: " + ids[0] + " - " + c.getAttributeValue("class"));
                        continue;
                    }
                }
                List<Element> inputs = c.getChild("InputList").getChildren();
                for (Element e : inputs) {
                    String id = e.getChildText("LocalId").trim();
                    if (id.equals("0")) {
                        Element value = resources.get(e.getChildText("GlobalId"));
                        List<Element> internal = value.getChildren();
                        for (Element v : internal) {
                            ArgumentDescription ab = getArgumentDescriptionByName(pb, v.getName());
                            if (ab != null) {
                                String val = v.getTextTrim();
                                if (val != null && val.length() > 0)
                                    args.add(new BoundArgumentValue(ab, val));
                            }
                        }
                    } else if (id.equals("1")) {
                        graphName = map.get(String.valueOf(e.getChildText("GlobalId").trim()));
                        ArgumentDescription ab = getArgumentDescriptionByName(pb, "graphId");
                        args.add(new BoundArgumentValue(ab, graphName));
                    }
                }
                List<Element> outputs = c.getChild("OutputList").getChildren();
                for (Element e : outputs) {
                    String id = e.getChildText("LocalId").trim();
                    if (id.equals("0")) {
                        map.put(String.valueOf(e.getChildText("GlobalId").trim()), graphName);
                    } else if (id.equals("1") && pb.getOndexType().getName().equals("Filter")) {
                        map.put(String.valueOf(e.getChildText("GlobalId").trim()), e.getChildText("GlobalId").trim());
                        ArgumentDescription ab = getArgumentDescriptionByName(pb, "secondaryGraphId");
                        args.add(new BoundArgumentValue(ab, graphName));
                    }
                }
                result.addPlugin(new WorkflowTask(pb, args));
            }
        }
        return result;
    }

    private static WorkflowDescription parseVersion_3(File file, PluginRegistry pr) throws IOException, JDOMException, XMLStreamException, PluginType.UnknownPluginTypeException {
        WorkflowDescription result = new WorkflowDescription();
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(file);
        Element root = doc.getRootElement();
        List<Element> components = root.getChild("Workflow").getChildren();
        String id = "";
        for (Element c : components) {
            List<BoundArgumentValue> ads = new ArrayList<BoundArgumentValue>();
            PluginDescription pb = pr.getPluginDescription(PluginType.getType(c.getName()), c.getAttributeValue("name"));
            if (pb == null) {
                System.err.println("Missing plugin " + c.getName());
                continue;
            }

            List<Element> args = c.getChildren();
            for (Element arg : args) {
                String argumentName = arg.getAttributeValue("name");
                ArgumentDescription argBean = getArgumentDescriptionByName(pb, argumentName);
                if (argBean == null) {
                    System.err.println("Missing plugin argument" + argumentName);
                    continue;
                }
                ads.add(new BoundArgumentValue(argBean, arg.getText()));
            }
            WorkflowTask p = new WorkflowTask(pb, ads);
            Element commentElement = c.getChild("Comment");
            if (commentElement != null) {
                String comment = commentElement.getText();
                if (comment != null && comment.length() > 0)
                    p.setComment(comment);
            }

            result.addPlugin(p);
        }
        return result;
    }

    public static void saveToFile(File file, WorkflowDescription td) throws IOException {
        Format format = Format.getPrettyFormat();
        format.setTextMode(TextMode.PRESERVE);
        XMLOutputter outputter = new XMLOutputter(format);
        Element rootElement = new Element("Ondex");
        rootElement.setAttribute("version", "3.0");
        Element workflow = new Element("Workflow");
        for (WorkflowTask pc : td.getComponents()) {
            Element comp = new Element(pc.getPluginDescription().getOndexType().getName());
            comp.setAttribute("name", pc.getPluginDescription().getOndexId());
            if (pc.getComment() != null) {
                String comment = pc.getComment().trim();
                if (comment != null && comment.length() > 0) {
                    Element commentElement = new Element("Comment");
                    commentElement.setContent(new CDATA(comment));
                    comp.addContent(commentElement);
                }
            }
            for (BoundArgumentValue a : pc.getArgs()) {
                Element arg = new Element("Arg");
                if (a.getArg().getInteranlName() == null) {
                    System.err.println(pc.getPluginDescription().getOndexId() + " - " + a.getArg().getName());
                    continue;
                }
                arg.setAttribute("name", a.getArg().getInteranlName());
                if (a.getValue() == null) {
                    if (!a.getArg().getIsRequired())
                        continue;
                    arg.addContent("");
                } else {
                    arg.addContent(a.getValue());
                }
                comp.addContent(arg);
            }
            workflow.addContent(comp);
        }
        rootElement.addContent(workflow);
        Document doc = new Document(rootElement);
        FileOutputStream fos = new FileOutputStream(file);
        outputter.output(doc, fos);
        fos.close();
    }

    private static String firstUpper(String in) {
        return in.substring(0, 1).toUpperCase() + in.substring(1);
    }

    public static ArgumentDescription getArgumentDescriptionByName(PluginDescription pb, String name) {
        if(pb == null) throw new NullPointerException("Attempted to search null PluginDescription for argument name '" + name + "'");

        for (ArgumentDescription ab : pb.getArgDef()) {
            if (ab.getInteranlName().equalsIgnoreCase(name)) {
                return ab;
            }
        }
        return null;
    }

    private static class ComponentDelegate {
        private final List<BoundArgumentValue> args = new ArrayList<BoundArgumentValue>();
        private final PluginDescription pb;

        public ComponentDelegate(PluginDescription pb) {
            this.pb = pb;
        }

        public void addArgument(String internalId, String value) {
            ArgumentDescription ab = getArgumentDescriptionByName(pb, internalId);
            if (ab != null && (value != null && value.length() > 0))
                args.add(new BoundArgumentValue(ab, value));
        }

        public WorkflowTask getResult() {
            return new WorkflowTask(pb, args);
        }
    }
}
