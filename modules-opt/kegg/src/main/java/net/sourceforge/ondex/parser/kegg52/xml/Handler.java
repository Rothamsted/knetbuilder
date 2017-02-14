/*
 * Created on 08.01.2005
 *
 */
package net.sourceforge.ondex.parser.kegg52.xml;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.ondex.event.type.DataFileErrorEvent;
import net.sourceforge.ondex.parser.kegg52.Parser;
import net.sourceforge.ondex.parser.kegg52.data.Entry;
import net.sourceforge.ondex.parser.kegg52.data.Graphics;
import net.sourceforge.ondex.parser.kegg52.data.Pathway;
import net.sourceforge.ondex.parser.kegg52.data.Reaction;
import net.sourceforge.ondex.parser.kegg52.data.Relation;
import net.sourceforge.ondex.parser.kegg52.data.Subtype;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Jan
 */
public class Handler extends DefaultHandler {

    private Pathway pathway;

    private Entry entry;

    private Graphics graphics;

    private Relation relation;

    private Subtype subtype;

    private Reaction reaction;

    private Map<String, Entry> entryName = new HashMap<String, Entry>();

    public Handler() {
        super();
    }

    public Pathway getPathway() {
        return pathway;
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        String element = null;
        if (uri.length() == 0)
            element = qName;
        else
            element = name;

        if (element.equalsIgnoreCase("entry")) {
            pathway.getEntries().put(entry.getId().toUpperCase(), entry);
            entryName.put(entry.getName(), entry);
            // special case for when a reaction is missing in the reaction list,
            // but referenced from an entry, becomes orphan
            if (entry.getReaction() != null) {
                for (String rid : entry.getReaction().split(" ")) {
                    reaction = new Reaction(rid, "orphan");
                    pathway.getReactions().put(
                            reaction.getName().toUpperCase(), reaction);
                }
            }
        }

        if (element.equalsIgnoreCase("graphics")) {
            entry.setGraphics(graphics);
        }

        if (element.equalsIgnoreCase("relation")) {
            pathway.getRelations().add(relation);
        }

        if (element.equalsIgnoreCase("subtype")) {
            relation.getSubtype().add(subtype);
        }

        if (element.equalsIgnoreCase("reaction")) {
            pathway.getReactions().put(reaction.getName().toUpperCase(),
                    reaction);
        }
    }

    @Override
    public void startElement(String uri, String name, String qName,
                             Attributes atts) {
        String element = null;
        if (uri.length() == 0)
            element = qName;
        else
            element = name;

        if (element.equalsIgnoreCase("pathway")) {
            pathway = new Pathway(atts.getValue("name").trim(), atts.getValue(
                    "org").trim(), atts.getValue("number").trim());

            String at = atts.getValue("title");
            if (at != null)
                pathway.setTitle(at.trim());

            String ai = atts.getValue("image");
            if (ai != null)
                pathway.setImage(ai.trim());

            String al = atts.getValue("link");
            if (al != null)
                pathway.setLink(al.trim());
        }

        if (element.equalsIgnoreCase("entry")) {
            entry = new Entry(atts.getValue("id").trim(), atts.getValue("name")
                    .trim(), atts.getValue("type").trim());

            String al = atts.getValue("link");
            if (al != null)
                entry.setLink(al.trim());

            String ar = atts.getValue("reaction");
            if (ar != null)
                entry.setReaction(ar.trim());

            String am = atts.getValue("map");
            if (am != null)
                entry.setMap(am.trim());
        }

        if (element.equalsIgnoreCase("graphics")) {
            graphics = new Graphics();

            String an = atts.getValue("name");
            if (an != null)
                graphics.setName(an.trim());

            String ax = atts.getValue("x");
            if (ax != null)
                graphics.setX(ax.trim());

            String ay = atts.getValue("y");
            if (ay != null)
                graphics.setY(ay.trim());

            String at = atts.getValue("type");
            if (at != null)
                graphics.setType(at.trim());

            String aw = atts.getValue("width");
            if (aw != null)
                graphics.setWidth(aw.trim());

            String ah = atts.getValue("height");
            if (ah != null)
                graphics.setHeight(ah.trim());

            String afg = atts.getValue("fgcolor");
            if (afg != null)
                graphics.setFgcolor(afg.trim());

            String abg = atts.getValue("bgcolor");
            if (abg != null)
                graphics.setBgcolor(abg.trim());
        }

        if (element.equalsIgnoreCase("component")) {
            Entry comp = pathway.getEntries().get(
                    atts.getValue("id").trim().toUpperCase());
            if (comp == null) {
                Parser.propagateEventOccurred(new DataFileErrorEvent(
                        "Inconsitency in pathway " + pathway.getId()
                                + ": component with id " + atts.getValue("id")
                                + " not found.", ""));
            } else {
                entry.getComponents().put(comp.getId().toUpperCase(), comp);
            }
        }

        if (element.equals("relation")) {
            Entry entry1 = pathway.getEntries().get(
                    atts.getValue("entry1").toUpperCase());
            Entry entry2 = pathway.getEntries().get(
                    atts.getValue("entry2").toUpperCase());
            relation = new Relation(entry1, entry2, atts.getValue("type"));
        }

        if (element.equalsIgnoreCase("subtype")) {
            subtype = new Subtype(atts.getValue("name"), atts.getValue("value"));
        }

        if (element.equalsIgnoreCase("reaction")) {
            reaction = new Reaction(atts.getValue("name"), atts
                    .getValue("type"));
        }

        if (element.equalsIgnoreCase("substrate")) {
            Entry substrate = entryName.get(atts.getValue("name"));
            if (substrate == null) {
                substrate = new Entry(atts.getValue("name"), atts
                        .getValue("name"), "compound");
                entryName.put(substrate.getName(), substrate);
                pathway.getEntries().put(substrate.getId().toUpperCase(),
                        substrate);
            }
            reaction.getSubstrates().add(substrate);
        }

        if (element.equalsIgnoreCase("product")) {
            Entry product = entryName.get(atts.getValue("name"));
            if (product == null) {
                product = new Entry(atts.getValue("name"), atts
                        .getValue("name"), "compound");
                entryName.put(product.getName(), product);
                pathway.getEntries()
                        .put(product.getId().toUpperCase(), product);
            }
            reaction.getProducts().add(product);
        }
    }

}
