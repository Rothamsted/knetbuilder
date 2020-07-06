package net.sourceforge.ondex.init;

import net.sourceforge.ondex.export.ONDEXExport;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.mapping.ONDEXMapping;
import net.sourceforge.ondex.parser.ONDEXParser;
import net.sourceforge.ondex.producer.ProducerONDEXPlugin;
import net.sourceforge.ondex.transformer.ONDEXTransformer;
import net.sourceforge.ondex.validator.AbstractONDEXValidator;

/**
 * Describes/defines producer types
 *
 * @author hindlem
 */
public enum PluginType {
    PARSER(ONDEXParser.class, "Parser", "Imports data from external databases into an Ondex graph."),
    MAPPING(ONDEXMapping.class, "Mapping", "Creates relations between existing Ondex concepts"),
    FILTER(ONDEXFilter.class, "Filter", "Returns a subset of the concepts and relations of the Ondex graph"),
    TRANSFORMER(ONDEXTransformer.class, "Transformer", "Adds/Removes elements or their attributes from an Ondex graph"),
    EXPORT(ONDEXExport.class, "Export", "Exports an Ondex graph to another format"),
    VALIDATOR(AbstractONDEXValidator.class, "Validator", "A Factory for Ondex plugins"),
    PRODUCER(ProducerONDEXPlugin.class, "Graph", "Provides an Ondex graph data store");

    private Class pluginClass;
    private String name;
    private String description;

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public Class getPluginClass() {
        return pluginClass;
    }

    /**
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Enum constructor
     *
     * @param pluginClass the class of the interface of the producer
     * @param name        the common name of the producer
     * @param description a description of the producer type
     */
    private PluginType(Class pluginClass, String name, String description) {
        this.pluginClass = pluginClass;
        this.name = name;
        this.description = description;
    }


    public static PluginType getType(Object c) throws UnknownPluginTypeException {
        if (c instanceof ONDEXParser) {
            return PARSER;
        } else if (c instanceof ONDEXMapping) {
            return MAPPING;
        } else if (c instanceof ONDEXFilter) {
            return FILTER;
        } else if (c instanceof ONDEXTransformer) {
            return TRANSFORMER;
        } else if (c instanceof ONDEXExport) {
            return EXPORT;
        } else if (c instanceof ProducerONDEXPlugin) {
            return PRODUCER;
        } else if (c instanceof AbstractONDEXValidator) {
            return VALIDATOR;
        } else {
            throw new UnknownPluginTypeException(c.getClass().getName() + " is not a known Ondex plugin type");
        }
    }

    public static PluginType getType(String name) throws UnknownPluginTypeException {
        if (name.equalsIgnoreCase(PARSER.getName())) {
            return PARSER;
        } else if (name.equalsIgnoreCase(MAPPING.getName())) {
            return MAPPING;
        } else if (name.equalsIgnoreCase(FILTER.getName())) {
            return FILTER;
        } else if (name.equalsIgnoreCase(TRANSFORMER.getName())) {
            return TRANSFORMER;
        } else if (name.equalsIgnoreCase(EXPORT.getName())) {
            return EXPORT;
        } else if (name.equalsIgnoreCase(PRODUCER.getName())) {
            return PRODUCER;
        } else if (name.equalsIgnoreCase(VALIDATOR.getName())) {
            return VALIDATOR;
        } else {
            throw new UnknownPluginTypeException(name + " is not a known Ondex plugin type");
        }
    }

    public static class UnknownPluginTypeException extends Exception {

        /**
         * @param error
         */
        public UnknownPluginTypeException(String error) {
            super(error);
        }
    }

}
