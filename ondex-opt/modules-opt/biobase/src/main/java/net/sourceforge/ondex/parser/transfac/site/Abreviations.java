package net.sourceforge.ondex.parser.transfac.site;

/**
 * The easy way of finding out what a line tag means in transfac
 * @author hindlem
 *
 */
abstract class Abreviations {
	
	/**
	 * Spacer line seperates sections
	 */
	public final static String blank_spacer_line = "XX";
	
	/**
	 * Identifier
	 */
	public final static String identifier = "ID";
	
	/**
	 * Accession no.
	 */
	public final static String accession = "AC";
	
	/**
	 * Date; author
	 */
	public final static String date_author  = "DT";
	
	/**
	 * Sequence type
	 */
	public final static String sequence_type = "AC";
	
	/**
	 * Description (gene or gene product); GENE accession no.
	 */
	public final static String description  = "DE";
	
	/**
	 * Gene region (e. g. promoter, enhancer)
	 */
	public final static String gene_region  = "RE";
	
	/**
	 * Sequence of the regulatory element
	 */
	public final static String sequence  = "SQ";
	
	/**
	 * Denomination of the element
	 */
	public final static String element_denom  = "EL";
	
	
	/**
	 * First position of factor binding site
	 */
	public final static String first_pos_bindingsite  = "SF";
	
	
	/**
	 * Last position of factor binding site
	 */
	public final static String last_pos_bindingsite  = "ST";
	
	
	/**
	 * Definition of first position (if not transcription start site)
	 */
	public final static String def_first_pos_bindingsite  = "S1";
	
	
	/**
	 * Binding factor (FACTOR accession no.; name; quality; biological species)
	 */
	public final static String binding_factor  = "BF";
	
	
	/**
	 * Deduced matrix (MATRIX accession no.; identifier)
	 */
	public final static String deduced_matrix = "MX";
	
	
	/**
	 * Organism species
	 */
	public final static String org  = "OS";
	
	/**
	 * Organism classification
	 */
	public final static String org_class  = "OC";
	
	/**
	 * Factor source (TRANSFAC CELL accession no.; name)
	 */
	public final static String factor_source  = "SO";
	
	/**
	 * Method
	 */
	public final static String methods  = "MM";
	
	/**
	 * Comments
	 */
	public final static String comments  = "CC";
	
	/**
	 * External databases (EPD, Flybase, TRANSCompel, TRANSPRO, PathoDB)
	 */
	public final static String xdb_ref  = "DR";
	
	/**
	 * MEDLINE ID
	 */
	public final static String medline_id  = "RX";
	
	/**
	 * Paper Reference no.
	 */
	public final static String ref_no  = "RN";
	
	/**
	 * Reference authors
	 */
	public final static String ref_authors  = "RA";
	
	/**
	 * Reference title
	 */
	public final static String ref_title  = "RT";
	
	/**
	 * Reference data
	 */
	public final static String ref_data  = "RL";
	
	
}
