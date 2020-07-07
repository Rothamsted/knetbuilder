package net.sourceforge.ondex.ovtk2.ui.contentsdisplay;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Map;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * The producer options dialog for the contents display. Allows activating and
 * deactivating plugins, as well as changing their order by drag'n'drop.
 * 
 * @author Jochen Weile, B.Sc.
 * 
 */
public class CDOptionsDialog extends JDialog implements ActionListener {

	// ####FIELDS####

	/**
	 * version id.
	 */
	private static final long serialVersionUID = -2221885330038461494L;

	/**
	 * contains the producer names in the desired order.
	 */
	private Vector<String> pluginOrder;

	/**
	 * maps the producer names to their activation state (on or off).
	 */
	private Map<String, Boolean> name2activation;

	/**
	 * the displayed list.
	 */
	private DraggableList list;

	// ####CONSTRUCTOR####

	/**
	 * the constructor. sets up everything.
	 */
	public CDOptionsDialog(Frame owner, Vector<String> pluginOrder, Map<String, Boolean> name2activation) {
		super(owner, "Contents display options", true);

		this.pluginOrder = pluginOrder;
		this.name2activation = name2activation;

		list = new DraggableList();

		setupPanels();

		pack();
		setVisible(true);

		// restrict to a reasonable height
		setSize(getSize().width, 800);
	}

	// ####METHODS####

	/**
	 * sets up the gui with the list and the ok-button.
	 */
	private void setupPanels() {
		getContentPane().setLayout(new BorderLayout());

		getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);

		JPanel buttonpanel = new JPanel();
		buttonpanel.setLayout(new BoxLayout(buttonpanel, BoxLayout.LINE_AXIS));
		buttonpanel.add(Box.createHorizontalStrut(50));
		JButton okButton = new JButton("OK");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		buttonpanel.add(okButton);
		buttonpanel.add(Box.createHorizontalStrut(50));
		getContentPane().add(buttonpanel, BorderLayout.SOUTH);
	}

	/**
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ok")) {
			this.dispose();
		}
	}

	/**
	 * A List that supports drag and drop on its elements.
	 * 
	 * @author Jochen Weile, B.Sc.
	 * 
	 */
	private class DraggableList extends JPanel implements MouseListener, ActionListener, MouseMotionListener {

		/**
		 * version.
		 */
		private static final long serialVersionUID = 7780827954199500522L;

		/**
		 * height of a single element in the list.
		 */
		private int elementHeight = 0;

		/**
		 * width of a single element in the list.
		 */
		private int elementWidth = 0;

		/**
		 * the highest index in the list.
		 */
		private int max_index = 0;

		/**
		 * the currently dragged element's index.
		 */
		private int touched_index = -1;

		/**
		 * the currently highlighted border.
		 */
		private int highlightedBorder = -1;

		/**
		 * the currently dragged element's "ghost"-position
		 */
		private int rectPosX = -1, rectPosY = -1;

		/**
		 * the constructor.
		 */
		public DraggableList() {
			setBackground(Color.white);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

			for (String p : pluginOrder) {
				add(createCheckBox(p, name2activation.get(p)));
			}
			max_index = pluginOrder.size() - 1;

			addMouseListener(this);
			addMouseMotionListener(this);
		}

		/**
		 * updates the the panel to represent the current order in the
		 * pluginOrder-Vector.
		 */
		private void updatePanel() {
			removeAll();
			revalidate();
			for (String p : pluginOrder) {
				add(createCheckBox(p, name2activation.get(p)));
			}
			revalidate();
		}

		/**
		 * creates a new checkbox.
		 * 
		 * @param name
		 *            the name of the box.
		 * @param sel
		 *            the selection state.
		 * @return a new checkbox.
		 */
		private JCheckBox createCheckBox(String name, boolean sel) {
			JCheckBox b = new JCheckBox(name, sel);
			b.setActionCommand(name);
			b.addActionListener(this);
			b.setBackground(Color.white);
			return b;
		}

		/**
		 * overridden to visualise the drag and drop targets and the "ghost"
		 * element.
		 * 
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;
			if (highlightedBorder >= 0) {
				int y = highlightedBorder * elementHeight;
				g2.setStroke(new BasicStroke(2.0f));
				g2.drawLine(0, y, elementWidth, y);
			}
			if (rectPosX >= 0 && rectPosY >= 0) {
				float dash1[] = { 3.0f };
				BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
				g2.setStroke(dashed);
				int x = rectPosX - (elementWidth / 2);
				int y = rectPosY - (elementHeight / 2);
				g2.drawRect(x, y, elementWidth, elementHeight);
				g2.setPaint(new Color(0x44888888, true));
				g2.fillRect(x, y, elementWidth, elementHeight);
			}
		}

		/**
		 * updates the activation states.
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (e.getSource() instanceof JCheckBox) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (name2activation.containsKey(cmd)) {
					name2activation.put(cmd, box.isSelected());
				}
			}
		}

		/**
		 * takes a y coordinate and returns the corresponding element index.
		 * 
		 * @param y
		 *            the coordinate.
		 * @return the index.
		 */
		private int resolveRectIndex(int y) {
			int i = y / elementHeight;

			if (i < 0)
				return 0;
			else if (i > max_index)
				return max_index;
			else
				return i;
		}

		/**
		 * takes a y coordinate and returns the corresponding border index.
		 * 
		 * @param y
		 *            the coordinate.
		 * @return the index.
		 */
		private int resolveBorderIndex(int y) {
			int i = y / elementHeight;

			int r = y % elementHeight;
			if (r > elementHeight / 2)
				i++;

			if (i < 0)
				return 0;
			else if (i > max_index + 1)
				return max_index + 1;
			else
				return i;
		}

		/**
		 * when the mouse is pressed the currently selected component is stored.
		 * 
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			elementHeight = this.getComponent(0).getHeight();
			elementWidth = this.getWidth();

			if (e.getButton() == MouseEvent.BUTTON1) {
				int y = e.getY();
				touched_index = resolveRectIndex(y);
			}
		}

		/**
		 * during dragging the ghost and the highlighted border are continuously
		 * updated.
		 * 
		 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			rectPosX = x;
			rectPosY = y;
			highlightedBorder = resolveBorderIndex(y);
			repaint();
		}

		/**
		 * when the mouse is released the components are rearranged so that the
		 * selected element is moved to the border destination index.
		 * 
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				int y = e.getY();

				int dest_index = resolveBorderIndex(y);

				if (dest_index == touched_index || dest_index == touched_index + 1) {
					// do nothing cause it's the same place.
				} else {
					String[] dummy = new String[pluginOrder.size()];
					if (dest_index < touched_index) {
						for (int i = 0; i < dest_index; i++) {
							dummy[i] = pluginOrder.elementAt(i);
						}
						dummy[dest_index] = pluginOrder.elementAt(touched_index);
						for (int i = dest_index + 1; i <= touched_index; i++) {
							dummy[i] = pluginOrder.elementAt(i - 1);
						}
						for (int i = touched_index + 1; i < pluginOrder.size(); i++) {
							dummy[i] = pluginOrder.elementAt(i);
						}
					} else if (dest_index > touched_index + 1) {
						for (int i = 0; i < touched_index; i++) {
							dummy[i] = pluginOrder.elementAt(i);
						}
						for (int i = touched_index; i < dest_index - 1; i++) {
							dummy[i] = pluginOrder.elementAt(i + 1);
						}
						dummy[dest_index - 1] = pluginOrder.elementAt(touched_index);
						for (int i = dest_index; i < pluginOrder.size(); i++) {
							dummy[i] = pluginOrder.elementAt(i);
						}
					}
					pluginOrder.removeAllElements();
					for (int i = 0; i < dummy.length; i++) {
						pluginOrder.add(dummy[i]);
					}
					updatePanel();
				}

				highlightedBorder = touched_index = rectPosX = rectPosY = -1;
				repaint();
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}

	}

}
