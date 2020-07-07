package net.sourceforge.ondex.parser.ecocyc.parse.readers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;

import net.sourceforge.ondex.parser.ecocyc.objects.AbstractNode;
import net.sourceforge.ondex.parser.ecocyc.parse.IParser;

/**
 * defines a framework for iterating over a file
 * @author peschr
 */
public abstract class AbstractReader  implements Iterator<AbstractNode> {
	protected BufferedReader buffReader;
	protected IParser parser;
	public AbstractReader(String fileName, IParser parser) throws FileNotFoundException{
		buffReader = new BufferedReader(new FileReader(fileName) );
		this.parser = parser;
	}
	/**
	 * returns the next AbstractNode in the current parsed file
	 */
	public AbstractNode next() {
		return parser.getNode();
	}
	/**
	 * UnsupportedOperation
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
