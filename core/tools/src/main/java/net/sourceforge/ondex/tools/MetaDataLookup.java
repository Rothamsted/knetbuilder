package net.sourceforge.ondex.tools;

import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;

import java.io.*;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * @author Jochen Weile, Christian Brenninkmeijer
 * @param <M> MetaData
 */
public final class MetaDataLookup<M extends MetaData> extends HashMap<String, M> {

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

    public MetaDataLookup(File file, ONDEXGraphMetaData md, Class<? extends MetaData> clazz) throws Exception {
        this.md = md;
        type = MetaDataType.fromClass(clazz);

        parse(file);
    }

    /**
     *
     * @param stream May be null.
     * @param md
     * @param clazz
     * @throws Exception
     */
    public MetaDataLookup(Reader reader, ONDEXGraphMetaData md, Class<? extends MetaData> clazz) throws Exception {
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
    private void parse(File file) throws IOException, MetaDataMissingException {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
        }
        catch (FileNotFoundException e) {
            throw new IOException("Unable to load file: " + file.getAbsoluteFile(), e);
        }
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
                    throw new IOException("Line: \""+line+"\" in File " + file + " corrupted!");
                } else {
                    insert(cols[0], cols[1]);
                }
            }
        }
        if (size() == 0) {
        	if(!type.equals(MetaDataType.RELATION_TYPE)){
        		throw new IOException("section " + type + " empty or missing in file " + file + "!");
        	}
        }
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
        reader.reset();
        BufferedReader br;
        br = new BufferedReader(reader);
        boolean ready = false;
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            logger.info(line);
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
        logger.info(key+" = "+id);
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
                    m = (M) md.getFactory().createConceptClass(id);
                }
                break;
            case DATASOURCE:
                m = (M) md.getDataSource(id);
                if (m == null) {
                    m = (M) md.getFactory().createDataSource(id);
                }
                break;
            case EVIDENCE_TYPE:
                m = (M) md.getEvidenceType(id);
                if (m == null) {
                    m = (M) md.getFactory().createEvidenceType(id);
                }
                break;
            case RELATION_TYPE:
                m = (M) md.getRelationType(id);
                if (m == null) {
                    m = (M) md.getFactory().createRelationType(id);
                }
                break;
            case UNIT:
                m = (M) md.getUnit(id);
                if (m == null) {
                    m = (M) md.getFactory().createUnit(id);
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
        ATTRIBUTE_NAME, CONCEPT_CLASS, DATASOURCE, EVIDENCE_TYPE, RELATION_TYPE, UNIT;

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
                return DATASOURCE;
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

    public String[] recognize(String str) {
        for (String key : keySet()) {
            if (str.startsWith(key)) {
                String acc = str.substring(key.length());
				return new String[] {key,acc};
			}
		}
		return null;
	}
}
