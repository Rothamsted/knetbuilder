package net.sourceforge.ondex.ovtk2.ui.popup;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.RootPaneContainer;
import javax.swing.undo.StateEdit;

import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.graph.VisibilityUndo;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Desktop;
import net.sourceforge.ondex.ovtk2.ui.OVTK2PropertiesAggregator;
import net.sourceforge.ondex.ovtk2.ui.OVTK2Viewer;

/**
 * Base-class for menu items acting upon graph entities.
 * 
 * @author Matthew Pocock
 * @author taubertj
 */
public abstract class EntityMenuItem<E extends ONDEXEntity> implements MenuPointListener {

	/**
	 * Which category to sort an item too.
	 * 
	 * @author taubertj
	 * 
	 */
	public static enum MENUCATEGORY {
		HIDE, LINK, SHOW, CHANGE
	}

	/**
	 * Find highest parent of current component.
	 * 
	 * @param c
	 *            start component
	 * @return root parent
	 */
	private static RootPaneContainer findRoot(Component c) {
		if (c instanceof RootPaneContainer)
			return (RootPaneContainer) c;
		return findRoot(c.getParent());
	}

	/**
	 * Set of graph entities
	 */
	protected Set<E> entities;

	/**
	 * associated item, can be JMenu or JMenuItem
	 */
	protected JMenuItem item;

	/**
	 * menu location
	 */
	private Point location = null;

	/**
	 * associated viewer with graph
	 */
	protected OVTK2PropertiesAggregator viewer;

	/**
	 * Empty constructor for easier reflection handling.
	 */
	public EntityMenuItem() {
		item = new JMenuItem();
		item.setText(Config.language.getProperty(getMenuPropertyName()));
	}

	/**
	 * Should be called after init. Checks whether or not the conditions for
	 * this menu item are fulfilled.
	 * 
	 * @return accepts entities supplied
	 */
	public abstract boolean accepts();

	/**
	 * Actual action of menu item in here
	 */
	protected abstract void doAction();

	/**
	 * Returns the sub-menu category name, e.g. "hide" or "show"
	 * 
	 * @return
	 */
	public abstract MENUCATEGORY getCategory();

	/**
	 * Returns associated JMenuItem.
	 * 
	 * @return JMenuItem
	 */
	public JMenuItem getItem() {
		return item;
	}

	/**
	 * Config property name for title of menu item
	 * 
	 * @return
	 */
	protected abstract String getMenuPropertyName();

	/**
	 * Return current menu location
	 * 
	 * @return
	 */
	protected Point getPoint() {
		return location;
	}

	/**
	 * Config property name for undo operations
	 * 
	 * @return
	 */
	protected abstract String getUndoPropertyName();

	/**
	 * Adds ActionListener to item and sets viewer and entity set.
	 * 
	 * @param v
	 *            OVTK2Viewer
	 * @param e
	 *            Set<E>
	 */
	public void init(final OVTK2PropertiesAggregator v, final Set<E> e) {
		this.viewer = v;
		this.entities = e;

		// required to trigger any action
		item.addActionListener(new ActionListener() {
			public final void actionPerformed(ActionEvent e) {

				// set to waiting cursor
				Cursor cursor = v.getVisualizationViewer().getCursor();
				v.getVisualizationViewer().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				RootPaneContainer root = findRoot((Component) v);
				root.getGlassPane().setVisible(true);
				root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				// StateEdit system, start edit
				StateEdit edit = null;

				if (getUndoPropertyName() != null) {
					edit = new StateEdit(new VisibilityUndo(v.getONDEXJUNGGraph()), Config.language.getProperty(getUndoPropertyName()));
					v.getUndoManager().addEdit(edit);
					if (v instanceof OVTK2Viewer)
						OVTK2Desktop.getInstance().getOVTK2Menu().updateUndoRedo(v);
				}

				try {
					// trigger action
					doAction();
				} finally {

					// StateEdit system, end edit
					if (edit != null) {
						v.getVisualizationViewer().repaint();
						edit.end();
					}

					// restore cursor
					root.getGlassPane().setVisible(false);
					v.getVisualizationViewer().setCursor(cursor);
				}

				v.getVisualizationViewer().repaint();
			}
		});
	}

	@Override
	public final void setPoint(Point point) {
		location = new Point(((Component) viewer).getX() + point.x, ((Component) viewer).getY() + point.y);
	}
}
