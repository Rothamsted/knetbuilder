package net.sourceforge.ondex.workflow2.gui.tree;

import net.sourceforge.ondex.annotations.DatabaseTarget;
import net.sourceforge.ondex.init.PluginDescription;
import net.sourceforge.ondex.init.PluginDocumentationFactory;
import net.sourceforge.ondex.init.PluginRegistry;

public class PluginTreeNode extends DocumentedTreeNode {
    private static final long serialVersionUID = 1L;
    private String toolTip;
    private final PluginDescription pb;

    public PluginTreeNode(PluginDescription pb) {
        super(pb.getName());
        try {
            Class<?> plugin = Class.forName(pb.getCls(), false, PluginRegistry.getInstance().getClassLoader());
            DatabaseTarget target = plugin.getAnnotation(DatabaseTarget.class);
            if (target != null) {
                StringBuilder versions = new StringBuilder();
                for (String version : target.version()) {
                    if (version.trim().length() > 0) {
                        versions.append(version);
                        versions.append(',');
                    }
                }

                if (versions.toString().trim().length() > 0) {
                    versions.setLength(versions.length() - 1);
                    super.setUserObject(pb.getName() + " (" + versions.toString() + ")");
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        this.pb = pb;
    }

    public PluginDescription getPlugin() {
        return pb;
    }

    @Override
    public String getDocumentation() {
        return PluginDocumentationFactory.getDocumentation(pb);
    }

    @Override
    public String getArguments() {
        return PluginDocumentationFactory.getArguments(pb);
    }

    @Override
    public String getFiles() {
        return PluginDocumentationFactory.getFiles(pb);
    }

    public String getComment() {
        //TODO
        return "";
    }

    public void setComment(String comment) {
        //TODO
    }

    public String getTooltip() {
        return toolTip;
    }

    public void setTooltip(String toolTip) {
        this.toolTip = toolTip;
    }
}

