package net.sourceforge.ondex.workflow2.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;


/**
 * @author lysenkoa
 */
public class ListPanel extends JPanel implements MouseListener {

    private static final long serialVersionUID = 8850965932534824128L;
    private PositionPanel moved = null;
    private GridBagConstraints main = new GridBagConstraints();
    private GridBagConstraints push = new GridBagConstraints();
    private JPanel pusher = new JPanel();
    private List<PositionPanel> positions = new ArrayList<PositionPanel>();
    private Set<PositionPanel> selectedPanels = new HashSet<PositionPanel>();
    private final ListPanelContainer lp;

    private static Color defaultColour;
    private boolean autoSortOn = true;

    public ListPanel(ListPanelContainer lp) {
    	this.lp = lp;
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createLoweredBevelBorder());
        init();
        pusher.addMouseListener(this);
        defaultColour = this.getBackground();
    }

    public int getElementCount() {
        return positions.size();
    }

    public void init() {
        this.removeAll();
        main.gridx = 0;
        main.gridy = 0;
        main.weightx = 1;
        main.weighty = 0;
        main.fill = GridBagConstraints.HORIZONTAL;
        push.gridx = 0;
        push.gridy = 0;
        push.weightx = 1;
        push.weighty = 1;
        push.fill = GridBagConstraints.BOTH;
        this.add(pusher, push);
    }

    public int addListElement(ComponentGUI p) {
        p.setParent(this);
        this.remove(pusher);
        if (selectedPanels.size() == 0) {
            PositionPanel position = new PositionPanel(p);
            positions.add(position);
            this.add(position, main);
            main.gridy++;
        } else {
            List<Integer> inFocus = getSelectedPositions();
            int pos = inFocus.get(inFocus.size() - 1);
            for (int i = pos; i < positions.size(); i++) {
                if (i > 0)
                    this.remove(positions.get(i));
            }
            main.gridy = pos;
            positions.add(pos, new PositionPanel(p));
            for (int i = pos; i < positions.size(); i++) {
                this.add(positions.get(i), main);
                main.gridy++;
            }
        }
        push.gridy++;
        this.add(pusher, push);
        if (getSelectedPositions().size() > 0 && getElementCount() >= getSelectedPositions().get(getSelectedPositions().size() - 1) + 1) {
            clearFocus();
            setFocus(p);
        }
        this.getParent().getParent().validate();
        this.refresh();
        return main.gridy - 1;
    }

    public void insertAt(PositionPanel toInsert, int pos) {
        main.gridy = pos;
        positions.add(pos, toInsert);
        rebuildFromPosition(pos);
    }

    private void refresh() {
    	this.lp.setSaved(false);
        this.validate();
        this.updateUI();
        this.repaint();
    }

    public void deleteComponent(Component p) {
        int pos = positions.indexOf(p.getParent());
        deleteComponent(pos);
    }

    public PositionPanel deleteComponent(int pos) {
        PositionPanel result = positions.remove(pos);
        selectedPanels.remove(result);
        this.remove(result);
        rebuildFromPosition(pos);
        return result;
    }

    private void rebuildFromPosition(int pos) {
        this.remove(pusher);
        main.gridy = pos;
        push.gridy = pos;
        for (int i = pos; i < positions.size(); i++) {
            this.remove(positions.get(i));
        }
        for (int i = pos; i < positions.size(); i++) {
            this.add(positions.get(i), main);
            main.gridy++;
            push.gridy++;
        }
        this.add(pusher, push);
        refresh();
    }

    public int getIndex(JPanel item) {
        Integer result;
        result = positions.indexOf(item.getParent());
        return result;
    }

    public void clearFocus() {
        for (PositionPanel p : selectedPanels) {
            p.getWComponent().setBackground(defaultColour);
            p.getWComponent().firePropertyChange("isInFocus", true, false);
            p.getSpacer().setBackground(defaultColour);
        }
        selectedPanels.clear();
    }

    public void mouseClicked(MouseEvent e) {
        Component c = ((Component) e.getSource()).getParent();
        if (positions.contains(c)) {
            JPanel panelInFocus = (JPanel) e.getSource();
            boolean isSelected = isSelected(panelInFocus);
            if (e.getModifiers() != 17) {
                clearFocus();
            }
            if (!isSelected) {
                setFocus(panelInFocus);
            }
        } else {
            clearFocus();
        }
    }

    private boolean isSelected(JPanel panelInFocus) {
        Component c = panelInFocus.getParent();
        return selectedPanels.contains(c);
    }

    private void setFocus(JPanel panelInFocus) {
        Component c = panelInFocus.getParent();
        selectedPanels.add((PositionPanel) c);
        panelInFocus.setBackground(Color.DARK_GRAY);
        panelInFocus.firePropertyChange("isInFocus", false, true);
    }

    @SuppressWarnings("unchecked")
    public List getContent() {
        List result = new ArrayList(positions.size());
        for (JPanel p : positions) {
            result.add(p.getComponent(1));
        }
        return result;
    }

    public void dispose() {
        ListPanel.defaultColour = null;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        Component uncc = ((Component) e.getSource()).getParent();
        if (uncc instanceof PositionPanel) {
            PositionPanel c = (PositionPanel) ((Component) e.getSource()).getParent();
            if (positions.contains(c))
                moved = c;
        }
    }

    public void mouseReleased(MouseEvent e) {
        Point p = new Point(e.getLocationOnScreen().x, e.getLocationOnScreen().y);
        SwingUtilities.convertPointFromScreen(p, this);
        Component atLoaction = this.getComponentAt(p);
        if (moved == null || moved.equals(atLoaction)) return;
        int pos = positions.indexOf(atLoaction);
        if (pos >= 0) {
            PositionPanel toMove = deleteComponent(positions.indexOf(moved));
            insertAt(toMove, pos);
        }
        moved = null;
    }

    @Override
    public void validate() {
        for (PositionPanel p : positions) {
            p.validate();
            p.getWComponent().validate();
        }
        super.validate();
    }

    public boolean isAutoSortOn() {
        return autoSortOn;
    }

    public void setAutoSortOn(boolean autoSortOn) {
        this.autoSortOn = autoSortOn;
    }

    private class PositionPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final ComponentGUI p;
        private final JPanel spacer;

        public PositionPanel(ComponentGUI p) {
            super();
            this.p = p;
            this.setLayout(new GridBagLayout());
            GridBagConstraints elCons = new GridBagConstraints();
            elCons.fill = GridBagConstraints.HORIZONTAL;
            elCons.gridx = 0;
            elCons.gridy = 0;

            elCons.weightx = 1;
            spacer = new JPanel();
            spacer.addMouseListener(ListPanel.this);
            spacer.setPreferredSize(new Dimension(5, 5));
            this.add(spacer, elCons);

            elCons.gridy = 1;
            p.addMouseListener(ListPanel.this);
            this.add(p, elCons);
        }

        public void unregister() {
            spacer.removeMouseListener(ListPanel.this);
            p.removeMouseListener(ListPanel.this);
        }

        public ComponentGUI getWComponent() {
            return p;
        }

        public JPanel getSpacer() {
            return spacer;
        }
    }

    public void sortGraphRefs(ComponentGUI p) {
        if (positions.size() == 0) return;
        String currentRef = "default";
        for (PositionPanel position : positions) {
            ComponentGUI current = position.getWComponent();
            if (current.getPluginId()[1].equals("newgraph")) {
                String id = current.getGraphRefId();
                if (id != null && id.length() > 0) {
                    currentRef = id;
                }
            } else {
                current.setInputByName("graphId", currentRef);
            }
        }
    }

    private List<Integer> getSelectedPositions() {
        List<Integer> result = new ArrayList<Integer>();
        for (PositionPanel p : selectedPanels) {
            result.add(positions.indexOf(p));
        }
        Collections.sort(result);
        return result;
    }
}
