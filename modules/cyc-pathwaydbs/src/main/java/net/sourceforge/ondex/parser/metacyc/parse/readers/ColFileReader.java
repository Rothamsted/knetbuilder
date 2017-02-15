package net.sourceforge.ondex.parser.metacyc.parse.readers;

import java.util.ArrayList;
import java.util.StringTokenizer;

import net.sourceforge.ondex.parser.metacyc.parse.IParser;

/**
 * Reads a column seperated file. In the first step it analyzed the column
 * description line. The file format usally looks like;
 * description1	description2	description3
 * data1	data2	data3
 * @author peschr
 * 
 */
public class ColFileReader extends AbstractReader {

	private ArrayList<String> values = new ArrayList<String>();

	public ColFileReader(String fileName, IParser listener) throws Exception {
		super(fileName, listener);
		this.getColumns();
	}
	/*
	 * 
	 */
	private void getColumns() throws Exception {
		String line;
		while ((line = super.buffReader.readLine()) != null) {
			if (!line.startsWith("#")) {
				StringTokenizer st = new StringTokenizer(line, "\t", false);
				while (st.hasMoreTokens()) {
					values.add(st.nextToken());
				}
				return;
			}
		}
		throw new Exception("no column description");
	}
	public boolean hasNext() {
		String line;
		int position = 0;
		try {
			if ((line = buffReader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, "\t", true);
				position = 0;
				boolean firstSkip = true;
				while (st.hasMoreTokens()) {
					String value = st.nextToken();
					if (value.equals("\t")) {
						if (firstSkip == false)
							position++;
						firstSkip = false;
						continue;
					} else {
						firstSkip = true;
						String key = values.get(position);
						parser.distributeCore(key, value);
						position++;
					}
				}
			} else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

}
