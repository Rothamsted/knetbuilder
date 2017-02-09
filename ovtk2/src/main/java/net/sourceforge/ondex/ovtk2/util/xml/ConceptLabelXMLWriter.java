package net.sourceforge.ondex.ovtk2.util.xml;

import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.ovtk2.ui.dialog.DialogConceptLabel;

import org.codehaus.stax2.XMLStreamWriter2;

import edu.uci.ics.jung.visualization.renderers.Renderer;

/**
 * Turns a DialogConceptLabel into XML.
 * 
 * @author taubertj
 * 
 */
public class ConceptLabelXMLWriter {

	public static final String SETTINGS = "settings";

	public static final String SETTING = "setting";

	public static final String DATASOURCE = "dataSource";

	public static final String CONCEPTCLASS = "conceptClass";

	public static final String ACCESSIONPREFIX = "accessionPrefix";

	public static final String ACCESSIONSOURCE = "accessionSource";

	public static final String INCLUDEPARSERID = "includeParserID";

	public static final String INCLUDEPREFERREDNAME = "includePreferredName";

	public static final String SEPARATOR = "separator";

	public static final String LABELLENGTH = "labelLength";

	public static final String LABELPOSITION = "labelPosition";

	/**
	 * Writes attributes of a given DialogConceptLabel to XML
	 * 
	 * @param xmlw
	 * @param dialog
	 * @throws XMLStreamException
	 */
	public static void write(XMLStreamWriter2 xmlw, DialogConceptLabel dialog) throws XMLStreamException {

		xmlw.writeStartElement(SETTINGS);

		for (int row = 0; row < dialog.table.getRowCount(); row++) {

			// start a new setting definition
			xmlw.writeStartElement(SETTING);

			// data source filter
			xmlw.writeStartElement(DATASOURCE);
			xmlw.writeCharacters((String) dialog.table.getValueAt(row, 0));
			xmlw.writeEndElement();

			// concept class filter
			xmlw.writeStartElement(CONCEPTCLASS);
			xmlw.writeCharacters((String) dialog.table.getValueAt(row, 1));
			xmlw.writeEndElement();

			// accession data source
			xmlw.writeStartElement(ACCESSIONSOURCE);
			xmlw.writeCharacters((String) dialog.table.getValueAt(row, 2));
			xmlw.writeEndElement();

			// separator
			xmlw.writeStartElement(SEPARATOR);
			xmlw.writeCharacters((String) dialog.table.getValueAt(row, 3));
			xmlw.writeEndElement();

			// accession prefix
			xmlw.writeStartElement(ACCESSIONPREFIX);
			xmlw.writeCharacters((String) dialog.table.getValueAt(row, 4));
			xmlw.writeEndElement();

			// label length
			xmlw.writeStartElement(LABELLENGTH);
			xmlw.writeInt((Integer) dialog.table.getValueAt(row, 5));
			xmlw.writeEndElement();

			// include preferred name
			xmlw.writeStartElement(INCLUDEPREFERREDNAME);
			xmlw.writeBoolean((Boolean) dialog.table.getValueAt(row, 6));
			xmlw.writeEndElement();

			// include parser ID
			xmlw.writeStartElement(INCLUDEPARSERID);
			xmlw.writeBoolean((Boolean) dialog.table.getValueAt(row, 7));
			xmlw.writeEndElement();

			xmlw.writeEndElement();
		}

		// label position, write only once
		Renderer.VertexLabel.Position position = (Renderer.VertexLabel.Position) dialog.labelPositions.getSelectedItem();
		xmlw.writeStartElement(LABELPOSITION);
		xmlw.writeCharacters(position.name());
		xmlw.writeEndElement();

		xmlw.writeEndElement();
	}

}
