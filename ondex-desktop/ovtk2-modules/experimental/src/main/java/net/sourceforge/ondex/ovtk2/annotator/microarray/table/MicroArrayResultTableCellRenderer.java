package net.sourceforge.ondex.ovtk2.annotator.microarray.table;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MicroArrayResultTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	// histogram breaks
	private DecimalFormat format = new DecimalFormat();

	private Color greaterColor = Color.RED;
	private Color lesserColor = Color.GREEN;
	private Color middleColor = Color.YELLOW;

	private double midpoint;
	
	/**
	 * 
	 * @param greatestAbsolute 
	 */
	public MicroArrayResultTableCellRenderer(int fractionDigits, double midpoint) {
		super();
		format.setMaximumFractionDigits(fractionDigits);
		this.midpoint = midpoint;
	}
	
	public MicroArrayResultTableCellRenderer() {
		this(5, 0);
	}
	
	 public Component getTableCellRendererComponent(JTable table, Object value,
             boolean isSelected, boolean hasFocus, int row, int column) {
		 Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		 
		 Double ratio = (Double)value;
		 Color color = calculateColor(ratio);
		 
		 component.setBackground(color);
		 
		 component.setForeground(Color.black);
		 
		 if (value instanceof Double) {
			 ((JLabel)component).setText(format.format(value));
		 }
		 return component;
	  }

	 /**
	  * Calcuate target color
	  * @param ratio
	  * @return
	  */
	public Color calculateColor(Double ratio) {
		Color color;
		 if (ratio > midpoint) {
			 color = greaterColor;
		 } else if (ratio < midpoint) {
			 color = lesserColor;
		 } else {
			 color = middleColor;
		 }
		return color;
	}

	public Color getGreaterColor() {
		return greaterColor;
	}

	public void setGreaterColor(Color greaterColor) {
		this.greaterColor = greaterColor;
	}

	public Color getLesserColor() {
		return lesserColor;
	}

	public void setLesserColor(Color lesserColor) {
		this.lesserColor = lesserColor;
	}

	public Color getMiddleColor() {
		return middleColor;
	}

	public void setMiddleColor(Color middleColor) {
		this.middleColor = middleColor;
	}
	 

}
