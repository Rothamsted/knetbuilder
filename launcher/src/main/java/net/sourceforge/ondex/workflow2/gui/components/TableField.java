package net.sourceforge.ondex.workflow2.gui.components;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.workflow.model.BoundArgumentValue;
import net.sourceforge.ondex.workflow2.gui.arg.ArgumentContainer;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightHandler;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightManager;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightableComponent;

/**
 * @author lysenkoa
 */
public class TableField extends JPanel implements ArgumentContainer, KeyListener, FocusListener, HighlightableComponent {

    private static final long serialVersionUID = -2905385403930611079L;
    private static JFileChooser fc = new JFileChooser();
    private ArgumentDescription at;
    private JTextField tf = new JTextField();
    private JButton btn = new JButton("Browse");
    private final HighlightHandler hlh;

    public TableField(ArgumentDescription at) {
        this.at = at;
        this.setBorder(null);
        this.hlh = HighlightManager.getInstance().getHighlighter(tf);
        if (at.getDescription() != null && !at.getDescription().equals("")){
            //tf.setToolTipText(at.getDescription());
            tf.setBorder(null);
        }
        tf.setEditable(true);
        parseHint();
        tf.addKeyListener(this);
    }


    @Override
    public BoundArgumentValue getContent() {
        return new BoundArgumentValue(at, getValue());
    }

    public void setValue(String value) {
        tf.setText(value);
        tf.validate();
    }

    public JTextField _getTextField() {
        return tf;
    }

    public void parseHint() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 0;
        con.weighty = 1;
        con.weightx = 1;
        con.fill = GridBagConstraints.BOTH;
        this.add(tf, con);
        String hint = at.getContentHint();
        if (hint == null)
            return;
        if (hint.startsWith("browse")) {
            con.gridx = 1;
            con.weightx = 0;
            this.add(btn, con);
            if (hint.startsWith("browse_file")) {
                btn.addActionListener(new OpenFile());
            } else if (hint.startsWith("browse_folder")) {
                btn.addActionListener(new OpenFolder());
            }
        } else if (at.isOutputObject() || at.isInputObject()) {
            this.setForeground(Color.blue);
        }
    }

    @Override
    public ArgumentDescription getArgumentTemplate() {
        return at;
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == '\t') {
            String text = tf.getText();
            String value = text.substring(0, tf.getCaretPosition()) + "\t" + text.substring(tf.getCaretPosition());
            //value = Config.workingDirToVar(value);
            tf.setText(value);
        } else {

            //String value = tf.getText();
            //String temp = Config.workingDirToVar(value);
            //if(temp != value)
            //	tf.setText(temp);
        }
    }

    public String getValue() {
        return tf.getText();
    }

    private class OpenFolder implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            String folder = TableField.this.tf.getText();
            File dir = null;
            if (folder != null && !folder.equals("")) dir = new File(folder);
            if (dir != null) {
                if (!dir.exists() && Config.ondexDir != null) dir = new File(Config.ondexDir);
            } else if (Config.ondexDir != null) dir = new File(Config.ondexDir);
            if (dir != null) fc.setCurrentDirectory(dir);
            fc.setDialogTitle("Choose directory");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            int returnVal = fc.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                tf.setText(fc.getSelectedFile().getAbsolutePath());
            }
        }
    }

    private class OpenFile implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            String file = TableField.this.tf.getText();
            File dir = null;
            if (file != null && file.length() > 0) dir = new File(file);
            if (dir != null && dir.exists()) fc.setCurrentDirectory(dir);
            fc.setDialogTitle("Specify file");
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setAcceptAllFileFilterUsed(true);
            int returnVal = fc.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                tf.setText(fc.getSelectedFile().getAbsolutePath());
            }
        }
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    @Override
    public void setForeground(Color color) {
        if (tf != null)
            tf.setForeground(color);
    }

    public void focusLost(FocusEvent e) {
        String value = tf.getText();
        tf.setText(value);
    }

    public void focusGained(FocusEvent e) {
    	System.err.println("Focus gained");
    }

    @Override
    public void highlight() {
        hlh.highlight();
    }
    
    @Override
    public void addKeyListener(KeyListener l){
    	super.addKeyListener(l);
    	tf.addKeyListener(l);
    	btn.addKeyListener(l);
    }


	@Override
	public void sendFocus() {
		tf.dispatchEvent(new FocusEvent(tf, FocusEvent.FOCUS_GAINED));
	}
}

