package net.sourceforge.ondex.ovtk2.modules;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

/**
 * @author hindlem
 */
public class FetchPluginDescription {

	private TransformerFactory tFactory = TransformerFactory.newInstance();

	public FetchPluginDescription() {
		System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
		System.setProperty("javax.xml.stream.XMLOutputFactory", "com.ctc.wstx.stax.WstxOutputFactory");
		System.setProperty("javax.xml.stream.XMLEventFactory", "com.ctc.wstx.stax.WstxEventFactory");
	}

	public InputStream getDocumentation(Artifact artifact) throws TransformerException, IOException {
		String zipArtifact = NexusURLs.NEXUS_SERVICE + "/local/artifact/maven/redirect?" + artifact.getNexusOptions();
		return getDescriptorXml(zipArtifact, true);
	}

	public InputStream getXML(Artifact artifact) throws IOException {
		String zipArtifact = NexusURLs.NEXUS_SERVICE + "/local/artifact/maven/redirect?" + artifact.getNexusOptions();
		try {
			return getDescriptorXml(zipArtifact, false);
		} catch (TransformerException e) {
			throw new IOException(e);
		}
	}

	private InputStream getDescriptorXml(String zipFile, boolean transformXsl) throws TransformerException, IOException {

		URL zipFileUrl = new URL(zipFile);
		ZipInputStream zis = new ZipInputStream(zipFileUrl.openStream());
		ZipEntry entry = null;

		byte[] xmlFile = null;

		Transformer transformer = null;

		while ((entry = zis.getNextEntry()) != null) {
			if (!entry.isDirectory() && entry.getName().equals("workflow-component-description.xml") || (entry.getName().endsWith("workflow-component-description.xsl") && transformXsl)) {

				long size = entry.getCompressedSize();
				if (size < 0)
					size = 32;
				ByteArrayOutputStream fout = new ByteArrayOutputStream((int) size);

				for (int c = zis.read(); c != -1; c = zis.read()) {
					fout.write(c);
				}
				zis.closeEntry();
				fout.close();

				if (entry.getName().endsWith(".xml")) {
					xmlFile = fout.toByteArray();
					if (!transformXsl) {
						zis.close();
						return new ByteArrayInputStream(xmlFile);
					}
				} else if (entry.getName().endsWith(".xsl") && transformXsl) {
					transformer = tFactory.newTransformer(new StreamSource(new ByteArrayInputStream(fout.toByteArray())));
				}
			}
		}
		zis.close();

		if (xmlFile == null) {
			throw new IOException("missing workflow-component-description.xml from " + zipFile);
		}

		if ((transformXsl && transformer != null)) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			transformer.transform(new StreamSource(new ByteArrayInputStream(xmlFile)), new javax.xml.transform.stream.StreamResult(bos));

			return new ByteArrayInputStream(bos.toByteArray());
		} else {
			return new ByteArrayInputStream(xmlFile);
		}
	}

}