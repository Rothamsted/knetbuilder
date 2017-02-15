package net.sourceforge.ondex.tools.tab.importer.params;

import net.sourceforge.ondex.algorithm.pathmodel.Path;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.tools.tab.exporter.AttributeExtractorModel;
import net.sourceforge.ondex.tools.tab.exporter.Label;
import net.sourceforge.ondex.tools.tab.exporter.extractors.*;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.apache.commons.collections15.functors.MapTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses user arguments and builds the relevent transformer to translate between depth and the user defined AttributeExtractor definitions
 *
 * @author lysenkoa, hindlem
 */
public class FieldArgumentParser {

    private boolean useLinks = true;
    private boolean translateTaxID = true;

    private MapTransformer<Integer, List<AttributeExtractor>> definedFields =
            (MapTransformer<Integer, List<AttributeExtractor>>)
                    MapTransformer.getInstance(new HashMap<Integer, List<AttributeExtractor>>());

    private List<AttributeExtractor> generalFields = new ArrayList<AttributeExtractor>();

    private Map<Integer, List<AttributeExtractor>> greaterThanFields = new HashMap<Integer, List<AttributeExtractor>>();
    private Map<Integer, List<AttributeExtractor>> lessThanFields = new HashMap<Integer, List<AttributeExtractor>>();
    private Map<Integer, List<AttributeExtractor>> notEqualFields = new HashMap<Integer, List<AttributeExtractor>>();

    private Map<Integer, List<AttributeExtractor>> relativeToEndFields = new HashMap<Integer, List<AttributeExtractor>>();

    private List<AttributeExtractor> startPointAttributes = new ArrayList<AttributeExtractor>();
    private List<AttributeExtractor> endPointAttributes = new ArrayList<AttributeExtractor>();

    /**
     * @param useLinks       create links out of accessions for use in excel
     * @param translateTaxID translate taxids to species name
     */
    public FieldArgumentParser(boolean useLinks, boolean translateTaxID) {
        this.useLinks = useLinks;
        this.translateTaxID = translateTaxID;
    }

    /**
     * Call this method for as many AttributeExtractor definition as provided by the user
     *
     * @param args  arguments provided by user for one AttributeExtractor definition
     * @param graph
     */
    public void parseArguments(String args, ONDEXGraph graph) {
        String[] processed = args.split(":");
        String position = processed[0].trim();
        String value = processed[1].trim().toLowerCase();

        List<AttributeExtractor> list = null;
        AttributeExtractor extractor = null;

        if (value.equalsIgnoreCase("id")) {
            extractor = new IdExtractor();
        } else if (value.equalsIgnoreCase("name")) {
            extractor = new NameAttributeExtractor();
        } else if (value.equalsIgnoreCase("accession")) {
            extractor = new AccessionAttributeExtractor(
                    processed[2], useLinks);
        } else if (value.equalsIgnoreCase("gds")) {
            AttributeName attribute = graph.getMetaData().getAttributeName(processed[2]);
            if (attribute == null) {
                System.err.println(processed[2] + " is an unknown AttributeName (ignored)");
                return;
            }
            extractor = new GDSAttributeExtractor(attribute, translateTaxID);
        } else if (value.equalsIgnoreCase("context")) {
            Label userDefined = Label.NAME;
            if (processed.length == 4) {
                userDefined = Label.translate(processed[4]);
            }

            extractor = new ContextExtractor(
                    processed[2], userDefined);
        } else if (value.equalsIgnoreCase("evidence")) {
            if (processed.length == 3) {
                extractor = new DefinedEvidenceAttributeExtractor(
                        graph.getMetaData().getEvidenceType(processed[2]));
            } else {
                extractor = new EvidenceAttributeExtractor();
            }
        } else if (value.equalsIgnoreCase("class")) {
            extractor = new TypeAttributeExtractor();
        } else if (value.equalsIgnoreCase("pid")) {
            extractor = new PidAttributeExtractor();
        } else if (value.equalsIgnoreCase("description")) {
            extractor = new DescriptionAttributeExtractor();
        } else if (value.equalsIgnoreCase("annotation")) {
            extractor = new AnnotationAttributeExtractor();
        } else if (value.equalsIgnoreCase("cv")) {
            extractor = new CVAttributeExtractor();
        } else if (value.equalsIgnoreCase("synonyms")) {
        	extractor = new SynonymsExtractor();
        } else {
            System.err.println(value + " is an unknown field (ignored)");
            return;
        }

        if (position.equalsIgnoreCase("*")) {
            list = generalFields;
        } else if (position.equalsIgnoreCase("^")) { //start point in route
            list = startPointAttributes;
        } else if (position.startsWith("$-")) { //end point in route
            Integer pos = Integer.valueOf(position.substring(2));
            list = relativeToEndFields.get(pos);
            if (list == null) {
                list = new ArrayList<AttributeExtractor>();
                relativeToEndFields.put(pos, list);
            }
        } else if (position.equalsIgnoreCase("$")) { //end point in route
            list = endPointAttributes;
        } else if (position.startsWith("<")) {
            Integer pos = Integer.valueOf(position.substring(1));
            list = greaterThanFields.get(pos);
            if (list == null) {
                list = new ArrayList<AttributeExtractor>();
                greaterThanFields.put(pos, list);
            }
        } else if (position.startsWith(">")) {
            Integer pos = Integer.valueOf(position.substring(1));
            list = lessThanFields.get(pos);
            if (list == null) {
                list = new ArrayList<AttributeExtractor>();
                lessThanFields.put(pos, list);
            }
        } else if (position.startsWith("!=")) {
            Integer pos = Integer.valueOf(position.substring(2));
            list = notEqualFields.get(pos);
            if (list == null) {
                list = new ArrayList<AttributeExtractor>();
                notEqualFields.put(pos, list);
            }
        } else {
            Integer pos = Integer.valueOf(position);
            list = definedFields.transform(pos);
            if (list == null) {
                list = new ArrayList<AttributeExtractor>(1);
                definedFields.getMap().put(pos, list);
            }
        }

        list.add(extractor);
    }

    /**
     * @return transformer that translates depths to tab fields
     */
    @SuppressWarnings("unchecked")
    private Transformer<Integer, List<AttributeExtractor>> getTransformer() {
        Transformer<Object, List<AttributeExtractor>> generalFieldsTrans = ConstantTransformer.getInstance(generalFields);

        ThresholdTransformer greaterThan = new ThresholdTransformer(greaterThanFields, Action.GREATERTHAN);
        ThresholdTransformer lessThan = new ThresholdTransformer(lessThanFields, Action.LESSTHAN);
        ThresholdTransformer notEquThan = new ThresholdTransformer(notEqualFields, Action.NOT_EQUAL);
        ThresholdTransformer relativeToEnd = new ThresholdTransformer(relativeToEndFields, Action.DISTANCE_FROM_END);

        return new AdditiveTransformer(
                new Transformer[]{definedFields,
                        generalFieldsTrans,
                        greaterThan,
                        lessThan,
                        notEquThan});
    }

    /**
     * @return a model which converts depth and route vars. to a Attribute list
     */
    public AttributeExtractorModel getAttributeModel() {
        return new Model(getTransformer(),
                startPointAttributes,
                endPointAttributes,
                relativeToEndFields);
    }

    /**
     * concatinates results from muliple transformers
     *
     * @author hindlem
     */
    private class AdditiveTransformer implements Transformer<Integer, List<AttributeExtractor>> {

        private Transformer<Integer, List<AttributeExtractor>>[] transformers;

        public AdditiveTransformer(Transformer<Integer, List<AttributeExtractor>>[] transformers) {
            this.transformers = transformers;
        }

        @Override
        public List<AttributeExtractor> transform(Integer arg0) {

            List<AttributeExtractor> existing = null;

            for (Transformer<Integer, List<AttributeExtractor>> transformer : transformers) {
                List<AttributeExtractor> result = transformer.transform(arg0);
                if (result == null) continue;

                if (existing == null) {
                    existing = result;
                } else {
                    existing.addAll(result);
                }
            }
            return existing;
        }

    }

    /**
     * Defines action logic to apply to values to transform
     *
     * @author hindlem
     */
    public enum Action {
        GREATERTHAN, LESSTHAN, NOT_EQUAL, DISTANCE_FROM_END
    }

    /**
     * Evaluates AttributeExtractors to return based on operators
     *
     * @author hindlem
     */
    private class ThresholdTransformer implements Transformer<Integer, List<AttributeExtractor>> {

        private final List<AttributeExtractor> EMPTYLIST = new ArrayList<AttributeExtractor>(0);

        private Map<Integer, List<AttributeExtractor>> values;
        private Action action;

        public ThresholdTransformer(Map<Integer, List<AttributeExtractor>> values, Action action) {
            this.values = values;
            this.action = action;
        }

        @Override
        public List<AttributeExtractor> transform(Integer arg0) {

            for (Integer value : values.keySet()) {
                switch (action) {
                    case GREATERTHAN:
                        if (arg0 > value) return values.get(value);
                    case LESSTHAN:
                        if (arg0 < value) return values.get(value);
                    case NOT_EQUAL:
                        if (!arg0.equals(value)) return values.get(value);
                    default:
                        throw new RuntimeException("Programatic error: unknown action in Threshold transformer");
                }
            }
            return EMPTYLIST;
        }

    }

    /**
     * The external model that translates depth and path to a AttributeList
     *
     * @author hindlem
     */
    private class Model implements AttributeExtractorModel {

        private Transformer<Integer, List<AttributeExtractor>> depthSpecificTransformer;
        private List<AttributeExtractor> startPointAttributes;
        private List<AttributeExtractor> endPointAttributes;
        private Map<Integer, List<AttributeExtractor>> relativeToEndFields;

        public Model(
                Transformer<Integer, List<AttributeExtractor>> depthSpecificTransformer,
                List<AttributeExtractor> startPointAttributes,
                List<AttributeExtractor> endPointAttributes,
                Map<Integer, List<AttributeExtractor>> relativeToEndFields) {
            this.depthSpecificTransformer = depthSpecificTransformer;
            this.startPointAttributes = startPointAttributes;
            this.endPointAttributes = endPointAttributes;
            this.relativeToEndFields = relativeToEndFields;
        }

        public List<AttributeExtractor> getAttributes(int depth, Path route) {
            List<AttributeExtractor> attributes = new ArrayList<AttributeExtractor>(depthSpecificTransformer.transform(depth));

            if (depth == 0 && startPointAttributes.size() > 0) {
                attributes.addAll(startPointAttributes);
            }

            if (depth + 1 == route.getLength()
                    && endPointAttributes.size() > 0) {
                attributes.addAll(endPointAttributes);
            }

            List<AttributeExtractor> atts = relativeToEndFields.get(route.getLength() - (depth + 1));
            if (atts != null)
                attributes.addAll(atts);

            return attributes;
        }

        @Override
        public String[] getHeader(int depth, int headerLength) {
            List<AttributeExtractor> attributes = new ArrayList<AttributeExtractor>(depthSpecificTransformer.transform(depth));
            if (depth == 0) {
                attributes.addAll(startPointAttributes);
            } else if (depth + 1 == headerLength) {
                attributes.addAll(endPointAttributes);
            }
            String[] headers = new String[attributes.size()];
            for (int i = 0; i < headers.length; i++) {
                headers[i] = attributes.get(i).getHeaderName();
            }
            return headers;
        }

    }


}
