package net.sourceforge.ondex.ovtk2.modules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sourceforge.ondex.init.ArrayKey;

public class PluginTreeModelBuilder<Z extends Object> {
	private final DefaultTreeModel model;
	private final DocumentedTreeNode root = new DocumentedTreeNode("All");
	private final Map<ArrayKey<String>, DefaultMutableTreeNode> index = new HashMap<ArrayKey<String>, DefaultMutableTreeNode>();

	public PluginTreeModelBuilder() {
		index.put(new ArrayKey<String>(new String[] { "All" }), root);

		model = new DefaultTreeModel(root);
	}

	public DefaultTreeModel getTreeModel() {
		return model;
	}

	public void addPlugin(Z pb, String doc, String... path) {
		DefaultMutableTreeNode n = new PluginTreeNode(pb, doc, path[path.length - 1]);

		for (int i = path.length - 2; i >= 0; i--) {
			ArrayKey<String> key = new ArrayKey<String>(Arrays.copyOf(path, i + 1));
			DefaultMutableTreeNode parent = index.get(key);
			if (parent == null) {
				DefaultMutableTreeNode temp = new DocumentedTreeNode(path[i]);
				index.put(key, temp);
				temp.add(n);
				n = temp;
			} else {
				parent.add(n);
				return;
			}
		}
		root.add(n);
	}
}
