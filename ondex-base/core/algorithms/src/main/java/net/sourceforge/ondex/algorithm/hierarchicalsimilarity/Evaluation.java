package net.sourceforge.ondex.algorithm.hierarchicalsimilarity;

/**
 * Data structure to store precision, recall and F-score
 * 
 * @author keywan
 * @modified lysenkoa
 */
public class Evaluation{
	
	private double precision;
	private double recall;
	private double score;
	private double truePositives;
	private double allPredictionsMade;
	private double allTrueCasesInRefSet;
	
	
	public Evaluation(double precision, double recall, double score) {
		this.precision = precision;
		this.recall = recall;
		this.score = score;
	}
	
	public Evaluation(double precision, double recall, double score, double truePositives, double allPredictionsMade, double allTrueCasesInRefSet) {
		this.precision = precision;
		this.recall = recall;
		this.score = score;
		this.truePositives = truePositives;
		this.allPredictionsMade = allPredictionsMade;
		this.allTrueCasesInRefSet = allTrueCasesInRefSet;
	}
	
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public double getPrecision() {
		return precision;
	}
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	public double getRecall() {
		return recall;
	}
	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getTruePositives() {
		return truePositives;
	}

	public void setTruePositives(double truePositives) {
		this.truePositives = truePositives;
	}

	public double getAllPredictionsMade() {
		return allPredictionsMade;
	}

	public void setAllPredictionsMade(double allPredictionsMade) {
		this.allPredictionsMade = allPredictionsMade;
	}

	public double getAllTrueCasesInRefSet() {
		return allTrueCasesInRefSet;
	}

	public void setAllTrueCasesInRefSet(double allTrueCasesInRefSet) {
		this.allTrueCasesInRefSet = allTrueCasesInRefSet;
	}
}
