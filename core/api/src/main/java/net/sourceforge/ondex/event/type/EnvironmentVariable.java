package net.sourceforge.ondex.event.type;

public class EnvironmentVariable extends EventType {

	public EnvironmentVariable(String message) {
		this(message,"");
	}
	
	public EnvironmentVariable(String message, String extension) {
		super(message,extension);
		super.desc = "Output of an environment variable.";
	}

}
