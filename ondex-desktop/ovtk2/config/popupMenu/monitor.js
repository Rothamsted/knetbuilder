importPackage(net.sourceforge.ondex.tools.threading.monitoring)
importPackage(net.sourceforge.ondex.ovtk2.util)

/* usage: only use one single monitor at a time

	// show progress bar
	startMonitor("download and search in full text", getPickedConcepts().size()*3+1)
	...
	if(!nextState("...")) return
	...
	monitorComplete()
*/

function startMonitor(title, totalSteps) {
	monitor = new SimpleMonitor("initializing...", totalSteps)
	OVTKProgressMonitor.start(title, monitor)
	
}

function nextState(text) {
	if(monitor==null) return
		
	// check and update progress bar
	if(monitor.getState()==Monitorable.STATE_TERMINAL)
		return false
	monitor.next(text)
	return true
}

function monitorComplete() {
	if(monitor==null) return
	monitor.complete()
}
