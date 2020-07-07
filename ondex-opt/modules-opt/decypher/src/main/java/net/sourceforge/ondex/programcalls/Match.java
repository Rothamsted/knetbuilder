package net.sourceforge.ondex.programcalls;



/**
 * Contains id and score of the two matching sequences. 
 * 
 * @author taubertj, hindlem
 */
public class Match {

	private final int queryId;
	private final int queryTaxId;
	private final int targetId;
	private final int targetTaxId;
	private final double score;
	private final double eValue;
	
	private final int lengthOfQuerySequence;
	private final int lengthOfTargetSequence;
	
	private final SequenceType querySeqType;
	private final SequenceType targetSeqType;
	private final int overlapingLength;

	private Integer targetFrame;
	private Integer queryFrame;
	
	private String queryType;
	private String targetType;
	
	/**
	 * 
	 * @param queryId
	 * @param queryTaxId
	 * @param targetId
	 * @param targetTaxId
	 * @param score
	 * @param eValue
	 * @param lengthOfQuerySequence
	 * @param lengthOfTargetSequence
	 * @param overlapingLength
	 * @param querySeqType
	 * @param targetSeqType
	 */
	public Match(int queryId, 
			int queryTaxId, 
			int targetId, 
			int targetTaxId, 
			double score,
			double eValue,
			int lengthOfQuerySequence,
			int lengthOfTargetSequence,
			int overlapingLength,
			SequenceType querySeqType,
			SequenceType targetSeqType) {
		this.queryId = queryId;
		this.queryTaxId = queryTaxId;
		this.targetId = targetId;
		this.targetTaxId = targetTaxId;
		this.score = score;
		this.eValue = eValue;
		this.lengthOfQuerySequence = lengthOfQuerySequence;
		this.lengthOfTargetSequence = lengthOfTargetSequence;
		this.overlapingLength = overlapingLength;
		this.querySeqType = querySeqType;
		this.targetSeqType = targetSeqType;
	}

	public static Match createSelfMatch(int id, int taxID, int seqLength, SequenceType seqType) {
		Match match = new Match(id, taxID, id, taxID, Float.MAX_VALUE, 0, seqLength, seqLength, seqLength, seqType, seqType);
		return match;
	}
	
	/**
	 * This returns the coverage in view of the longest sequence.
	 * 
	 * @return coverage
	 */
	public float getCoverageLongestSequence() {
		int tempLengthQuery = lengthOfQuerySequence;
		int tempLengthTarget = lengthOfTargetSequence;
    	if(querySeqType != targetSeqType){
    	    if(querySeqType.equals(SequenceType.AA) && targetSeqType.equals(SequenceType.NA)){
    	    	tempLengthQuery = tempLengthQuery*3; //make it NA size.
    	    } else {
    	    	tempLengthTarget = tempLengthTarget*3; //make it NA size.
    	    }
    	}

    	float coverage;
		if (tempLengthQuery > tempLengthTarget) {
			coverage = (float) overlapingLength/ (float) tempLengthQuery;
		} else {
			coverage = (float) overlapingLength / (float) tempLengthTarget; 
		}
		return coverage;
	}
	
	/**
	 * This returns the coverage in view of the shortest sequence.
	 * 
	 * This is especially handy for EST sequence as they are by default not of maximum length.
	 * 
	 * @return coverage
	 */
	public float getCoverageSmallestSequence() {
	    	int tempLengthQuery = lengthOfQuerySequence;
	    	int tempLengthTarget = lengthOfTargetSequence;
	    	if(querySeqType != targetSeqType){
	    	    if(querySeqType.equals(SequenceType.AA) && targetSeqType.equals(SequenceType.NA)){
	    	    	tempLengthQuery = tempLengthQuery*3; //make it NA size.
	    	    } else {
	    	    	tempLengthTarget = tempLengthTarget*3; //make it NA size.
	    	    }
	    	}
	    
	    	float coverage;
		if (tempLengthQuery < tempLengthTarget) {
		    	coverage = (float) overlapingLength/ (float) tempLengthQuery;
		} else {
		    	coverage = (float) overlapingLength / (float) tempLengthTarget; 
		}
		return coverage;
	}
	
	/**
	 * This returns the coverage in view of the query sequence.
	 * 
	 * @return coverage
	 */
	public float geQueryCoverageSequence() {
    	int tempLengthQuery = lengthOfQuerySequence;
    	if(querySeqType != targetSeqType){
    	    if(querySeqType.equals(SequenceType.AA) && targetSeqType.equals(SequenceType.NA)){
    	    	tempLengthQuery = tempLengthQuery*3; //make it NA size.
    	    } 
    	}
    	return (float) overlapingLength / (float) tempLengthQuery; 
	}
	
	/**
	 * This returns the coverage in view of the target sequence.
	 * 
	 * @return coverage
	 */
	public float geTargetCoverageSequence() {
    	int tempLengthQuery = lengthOfTargetSequence;
    	if(targetSeqType != querySeqType){
    	    if(targetSeqType.equals(SequenceType.AA) && querySeqType.equals(SequenceType.NA)){
    	    	tempLengthQuery = tempLengthQuery*3; //make it NA size.
    	    } 
    	}
    	return (float) overlapingLength / (float) tempLengthQuery; 
	}

	/**
	 * 
	 * @return
	 */
	public int getOverlapingLength() {
		return overlapingLength;
	}

	
	public int getQueryId() {
		return queryId;
	}

	public double getScore() {
		return score;
	}

	public int getTargetId() {
		return targetId;
	}

	public int getQueryTaxId() {
		return queryTaxId;
	}

	public int getTargetTaxId() {
		return targetTaxId;
	}

	public double getEValue() {
		return eValue;
	}

	public Integer getTargetFrame() {
		return targetFrame;
	}

	public void setTargetFrame(Integer targetFrame) {
		this.targetFrame = targetFrame;
	}

	public Integer getQueryFrame() {
		return queryFrame;
	}

	public void setQueryFrame(Integer queryFrame) {
		this.queryFrame = queryFrame;
	}

	public int getLengthOfQuerySequence() {
		return lengthOfQuerySequence;
	}
	
	public int getLengthOfTargetSequence() {
		return lengthOfTargetSequence;
	}
	
	/**
	 * 
	 * @return the AttributeName id for the query sequence
	 */
	public String getQueryType() {
		return queryType;
	}

	/**
	 * 
	 * @param queryType the AttributeName id for the query sequence
	 */
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}

	/**
	 * 
	 * @return the AttributeName id for the target sequence
	 */
	public String getTargetType() {
		return targetType;
	}

	/**
	 * 
	 * @param targetType id for the target sequence
	 */
	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}
}
