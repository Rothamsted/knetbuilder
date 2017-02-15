package net.sourceforge.ondex.statistics.ontologydistance;
/**
 * Stores the final results for one type (BioProc or MolFunc or CelComp)
 * 
 * @author keywan
 * @modified lysenkoa
 */
public class Result{
	private double precision;
	private double recall;
	private double fScore;

	public Result(double precision, double recall, double fScore){
		this.precision = precision;
		this.recall = recall;
		this.fScore = fScore;
	}
	
	public double getFScore() {
		return fScore;
	}

	public double getPrecision() {
		return precision;
	}

	public double getRecall() {
		return recall;
	}
	public String toString(){
		return precision + " " + recall + " " + fScore;
	}
}
