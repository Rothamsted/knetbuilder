package net.sourceforge.ondex.ovtk2.annotator.scaleconcept;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.TransferHandler;

import net.sourceforge.ondex.ovtk2.util.listmodel.AttributeNameListModel;
import net.sourceforge.ondex.ovtk2.util.listmodel.MutableListModel;

/**
 * adapted from
 * http://www.java-forums.org/awt-swing/3030-help-drag-drop-jlist.html
 * 
 * @author hardwired, hindlem
 * 
 */
public class ReportingListTransferHandler extends TransferHandler {

	private static final long serialVersionUID = -944996350118307008L;

	private DataFlavor localArrayListFlavor, serialArrayListFlavor;
	private String localArrayListType = DataFlavor.javaJVMLocalObjectMimeType + ";class=java.util.ArrayList";
	private JList source = null;
	private int[] indices = null;
	private int addIndex = -1; // Location where items were added
	private int addCount = 0; // Number of items added

	public ReportingListTransferHandler() {
		try {
			localArrayListFlavor = new DataFlavor(localArrayListType);
		} catch (ClassNotFoundException e) {
			System.out.println("ReportingListTransferHandler: unable to create data flavor");
		}
		serialArrayListFlavor = new DataFlavor(ArrayList.class, "ArrayList");
	}

	public boolean importData(JComponent c, Transferable t) {

		JList target = null;
		List<?> alist = null;
		if (!canImport(c, t.getTransferDataFlavors())) {
			System.err.println("un supported");
			return false;
		}
		try {
			target = (JList) c;
			if (hasLocalArrayListFlavor(t.getTransferDataFlavors())) {
				alist = (List<?>) t.getTransferData(localArrayListFlavor);
			} else if (hasSerialArrayListFlavor(t.getTransferDataFlavors())) {
				alist = (List<?>) t.getTransferData(serialArrayListFlavor);
			} else {
				System.err.println("unknown");
				return false;
			}
		} catch (UnsupportedFlavorException ufe) {
			System.err.println("importData: unsupported data flavor");
			return false;
		} catch (IOException ioe) {
			System.err.println("importData: I/O exception");
			return false;
		}

		// At this point we use the same code to retrieve the data
		// locally or serially.

		// We'll drop at the current selected index.
		int index = target.getSelectedIndex();
		// for (int i = 0; i < indices.length; i++) {
		// indices[i] = indices[i]-1;
		// }
		// Prevent the user from dropping data back on itself.
		// For example, if the user is moving items #4,#5,#6 and #7 and
		// attempts to insert the items after item #5, this would
		// be problematic when removing the original items.
		// This is interpreted as dropping the same data on itself
		// and has no effect.
		if (source.equals(target)) {
			if (indices != null && index >= indices[0] && index <= indices[indices.length - 1]) {
				indices = null;
				return true;
			}
		}

		ListModel listModel = target.getModel();

		int max = listModel.getSize() - 1;
		if (index < 0) {
			index = max;
		} else {
			if (index > max) {
				index = max;
			}
		}
		addIndex = index;
		addCount = alist.size();
		for (int i = 0; i < alist.size(); i++) {
			if (listModel instanceof MutableListModel) {
				try {
					((MutableListModel) listModel).add(index++, alist.get(i));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.err.println("Can not work with this ListModel: " + listModel.getClass());
				return false;
			}

		}
		return true;
	}

	protected void exportDone(JComponent c, Transferable data, int action) {

		if ((action == MOVE) && (indices != null)) {
			ListModel listModel = source.getModel();

			int size = 0;
			if (listModel instanceof MutableListModel) {
				size = listModel.getSize();
			} else {
				System.err.println("Can not work with this ListModel: " + listModel.getClass());
			}

			// If we are moving items around in the same list, we
			// need to adjust the indices accordingly since those
			// after the insertion point have moved.
			if (addCount > 0) {
				for (int i = 0; i < indices.length; i++) {
					if (indices[i] > addIndex && indices[i] + addCount < size) {
						indices[i] += addCount;
					}
				}
			}
			for (int i = indices.length - 1; i >= 0; i--) {
				if (listModel instanceof MutableListModel) {
					((MutableListModel) listModel).remove(indices[i]);
				} else {
					System.err.println("Can not work with this ListModel: " + listModel.getClass());
				}
			}
		}
		indices = null;
		addIndex = -1;
		addCount = 0;
	}

	private boolean hasLocalArrayListFlavor(DataFlavor[] flavors) {

		if (localArrayListFlavor == null) {
			return false;
		}

		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(localArrayListFlavor)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasSerialArrayListFlavor(DataFlavor[] flavors) {

		if (serialArrayListFlavor == null) {
			return false;
		}

		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(serialArrayListFlavor)) {
				return true;
			}
		}
		return false;
	}

	public boolean canImport(JComponent c, DataFlavor[] flavors) {
		if (hasLocalArrayListFlavor(flavors)) {
			return true;
		}
		if (hasSerialArrayListFlavor(flavors)) {
			return true;
		}
		return false;
	}

	protected Transferable createTransferable(JComponent c) {
		if (c instanceof JList) {
			source = (JList) c;
			indices = source.getSelectedIndices();

			if (indices.length == 0) {
				return null;
			}
			ArrayList<Object> alist = new ArrayList<Object>(indices.length);
			for (int index : indices) {
				if (source.getModel() instanceof AttributeNameListModel) {
					alist.add(((AttributeNameListModel) source.getModel()).getAttributeNameAt(index));
				} else {
					alist.add(source.getModel().getElementAt(index).toString());
				}
			}
			return new ReportingListTransferable(alist);
		}
		return null;
	}

	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	public class ReportingListTransferable implements Transferable {
		List<?> data;

		public ReportingListTransferable(List<?> alist) {
			data = alist;
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return data;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { localArrayListFlavor, serialArrayListFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			if (localArrayListFlavor.equals(flavor)) {
				return true;
			}
			if (serialArrayListFlavor.equals(flavor)) {
				return true;
			}
			return false;
		}
	}

}
