package net.sourceforge.ondex.ovtk2.annotator.microarray;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import net.sourceforge.ondex.ovtk2.annotator.microarray.table.MicroArrayResultTableCellRenderer;
import net.sourceforge.ondex.ovtk2.annotator.microarray.table.ResultButtonHeaderRenderer;
import net.sourceforge.ondex.ovtk2.annotator.microarray.table.ResultHeaderListener;
import net.sourceforge.ondex.ovtk2.annotator.microarray.table.ResultStringTableCellRenderer;
import net.sourceforge.ondex.ovtk2.annotator.microarray.table.ResultTableModel;
import net.sourceforge.ondex.ovtk2.annotator.microarray.table.ResultTableUtil;

/**
 * The main display for normal microarray data
 *
 * @author hindlem
 */
public class MicroarrayDataTablePanel extends JPanel {

    protected final static String PROBE = "ProbeID";
    protected final static String ONDEXID = "ONDEXID";
    protected String LSD = "LSD";
    private ResultButtonHeaderRenderer myRatioTitleCellRenderer;
    private JTable myTable;

    private static final long serialVersionUID = 1L;

    /**
     * Main constructor creates table
     *
     * @param dataIndex
     */
    public MicroarrayDataTablePanel(MicroarrayDataIndex dataIndex, Map<String, Integer> probeIdToONDEXId) {
        this.setLayout(new BorderLayout());

        Set<String> treatments = dataIndex.getTreatments();
        treatments = new TreeSet<String>(treatments); //sort it to alphabetical order

        Set<String> probes = dataIndex.getProbes();

        boolean areLsds = (dataIndex.getProbeToLSD().size() > 0);

        Object[] headers = null;

        if (areLsds) {
            headers = new Object[]{PROBE, ONDEXID, LSD};
        } else {
            headers = new Object[]{PROBE, ONDEXID};
        }

        headers = addTreatments(headers, treatments);
        headers = addComparisons(headers, treatments);

        //build an index for flexibility
        HashMap<Object, Integer> headerIndex = new HashMap<Object, Integer>();
        for (int i = 0; i < headers.length; i++) {
            Object header = headers[i];
            headerIndex.put(header, i);
            System.out.println(header + " " + i + " " + header.getClass());
        }
        System.out.println("\n\n");
        Object[][] data = new Object[probes.size()][headers.length];

        int dataPlace = 0;

        Iterator<String> probesIt = probes.iterator();
        while (probesIt.hasNext()) {
            String probe = probesIt.next();

            Double lsd = dataIndex.getProbeToLSD().get(probe);
            data[dataPlace][headerIndex.get(LSD)] = lsd.floatValue();

            data[dataPlace][headerIndex.get(PROBE)] = probe;

            Integer id = probeIdToONDEXId.get(probe);
            if (id == null)
                id = -1;

            data[dataPlace][headerIndex.get(ONDEXID)] = id;

            for (String treatment : treatments) {
                Double expression = dataIndex.getProbe2TreatmentIndex()
                        .get(probe.toUpperCase())
                        .get(treatment);
                data[dataPlace][headerIndex.get(treatment)] = expression;
            }

            for (String treatmentA : treatments) {
                for (String treatmentB : treatments) {
                    if (!treatmentA.equals(treatmentB)) {
                        Double ratio = dataIndex.getProbe2RatioIndex().get(
                                probe.toUpperCase()).get(treatmentA)
                                .get(treatmentB);

                        data[dataPlace][headerIndex.get(treatmentA + ":" + treatmentB)] = ratio;
                    }
                }
            }

            dataPlace++;
        }

        ResultTableModel tableModel = new ResultTableModel(data, headers);
        myTable = new JTable(tableModel);
        myTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // create and add string renderer
        ResultStringTableCellRenderer myStringTableCellRenderer =
                new ResultStringTableCellRenderer();
        myTable.setDefaultRenderer(String.class, myStringTableCellRenderer);

        // create and add double renderer
        MicroArrayResultTableCellRenderer myDoubleTableCellRenderer =
                new MicroArrayResultTableCellRenderer(5, 0);
        myTable.setDefaultRenderer(Double.class, myDoubleTableCellRenderer);

        // create and add ratio title renderer
        myRatioTitleCellRenderer =
                new ResultButtonHeaderRenderer();
        TableColumnModel model = myTable.getColumnModel();
        int n = tableModel.getColumnCount();
        for (int i = 0; i < n; i++) {
            model.getColumn(i).setHeaderRenderer(myRatioTitleCellRenderer);
        }

        // add mouse listener
        JTableHeader header = myTable.getTableHeader();
        header.addMouseListener(new ResultHeaderListener(header, myRatioTitleCellRenderer, myTable));
        myTable.setTableHeader(header);

        // rearrange table
        ResultTableUtil.calcColumnWidths(myTable);

        add(new JScrollPane(myTable));
    }

    /**
     * Appends comparisons between treatments to the end of the array
     *
     * @param headers    array to append to
     * @param treatments to be considered
     * @return appended array
     */
    private Object[] addComparisons(Object[] headers, Set<String> treatments) {
        Object[] newHeaders = new Object[headers.length + ((treatments.size() * treatments.size()) - treatments.size())];
        System.arraycopy(headers, 0, newHeaders, 0, headers.length);

        int place = headers.length; //last empty element
        for (String treatmentA : treatments) {
            for (String treatmentB : treatments) {
                if (!treatmentA.equals(treatmentB)) {
                    String ratioName = treatmentA + ":" + treatmentB;
                    newHeaders[place] = ratioName;
                    place++;
                }
            }
        }
        headers = newHeaders;
        return newHeaders;
    }

    /**
     * Appends treatments to the end of the array
     *
     * @param headers    array to append to
     * @param treatments values to append
     * @return appended array
     */
    private Object[] addTreatments(Object[] headers, Set<?> treatments) {
        Object[] newHeaders = new Object[headers.length + treatments.size()];
        System.arraycopy(headers, 0, newHeaders, 0, headers.length);

        int place = headers.length; //last empty element
        for (Object treatment : treatments) {
            newHeaders[place] = treatment;
            place++;
        }
        headers = newHeaders;
        return newHeaders;
    }

    public void highlightHeader(String header) {
        myRatioTitleCellRenderer.highlightColumn(header);
        myTable.clearSelection();
        myTable.validate();
        myTable.repaint();
		validate();
		repaint();
	}
	
}
