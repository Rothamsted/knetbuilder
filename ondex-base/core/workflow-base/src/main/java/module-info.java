 module net.sourceforge.ondex.workflow.base {
	exports net.sourceforge.ondex.workflow.model;
	exports net.sourceforge.ondex.workflow.engine;
	exports net.sourceforge.ondex.workflow.validation;
	 requires java.desktop;
	requires java.activation;
	requires base;
	requires workflow.api;
	requires api;
	requires commons.cli;
	requires org.apache.commons.io;
	requires velocity;
	requires java.xml.bind;
	requires org.codehaus.stax2;
	requires memory;
	requires lucene;
	requires tools;
	//requires jdom;
	requires slf4j.api;
	requires org.jdom2;
	//requires jdom2;
	opens net.sourceforge.ondex.workflow.model to net.sourceforge.ondex.launcher;
	opens net.sourceforge.ondex.workflow.engine to net.sourceforge.ondex.launcher;
	
	opens net.sourceforge.ondex.workflow.validation to net.sourceforge.ondex.launcher;
	
}