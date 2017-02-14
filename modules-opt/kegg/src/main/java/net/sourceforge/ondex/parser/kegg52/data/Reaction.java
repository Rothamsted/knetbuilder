/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.data;

import com.sleepycat.persist.model.Persistent;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author taubertj
 */
@Persistent
public class Reaction implements Serializable {

    /**
     * Default serial version unique id
     */
    private static final long serialVersionUID = 1L;

    private HashSet<Entry> substrates = new HashSet<Entry>();
    private HashSet<Entry> products = new HashSet<Entry>();

    private String name; //required
    private String type; //required

    public Reaction(String name, String type) {
        if (name == null) throw new NullPointerException("Name is null");
        if (type == null) throw new NullPointerException("type is null");
        this.name = name.toUpperCase();
        this.type = type.intern();
    }

    @SuppressWarnings("unused")
    //is required for Berkley Layer
    private Reaction() {
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Set<Entry> getProducts() {
        return products;
    }

    public Set<Entry> getSubstrates() {
        return substrates;
    }

}
