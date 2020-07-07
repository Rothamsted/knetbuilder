package net.sourceforge.ondex.parser.fourdim;

public class MultiVector {
	private double[] vec;
	private String name;
	
	public MultiVector(int length) {
		vec = new double[length];
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void set(int index, double value) {
		vec[index] = value;
	}
	
	public double[] getAll() {
		return vec;
	}
	
	public double get(int i) {
		return vec[i];
	}
	
	public static MultiVector parse(String[] strVec, int first, int last, int name) {
		MultiVector v = new MultiVector(last - (first - 1));
		for (int i = first; i < last; i++) {
			v.set(i-first, Double.parseDouble(strVec[i]));
			v.setName(strVec[name].substring(1, strVec[name].length() - 1));
		}
		return v;
	}
	
	public double euclidNorm() {
		double sum = 0.0;
		for (double d : vec) {
			sum += Math.pow(d, 2.0);
		}
		return Math.sqrt(sum);
	}
	
	public double euclidDistance(MultiVector vector) {
		double[] vec2 = vector.getAll();
		double sum = 0.0;
		for (int i = 0; i < vec.length; i++) {
			sum += Math.pow(vec[i] - vec2[i], 2.0);
		}
		return Math.sqrt(sum);
	}
	
	
}
