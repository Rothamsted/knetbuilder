package net.sourceforge.ondex.parser.aracyc.objects;

import java.util.ArrayList;

/**
 * Represents a publication
 * @author peschr
 *
 */
public class Publication extends AbstractNode{
	private String title;
	private ArrayList<String> authors = new ArrayList<String>();
	private int pubMedId;
	
	public int getPubMedId() {
		return pubMedId;
	}

	public void setPubMedId(int pubMedId) {
		this.pubMedId = pubMedId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	public void addAuthor(String author){
		this.authors.add(author);
	}
	public ArrayList<String> getAuthors() {
		return authors;
	}
}
