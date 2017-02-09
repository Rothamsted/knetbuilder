package net.sourceforge.ondex.ovtk2.ui.mouse;

import edu.uci.ics.jung.visualization.control.ScalingControl;

/**
 * Interface for mouse cross compatibility between desktop and applet.
 * 
 * @author taubertj
 * 
 */
public interface OVTK2GraphMouse {

	/**
	 * Returns OVTK2 specific picking plug-in with mouse over features
	 * 
	 * @return
	 */
	public OVTK2PickingMousePlugin getOVTK2PickingMousePlugin();

	/**
	 * Sets view scaling enabled or disabled.
	 * 
	 * @param enabled
	 */
	public void setViewScaling(boolean enabled);

	/**
	 * @return the scaler
	 */
	public ScalingControl getScaler();

	/**
	 * @param scaler
	 *            the scaler to set
	 */
	public void setScaler(ScalingControl scaler);
}
