package net.sourceforge.ondex.ovtk2.modules;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author hindlme
 */
public class SavePlugin {

	/**
	 * Downloads a given nexus maven artifact jar-with-dependencies
	 * 
	 * @param artifact
	 *            maven artifact to download
	 * @param pluginDir
	 *            dir to download artifact to
	 * @throws IOException
	 *             error in downloading file
	 */
	public static void downloadPlugin(Artifact artifact, File pluginDir) throws IOException {
		if (!pluginDir.isDirectory())
			throw new IOException(pluginDir.getAbsolutePath() + ": is not a valid directory");

		URL url = new URL(NexusURLs.NEXUS_SERVICE + "/local/artifact/maven/redirect?" + artifact.getNexusOptions().replaceAll("&", "&amp;").replaceAll("workflow-component-description", "jar-with-dependencies").replaceAll("p=zip", "p=jar"));// TODO
																																																												// Dirty
																																																												// hack
																																																												// fix
																																																												// later
		URLConnection uc = url.openConnection();
		String contentType = uc.getContentType();
		int contentLength = uc.getContentLength();
		if (contentType.startsWith("text/") || contentLength == -1) {
			throw new IOException("Expected binary file, found text!");
		}
		InputStream rawinStream = url.openStream();

		InputStream in = new BufferedInputStream(rawinStream);
		byte[] data = new byte[contentLength];
		int bytesRead = 0;
		int offset = 0;
		while (offset < contentLength) {
			bytesRead = in.read(data, offset, data.length - offset);
			if (bytesRead == -1)
				break;
			offset += bytesRead;
		}
		in.close();

		if (offset != contentLength) {
			throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
		}

		String filename = url.getFile().substring(url.getFile().lastIndexOf('/') + 1);
		FileOutputStream out = new FileOutputStream(pluginDir.getAbsoluteFile().getAbsolutePath() + File.separator + filename);
		out.write(data);
		out.flush();
		out.close();
	}

}
