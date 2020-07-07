/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.ondex.stats.datastructures;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 *
 * @author jweile
 */
public abstract class Tree {

    /**
     * finds the full name for the given id.
     * @param id
     * @return
     */
    public abstract String retrieveFullName(String id);

    /**
     * recursively prints out the results for the given tree node.
     * @param node
     * @param maps
     * @param level
     */
    protected void recursivelyPrintStats(TreeNode node, PropertyMaps maps, 
            int level, OutputStreamWriter w) throws IOException {

        indent(level, w);
        w.write(node.getMD().getFullname().toUpperCase());

        Map<String,Integer> props = maps.getMaps(node.getMD());
        double count = (double)props.get(PropertyMaps.KEY_COUNT);

        w.write(String.format(": %.0f\n",count));

        for (String prop : props.keySet()) {

            if (prop.equals(PropertyMaps.KEY_COUNT)) {
                continue;
            }

            indent(level, w);

            String fullname = retrieveFullName(prop);
            w.write("  - "+fullname+" ("+prop+"): ");

            double freq = (double)props.get(prop);
            double percent = freq * 100.0 / count;
            w.write(String.format("%.2f",percent));
            w.write("%\n");

        }

        for (TreeNode child : node.getChildren()) {
            recursivelyPrintStats(child, maps, level+1, w);
        }

    }

    /**
     * creates an indentation of size <code>level</code>
     * @param level
     */
    private void indent(int level, OutputStreamWriter w) throws IOException {
        for (int i = 0; i < level; i++) {
            w.write("  ");
        }
    }

    /**
     * prints out the statistics information in this tree.
     *
     * @param maps
     */
    public abstract void printStats(PropertyMaps maps, OutputStream out) throws IOException;

}
