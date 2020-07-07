package net.sourceforge.ondex.parser.metacyc.objects;

/**
 * Represents a gene
 * @author peschr
 *
 */
public class Gene extends AbstractNode {
	
	private float centisomePosition;
	private String componentOf;
	private String swissProtId;
	
	private Protein product;

	public float getCentisomePosition() {
		return centisomePosition;
	}
	public void setCentisomePosition(float centisomePosition) {
		this.centisomePosition = centisomePosition;
	}
	public String getComponentOf() {
		return componentOf;
	}
	public void setComponentOf(String componentOf) {
		this.componentOf = componentOf;
	}
	public String getSwissProtId() {
		return swissProtId;
	}
	public void setSwissProtId(String swissProtId) {
		this.swissProtId = swissProtId;
	}

	public Protein getProduct() {
		return product;
	}
	public void setProduct(Protein product) {
		this.product = product;
	}
	
}
