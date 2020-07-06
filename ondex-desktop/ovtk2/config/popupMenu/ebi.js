
function runJobPool(client, params, title, proteins) {
	
	startMonitor(title,proteins.size()*2+1+1)
	var jobAddedCount=1
	var jobFinishedCount=0
	var jobTotal=proteins.size()
	
	var jobPool=initJobPool(10)

	// fill up job pool
	var i = proteins.iterator()
	while(i.hasNext()) {
		var protein=i.next()
		
		if(getFreeSlot(client,jobPool)==-1) {
			if(!nextState("waiting for results... ("+(jobFinishedCount++)+" of "+jobTotal+" jobs done)")) return
			var index=waitForFinishedJob(client,jobPool)
			evaluateFinishedJob(client, jobPool, index)
			jobPool[index]=null
			printJobPool(jobPool)
		}
		if(!nextState("starting job "+(jobAddedCount++)+" of "+jobTotal)) return
		addToJobPool(client, params,jobPool,protein)
		printJobPool(jobPool)
	}
	
	// wait for job pool to run dry
	while( true ) {
		if(!nextState("waiting for results... ("+(jobFinishedCount++)+" of "+jobTotal+" jobs done)")) return
		var index = waitForFinishedJob(client,jobPool)
		if(index==-1) break
		evaluateFinishedJob(client, jobPool, index)
		jobPool[index]=null	
		printJobPool(jobPool)
	}
}



function printJobPool(jobPool) {
	out("------ job pool state "+ new Date().toString()+ " --------")
	
	var i=0
	while(i<jobPool.length) {
		out("slot "+i+"\t"+(jobPool[i]==null?"-":jobPool[i][0]))
		i++
	}
	
}

function getFreeSlot(client, jobPool) {
	var i=0
	while(i<jobPool.length && jobPool[i]!=null)
		i++
	if(i==jobPool.length)
		return -1
	return i
	
}

function isEmpty(jobPool) {
	var i=0
	while(i<jobPool.length && jobPool[i]==null)
		i++
	if(i==jobPool.length)
		return true
	return false	
}

function addToJobPool(client,params,jobPool,protein) {
	
	// ask for mail once a session
	if(typeof(email)=="undefined")
		email = prompt("In case of accidental over-usage, EBI is going to ban your IP address.\nPlease provide your email adress to allow EBI to contact you.")

	var slot = getFreeSlot(client, jobPool)
	if(slot==-1) return

	var seq = protein.getAttribute("AA").getValue()
	params.setSequence(seq)
	
	jobPool[slot] = Array(client.runApp(email, null, params), protein)
}

function waitForFinishedJob(client,jobPool) {
	if(isEmpty(jobPool))
		return -1
	var slot = findJobNotRunning(client, jobPool)
	if(slot!=-1) {
		if("FINISHED"!=client.checkStatus(jobPool[slot][0]))
			alert("job not successful: "+client.checkStatus(jobPool[slot][0]))
		return slot
	}
	
}

iSlot=0	// global
function findJobNotRunning(client, jobPool) {
	while(true) {
		while(iSlot<jobPool.length) {
			if( jobPool[iSlot]!=null && jobPool[iSlot][0]!=null && "RUNNING"!=client.checkStatus(jobPool[iSlot][0]) )
				return iSlot
			iSlot++
		}
		java.lang.Thread.sleep(1000)
		java.lang.System.out.print(".")
		iSlot=0
	}
	return -1
}

function initJobPool(size) {
	var jobPool = Array()
	for(var i=0;i<size;i++)
		jobPool[i]=null
	return jobPool
}

function getJobResults(client, jobPool, index, format, basePath) {
	
	var f=new java.io.File(basePath)
	if(!f.exists())
		f.mkdir()
	
	// job and corresponding protein
	var job = jobPool[index][0]
	var protein = jobPool[index][1]
	
	var path = basePath + protein.getConceptName().getName()+"-"+job
	client.getResults(job,path,format)
	path=path+"."+format+"."+format.substring(format.length-3)
	if(format=="xml")
		new java.io.File(path).deleteOnExit()

	return path
	
}