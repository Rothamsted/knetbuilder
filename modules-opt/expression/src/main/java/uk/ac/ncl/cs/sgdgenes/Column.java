package uk.ac.ncl.cs.sgdgenes;

public enum Column {

    GENE_NAME(0), ALIASES(1), DESCRIPTION(2), PRODUCT_NAME(3), MUTANT(4), ORF_NAME(5), SGD_ID(6);
    private int index;

    Column(int index) {
        this.index = index;
    }

    public int index() {
        return index;
    }
}
