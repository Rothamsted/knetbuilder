package net.sourceforge.ondex.tools;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;

import java.io.*;
import java.util.HashMap;
import net.sourceforge.ondex.exception.type.DataSourceMissingException;
import net.sourceforge.ondex.exception.type.ConceptClassMissingException;
import net.sourceforge.ondex.exception.type.EvidenceTypeMissingException;
import net.sourceforge.ondex.exception.type.RelationTypeMissingException;
import net.sourceforge.ondex.exception.type.UnitMissingException;
import org.apache.log4j.Logger;

/**
 * @author Jochen Weile
 * @param <M> MetaData
 */
public final class MetaDataLookup2<M extends MetaData> extends HashMap<String, M> {

    private static final Logger logger = Logger.getLogger(MetaDataLookup.class);

    /**
     * serial id.
     */
    private static final long serialVersionUID = -3342994239536902467L;

    /**
     * enum representing the parameterization type
     */
    private MetaDataType type;

    /**
     * the graphs metadata object.
     */
    private ONDEXGraphMetaData md;

    /**
     *
     * @param stream May be null.
     * @param md
     * @param clazz
     * @throws Exception
     */
    public MetaDataLookup2(Reader reader, ONDEXGraphMetaData md, Class<? extends MetaData> clazz) throws Exception {
        this.md = md;
        type = MetaDataType.fromClass(clazz);

        parse(reader);
    }



    /**
     * parses the metadata mapping file
     *
     * @param file the file to load
     * @throws IOException
     */
    private void parse(Reader reader) throws IOException, MetaDataMissingException {
        if (reader == null){
            return;
        }
        BufferedReader br;
        br = new BufferedReader(reader);
        boolean ready = false;
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            if (line.startsWith("#" + type)) {
                ready = true;
            } else if (line.startsWith("#")) {
                ready = false;
            } else if (ready) {
                if (line.startsWith("\\")) {
                    line = line.substring(1);
                }
                String[] cols = line.split("\t");
                if (cols.length != 2 || cols[0].trim().length() == 0 || cols[1].trim().length() == 0) {
                    throw new IOException("Line: \""+line+"\" in Stream corrupted!");
                } else {
                    insert(cols[0], cols[1]);
                }
            }
        }
        if (size() == 0) {
            throw new IOException("section " + type + " empty or missing!");
        }
    }

    /**
     * inserts a new key value pair into the dictionary
     *
     * @param key
     * @param id
     * @throws MetaDataMissingException
     */
    @SuppressWarnings("unchecked")
    private void insert(String key, String id) throws MetaDataMissingException {
        M m = null;
        switch (type) {
            case ATTRIBUTE_NAME:
                m = (M) md.getAttributeName(id);
                if (m == null) {
                    throw new AttributeNameMissingException(id);
                }
                break;
            case CONCEPT_CLASS:
                m = (M) md.getConceptClass(id);
                if (m == null) {
                    throw new ConceptClassMissingException(id);
                }
                break;
            case CV:
                m = (M) md.getDataSource(id);
                if (m == null) {
                    throw new DataSourceMissingException(id);
                }
                break;
            case EVIDENCE_TYPE:
                m = (M) md.getEvidenceType(id);
                if (m == null) {
                    throw new EvidenceTypeMissingException(id);
                }
                break;
            case RELATION_TYPE:
                m = (M) md.getRelationType(id);
                if (m == null) {
                    throw new RelationTypeMissingException(id);
                }
                break;
            case UNIT:
                m = (M) md.getUnit(id);
                if (m == null) {
                    throw new UnitMissingException(id);
                }
                break;
        }
        put(key, m);
    }

    /**
     * enum representing the different metadata types
     *
     * @author jweile
     */
    private enum MetaDataType {
        ATTRIBUTE_NAME, CONCEPT_CLASS, CV, EVIDENCE_TYPE, RELATION_TYPE, UNIT;

        /**
         * returns the correct enum value for a given metadata class
         *
         * @param clazz
         * @return
         */
        public static MetaDataType fromClass(Class<?> clazz) {
            if (clazz.equals(AttributeName.class)) {
                return ATTRIBUTE_NAME;
            } else if (clazz.equals(ConceptClass.class)) {
                return CONCEPT_CLASS;
            } else if (clazz.equals(DataSource.class)) {
                return CV;
            } else if (clazz.equals(EvidenceType.class)) {
                return EVIDENCE_TYPE;
            } else if (clazz.equals(RelationType.class)) {
                return RELATION_TYPE;
            } else if (clazz.equals(Unit.class)) {
                return UNIT;
            } else {
                throw new IllegalArgumentException("unknown Metadata class");
            }
        }
    }

}
