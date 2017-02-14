/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.data;

import com.sleepycat.persist.model.Persistent;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author taubertj
 */
@Persistent
public class Entry implements Serializable {

    /**
     * Default serial version unique id
     */
    private static final long serialVersionUID = 1L;

    private HashMap<String, Entry> components = new HashMap<String, Entry>();
    private HashSet<String> conceptIDs = new HashSet<String>(4);

    private String id; //required
    private String name; //required
    private String type; //required

    private String link;
    private String reaction;
    private String map;
    private Graphics graphics;

    public Entry(String id, String name, String type) {
        if (id == null) throw new NullPointerException("ID is null");
        if (name == null) throw new NullPointerException("Name is null");
        if (type == null) throw new NullPointerException("Type is null");
        this.id = id.toUpperCase();
        this.name = name;
        this.type = type.intern();
    }

    @SuppressWarnings("unused")
    //is required for Berkley Layer
    private Entry() {
    }

    public String getId() {
        return id.toUpperCase();
    }

    public String getIdInNativeCase() {
        return id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getName() {
        return name;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction.toUpperCase();
    }

    public String getType() {
        return type;
    }

    public Graphics getGraphics() {
        return graphics;
    }

    public void setGraphics(Graphics graphics) {
        this.graphics = graphics;
    }

    public Map<String, Entry> getComponents() {
        return components;
    }

    public Set<String> getConceptIDs() {
        return conceptIDs;
    }

    public int hashCode() {
        return id.hashCode();
    }

    public String toString() {
        return id;
    }

}
