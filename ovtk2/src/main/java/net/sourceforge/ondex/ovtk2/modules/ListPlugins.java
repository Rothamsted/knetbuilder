package net.sourceforge.ondex.ovtk2.modules;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import net.sourceforge.ondex.event.ONDEXEvent;
import net.sourceforge.ondex.event.type.UnspecifiedErrorEvent;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * @author hindlem
 */
public class ListPlugins {

	/**
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static Artifact[] getArtifacts() throws JDOMException, IOException {
		return getListOfDescriptorURLs(getWorkflowComponantDescriptors());
	}

	/**
	 * @param artifactListXml
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	private static Artifact[] getListOfDescriptorURLs(InputStream artifactListXml) throws JDOMException, IOException {

		SAXBuilder builder = new SAXBuilder(); // parameters control validation,
		// etc
		Document doc = builder.build(artifactListXml);

		Set<Artifact> urlsToDescriptors = new HashSet<Artifact>();

		Iterator artifactElements = doc.getDocument().getDescendants(new ElementFilter("artifact"));
		while (artifactElements.hasNext()) {
			Element artifactElement = (Element) artifactElements.next();

			Artifact artifact = new Artifact(artifactElement);

			urlsToDescriptors.add(artifact);
		}
		return urlsToDescriptors.toArray(new Artifact[urlsToDescriptors.size()]);
	}

	/**
	 * @return gets a stream containing the workflowdescriptor xml file
	 */
	private static InputStream getWorkflowComponantDescriptors() {
		try {
			Client client = Client.create();

			WebResource webResource = client.resource(NexusURLs.NEXUS_DATA_INDEX);

			MultivaluedMap queryParams = new MultivaluedMapImpl();
			queryParams.add("c", "workflow-component-description");
			queryParams.add("p", "zip");
			queryParams.add("g", "net.sourceforge.ondex");

			return webResource.queryParams(queryParams).get(InputStream.class);
		} catch (Exception e) {
			OVTK2Desktop.getDesktopResources().getLogger().eventOccurred(new ONDEXEvent(ListPlugins.class, new UnspecifiedErrorEvent(e.toString(), "")));
			return null;
		}
	}

}