/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.ondex.stats.datastructures;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.ondex.core.MetaData;

/**
 *
 * @author jweile
 */
public class TreeNode {

    /**
     * id of wrapped metadata
     */
    private String id;

    /**
     * Wrapped metadata
     */
    private MetaData md;

    /**
     * children of the wrapped metadata entity
     */
    private List<TreeNode> children = new ArrayList<TreeNode>();

    /**
     * parent of the wrapped metadata entity
     */
    private TreeNode parent;

    /**
     * Constructor over a metadata entity
     * @param md the metadata entity to wrap.
     */
    public TreeNode(MetaData md) {
        this.md = md;
        this.id = md.getId();
    }

    /**
     * Get wrapped metadata entity
     * @return
     */
    public MetaData getMD() {
        return md;
    }

    /**
     * get this entity's children
     * @return
     */
    public List<TreeNode> getChildren() {
        return children;
    }

    /**
     * add a child to this term
     * @param child
     */
    public void addChild(TreeNode child) {
        children.add(child);
    }

    /**
     * get this term's id.
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * get this term's parent
     * @return
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     * set this term's parent term
     * @param parent
     */
    public void setParent(TreeNode parent) {
        this.parent = parent;
    }
    
}
