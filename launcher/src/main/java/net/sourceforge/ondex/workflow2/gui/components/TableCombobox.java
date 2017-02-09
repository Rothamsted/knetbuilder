package net.sourceforge.ondex.workflow2.gui.components;

import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.workflow.model.BoundArgumentValue;
import net.sourceforge.ondex.workflow2.gui.arg.ArgumentContainer;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightHandler;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightManager;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightableComponent;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import java.awt.*;
import java.awt.event.FocusEvent;

/**
 * @author lysenkoa
 */
public class TableCombobox extends JComboBox implements ArgumentContainer, HighlightableComponent {

    private static final long serialVersionUID = -290538611079L;
    private ArgumentDescription at;
    private final HighlightHandler hlh;

    public TableCombobox(ArgumentDescription at) {
        this.at = at;
        this.hlh = HighlightManager.getInstance().getHighlighter(this);
        /*
          JLabel l = new JLabel(at.getName());
          if(!Boolean.valueOf(at.getRequired()))l.setFont(l.getFont().deriveFont(Font.ITALIC));
          this.add(l);
          */
        this.setBorder(null);
        this.setBackground(Color.WHITE);
        if (at.getDescription() != null && !at.getDescription().equals("")){
            //this.setToolTipText(at.getDescription());
            this.setEditable(true);
        }
        parseHint();
    }

    @Override
    public BoundArgumentValue getContent() {
        return new BoundArgumentValue(at, this.getSelectedItem().toString());
    }

    public void setValue(String value) {
        if (value == null) return;
        this.insertItemAt(value, 0);
        this.setSelectedIndex(0);
    }

    public void parseHint() {
        String hint = at.getContentHint();
        if (hint != null) {
            if (hint.startsWith("list_strict"))
                this.setEditable(false);
            else
                this.setEditable(true);
            String[] contents = hint.substring(hint.indexOf("[") + 1, hint.indexOf("]")).split(",");
            int i = 0;
            for (String content : contents) {
                this.insertItemAt(content, i++);
            }
            this.setSelectedIndex(0);
        } else {
            this.setEditable(true);
        }
    }

    @Override
    public ArgumentDescription getArgumentTemplate() {
        return at;
    }

    @Override
    public void highlight() {
        hlh.highlight();
    }

	@Override
	public void sendFocus() {
		this.dispatchEvent(new FocusEvent(this, FocusEvent.FOCUS_GAINED));
	}
}
