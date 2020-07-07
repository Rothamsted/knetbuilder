package net.sourceforge.ondex.parser.tair.protein;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.parser.tair.MetaData;
import net.sourceforge.ondex.parser.tair.Parser;

/**
 * 
 * This class needs the Proteins folder of the Tair website. These files are parsed:
 * ftp://ftp.arabidopsis.org/home/tair/Proteins/Id_conversions/AGI2Uniprot.20080418
 * ftp://ftp.arabidopsis.org/home/tair/Proteins/Domains/TAIR8_all.domains
 * 
 * @author hoekmanb
 * 
 */
public class ParseProtein {

	/**
	 * adds UNIPROT accession to the proteins ans corresponding IntrPro concepts
	 * 
	 * @param tairReleaseDir
	 * @param graph
	 * @param existingProteins
	 */
	public void parse(String tairReleaseDir, ONDEXGraph graph, Map<String,ONDEXConcept> existingProteins) {


		ParseMappingFile parseProteinMapping = new ParseMappingFile();
		DataSource uniprot = graph.getMetaData().getDataSource(MetaData.UNIPROTKB);	
		//DataSource nc_np = graph.getMetaData(s).getCV(s,MetaData.ncNP);
		
		
		String mappingFileUniProt2AGI = "";
		
		File dir = new File(tairReleaseDir+"Proteins/Id_conversions/");
		if(!dir.isDirectory()){
			System.err.println(dir.getAbsolutePath()+" is not a directory!");
		}
		String[] files = dir.list();
		
		for (String file: files) {
			if (file.startsWith("Uniprot2AGI")) {
				mappingFileUniProt2AGI = tairReleaseDir+"Proteins/Id_conversions"+File.separator+file;
			}
		}
		
		try {
			System.out.println("Parsing "+mappingFileUniProt2AGI);
			parseProteinMapping.parseMappingFile(mappingFileUniProt2AGI, graph, existingProteins,uniprot);
		} catch (FileNotFoundException e) {
			System.out.println("File does not exist: "+mappingFileUniProt2AGI);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String protDomains = tairReleaseDir+"Proteins"+File.separator+"Domains"+File.separator+Parser.TAIRX+"_all.domains";
		ParseDomains parseDomains = new ParseDomains();
		try {
			System.out.println("Parsing "+protDomains);
			parseDomains.parseDomains(protDomains, graph, existingProteins);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
