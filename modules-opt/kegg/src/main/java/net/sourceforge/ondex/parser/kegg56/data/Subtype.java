/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg56.data;

import com.sleepycat.persist.model.Persistent;

import java.io.Serializable;

/**
 * @author taubertj
 */
@Persistent
public class Subtype implements Serializable {

    /**
     * Default serial version unique id
     */
    private static final long serialVersionUID = 1L;

    private String name; //required
    private String value; //required

    public Subtype(String name, String value) {
        if (name == null) throw new NullPointerException("Name is null");
        if (value == null) throw new NullPointerException("Value is null");

        this.name = name;
        this.value = value.intern();
    }

    @SuppressWarnings("unused")
    //is required for Berkley Layer
    private Subtype() {
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
