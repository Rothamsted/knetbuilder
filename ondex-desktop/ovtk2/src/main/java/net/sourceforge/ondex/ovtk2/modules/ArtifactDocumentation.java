package net.sourceforge.ondex.ovtk2.modules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.transform.TransformerException;

/**
 * @author hindlem
 */
public class ArtifactDocumentation {

	private FetchPluginDescription pluginDescriptor = new FetchPluginDescription();

	private Artifact artifact;
	private String xmlDocumentation;

	public Artifact getArtifact() {
		return artifact;
	}

	public String getXmlDocumentation() {
		return xmlDocumentation;
	}

	public ArtifactDocumentation(Artifact artifact) throws IOException {
		this.artifact = artifact;
		init();
	}

	public void init() throws IOException {
		try {
			StringBuilder sb = new StringBuilder();
			InputStream stream = pluginDescriptor.getDocumentation(artifact);
			BufferedReader isr = new BufferedReader(new InputStreamReader(stream));
			while (isr.ready()) {
				sb.append(isr.readLine());
				sb.append('\n');
			}

			xmlDocumentation = sb.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		return artifact.getArtifactId();
	}

}
