/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.ondex.stats.datastructures;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.RelationType;

/**
 *
 * @author jweile
 */
public abstract class RelationTypeTree extends Tree {

    private Map<String,TreeNode> nodes;

    public RelationTypeTree(ONDEXGraph graph, Set<String> ids) {

        nodes = new HashMap<String, TreeNode>();

        for (String id : ids) {
            RelationType cc = graph.getMetaData().getRelationType(id);
            TreeNode n = new TreeNode(cc);
            nodes.put(id, n);
        }

        for (TreeNode node : nodes.values()) {
            RelationType parent = ((RelationType)node.getMD()).getSpecialisationOf();
            if (parent != null) {
                TreeNode parentNode = nodes.get(parent.getId());
                node.setParent(parentNode);
                parentNode.addChild(node);
            }
        }

    }

    public void printStats(PropertyMaps maps, OutputStream out) throws IOException {
        OutputStreamWriter w = new OutputStreamWriter(out);
        recursivelyPrintStats(nodes.get("r"), maps, 0, w);
    }


    


}
