package net.sourceforge.ondex.workflow2.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import net.sourceforge.ondex.workflow.engine.BasicJobImpl;
import net.sourceforge.ondex.workflow.engine.JobExecutor;
import net.sourceforge.ondex.workflow.engine.JobProgressObserver;
import net.sourceforge.ondex.workflow.engine.ResourcePool;
import net.sourceforge.ondex.workflow.model.WorkflowDescription;
import net.sourceforge.ondex.workflow.model.WorkflowTask;
import net.sourceforge.ondex.workflow.validation.ErrorReport;
import net.sourceforge.ondex.workflow.validation.PluginValidator;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightManager;

import org.apache.log4j.Logger;

/**
 * @author lysenkoa
 */
public class ListPanelContainer extends JPanel implements JobProgressObserver {
    private final Logger LOG = Logger.getLogger(ListPanelContainer.class);

    private static final long serialVersionUID = 1L;
    private static final Color runningColor = new Color(236, 153, 0);
    private final JTabbedPane parentTab;
    private final Map<Object, ComponentGUI> map = new HashMap<Object, ComponentGUI>();
    private final ListPanel content;
    private Thread runningTread = null;
    private JobExecutor job = null;
    private static Cursor curNormal = new Cursor(Cursor.DEFAULT_CURSOR);
    private static Cursor curWait = new Cursor(Cursor.WAIT_CURSOR);
    private final PluginValidator pv = new PluginValidator();
    private JScrollPane listScroller1;
    private File sourceFile;
    private boolean isDefault = false;
    private final JLabel namingComponent = new JLabel();

	public void setSaved(boolean b){
		if(b){
			namingComponent.setText(namingComponent.getText().replaceAll("\\*", ""));
		}
		else{
			if(!namingComponent.getText().startsWith("*")){
				namingComponent.setText("* "+namingComponent.getText());
			}
		}
		namingComponent.updateUI();
	}

	public JLabel getNamingComponent() {
		return namingComponent;
	}

	public ListPanelContainer(JTabbedPane parentTab, File f, String name) {
        super(new GridBagLayout());
        namingComponent.setText(name);
        this.sourceFile = f;
        this.parentTab = parentTab;
        GridBagConstraints con = new GridBagConstraints();
        con.fill = GridBagConstraints.BOTH;
        con.weightx = 1;
        con.weighty = 1;
        con.gridx = 0;
        con.gridy = 0;
        con.insets = new Insets(2, 2, 3, 3);
        content = new ListPanel(this);
        listScroller1 = new JScrollPane(content);
        listScroller1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        listScroller1.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(listScroller1, con);
    }
    
    
    
    public void setSourceFile(File f){
    	namingComponent.setText(f.getName());
    	this.sourceFile = f;
    }
    
    public File getSourceFile(){
    	return this.sourceFile;
    }

    public ListPanel getListPanel() {
        return content;
    }

    void resetAll() {
        for (Object o : content.getContent()) {
            if (o instanceof ComponentGUI) {
                ((ComponentGUI) o).setReadyState();
            }
        }
    }

    @Override
    public void notifyComplete(JobExecutor je) {
        SwingUtilities.invokeLater(new JobFinishedCleanup(je));

    }

    @Override
    public void notifyStageComplete(final Object callbackRef) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ComponentGUI g = map.get(callbackRef);
                if (g != null){
                	g.setCompletedState();
                	parentTab.repaint();
                }
            }
        });
    }

    @Override
    public void notifyStageError(final Object callbackRef) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ComponentGUI g = map.get(callbackRef);
                if (g != null){
                	g.setErrorState();
                	 parentTab.repaint();
                }
                    
            }
        });
    }

    @Override
    public void notifyStageStarted(final Object callbackRef) {
    	SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
                ComponentGUI g = map.get(callbackRef);
                if (g != null){
                	g.setRunningState();
                	parentTab.repaint();
                }
			}
    	});


    }

    private class JobFinishedCleanup implements Runnable {
        private JobExecutor internal;

        public JobFinishedCleanup(JobExecutor je) {
            this.internal = je;
        }

        public void run() {
            int index = parentTab.indexOfComponent(ListPanelContainer.this);
            if (index < 0)
                return;
            if (internal.getErrorState()) {
                LOG.error("Workflow terminated", internal.getException());
                parentTab.setForegroundAt(index, Color.RED);
                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                JLabel l1 = new JLabel("The plugin '" + internal.getCurrentPluginName() + "' encountered an error, leading to the controlled termination of your workflow.");
                p.add(l1, Component.LEFT_ALIGNMENT);
                JLabel l2 = new JLabel("Please refer to the error information returned by the plugin to identify possible cause(s): ");
                p.add(l2, Component.LEFT_ALIGNMENT);
                JTextArea textArea = new JTextArea();
                textArea.setBackground(p.getBackground());
                textArea.setEditable(false);
                if (internal.getException().getMessage() != null) {
                    textArea.append(internal.getException().getMessage() + "\n");
                }
                textArea.append(internal.getException().toString() + "\n");
                for (StackTraceElement el : internal.getException().getStackTrace()) {
                    textArea.append(el.toString() + "\n");
                }
                JScrollPane scrollPane = new JScrollPane(textArea);
                setPreferredSize(new Dimension(250, 250));
                p.add(scrollPane, Component.LEFT_ALIGNMENT);
                JOptionPane.showMessageDialog(null, p, "Plugin configuration problem", JOptionPane.WARNING_MESSAGE);
            } else {
                parentTab.setForegroundAt(index, Color.BLACK);
            }
            parentTab.repaint();
            resetAll();
            ListPanelContainer.this.runningTread = null;
            ListPanelContainer.this.job = null;
            ListPanelContainer.this.setCursor(curNormal);
        }
    }

    public void execute() throws Exception {
        this.resetAll();
        this.setCursor(curWait);
        int index = parentTab.indexOfComponent(ListPanelContainer.this);
        if (index < 0)
            return;
        parentTab.setForegroundAt(index, runningColor);
        BasicJobImpl job = new BasicJobImpl(new ResourcePool());
        this.job = job;
        buildTask().toOndexJob(job);
        job.addObserver(this, true);
        new Thread(job).start();
    }
    
    public void executePlus(WorkflowTask ... tasks) throws Exception {
        this.resetAll();
        this.setCursor(curWait);
        int index = parentTab.indexOfComponent(ListPanelContainer.this);
        if (index < 0)
            return;
        parentTab.setForegroundAt(index, runningColor);
        BasicJobImpl job = new BasicJobImpl(new ResourcePool());
        this.job = job;
        WorkflowDescription  desc = buildTask();
        for(WorkflowTask t : tasks){
        	desc.addPlugin(t);
        }
        desc.toOndexJob(job);
        job.addObserver(this, true);
        new Thread(job).start();
    }

    private WorkflowDescription buildTask() {
        WorkflowDescription task = new WorkflowDescription();
        for (Object o : this.getListPanel().getContent()) {
            ComponentGUI g = ((ComponentGUI) o);
            WorkflowTask conf = g.getPluginConfig();
            map.put(conf, g);
            task.addPlugin(conf);
        }
        return task;
    }
    
    public boolean isEmpty(){
    	return this.getListPanel().getContent().size() == 0;
    }

    @SuppressWarnings("unchecked")
    public boolean validateConfiguration() throws Exception {
        List<Object> list = this.getListPanel().getContent();
        List<ErrorReport> rs = new ArrayList<ErrorReport>();
        for (int i = 0; i < list.size(); i++) {
            ComponentGUI g = ((ComponentGUI) list.get(i));
            WorkflowTask conf = g.getPluginConfig();
            rs.addAll(pv.check(conf, i));
        }
        pv.clear();
        JFrame validationReport = new JFrame("Validation report");
        validationReport.addWindowListener(HighlightManager.getInstance());
        validationReport.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        validationReport.setLayout(new BorderLayout());
        validationReport.setResizable(false);
        validationReport.setMinimumSize(new Dimension(500, 400));
        validationReport.setMaximumSize(new Dimension(500, 400));
        validationReport.setSize(new Dimension(500, 400));
        int x = (Toolkit.getDefaultToolkit().getScreenSize().width - validationReport.getWidth()) / 2;
        int y = (Toolkit.getDefaultToolkit().getScreenSize().height - validationReport.getHeight()) / 2;
        validationReport.setLocation(x, y);

        if (rs.size() > 0) {
            JPanel p = new JPanel(new GridBagLayout());
            JScrollPane sp = new JScrollPane(p, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            validationReport.add(sp, BorderLayout.CENTER);
            GridBagConstraints con1 = new GridBagConstraints();
            con1.insets = new Insets(3, 3, 3, 3);
            con1.anchor = GridBagConstraints.NORTHWEST;
            Color bg = validationReport.getBackground();
            for (int i = 0; i < rs.size(); i++) {
                ErrorReport er = rs.get(i);
                con1.fill = GridBagConstraints.HORIZONTAL;
                con1.gridx = 0;
                con1.weightx = 1.0;
                con1.weighty = 0.0;
                con1.gridy = i;
                JTextArea t = new JTextArea("Problem found with " + er.getPluginName() + " plugin at position " + (er.getPosition() + 1) + " [" + er.getArgId() + "]: " + er.getMessage());
                t.setFont(t.getFont().deriveFont(Font.BOLD));
                t.setEditable(false);
                t.setBackground(bg);
                t.setWrapStyleWord(true);
                t.setBorder(BorderFactory.createEmptyBorder());
                t.setLineWrap(true);
                p.add(t, con1);
                con1.weightx = 0.0;
                con1.fill = GridBagConstraints.NONE;
                con1.gridx = 1;
                JButton goTo = new JButton(">>");
                p.add(goTo, con1);
                goTo.addActionListener(new GoToActionListener((ComponentGUI) list.get(er.getPosition()), er.getArgId()));
            }
            con1.gridy = (rs.size());
            con1.weighty = 1.0;
            p.add(new JPanel(), con1);
            validationReport.pack();
            validationReport.setVisible(true);

        }
        return rs.size() == 0;
    }

    private class GoToActionListener implements ActionListener {
        private final ComponentGUI p;
        private final String attId;

        public GoToActionListener(ComponentGUI plugin, String attId) {
            this.p = plugin;
            this.attId = attId;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JComponent parent = (JComponent) p.getParent();
            Rectangle r = parent.getBounds();
            r.x = parent.getX();
            r.y = parent.getY();
            parent.scrollRectToVisible(r);
            p.focusOn(attId);
        }
    }

    public void finalize() throws Throwable {
        super.finalize();
        if (runningTread != null && runningTread.isAlive()) {
            try {
                job.terminate();
            }
            catch (Exception e) {
            }
        }
    }
    
    public boolean isDefault() {
		return isDefault;
	}

    public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
}
