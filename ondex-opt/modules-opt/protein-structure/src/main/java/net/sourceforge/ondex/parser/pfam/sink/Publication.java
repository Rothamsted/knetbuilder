package net.sourceforge.ondex.parser.pfam.sink;
/**
 * stores a publication link with journal and title name
 * 
 * @author peschr
 * 
 */
public class Publication {
	private String id;
	private String title;
	private String journal;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getJournal() {
		return journal;
	}
	public void setJournal(String journal) {
		this.journal = journal;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
