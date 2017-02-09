package net.sourceforge.ondex.ovtk2.util.xml;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import net.sourceforge.ondex.ovtk2.ui.dialog.DialogConceptLabel;

import org.codehaus.stax2.XMLStreamReader2;

import edu.uci.ics.jung.visualization.renderers.Renderer;

/**
 * Translates XML settings for DialogConceptLabel.
 * 
 * @author taubertj
 * 
 */
public class ConceptLabelXMLReader {

	/**
	 * Reads the XML stream, de-serialises settings and sets dialog.
	 * 
	 * @param xmlr
	 * @param dialog
	 *            DialogConceptLabel to get modified
	 * @throws XMLStreamException
	 */
	public static void read(XMLStreamReader2 xmlr, DialogConceptLabel dialog) throws XMLStreamException {

		// contains new table data
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();

		// current row
		Vector<Object> row = new Vector<Object>();

		// iterate over XML
		while (xmlr.hasNext()) {

			// get next event
			int event = xmlr.next();

			// check for start of new element
			if (event == XMLStreamConstants.START_ELEMENT) {

				String element = xmlr.getLocalName();

				// filter data source
				if (element.equals(ConceptLabelXMLWriter.DATASOURCE)) {
					row.add(0, xmlr.getElementText());
				}

				// filter concept class
				else if (element.equals(ConceptLabelXMLWriter.CONCEPTCLASS)) {
					row.add(1, xmlr.getElementText());
				}

				// accession data source
				else if (element.equals(ConceptLabelXMLWriter.ACCESSIONSOURCE)) {
					row.add(2, xmlr.getElementText());
				}

				// label separator
				else if (element.equals(ConceptLabelXMLWriter.SEPARATOR)) {
					row.add(3, xmlr.getElementText());
				}

				// accession prefix
				if (element.equals(ConceptLabelXMLWriter.ACCESSIONPREFIX)) {
					row.add(4, xmlr.getElementText());
				}

				// label length
				else if (element.equals(ConceptLabelXMLWriter.LABELLENGTH)) {
					row.add(5, xmlr.getElementAsInt());
				}

				// include preferred name
				else if (element.equals(ConceptLabelXMLWriter.INCLUDEPREFERREDNAME)) {
					row.add(6, xmlr.getElementAsBoolean());
				}

				// include parser ID
				else if (element.equals(ConceptLabelXMLWriter.INCLUDEPARSERID)) {
					row.add(7, xmlr.getElementAsBoolean());
				}

				// label position
				else if (element.equals(ConceptLabelXMLWriter.LABELPOSITION)) {
					String name = xmlr.getElementText();
					dialog.labelPositions.setSelectedItem(Renderer.VertexLabel.Position.valueOf(name));
				}
			}

			else if (event == XMLStreamConstants.END_ELEMENT) {

				String element = xmlr.getLocalName();

				// save current row, start new one
				if (element.equals(ConceptLabelXMLWriter.SETTING)) {
					data.add(row);
					row = new Vector<Object>();
				}
			}
		}

		// set new data to model
		DefaultTableModel model = (DefaultTableModel) dialog.table.getModel();
		Vector<String> columnNames = new Vector<String>();
		for (int i = 0; i < model.getColumnCount(); i++) {
			columnNames.add(model.getColumnName(i));
		}
		model.setDataVector(data, columnNames);
	};

}
