package net.sourceforge.ondex.ovtk2.annotator.timeseries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import junit.framework.TestCase;

public class TimeSeriesDataFileWriter extends TestCase {

	private static final String CULTIVAR = "CULTIVAR";

	public void testWriteFile() {
		try {
			File temp = File.createTempFile("tmp", ".file");
			temp.deleteOnExit();
			writeExampleFile(temp.getAbsolutePath(), 5, 3, new String[] { "a", "b", "c", "d" });
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeExampleFile(String filename, int timepoints, int treatments, String[] geneElements) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

			HashSet<String> cultivars = new HashSet<String>();
			for (int i = 0; i < treatments; i++) {
				cultivars.add(CULTIVAR + i);
			}

			bw.newLine();
			bw.newLine();
			bw.newLine();
			bw.write("Long dull title");
			bw.newLine();

			Iterator<String> cultIt = cultivars.iterator();
			while (cultIt.hasNext()) {
				String cultivar = cultIt.next();
				for (int i = 0; i <= timepoints; i++) {
					bw.write("\t" + cultivar);
				}
			}
			bw.write("\tLSD\tLSD");
			bw.newLine();

			for (int i = 0; i < cultivars.size(); i++) {
				for (int j = 0; j < timepoints; j++) {
					bw.write("\t" + j);
				}
			}
			bw.write("\t5%\t10%");
			bw.newLine();

			Random rand = new Random();

			for (int i = 0; i < geneElements.length; i++) {
				String gene = geneElements[i];
				bw.write(gene);
				for (int j = 0; j < cultivars.size(); j++) {
					for (int k = 0; k < timepoints; k++) {
						bw.write("\t" + rand.nextDouble());
					}
				}
				bw.write("\t" + rand.nextDouble());
				bw.write("\t" + rand.nextDouble());
				bw.newLine();
			}
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
