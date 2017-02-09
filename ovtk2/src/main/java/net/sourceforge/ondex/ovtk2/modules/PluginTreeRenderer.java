package net.sourceforge.ondex.ovtk2.modules;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;

public class PluginTreeRenderer implements TreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent(JTree arg0, Object arg1, boolean arg2, boolean arg3, boolean arg4, int arg5, boolean arg6) {
		if (arg1 instanceof PluginTreeNode) {
			PluginTreeNode n = (PluginTreeNode) arg1;
			JLabel l = new JLabel(arg1.toString());
			String tt = n.getTooltip();
			if (tt != null && tt.length() > 0) {
				l.setToolTipText(tt);
			}
			return l;
		} else if (arg1 instanceof DocumentedTreeNode) {
			TreeNode n = (TreeNode) arg1;
			JLabel l = new JLabel(arg1.toString());
			l.setFont(l.getFont().deriveFont(Font.BOLD));
			if (n.getChildCount() == 0) {
				l.setForeground(Color.LIGHT_GRAY);
			}
			return l;
		} else
			return new JLabel(arg1.toString());
	}
}
