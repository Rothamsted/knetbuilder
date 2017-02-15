package net.sourceforge.ondex.export.delimitedfile;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.InvalidRouteException;
import net.sourceforge.ondex.algorithm.pathmodel.ONDEXEntityPath;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.FileArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.export.ONDEXExport;
import net.sourceforge.ondex.tools.tab.exporter.OndexPathPrinter;
import net.sourceforge.ondex.tools.tab.importer.params.FieldArgumentParser;

import java.io.File;
import java.util.*;
import net.sourceforge.ondex.args.CompressResultsArguementDefinition;

/**
 * @author lysenkoa, hindlem
 */
@Status(description = "Tested December 2013 (Artem Lysenko)", status = StatusType.STABLE)
public class Export extends ONDEXExport implements ArgumentNames, net.sourceforge.ondex.export.delimitedfile.MetaData {

    private ONDEXGraphMetaData om;
    private List<List<MetaData>> metaDataPath;
    private int cutoff = Integer.MAX_VALUE;
    private boolean noDuplicates = false;

    private enum ProcessingType {
        CONCEPT, OUTGOING_RELATION, INCOMING_RELATION
    }

    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(ORDER_ARG, ORDER_ARG_DESC, true, null, false),
                new StringArgumentDefinition(ATTRIB_ARG, ATTRIB_ARG_DESC, false, null, true),
                new StringArgumentDefinition(MIN_REPORT_DEPTH_ARG, MIN_REPORT_DEPTH_ARG_DESC, false, null, false),
                new BooleanArgumentDefinition(REMOVE_DUPLICATES_ARG, REMOVE_DUPLICATES_ARG_DESC, false, false),
                new BooleanArgumentDefinition(LINKS_ARG, LINKS_ARG_DESC, false, false),
                new BooleanArgumentDefinition(TRANSLATE_TAXID_ARG, TRANSLATE_TAXID_ARG_DESC, false, false),
                new CompressResultsArguementDefinition(ZIP_FILE_ARG, ZIP_FILE_ARG_DESC, false, false),
                new FileArgumentDefinition(FileArgumentDefinition.EXPORT_FILE, "delimited file to export", true, false, false, false)
        };
    }

    public String getName() {
        return "Report writer";
    }

    public String getVersion() {
        return "07-Feb-08";
    }

    @Override
    public String getId() {
        return "delimitedfile";
    }

    public boolean requiresIndexedGraph() {
        return false;
    }

    public void start() throws InvalidPluginArgumentException {
        noDuplicates = (Boolean) args.getUniqueValue(REMOVE_DUPLICATES_ARG);
        boolean useLinks = (Boolean) args.getUniqueValue(LINKS_ARG);
        boolean translateTaxID = (Boolean) args.getUniqueValue(TRANSLATE_TAXID_ARG);
        boolean zipFile = (Boolean) args.getUniqueValue(ZIP_FILE_ARG);

        Object min = args.getUniqueValue(MIN_REPORT_DEPTH_ARG);
        if (min != null) {
            cutoff = Integer.valueOf(min.toString());
        }
        try {
            om = graph.getMetaData();

            String pathDefinition = (String) args.getUniqueValue(ORDER_ARG);
            metaDataPath = parsePathDefinition(pathDefinition);

            //parse attributes
            Object[] attArgs = args.getObjectValueArray(ATTRIB_ARG);
            FieldArgumentParser parser = new FieldArgumentParser(useLinks, translateTaxID);

            for (Object arg : attArgs) {
                parser.parseArguments(arg.toString(), graph);
            }

            List<ONDEXEntityPath> paths = new ArrayList<ONDEXEntityPath>();

            if (metaDataPath.get(0).get(0) instanceof ConceptClass) { //indicates starts with concept
                //Set<ONDEXConcept> ov = SetImpl.create(graph, s, ONDEXConcept.class, new SparseDefaultBitSet());
                Set<ONDEXConcept> tmp = new HashSet<ONDEXConcept>();
                for (MetaData md : metaDataPath.get(0)) {
                    tmp.addAll(graph.getConceptsOfConceptClass((ConceptClass) md));
                    /*Set<ONDEXConcept> temp = graph.getConceptsOfConceptClass((ConceptClass)md);
                         if(temp!=null){
                             ov = Set.or(ov, temp);
                         }*/

                }
                for (ONDEXConcept concept: tmp) {
                    ONDEXEntityPath route = new ONDEXEntityPath(concept);
                    processEntity(concept, route, paths, 0, ProcessingType.CONCEPT, false);
                }
            } else { //therefore path.get(0) must be a RelationType
                Set<ONDEXRelation> tmp = new HashSet<ONDEXRelation>();
                //Set<ONDEXRelation> ov = SetImpl.create(graph, s, ONDEXRelation.class, new SparseDefaultBitSet());
                for (MetaData md : metaDataPath.get(0)) {
                    tmp.addAll(graph.getRelationsOfRelationType((RelationType) md));
                    /*Set<ONDEXRelation> temp = graph.getRelationsOfRelationType((RelationType)md);
                         if(temp != null){
                             ov = Set.or(ov, temp);
                         }*/
                }
                Iterator<ONDEXRelation> ov = tmp.iterator();
                while (ov.hasNext()) {
                    ONDEXRelation r = ov.next();
                    ONDEXEntityPath path = new ONDEXEntityPath(r);

                    processEntity(r, path, paths, 0, ProcessingType.INCOMING_RELATION, false);
                    processEntity(r, (ONDEXEntityPath) path.clone(), paths, 0, ProcessingType.OUTGOING_RELATION, true);
                }
            }
            int maxLength = 0;
            for (ONDEXEntityPath path : paths) {
                if (path.getLength() > maxLength) {
                    maxLength = path.getLength();
                }
            }

            if (noDuplicates) {
                Set<ONDEXEntityPath> nonredundantRoutes = new HashSet<ONDEXEntityPath>(paths);
                paths = new ArrayList<ONDEXEntityPath>(nonredundantRoutes);
            }

            File file = new File((String) args.getUniqueValue(FileArgumentDefinition.EXPORT_FILE));
            OndexPathPrinter printer = new OndexPathPrinter(parser.getAttributeModel(),file, maxLength, graph, zipFile);
            for (ONDEXEntityPath path : paths) {
                printer.printPath(path);
            }
            printer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<List<MetaData>> parsePathDefinition(String pathDefinition) {
        //parse path
        boolean switchrc = true; //relation = false; concept = true
        String[] divided = pathDefinition.split("#");
        ArrayList<List<MetaData>> path = new ArrayList<List<MetaData>>();
        if (om.getConceptClass(divided[0].split(",")[0]) == null) {
            switchrc = false;
        }
        for (String tuple : divided) {
            List<MetaData> meta = new ArrayList<MetaData>();
            for (String id : tuple.split(",")) {
                if (id.trim().length() > 0) {
                    if (switchrc) {
                        ConceptClass cc = om.getConceptClass(id.trim());
                        if (cc != null) meta.add(cc);
                        else System.err.println(id.trim() + " not found as a ConceptClass");
                    } else {
                        RelationType rt = om.getRelationType(id.trim());
                        if (rt != null) meta.add(rt);
                        else System.err.println(id.trim() + " not found as a RelationType");
                    }
                }

            }
            switchrc = !switchrc;
            path.add(meta);
        }
        return path;
    }

    /**
     * @param cr
     * @param path
     * @param completedRoutes
     * @param level
     * @param pt
     * @param silentPass
     * @throws NullValueException
     * @throws AccessDeniedException
     * @throws InvalidRouteException
     */
    private void processEntity(ONDEXEntity cr,
                               ONDEXEntityPath path,
                               List<ONDEXEntityPath> completedRoutes,
                               int level,
                               ProcessingType pt,
                               boolean silentPass) throws NullValueException, AccessDeniedException, InvalidRouteException, CloneNotSupportedException {

        MetaData md;
        if (pt == ProcessingType.CONCEPT)
            md = ((ONDEXConcept) cr).getOfType();
        else
            md = ((ONDEXRelation) cr).getOfType();

        if (!metaDataPath.get(level).contains(md)) {
            return;
        } else if (path.getLength() >= metaDataPath.size()) {
//        	System.out.println(level+" "+path.getLength());
            completedRoutes.add(path);
            return;
        }

        boolean continued = false;
        level++;
        switch (pt) {
            case CONCEPT:
                ONDEXConcept c = (ONDEXConcept) cr;
                for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
                    if (!path.containsEntity(r)) {
                        ONDEXEntityPath newPath = (ONDEXEntityPath) path.clone();

                        newPath.addPathStep(r);
                        continued = true;
                        if (r.getToConcept().equals(c)) {
                            processEntity(r, newPath, completedRoutes, level, ProcessingType.INCOMING_RELATION, false);
                        } else {
                            processEntity(r, newPath, completedRoutes, level, ProcessingType.OUTGOING_RELATION, false);
                        }
                    }
                }
                break;
            case OUTGOING_RELATION:
                ONDEXConcept concept = ((ONDEXRelation) cr).getToConcept();
                if (!path.containsEntity(concept)) {
                    path.addPathStep(concept);
                    processEntity(concept, path, completedRoutes, level, ProcessingType.CONCEPT, false);
                    continued = true;
                }
                break;
            case INCOMING_RELATION:
                concept = ((ONDEXRelation) cr).getFromConcept();
                if (!path.containsEntity(concept)) {
                    path.addPathStep(concept);
                    processEntity(concept, path, completedRoutes, level, ProcessingType.CONCEPT, false);
                    continued = true;
                }
                break;
            default:
                throw new RuntimeException("Unknown ProcessingType " + pt);
        }

        if (!continued && level >= cutoff) {
            completedRoutes.add(path); //add the incomplete route
        }
    }

    /**
     * Convenience method for outputting the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        Exception e = new Exception();
        StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line + "]";
    }

    @Override
    public String[] requiresValidators() {
        return new String[]{"htmlaccessionlink", "scientificspeciesname"};
    }

}
