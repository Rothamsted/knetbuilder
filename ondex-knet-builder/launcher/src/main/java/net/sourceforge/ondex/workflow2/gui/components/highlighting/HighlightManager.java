package net.sourceforge.ondex.workflow2.gui.components.highlighting;

import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class HighlightManager implements HighlightListener, WindowListener{
	private static HighlightManager instance;
	private HighlightHandler current;
	
	private HighlightManager(){}
	
	public static synchronized HighlightManager getInstance(){
		if (instance == null){
			instance = new HighlightManager();
		}
		return instance;
	}
	
	public HighlightHandler getHighlighter(Component c){
		HighlightHandler h = new HighlightHandler(c);
		h.addHighlightEventListener(this);
		return h;
	}

	@Override
	public void HighlightEventOccurred(HighlightEvent evt) {
		if(evt.isCurrentlyHighlighted()){
			if(current != null)
				current.resetHighlgiht();
			current = (HighlightHandler)evt.getSource();
		}
	}
	
	public void removeHighlight(){
		if(current != null){
			current.resetHighlgiht();
			current = null;
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		removeHighlight();			
	}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}
}
