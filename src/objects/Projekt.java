package objects;

public class Projekt {

	private String name;
	private String teilnehmer;
	private int projektID;
	private Nutzer projektLeiter;
	private boolean chat; //darf gechattet werden?
	private boolean akzeptiert; //habe ich (der Betreuer) es schon akzeptiert (für den Fall, dass andere Betreuer es noch nicht getan haben)
	
	//Konstruktor für MEINE Projekte
	public Projekt(String name, String teilnehmer, int projektID, Nutzer projektLeiter, boolean chat, boolean akzeptiert) {
		this.name = name;
		this.teilnehmer = teilnehmer;
		this.projektID = projektID;
		this.projektLeiter = projektLeiter;
		this.chat = chat;
		this.akzeptiert = akzeptiert;
	}
	
	//Konstruktor für ALLE Projekte
	public Projekt(String name, int projektID, Nutzer projektLeiter, boolean chat) {
		this.name = name;
		this.teilnehmer = null;
		this.projektID = projektID;
		this.projektLeiter = projektLeiter;
		this.chat = chat;
	}
	
	//callback beim Projekt erstellen
	public Projekt(int projektID, String name) {
		this.projektID = projektID;
		this.name = name;
	}
	
	//umbenennen in ProjektOpen
	public void update(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getTeilnehmer() {
		return teilnehmer;
	}
	
	public boolean isChat() {
		return chat;
	}
	
	public boolean isAkzeptiert() {
		return akzeptiert;
	}
	
	public int getProjektID() {
		return projektID;
	}
	
	public Nutzer getProjektLeiter() {
		return projektLeiter;
	}
	
	public String getChatIcon() {
		return chat ? "fa-commenting-o" : "fa-times ";
	}

}
