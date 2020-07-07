package net.sourceforge.ondex.parser.gramene.genes;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.gramene.MetaData;

/**
 * INDEX for expected data table
 * 0 `dbxref_id` int(11) NOT NULL default '0' PRIMARY KEY,
 * 1 `dbxref_name` varchar(255) NOT NULL default '',
 * 2 `url` text NOT NULL
 * 
 * @author hoekmanb
 *
 */
public class GeneXRefDB {

	private Map<Integer, DataSource> dataSourceMapped;

	public GeneXRefDB(String xrefFile, ONDEXGraph graph) {

		dataSourceMapped = new HashMap<Integer, DataSource>();
		try {
			BufferedReader input = new BufferedReader(
					new InputStreamReader(
							new FileInputStream(xrefFile),"UTF8"));
			Pattern tabPattern = Pattern.compile("\t");
			
			while (input.ready()) {
				String inputLine = input.readLine();
				String[] columns = tabPattern.split(inputLine);

				int internalId = Integer.parseInt(columns[0]);

				setIdToCVMap(internalId, columns[1], graph);
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setIdToCVMap(int dbxrefId, String dbxrefName,
			ONDEXGraph graph) {

		String localCv = MetaData.getMapping(dbxrefName);
		if (localCv != null) {
			DataSource dataSource = graph.getMetaData().getDataSource(localCv);
			if(dataSource == null) {
				System.err.println("INTERNAL ERROR not found DataSource: " + MetaData.uniprot);
				}
			dataSourceMapped.put(dbxrefId, dataSource);
		} else {
			System.out.println("Database is unknown "+dbxrefName);
		}

	}

	public DataSource getDataSourceForID(int dbxrefId) {

		if (dataSourceMapped.containsKey(dbxrefId)) {
			return dataSourceMapped.get(dbxrefId);
		} else {
			return null;
		}
	}
}
