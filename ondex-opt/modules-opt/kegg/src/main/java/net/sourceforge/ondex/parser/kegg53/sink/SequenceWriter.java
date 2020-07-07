/*
 * Created on 26-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.sink;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;

/**
 * @author taubertj
 */ 
public class SequenceWriter {


    private ConceptWriter cw;
    private ONDEXGraph og;

    public SequenceWriter(
            ONDEXGraph og,
            ConceptWriter cw) {

        this.cw = cw;
        this.og = og;
    }

    public Map<String, AttributeName> attNames = new HashMap<String, AttributeName>();


    public void createSequence(Sequence seq) throws MetaDataMissingException, ConceptWriter.ConceptNotExistingException {
        Integer cid = cw.getWrittenConceptId(seq.getConcept_fk());

        ONDEXConcept ac = og.getConcept(cid);
        if (ac == null) throw new NullPointerException("Concept is null ");

        AttributeName an = attNames.get(seq.getSequence_type_fk());

        if (an == null) {
            an = og.getMetaData().getAttributeName(seq.getSequence_type_fk());
            if (an == null)
                throw new AttributeNameMissingException(seq.getSequence_type_fk());
            attNames.put(seq.getSequence_type_fk(), an);
        }

        ac.createAttribute(an, seq.getSeq(), false);
    }

}
