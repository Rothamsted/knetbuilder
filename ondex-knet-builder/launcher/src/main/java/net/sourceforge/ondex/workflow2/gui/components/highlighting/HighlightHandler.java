package net.sourceforge.ondex.workflow2.gui.components.highlighting;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class HighlightHandler implements FocusListener{
	private final Component c;
	private Color original;  
	private boolean isHighlighted = false;
	protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList(); 
	
	public HighlightHandler(Component c){
		this.c = c;
		c.addFocusListener(this);
	}

	public void highlight(){
		if(!isHighlighted){
			isHighlighted = true;
			original = c.getBackground();
			c.setBackground(Color.red);
			c.validate();
			fireHighlightEvent(new HighlightEvent(this, isHighlighted));
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		resetHighlgiht();
	}
	
	public void resetHighlgiht(){
		if(isHighlighted){
			isHighlighted = false;	 
			c.setBackground(original);
			c.validate();
			fireHighlightEvent(new HighlightEvent(this, false));	
		}
		
	}

	@Override
	public void focusLost(FocusEvent e) {}
	
	public void addHighlightEventListener(HighlightListener listener) {
		listenerList.add(HighlightListener.class, listener); 
	} 
	public void removeHighlightEventListener(HighlightListener listener) {
		listenerList.remove(HighlightListener.class, listener); 
	} 
	
	void fireHighlightEvent(HighlightEvent evt) {
		Object[] listeners = listenerList.getListenerList();
		for (int i=0; i<listeners.length; i+=2) {
			if (listeners[i]==HighlightListener.class) { 
				((HighlightListener)listeners[i+1]).HighlightEventOccurred(evt); 
			} 
		} 
	} 
}
