package net.sourceforge.ondex.programcalls;

public class HMMMatch implements Comparable<HMMMatch> {

    public static final int SEQTYPE_NA = 0;
    public static final int SEQTYPE_AA = 1;

    private final String hmmAccession;
    private final int targetId;
    private final int targetTaxId;
    private final double score;
    private final double eValue;

    private final SequenceType targetSeqType;

    private final int alignmentLength;

    private Integer targetFrame;
    private Integer queryFrame;

    private Double bestDomainEvalue;
    private Double bestDomainScore;

    /**
     * @param hmmAccession
     * @param targetId
     * @param targetTaxId
     * @param score
     * @param eValue
     * @param alignmentLength
     * @param targetSeqType
     */
    public HMMMatch(String hmmAccession,
                    int targetId,
                    int targetTaxId,
                    double score,
                    double eValue,
                    int alignmentLength,
                    SequenceType targetSeqType) {
        this.hmmAccession = hmmAccession.intern();
        this.alignmentLength = alignmentLength;
        this.targetId = targetId;
        this.targetTaxId = targetTaxId;
        this.score = score;
        this.eValue = eValue;
        this.targetSeqType = targetSeqType;
    }

    public static Match createSelfMatch(int id, int taxID, int seqLength, SequenceType seqType) {
        Match match = new Match(id, taxID, id, taxID, Float.MAX_VALUE, 0, seqLength, seqLength, seqLength, seqType, seqType);
        return match;
    }


    public double getScore() {
        return score;
    }

    public int getTargetId() {
        return targetId;
    }

    public int getTargetTaxId() {
        return targetTaxId;
    }

    public double getEValue() {
        return eValue;
    }

    /**
     * Compare Matches according to their score.
     *
     * @param o - Object
     * @return int
     */
    public int compareTo(HMMMatch o) {
        return (score < o.score ? -1 : (score == o.score ? 0 : 1));
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

    public String getHmmAccession() {
        return hmmAccession;
    }

    public SequenceType getTargetSeqType() {
        return targetSeqType;
    }

    public int getAlignmentLength() {
        return alignmentLength;
    }

    public Double getBestDomainEvalue() {
        return bestDomainEvalue;
    }

    public void setBestDomainEvalue(Double bestDomainEvalue) {
        this.bestDomainEvalue = bestDomainEvalue;
    }

    public Double getBestDomainScore() {
        return bestDomainScore;
    }

    public void setBestDomainScore(Double bestDomainScore) {
        this.bestDomainScore = bestDomainScore;
    }
}
