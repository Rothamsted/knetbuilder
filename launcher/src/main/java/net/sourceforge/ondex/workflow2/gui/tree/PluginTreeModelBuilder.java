package net.sourceforge.ondex.workflow2.gui.tree;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sourceforge.ondex.init.ArrayKey;
import net.sourceforge.ondex.init.PluginDescription;
import net.sourceforge.ondex.init.PluginType;

public class PluginTreeModelBuilder {
	private PluginTreeModelBuilder(){}
    
    public static DefaultTreeModel build(List<PluginDescription> list, boolean showStableOnly){
    	Map<ArrayKey<String>, DefaultMutableTreeNode> index = new HashMap<ArrayKey<String>, DefaultMutableTreeNode>();
    	DocumentedTreeNode root = new DocumentedTreeNode("All");
    	index.put(new ArrayKey<String>(new String[]{"All"}), root);
        DefaultTreeModel model = new DefaultTreeModel(root);
        for(PluginDescription pb : list){
        	if(!pb.getOndexType().equals(PluginType.VALIDATOR))
        		addPlugin(pb, index, root, showStableOnly);
        }
        return model;
    }
    
    
    
/**
    private static void addPlugin(PluginDescription pb, Map<ArrayKey<String>, DefaultMutableTreeNode> index, DocumentedTreeNode root) {
        String path[] = pb.getPath().split("/");
        DefaultMutableTreeNode n = new PluginTreeNode(pb);
        for (int i = path.length - 1; i >= 0; i--) {
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
 */

    private static void addPlugin(PluginDescription pb, Map<ArrayKey<String>, DefaultMutableTreeNode> index, DocumentedTreeNode root, boolean showStableOnly) {
        String path[] = pb.getPath().split("/");
        try{
            if(showStableOnly){
            	if(path.length < 1){
            		System.err.println("Incorrect plugin descriptor found for: "+pb.getName()+" [class: "+pb.getCls()+"]. Plug-in was omitted because of these issues.");
            		return;
            	}
            	//System.out.println(pb.getOndexId()+"::"+pb.getPath()+"::"+path[1]);
            	if(!path[1].equalsIgnoreCase("Stable")){
            		return;
            	}
            	path = skipValue(path, 1);
            }
            
            DefaultMutableTreeNode n = new PluginTreeNode(pb);
            for (int i = path.length - 1; i >= 0; i--) {
                ArrayKey<String> key = new ArrayKey<String>(Arrays.copyOf(path, i + 1));
                DefaultMutableTreeNode parent = index.get(key);
                if (parent == null) {
                    DefaultMutableTreeNode temp = new DocumentedTreeNode(path[i]);
                    index.put(key, temp);
                    temp.add(n);
                    n = temp;
                } else {
                	int in = findIndex(parent, pb.getName());
                    parent.insert(n, in);
                    return;
                }
            }
            root.insert(n, findIndex(root, ((DocumentedTreeNode)n).getName()));
        }
        catch(java.lang.ArrayIndexOutOfBoundsException e){
        	System.err.println("Bad path: "+pb.getPath());
        	//e.printStackTrace();
        }

    }
    
    @SuppressWarnings("unchecked")
	private static int findIndex(final DefaultMutableTreeNode parent, final String name){
    	int result = 0;
    	Enumeration<DocumentedTreeNode> enu = parent.children();
    	while(enu.hasMoreElements()){
    		DocumentedTreeNode node = enu.nextElement();
    		if(node.getName().compareToIgnoreCase(name) >= 0){
    			break;
    		}
    		result++;
    	}
    	return result;
    }
    
    private static String[] skipValue(String[] input, int skip){
    	String[] result = new String[input.length-1]; 
    	for(int i = 0; i < input.length; i++){
    		if(i == skip){
    			continue;
    		}
    		int p=i<skip?i:i-1;
    		result[p] = input[i];
    	}
    	return result;
    }
}
