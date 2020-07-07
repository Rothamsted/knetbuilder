package net.sourceforge.ondex.ovtk2.modules;

import javax.swing.tree.DefaultMutableTreeNode;

public class DocumentedTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;

	protected String doc = "No documentation found";

	public DocumentedTreeNode(String name) {
		super(name);
	}

	public DocumentedTreeNode(String name, String doc) {
		this(name);
		this.doc = doc;
	}

	public String getDocumentation() {
		return doc;
	}
}
