package net.sourceforge.ondex.scripting.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Random;

import net.sourceforge.ondex.config.Config;

public class EditorCalls {
	private EditorCalls(){}

	public static final void callEditor(String fileToEdit, String extension) throws Exception{
		Object path = Config.properties.get("Scripting.Editor");
		if(path != null && new File(path.toString()).exists()){
			File edited = new File(fileToEdit);
			if(edited.exists()){
				Runtime.getRuntime().exec(path.toString()+" "+fileToEdit);
				return;
			}
			else throw new Exception("File "+edited.getAbsolutePath()+" does not exist!");
		}
		else throw new Exception("The editor to use was not not configured!");
	}
	
	public static final String callEditorWithText(String textToEdit, String extension) throws Exception{
		Object path = Config.properties.get("Scripting.Editor");
		if(path != null && new File(path.toString()).exists()){
			String fileToEdit = System.getProperty("java.io.tmpdir")+File.separator+String.valueOf(System.currentTimeMillis())+new Random(System.currentTimeMillis()).nextInt()+"."+extension;
			File edited = new File(fileToEdit);
			edited.createNewFile();
			edited.deleteOnExit();
			Writer output = new BufferedWriter(new FileWriter(edited));
			output.write(textToEdit);
			output.flush();
			output.close();
			if(edited.exists()){
				Runtime.getRuntime().exec(path.toString()+" "+fileToEdit);
				return fileToEdit;
			}
			else throw new Exception("File "+edited.getAbsolutePath()+" does not exist!");
		}
		else throw new Exception("The editor to use was not not configured!");
	}
}