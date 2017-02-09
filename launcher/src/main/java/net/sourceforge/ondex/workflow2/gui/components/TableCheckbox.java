package net.sourceforge.ondex.workflow2.gui.components;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.UIManager;

import net.sourceforge.ondex.init.ArgumentDescription;
import net.sourceforge.ondex.workflow.model.BoundArgumentValue;
import net.sourceforge.ondex.workflow2.gui.arg.ArgumentContainer;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightHandler;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightManager;
import net.sourceforge.ondex.workflow2.gui.components.highlighting.HighlightableComponent;

/**
 * @author lysenkoa
 */
public class TableCheckbox extends JCheckBox implements ArgumentContainer, HighlightableComponent, FocusListener, KeyListener {

    private static final long serialVersionUID = 1L;
    private ArgumentDescription at;
    private final HighlightHandler hlh;

    public TableCheckbox(ArgumentDescription at) {
        super();
        this.at = at;
        this.hlh = HighlightManager.getInstance().getHighlighter(this);
        if (at.getDescription() != null && !at.getDescription().equals("")){
            //this.setToolTipText(at.getDescription());
            this.setBackground(Color.WHITE);
        }
        this.setHorizontalAlignment(CENTER);
        this.addFocusListener(this);
        this.setBorderPaintedFlat(true);
        this.setBorder(BorderFactory.createLineBorder((Color)UIManager.getDefaults().get("CheckBox.focus")));

        //if(!Boolean.valueOf(at.getRequired()))this.setFont(this.getFont().deriveFont(Font.ITALIC));
    }


    @Override
    public BoundArgumentValue getContent() {
        return new BoundArgumentValue(at, String.valueOf(this.isSelected()));
    }

    public void setValue(String value) {
        if (value == null) return;
        this.setSelected(Boolean.valueOf(value));
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


	@Override
	public void focusGained(FocusEvent e) {
		this.setBorderPainted(true);
	}


	@Override
	public void focusLost(FocusEvent e) {
		this.setBorderPainted(false);		
	}


	@Override
	public void keyTyped(KeyEvent e) {
		if(e.getKeyCode() == 0){
			this.doClick();
		}
		
	}


	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
