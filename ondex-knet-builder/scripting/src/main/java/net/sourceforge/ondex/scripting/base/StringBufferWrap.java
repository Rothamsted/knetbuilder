package net.sourceforge.ondex.scripting.base;

public class StringBufferWrap {
	private final StringBuffer sb = new StringBuffer();
	
	public StringBufferWrap(){
		
	}
	
	public void append(final String ...strings){
		for(String s : strings){
			sb.append(s);
		}
	}
	
	@Override
	public String toString(){
		return sb.toString();
	}
}
