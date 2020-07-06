package net.sourceforge.ondex.workflow2.gui.tree;

import javax.swing.tree.DefaultMutableTreeNode;

public class DocumentedTreeNode extends DefaultMutableTreeNode {
    private static final long serialVersionUID = 1L;
    private final String name;

    protected String doc = "No documentation found";

    public DocumentedTreeNode(String name) {
    	super(name);
    	this.name = name;
    }

    public DocumentedTreeNode(String name, String doc) {
        this(name);
        this.doc = doc;
    }

    public String getDocumentation() {
        return doc;
    }

    public String getArguments() {
        return "";
    }

    public String getFiles() {
        return "";
    }

	public String getName() {
		return name;
	}
}
