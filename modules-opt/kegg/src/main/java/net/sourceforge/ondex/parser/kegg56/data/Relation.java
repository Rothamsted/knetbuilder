/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.data;

import com.sleepycat.persist.model.Persistent;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author taubertj
 */
@Persistent
public class Relation implements Serializable {

    /**
     * Default serial version unique id
     */
    private static final long serialVersionUID = 1L;

    private ArrayList<Subtype> subtype = new ArrayList<Subtype>();

    private Entry entry1; //required
    private Entry entry2; //required
    private String type; //required

    public Relation(Entry entry1, Entry entry2, String type) {
        if (entry1 == null) throw new NullPointerException("Entry 1 is null");
        if (entry2 == null) throw new NullPointerException("Entry 2 is null");
        if (type == null) throw new NullPointerException("type is null");

        this.entry1 = entry1;
        this.entry2 = entry2;
        this.type = type.intern();
    }

    @SuppressWarnings("unused")
    //is required for Berkley Layer
    private Relation() {
    }

    public Entry getEntry1() {
        return entry1;
    }

    public Entry getEntry2() {
        return entry2;
    }

    public String getType() {
        return type;
    }

    public ArrayList<Subtype> getSubtype() {
        return subtype;
    }

}
