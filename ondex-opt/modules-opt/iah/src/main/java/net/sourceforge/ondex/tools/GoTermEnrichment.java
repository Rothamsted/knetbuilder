package net.sourceforge.ondex.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import net.sourceforge.ondex.algorithm.annotationquality.TermEnrichment;
import net.sourceforge.ondex.algorithm.annotationquality.TermEnrichment.TermScore;

public class GoTermEnrichment {
	public static void main(String[] args) {
		try {
			TermEnrichment te = new TermEnrichment(args[0], args[1], Integer.parseInt(args[2]), args[3]);
			
			HashMap<String,String> aliases = parseAliases(args[4]);
			
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				System.out.print("> ");
				String cmd = r.readLine();
				if (cmd.equals("exit") || cmd.equals("quit")) {
					System.exit(0);
				}
				else if (cmd.startsWith("gte ")) {
					String[] cols = cmd.split(" ");
					ArrayList<String> query = new ArrayList<String>();
					for (int i = 1; i < cols.length; i++) {
						String gene = aliases.get(cols[i].toUpperCase());
						if (gene != null) {
							query.add(gene);
						} else {
							System.out.println("ignoring unknown gene "+cols[i].toUpperCase());
						}
					}
					TermScore[] result = te.getEnrichedTerms(query.toArray(new String[query.size()]), 2, 2.0);
					System.out.println("^Term^Frequency^IC^Score^");
					for (int i = result.length -1 ; i >= 0; i--) {
						TermScore score = result[i];
						double completeness = (double)score.getCount() / (double)query.size();
						System.out.println("|"+score.getTerm()+"|"+completeness
								+"|"+score.getIc()+"|"+completeness * score.getIc()+"|");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static HashMap<String,String> parseAliases(String fileName) throws IOException {
		HashMap<String,String> aliases = new HashMap<String,String>();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;
		while ((line = br.readLine()) != null) {
			String[] cols = line.split("\t");
			if (cols.length >= 6) {
				String gName = cols[0];
				String yName = cols[5];
				String sName = cols[6];
				if (!sName.equals("")) {
					if (!gName.equals("")) {
						aliases.put(gName, sName);
					}
					if (!yName.equals("")) {
						aliases.put(yName, sName);
					}
				}
			}
		}
		br.close();
		return aliases;
	}
}
