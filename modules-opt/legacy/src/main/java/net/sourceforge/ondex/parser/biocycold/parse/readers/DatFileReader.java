package net.sourceforge.ondex.parser.biocycold.parse.readers;

import java.io.FileNotFoundException;

import net.sourceforge.ondex.parser.biocycold.parse.IParser;

/**
 * Reads a file in a "KEY - VALUE" format.
 * @author peschr
 *
 */
public class DatFileReader extends AbstractReader {
	private static String delimiter = " - ";
	public DatFileReader(String fileName, IParser listener) throws FileNotFoundException{
		super(fileName,listener);
	}
	/**
	 * reads the next entry in a dat file
	 */
	public boolean hasNext() {
		String line;
		try{
			boolean moreThanOneLine = false;
			String value = "";
			String key = "";
			while( ((line = buffReader.readLine()) != null)){
				// comments and blank lines (occurs exactly one time in the pathway.dat) are skipped
				if (line.startsWith("#") || line.equals(" ")) continue;
				//entrie read
				if ( line.startsWith("//"))return true;
				//the slash signalise that the value is splitted in lines
				else if ( line.startsWith("/") ){
					//the max size of a string is 2^16
					if ( value.length() + line.length() < 65536)
						value += line.substring(1);
					moreThanOneLine = true;
				}else {
					//the first occurrence of the delimiter
					int middle = line.indexOf(delimiter);
					if (middle != -1){
						if(moreThanOneLine == true)
							moreThanOneLine = false;
						else{
							//from the start to the position of the delimiter
							key = line.substring(0,middle).trim();
							//the rest
							value = line.substring(middle + delimiter.length(), line.length()).trim();
						}
						parser.distributeCore(key, value);
					}
				}
			}
		}catch(Exception e){ 	
			e.printStackTrace();
		}
		//no more entries
		return false;
	}
}
