package net.sourceforge.ondex.ovtk2.modules;

public class PluginTreeNode<Z extends Object> extends DocumentedTreeNode {
	private static final long serialVersionUID = 1L;
	private String toolTip;
	private final Z pb;
	private final String doc;

	public PluginTreeNode(Z pb, String doc, String name) {
		super(name);
		this.pb = pb;
		this.doc = doc;
	}

	public Z getPlugin() {
		return pb;
	}

	@Override
	public String getDocumentation() {
		return doc;
	}

	public String getComment() {
		// TODO
		return "";
	}

	public String getTooltip() {
		return toolTip;
	}

	public void setTooltip(String toolTip) {
		this.toolTip = toolTip;
	}
}
