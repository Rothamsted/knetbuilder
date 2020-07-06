package net.sourceforge.ondex.statistics.ontologydistance;
/**
 * Stores the final results for all, molFunc, bioProc, celComp type
 * 
 * @author keywan
 * @modified lysenkoa
 *
 */
class StatisticResult extends Result{

	private Result molFunc;
	private Result bioProc;
	private Result celComp;
	public StatisticResult(double precision, double recall, double fScore) {
		super(precision, recall, fScore);
	}
	public Result getBioProc() {
		return bioProc;
	}
	public void setBioProc(Result bioProc) {
		this.bioProc = bioProc;
	}
	public Result getCelComp() {
		return celComp;
	}
	public void setCelComp(Result celComp) {
		this.celComp = celComp;
	}
	public Result getMolFunc() {
		return molFunc;
	}
	public void setMolFunc(Result molFunc) {
		this.molFunc = molFunc;
	}
	public String toString(){
		return "Over all\t" + super.toString() + "\n" +
		"Bio Proc\t" + bioProc.toString() + "\n" + 
		"Mol Func\t" +molFunc.toString() + "\n" + 
		"Cel Comp\t" +celComp.toString();  
	}
	
}
