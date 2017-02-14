package net.sourceforge.ondex.parser.kegg56;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;

import com.ice.tar.TarEntry;
import com.ice.tar.TarEntryEnumerator;
import com.ice.tar.TarInputStream;

import de.schlichtherle.io.File;
import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.FileOutputStream;
import de.schlichtherle.util.zip.ZipEntry;
import de.schlichtherle.util.zip.ZipFile;
import de.schlichtherle.util.zip.ZipOutputStream;

/**
 * @author hindlem
 */
public class FileIndex {

	private File file;

	private ZipFile zipfile;

	/**
	 * @param file
	 *            a tar, tar.gz, or directory
	 */
	public FileIndex(File file) throws IOException {
		this.file = file;
		if (file.isFile() && !file.getName().endsWith(".zip")) {
			System.out.println("Converting " + file + " to zip format");
			InputStream in;

			String newFile = file.getAbsolutePath();

			if (file.getName().endsWith("tar.gz")) {
				in = new GZIPInputStream(new FileInputStream(file));
				newFile = newFile.substring(0, newFile.length()
						- ".tar.gz".length())
						+ ".zip";
			} else if (file.getName().endsWith(".tar")) {
				in = new FileInputStream(file);
				newFile = newFile.substring(0, newFile.length()
						- ".tar".length())
						+ ".zip";
			} else {
				throw new IOException(
						"file is not in a compatible tar or tar.gz format");
			}

			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
					newFile));
			out.setLevel(Deflater.BEST_COMPRESSION);

			TarInputStream tarStream = new TarInputStream(in);
			tarStream.setBufferDebug(false);
			tarStream.setDebug(false);

			TarEntryEnumerator tarEnum = new TarEntryEnumerator(tarStream);

			byte[] buf = new byte[512];

			while (tarEnum.hasMoreElements()) {
				TarEntry entry = (TarEntry) tarEnum.nextElement();

				if (!entry.getName().endsWith(".gif")
						&& !entry.getName().endsWith(".png")
						&& !entry.getName().endsWith(".html")) {
					ZipEntry zipEntry = new ZipEntry(entry.getName());

					int len;
					out.putNextEntry(zipEntry);
					while ((len = tarStream.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					out.closeEntry();
				}
			}
			in.close();
			out.flush();
			out.close();
			System.out.println("Done Converting " + file
					+ " to zip format new file is at " + newFile);

			// set pointer to newly created file
			this.file = new File(newFile);
			this.zipfile = new ZipFile(newFile);
		} else if (file.isArchive() && file.getName().endsWith(".zip")) {
			this.zipfile = new ZipFile(file);
		}
	}

	/**
	 * @param fileName
	 *            include directory names relative to data resource
	 * @return file in stream form
	 * @throws IOException
	 *             if there is a problem reading the file
	 */
	public InputStream getFile(String fileName) throws IOException {

		if (zipfile != null) {
			ZipEntry entry = zipfile.getEntry(fileName);
			if (entry != null) {
				return zipfile.getInputStream(entry);
			}
		} else if (file.isDirectory()) {
			return new FileInputStream(file.getAbsolutePath() + File.separator
					+ fileName);
		} else {
			throw new IOException(file.getAbsolutePath()
					+ " is not in a compatible form");
		}
		throw new FileNotFoundException(fileName + " is not present in "
				+ file.getName());
	}

	/**
	 * @param regex
	 *            a regular expression to match files with
	 * @param matchOnlyFileName
	 *            if true ignore folders preceding the file name
	 * @return list of files matching the regex
	 * @throws IOException
	 */
	public List<String> getFileNames(Pattern regex, boolean matchOnlyFileName)
			throws IOException {

		if (zipfile != null) {
			List<String> results = new ArrayList<String>();
			for (Enumeration<?> entries = zipfile.entries(); entries
					.hasMoreElements();) {
				ZipEntry zipEntry = (ZipEntry) entries.nextElement();
				if (!matchOnlyFileName
						&& regex.matcher(zipEntry.getName()).find()) {
					results.add(zipEntry.getName());
				} else if (regex
						.matcher(new File(zipEntry.getName()).getName()).find()) {
					results.add(zipEntry.getName());
				}
			}
			return results;
		} else if (file.isDirectory()) {
			return findFilesInDir(regex, matchOnlyFileName, file);
		} else {
			throw new IOException(file.getAbsolutePath()
					+ " is not in a compatible form");
		}
	}

	private List<String> findFilesInDir(Pattern regex,
			boolean matchOnlyFileName, File dir) {
		List<String> results = new ArrayList<String>();
		for (String subFile : dir.list()) {
			File sub_file = new File(subFile);

			String relativePath = sub_file.getAbsolutePath().substring(
					file.getAbsolutePath().length(),
					sub_file.getAbsolutePath().length());

			if (sub_file.isDirectory()) {
				results.addAll(findFilesInDir(regex, matchOnlyFileName,
						sub_file));
			} else if (matchOnlyFileName
					&& regex.matcher(sub_file.getName()).find()) {
				results.add(relativePath);
			} else if (regex.matcher(relativePath).find()) {
				results.add(relativePath);
			}
		}
		return results;
	}

}
