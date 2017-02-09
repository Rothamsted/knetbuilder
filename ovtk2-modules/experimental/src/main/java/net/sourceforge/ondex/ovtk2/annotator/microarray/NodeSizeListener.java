package net.sourceforge.ondex.ovtk2.annotator.microarray;

import javax.swing.JFormattedTextField;

/**
 * A node listener for 2 Int JFormattedTextField that forces a min and a max and controls access to minField and maxField
 * @author hindlem
 *
 */
public class NodeSizeListener {

	private JFormattedTextField minField;
	private JFormattedTextField maxField;

	private int absoluteMin;
	private int absoluteMax;

	private int minSizeOfNode = 5;
	private int maxSizeOfNode = 35;

	/**
	 * Adds listeners to the given fields and enforces min and max
	 * requirements
	 * 
	 * @param minField
	 * @param maxField
	 * @param absoluteMin
	 * @param absoluteMax
	 */
	public NodeSizeListener(JFormattedTextField minField,
			JFormattedTextField maxField, int absoluteMin, int absoluteMax) {
		this.minField = minField;
		this.maxField = maxField;
		this.absoluteMin = absoluteMin;
		this.absoluteMax = absoluteMax;
		extractValues();
	}

	public void extractValues() {
		
		Integer min = (Integer) minField.getValue();
		Integer max = (Integer) maxField.getValue();
		System.out.println(min+" "+max+" vals");
		if (min < absoluteMin) {
			min = absoluteMin;
			minField.setValue(min);
		} 
		if (min > absoluteMax) {
			min = absoluteMax;
			minField.setValue(min);
		}
		if (max < absoluteMin) {
			max = absoluteMin;
			maxField.setValue(max);
		}
		if (max > absoluteMax) {
			max = absoluteMax;
			maxField.setValue(max);
		}

		if (min > max) {
			Integer temp = min;
			min = max;
			max = temp;
			minField.setValue(min);
			maxField.setValue(max);
		}
		minSizeOfNode = (Integer) minField.getValue();
		maxSizeOfNode = (Integer) maxField.getValue();
	}

	/**
	 * returns the validated values for node minimum diamiter
	 * @return The selected min size of node
	 */
	public int getMinSizeOfNode() {
		extractValues();
		return minSizeOfNode;
	}

	/**
	 * returns the validated values for node maximum diamiter
	 * @return The selected max size of node
	 */
	public int getMaxSizeOfNode() {
		extractValues();
		return maxSizeOfNode;
	}

};
