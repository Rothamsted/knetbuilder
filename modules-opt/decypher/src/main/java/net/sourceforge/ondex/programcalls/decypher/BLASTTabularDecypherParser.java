package net.sourceforge.ondex.programcalls.decypher;

import net.sourceforge.ondex.programcalls.Match;
import net.sourceforge.ondex.programcalls.SequenceType;
import net.sourceforge.ondex.programcalls.exceptions.AlgorithmNotSupportedException;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Parsers a Decypher tab delim file
 * @author hindlem
 *
 */
public class BLASTTabularDecypherParser {

	public static boolean DEBUG = false;
	
	private static final Set<String> nonFrameReleventCodes;
	
	static {
		nonFrameReleventCodes = new HashSet<String>();
		nonFrameReleventCodes.add("D");
		nonFrameReleventCodes.add("E");
		nonFrameReleventCodes.add("C");
		
		
		String value = System.getProperty("decypher.debug");
		if (value != null) {
			try {
				DEBUG = Boolean.parseBoolean(value);
			} catch (Exception e) {
				System.err.println("unable to parse decypher.debug from sys variable assuming false");
			}
		}
	}
	
	
	//these are some possible fields see doc for latest
	//see http://decypher/doc/User_Guide_Keyword_Reference.pdf

	public final static String QUERYACCESSION = "QUERYACCESSION";
	public final static String TARGETACCESSION = "TARGETACCESSION";
	public final static String SEARCHID	 = "SEARCHID";
	public final static String RANK	 = "RANK";
	public final static String STATUS = "STATUS";
	public final static String SCORE = "SCORE";	
	public final static String SIGNIFICANCE = "SIGNIFICANCE";	
	public final static String ALGORITHM = "ALGORITHM";	
	public final static String OPENPENALTY = "OPENPENALTY";	
	public final static String EXTENDPENALTY = "EXTENDPENALTY";	
	public final static String FRAMEPENALTY = "FRAMEPENALTY";	
	public final static String SCALEFACTOR = "SCALEFACTOR";	
	public final static String MATRIX = "MATRIX";	
	public final static String QUERYFILE = "QUERYFILE";	
	public final static String QUERYLENGTH = "QUERYLENGTH";	
	public final static String QUERYFRAME = "QUERYFRAME";	
	public final static String QUERYTEXT = "QUERYTEXT";	
	public final static String TARGETFILE = "TARGETFILE";	
	public final static String TARGETLENGTH = "TARGETLENGTH";	
	public final static String TARGETFRAME = "TARGETFRAME";	
	public final static String TARGETLOCUS = "TARGETLOCUS";	
	public final static String QUERYLOCUS = "QUERYLOCUS";	
	public final static String TARGETDESCRIPTION = "TARGETDESCRIPTION";	
	public final static String PERCENTSCORE = "PERCENTSCORE";	
	public final static String MAXSCORE = "MAXSCORE";	
	public final static String MAXSCORED = "MAXSCORED";	
	public final static String MAXSCOREC = "MAXSCOREC";	
	public final static String MAXSCORE1 = "MAXSCORE1";	
	public final static String MAXSCORE2 = "MAXSCORE2";	
	public final static String MAXSCORE3 = "MAXSCORE3";	
	public final static String MAXSCOREN1 = "MAXSCOREN1";
	public final static String MAXSCOREN2 = "MAXSCOREN2";
	public final static String MAXSCOREN3 = "MAXSCOREN3";	
	public final static String MATCHES = "MATCHES";	
	public final static String SIMILARITIES = "SIMILARITIES";	
	public final static String GAPS = "GAPS";	
	public final static String PERCENTALIGNMENT = "PERCENTALIGNMENT";	
	public final static String PERCENTQUERY = "PERCENTQUERY";	
	public final static String PERCENTTARGET = "PERCENTTARGET";	
	public final static String SIMPERCENTALIGNMENT = "SIMPERCENTALIGNMENT";	
	public final static String SIMPERCENTQUERY = "SIMPERCENTQUERY";	
	public final static String SIMPERCENTTARGET = "SIMPERCENTTARGET";	
	public final static String QUERYSTART = "QUERYSTART";	
	public final static String QUERYEND = "QUERYEND";	
	public final static String TARGETSTART = "TARGETSTART";	
	public final static String TARGETEND = "TARGETEND";	
	public final static String QUERYNT = "QUERYNT";	
	public final static String QUERYAA = "QUERYAA";	
	public final static String COMPARISON = "COMPARISON";	
	public final static String TARGETNT = "TARGETNT";	
	public final static String TARGETAA = "TARGETAA";
	public final static String QUERYNUMBER = "QUERYNUMBER";
	public final static String ALIGNMENTLENGTH = "ALIGNMENTLENGTH";	

	private final static Pattern tabDelim = Pattern.compile("[\t]");
	private final static Pattern dotPattern = Pattern.compile("[\\.]");

	/**
	 * 
	 * @param file
	 * @param cutOff
	 * @param overlap
	 * @param blastType DecypherAlignment algorithm see static strings
	 * @param gzip
	 * @return
	 * @throws IOException
	 * @throws AlgorithmNotSupportedException  blastType is not supported
	 */
	public Collection<Match> parseMap(File file, int cutOff, float overlap,
			String blastType, boolean gzip) throws IOException, AlgorithmNotSupportedException {
		Collection<Match> results;
		if (!gzip) results = parseMap(new BufferedReader(new FileReader(file)), cutOff,overlap, blastType);
		else results = parseMap(new BufferedReader(new InputStreamReader(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file), 512*2)))), cutOff,overlap, blastType);
		file.delete();
		return results;
	}

	/**
	 * 
	 * @param br
	 * @param cutOff
	 * @param overlap
	 * @param blastType
	 * @return
	 * @throws IOException
	 * @throws AlgorithmNotSupportedException blastType is not supported
	 */
	public Collection<Match> parseMap(BufferedReader br, int cutOff, float overlap,
			String blastType) throws IOException, AlgorithmNotSupportedException {

		//queryid to HashMap
		List<Match> matches = new ArrayList<Match>(50000);

		Map<String, Integer> headerIndex = new HashMap<String, Integer>();
		String line = br.readLine();
		
		if (line == null) {
			throw new RuntimeException("The returned Decypher file was empty");
		}
		
		if (DEBUG) System.out.println(line);
		
		String[] headers = tabDelim.split(line);
		for (int i = 0; i < headers.length; i++) {
			String header = headers[i];
			headerIndex.put(header, i);
		}

		SequenceType queryType = DecypherAlignment.getSequenceTypeQuery(blastType);
		SequenceType targetType = DecypherAlignment.getSequenceTypeTarget(blastType);

		int scoreIndex = headerIndex.get(SCORE);
		int significanceIndex = headerIndex.get(SIGNIFICANCE);
		int alignmentLength = headerIndex.get(ALIGNMENTLENGTH);
		int queryFrame = headerIndex.get(QUERYFRAME);
		int targetFrame = headerIndex.get(TARGETFRAME);
		int queryLength = headerIndex.get(QUERYLENGTH);
		int targetLength = headerIndex.get(TARGETLENGTH);
		int queryId = headerIndex.get(QUERYTEXT);
		int targetId = headerIndex.get(TARGETLOCUS);

		int args = requiredFields.length;

		int lineCount = 0;

		while(br.ready()) {
			line = br.readLine();
			if (DEBUG) System.out.println(line);
			if (line.length() == 0 || line.charAt(0) == '\t') { //indicates target sequence is missing
				continue;
			}

			String[] values = tabDelim.split(line);
			if (values.length != args) {
				System.err.println("wrong number of arguments in line: "+lineCount+" :"+line);
				continue;
			}

			try {
				
				String[] query = dotPattern.split(values[queryId].trim());
				String[] target = dotPattern.split(values[targetId].trim());

				if (query.length < 2 || target.length < 2) {
					System.err.println("query and/or target in wrong form q:"+values[queryId]+" t:"+values[targetId]);
					continue;
				}
				
				String queryStringId = query[0].trim();;
				String queryTaxStringId = query[1].trim();
				String queryAttType = query[2].intern();
				
				int queryid = Integer.parseInt(queryStringId);
				int queryTaxId = -1;
				if (queryTaxStringId.charAt(0) != DecypherAlignment.EMPTY_TAXID) {
					queryTaxId = Integer.parseInt(queryTaxStringId);
				}
				
				String targetStringId = target[0].trim();;
				String targetTaxStringId = target[1].trim();
				String targetAttType = query[2].intern();;
				
				int targetid = Integer.parseInt(targetStringId);
				int targetTaxId = -1;
				if (targetTaxStringId.charAt(0) != DecypherAlignment.EMPTY_TAXID) {
					targetTaxId = Integer.parseInt(targetTaxStringId);
				}

				lineCount++;

				int lengthMatch = Integer.parseInt(values[alignmentLength]);
				int lengthQuery = Integer.parseInt(values[queryLength]);
				int lengthTarget = Integer.parseInt(values[targetLength]);
				double score = Double.parseDouble(values[scoreIndex]);
				double eValue = Double.parseDouble(values[significanceIndex]);

				//adjust to untranslated lengths...as this is the standard form for NameMatch
				if(!queryType.equals(targetType)) {
					if(queryType.equals(SequenceType.AA)
							&& targetType.equals(SequenceType.NA)){
						lengthTarget = lengthTarget*3; //make it NA size.
					} else {
						lengthQuery = lengthQuery*3; //make it NA size.
					}
					lengthMatch = lengthMatch*3; //adjust the alignment as well
				}

				Match m = new Match(queryid, queryTaxId,
						targetid, targetTaxId,
						score, eValue,
						lengthQuery, lengthTarget, lengthMatch, 
						queryType, targetType);
				m.setQueryType(queryAttType);
				m.setTargetType(targetAttType);

				if (!(lengthMatch < cutOff)) {
					if(!(m.getCoverageSmallestSequence() < overlap)){
						matches.add(m);
					} else {
						continue; //no need to parse the rest..requirements not met
					}
				} else {
					continue; //no need to parse the rest..requirements not met
				}

				if (values[targetFrame].trim().length() > 0) {
					if (values[targetFrame].length() > 1 
							|| (!nonFrameReleventCodes.contains(values[targetFrame].toUpperCase()))) {
						try {
							int targetFrameVal = Integer.parseInt(values[targetFrame]);
							m.setTargetFrame(targetFrameVal);
						} catch (NumberFormatException e) {
							//ignore its another char
						}
					}
				}

				if (values[queryFrame].trim().length() > 0) {
					if (values[queryFrame].length() > 1 
							||(!nonFrameReleventCodes.contains(values[targetFrame].toUpperCase()))) {
						try {
							int queryFrameVal = Integer.parseInt(values[queryFrame]);
							m.setQueryFrame(queryFrameVal);
						} catch (NumberFormatException e) {
							//ignore its another char
						}
					}
				}

			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println("errorAt-->"+line);
				System.err.println(values);
				e.printStackTrace();
			}
		}
		System.out.println("Total hits "+lineCount+" of which met conditions "+matches.size());
		return matches;
	}

	private final static String[] requiredFields = new String[] {
		TARGETLOCUS,
		QUERYTEXT,
		SCORE, 
		SIGNIFICANCE, 
		ALIGNMENTLENGTH, 
		QUERYFRAME, 
		TARGETFRAME, 
		QUERYLENGTH, 
		TARGETLENGTH 
	};

	public static String[] getRequiredFields() {
		return requiredFields;
	}
}
