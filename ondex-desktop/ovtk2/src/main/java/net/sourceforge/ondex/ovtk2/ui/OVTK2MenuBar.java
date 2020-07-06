package net.sourceforge.ondex.ovtk2.ui;

public interface OVTK2MenuBar {

	/**
	 * Updates menu bar to represent state of activeViewer.
	 * 
	 * @param activeViewer
	 */
	public void updateMenuBar(OVTK2PropertiesAggregator activeViewer);

	/**
	 * Updates settings for undo and redo
	 * 
	 * @param activeViewer
	 */
	public void updateUndoRedo(OVTK2PropertiesAggregator activeViewer);

}
