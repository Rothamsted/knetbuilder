package net.sourceforge.ondex.parser.uniprot.sink;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author peschr
 *
 */
public class Publication {
	
	private String journalName;
	private int year;
	private String firstPage;
	private String lastPage;
	private String title;
	private String volume;
	private List<DbLink> references = new ArrayList<DbLink> ();
	private List<String> scopes = new ArrayList<String> ();
	private boolean isLargeScalePaper = false;


	public String getJournalName() {
		return journalName;
	}
	public void setJournalName(String journalName) {
		this.journalName = journalName;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public List<DbLink> getReferences() {
		return references;
	}
	public void addReference(DbLink dbLink){
		this.references.add(dbLink);
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	public String getLastPage() {
		return lastPage;
	}
	public void setLastPage(String lastPage) {
		this.lastPage = lastPage;
	}
	public String getFirstPage() {
		return firstPage;
	}
	public void setFirstPage(String firstPage) {
		this.firstPage = firstPage;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public String toString(){
		return this.title;
	}
	public List<String> getScopes() {
		return scopes;
	}
	public void addScope(String scope) {
		this.scopes.add(scope);
	}
	public boolean isLargeScalePaper() {
		return isLargeScalePaper;
	}
	public void setLargeScalePaper(boolean isLargeScalePaper) {
		this.isLargeScalePaper = isLargeScalePaper;
	}
}
