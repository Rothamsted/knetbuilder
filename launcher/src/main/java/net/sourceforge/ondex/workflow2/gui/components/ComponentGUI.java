package net.sourceforge.ondex.workflow2.gui.components;

import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.init.PluginDescription;
import net.sourceforge.ondex.init.PluginDocumentationFactory;
import net.sourceforge.ondex.workflow.model.BoundArgumentValue;
import net.sourceforge.ondex.workflow.model.WorkflowTask;
import net.sourceforge.ondex.workflow.model.WorkflowDescriptionIO;
import net.sourceforge.ondex.workflow2.gui.arg.ArgumentHolder;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author lysenkoa
 */
public class ComponentGUI extends JPanel {
    private static final long serialVersionUID = 5905825174487193808L;
    private static final Icon ICON_CLOSE = new CloseIcon();
    private static final Border border = BorderFactory.createRaisedBevelBorder();
    private final Map<ArgumentDescription, ArgumentHolder> map = new HashMap<ArgumentDescription, ArgumentHolder>();

    public static enum STATE {
        READY, RUNNING, DONE, FAILED
    }

    private final TablePanel inputs;
    private JButton delete;
    private ListPanel parent;
    private final GridBagConstraints con;
    private final PluginDescription ct;
    private TitlePanel titlePanel;
    private String comment = null;


    public ComponentGUI(PluginDescription ct, boolean loadDefaultValues) {
        this.ct = ct;
        this.setLayout(new GridBagLayout());
        this.setBorder(border);
        con = new GridBagConstraints();
        con.fill = GridBagConstraints.BOTH;
        con.weightx = 1;
        con.gridx = 0;
        con.gridy = 0;
        titlePanel = new TitlePanel(ct.getName());
        this.add(titlePanel, con);
        con.insets = new Insets(3, 3, 3, 3);
        inputs = new TablePanel("Inputs");
        for (ArgumentDescription at : ct.getArgDef()) {
            ArgumentHolder ah = inputs.addItem(at);
            if (ah != null) {
                map.put(at, ah);
            }
        }
        addInputAndOutput(inputs);
        if (loadDefaultValues) {
            for (Entry<ArgumentDescription, ArgumentHolder> ent : map.entrySet()) {
                String value = ent.getKey().getDefaultValue();
                if (value != null && value.length() > 0) {
                    ent.getValue().setValue(value);
                }
            }
        }
    }

    public ComponentGUI(WorkflowTask pc) {
        this(pc.getPluginDescription(), false);
        for (BoundArgumentValue ab : pc.getArgs()) {
            ArgumentHolder ah = map.get(ab.getArg());
            if (ah != null && ab.getValue() != null) {
                ah.setValue(ab.getValue());
            }
        }
        String comment = pc.getComment();
        if (comment != null && comment.length() > 0) {
            this.comment = comment;
        }
    }

    private void addInputAndOutput(TablePanel inputs) {
        if (inputs.hasUserInputs()) {
            con.gridy++;
            this.add(inputs, con);
        }
    }

    public void setParent(ListPanel parent) {
        this.parent = parent;
    }

    public void addToInput(Component c) {
        inputs.addItem(c, null);
    }

    public void setInputValue(Integer id, String internalId, String value) {
        inputs.setValue(id, internalId, value);
    }

    public void setInputByName(String name, String value) {
        inputs.setGeneratedDefaultValue(name, value);
    }

    @Override
    public void validate() {
        inputs.fitContent();
        super.validate();
    }

    public void setReadyState() {
        titlePanel.setBackdrop(STATE.READY);
        titlePanel.repaint();
    }

    public void setErrorState() {
        titlePanel.setBackdrop(STATE.FAILED);
        titlePanel.repaint();
    }

    public void setRunningState() {
        titlePanel.setBackdrop(STATE.RUNNING);
        titlePanel.repaint();
    }

    public void setCompletedState() {
        titlePanel.setBackdrop(STATE.DONE);
        titlePanel.repaint();
    }

    private class TitlePanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private JLabel name;
        private Color c;
        //private Color BASE_COLOR;

        public TitlePanel(String title) {
            super(new GridBagLayout());

            delete = new JButton(ICON_CLOSE);
            delete.setPreferredSize(new Dimension(20, 20));
            delete.setMinimumSize(new Dimension(20, 20));
            GridBagConstraints subc = new GridBagConstraints();
            subc.fill = GridBagConstraints.HORIZONTAL;
            subc.anchor = GridBagConstraints.WEST;
            subc.weightx = 1;
            subc.gridx = 0;
            subc.gridy = 0;
            name = new JLabel(" " + title);
            setBackdrop(STATE.READY);
            name.setFont(name.getFont().deriveFont(Font.BOLD));
            this.add(name, subc);

            subc.fill = GridBagConstraints.NONE;
            subc.anchor = GridBagConstraints.EAST;
            subc.gridx = 2;
            subc.gridy = 0;
            delete.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    parent.deleteComponent(ComponentGUI.this);
                }
            });
            this.add(delete, subc);
        }

        public void setBackdrop(STATE s) {
            switch (s) {
                case READY:
                    c = new Color(0, 102, 255);
                    break;
                case RUNNING:
                    c = new Color(255, 204, 0);
                    break;
                case DONE:
                    c = new Color(0, 153, 51);
                    break;
                case FAILED:
                    c = new Color(255, 0, 0);
                    break;
                default:
                    c = new Color(0, 102, 255);
            }
            name.setForeground(c);
        }

        @Override
        protected void paintComponent(Graphics g) {
            /*
               BASE_COLOR = titlePanel.getBackground();
               Graphics2D g2d = (Graphics2D)g;
               GradientPaint gradient = new GradientPaint(0, 0, c, 0, getHeight(), BASE_COLOR);
               g2d.setPaint(gradient);
               g2d.fill( new Rectangle2D.Double( 0, 0, getWidth(), getHeight() ) );
               */
        }
    }

    private static class CloseIcon implements Icon {
        private int width = 18;
        private int height = 18;
        private int inset = 6;
        private final BasicStroke stroke = new BasicStroke(2);

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.black);
            g2d.setStroke(stroke);
            g2d.drawLine(x + inset, y + inset, x + width - inset, y + height - inset);
            g2d.drawLine(x + inset, y + height - inset, x + width - inset, y + inset);
            g2d.dispose();
        }

        @Override
        public int getIconHeight() {
            return height;
        }

        @Override
        public int getIconWidth() {
            return width;
        }
    }

    public WorkflowTask getPluginConfig() {
        WorkflowTask p = new WorkflowTask(ct, inputs.getContainerData());
        p.setComment(this.comment);
        return p;
    }

    public void focusOn(String argId) {
        inputs.focusOn(WorkflowDescriptionIO.getArgumentDescriptionByName(ct, argId));
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDocumentation() {
        return PluginDocumentationFactory.getDocumentation(ct);
    }

    public String[] getPluginId() {
        return new String[]{ct.getOndexType().toString(), ct.getOndexId()};
    }

    public String getGraphRefId() {
        for (BoundArgumentValue pair : inputs.getContainerData()) {
            if (pair.getArg().getInteranlName().equalsIgnoreCase("graphId")) {
                return pair.getValue();
            }
        }
        return null;
    }
}
