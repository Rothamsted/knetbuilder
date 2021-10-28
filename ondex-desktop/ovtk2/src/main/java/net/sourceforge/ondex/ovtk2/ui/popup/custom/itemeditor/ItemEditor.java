/*
 * This file was created using Netbeans GUI editor.
 */

package net.sourceforge.ondex.ovtk2.ui.popup.custom.itemeditor;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Arrays;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Re-useable item editor (parts generated using Netbeans GUI editor)
 * 
 * @author Martin Rittweger
 */

public class ItemEditor extends javax.swing.JFrame {

	/**
	 * generated
	 */
	private static final long serialVersionUID = -6434641497420270567L;
	private final ItemEditHandler itemEditor;
	private final JPanel itemEditPanel;

	/**
	 * Creates new form ContextMenuEditor
	 * 
	 * @param title
	 *            title given to the frame
	 * @param itemEditPanel
	 *            Panel used for editing
	 * @param itemEditor
	 *            may be the same like itemEditPanel
	 */
	public ItemEditor(String title, JPanel itemEditPanel, ItemEditHandler itemEditor) {
		this.itemEditPanel = itemEditPanel;
		this.itemEditor = itemEditor;
		setTitle(title);

		initComponents();

		jSplitPane1.setRightComponent(nothingToEditPanel);
		itemList.setListData(itemEditor.getItemNames());
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		nothingToEditPanel = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jPanel1 = new javax.swing.JPanel();
		jSplitPane1 = new javax.swing.JSplitPane();
		jPanel6 = new javax.swing.JPanel();
		jScrollPane1 = new javax.swing.JScrollPane();
		itemList = new javax.swing.JList();
		newButton = new javax.swing.JButton();
		deleteButton = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		saveButton = new javax.swing.JButton();
		closeButton = new javax.swing.JButton();

		jLabel1.setText("(no item to edit)");
		jLabel1.setEnabled(false);

		javax.swing.GroupLayout nothingToEditPanelLayout = new javax.swing.GroupLayout(nothingToEditPanel);
		nothingToEditPanel.setLayout(nothingToEditPanelLayout);
		nothingToEditPanelLayout.setHorizontalGroup(nothingToEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 358, Short.MAX_VALUE).addGroup(nothingToEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(nothingToEditPanelLayout.createSequentialGroup().addGap(0, 133, Short.MAX_VALUE).addComponent(jLabel1).addGap(0, 132, Short.MAX_VALUE))));
		nothingToEditPanelLayout.setVerticalGroup(nothingToEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 229, Short.MAX_VALUE).addGroup(nothingToEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(nothingToEditPanelLayout.createSequentialGroup().addGap(0, 107, Short.MAX_VALUE).addComponent(jLabel1).addGap(0, 106, Short.MAX_VALUE))));

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		jPanel1.setLayout(new java.awt.BorderLayout());

		jSplitPane1.setDividerLocation(150);

		jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.Y_AXIS));

		jScrollPane1.setMinimumSize(new java.awt.Dimension(150, 27));

		itemList.setModel(new javax.swing.AbstractListModel() {
			/**
			 * generated
			 */
			private static final long serialVersionUID = -8187183802658155570L;
			String[] strings = { "menuItem 1", "menuItem 2", "menuItem 3" };

			public int getSize() {
				return strings.length;
			}

			public Object getElementAt(int i) {
				return strings[i];
			}
		});
		itemList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		itemList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				itemListValueChanged(evt);
			}
		});
		jScrollPane1.setViewportView(itemList);

		jPanel6.add(jScrollPane1);

		newButton.setText("new");
		newButton.setAlignmentX(0.5F);
		newButton.setMaximumSize(new java.awt.Dimension(999, 999));
		newButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				newButtonActionPerformed(evt);
			}
		});
		jPanel6.add(newButton);

		deleteButton.setText("delete");
		deleteButton.setAlignmentX(0.5F);
		deleteButton.setMaximumSize(new java.awt.Dimension(999, 999));
		deleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteButtonActionPerformed(evt);
			}
		});
		jPanel6.add(deleteButton);

		jSplitPane1.setLeftComponent(jPanel6);

		jPanel1.add(jSplitPane1, java.awt.BorderLayout.CENTER);

		saveButton.setText("save");
		saveButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveButtonActionPerformed(evt);
			}
		});

		closeButton.setText("close");
		closeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				closeButtonActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup().addContainerGap(460, Short.MAX_VALUE).addComponent(saveButton).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(closeButton)));

		jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] { closeButton, saveButton });

		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup().addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(closeButton).addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))));

		jPanel1.add(jPanel2, java.awt.BorderLayout.SOUTH);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 613, Short.MAX_VALUE).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE).addContainerGap())));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 395, Short.MAX_VALUE).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE).addContainerGap())));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	@Override
	public void dispose() {
		if (makeSureContentsAreSaved())
			super.dispose();
	}

	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_closeButtonActionPerformed
		this.dispose();
	}// GEN-LAST:event_closeButtonActionPerformed

	private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveButtonActionPerformed
		saveItem();
	}// GEN-LAST:event_saveButtonActionPerformed

	private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_newButtonActionPerformed
		if (!makeSureContentsAreSaved())
			return;
		String name = JOptionPane.showInputDialog(this, "Name?");

		if (name != null) {
			String[] itemNames = itemEditor.getItemNames();
			if (Arrays.asList(itemNames).contains(name)) {
				JOptionPane.showMessageDialog(this, "Item with the name '" + name + "' already exists");
				return;
			}
			itemNames = Arrays.copyOf(itemNames, itemNames.length + 1);
			itemNames[itemNames.length - 1] = name;

			itemList.setListData(itemNames); // and save unsaved item
			itemEditor.newItem(name);
			saveItem();
			itemEditor.clearItem();
			itemEditor.setChanged(false);
			itemList.setSelectedValue(name, true);
		}

	}// GEN-LAST:event_newButtonActionPerformed

	private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_deleteButtonActionPerformed
		if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Sure?")) {
			itemEditor.setChanged(false);
			itemEditor.deleteItem();

			itemList.setListData(itemEditor.getItemNames());
		}
	}// GEN-LAST:event_deleteButtonActionPerformed

	private void itemListValueChanged(javax.swing.event.ListSelectionEvent evt) {
		if (itemEditor.getItemName() != null && itemEditor.getItemName().toString().equals ( itemList.getSelectedValue() ))
			return;
		if (!makeSureContentsAreSaved()) {
			return;
		}
		Object currentSelection = itemList.getSelectedValue();
		if (currentSelection == null) {
			jSplitPane1.setRightComponent(nothingToEditPanel);
		} else {
			if (jSplitPane1.getRightComponent() != itemEditPanel) {
				jSplitPane1.setRightComponent(itemEditPanel);
				jSplitPane1.invalidate();
			}
			try {
				itemEditor.setChanged(false);
				itemEditor.loadItem(currentSelection.toString());
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(this, "Loading file failed:\n" + e.getMessage());
				e.printStackTrace();
				itemList.clearSelection();
			} catch (ParseException e) {
				JOptionPane.showMessageDialog(this, "File has bad format.");
				e.printStackTrace();
				itemList.clearSelection();
			}
		}
	}

	private boolean makeSureContentsAreSaved() {
		if (itemEditor.getItemName() != null && itemEditor.isChanged()) {
			int result = JOptionPane.showConfirmDialog(this, "Save changes of '" + itemEditor.getItemName().toString() + "'?");
			if (result == JOptionPane.YES_OPTION) {
				return saveItem();
			}
			if (result == JOptionPane.NO_OPTION)
				return true;
			if (result == JOptionPane.CANCEL_OPTION) {
				itemList.clearSelection();
				itemList.setSelectedValue(itemEditor.getItemName(), true);
				return false;
			}
		}
		return true;
	}

	private boolean saveItem() {
		try {
			itemEditor.saveItem();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "Saving file failed:\n" + e.getMessage());
			e.printStackTrace();
			itemList.clearSelection();
			return false;
		}
		itemEditor.setChanged(false);
		return true;
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton closeButton;
	private javax.swing.JButton deleteButton;
	private javax.swing.JList itemList;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JSplitPane jSplitPane1;
	private javax.swing.JButton newButton;
	private javax.swing.JPanel nothingToEditPanel;
	private javax.swing.JButton saveButton;
	// End of variables declaration//GEN-END:variables

}
