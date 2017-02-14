/*
 * Created on 25-Apr-2005
 *
 */
package net.sourceforge.ondex.parser.kegg53.data;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author taubertj
 */
@Entity
public class Pathway {

    private HashMap<String, Entry> entries = new HashMap<String, Entry>(30);
    private ArrayList<Relation> relations = new ArrayList<Relation>(30);
    private HashMap<String, Reaction> reactions = new HashMap<String, Reaction>(30);

    @PrimaryKey
    private String pathid; //required

    private String org; //required
    private String number; //required
    private String title;
    private String image;
    private String link;

    public Pathway(String pid, String org, String number) {
        this.pathid = pid;
        this.org = org.intern();
        this.number = number;
    }

    @SuppressWarnings("unused")
    //is required for Berkley Layer
    private Pathway() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getId() {
        return pathid;
    }

    public String getNumber() {
        return number;
    }

    public String getOrg() {
        return org;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * preform update after changing this to keep persistance
     *
     * @return
     */
    public Map<String, Entry> getEntries() {
        return entries;
    }

    /**
     * preform update after changing this to keep persistance
     *
     * @return
     */
    public Map<String, Reaction> getReactions() {
        return reactions;
    }

    /**
     * preform update after changing this to keep persistance
     *
     * @return
     */
    public ArrayList<Relation> getRelations() {
        return relations;
    }

}
