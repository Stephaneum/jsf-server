package objects;

import java.io.Serializable;

import tools.Zugangscodes;

public class Nutzer implements Serializable{
	private static final long serialVersionUID = 3653446250608454549L;
	
	final public static int RANG_UNDEFINED = -2, RANG_GAST_NO_LOGIN = -1, RANG_SCHUELER = 0, RANG_LEHRER = 1, RANG_GAST = 2, RANG_ADMIN = 100;
	final public static String STR_MANN = "männlich", STR_FRAU = "weiblich", STR_UNBEKANNT = "keine Angabe";
	final public static int GESCHLECHT_MANN = 0, GESCHLECHT_FRAU = 1, GESCHLECHT_UNBEKANNT = 2;
	
	private static final String STRING_MANN = "Herr", STRING_FRAU = "Frau";
	
	private final int nutzer_id;
	private String key = ""; //z.B. für /preview/, oder für backup
	
	private byte geschlecht = -1;
	private int rang;
	private String vorname;
	private String nachname;
	private String anrede;
	private String email;
	
	// Auswahl
	private Projekt openProjekt;
	
	// Klasse
	private String klasse;
	private int klassenstufe, klasse_id;
	private String suffix;
	
	// Suche
	private String searchKeyWord;
	private Beitrag[] searchResults;
	 
	// Admin: Rubrikliste -> Rubrik öffnen
	private Rubrik openRubrik;
	
	// Login-Blockierung
	private int loginTries = 0;
	private long unlockTime = 0;
	
	// bereits freigeschaltete Gruppen / Beiträge
	private Unlocker unlocker;
	
	private String lastLehrerChatTime;
	
	// Fileupload: Ordner speichern (Bugfix: bei vielen Dateien, wird die Seite refresht und Ordnerselektion geht verloren
	private int ordnerID;
	
	// Konstruktor für KlasseBean.init(), Beiträge
	public Nutzer(int rang, String vorname, String nachname, byte geschlecht) {
		
		this.rang = rang;
		this.vorname = vorname;
		this.nachname = nachname;
		this.geschlecht = geschlecht;
		
		nutzer_id = -1;
		klasse_id = -1;
		anrede = convertToAnrede(geschlecht);
	}
	
	// Konstruktor für Rubriken, Beiträge (Nutzer, die das Recht dazu haben), Speicherverbrauch-Übersicht
	public Nutzer(int nutzerID, String vorname, String nachname) {
		this.nutzer_id = nutzerID;
		this.vorname = vorname;
		this.nachname = nachname;
		
		rang = RANG_UNDEFINED;
		klasse_id = -1;
		geschlecht = Nutzer.GESCHLECHT_UNBEKANNT;
	}
	
	// Konstruktor für ProjektOpen
	public Nutzer(int nutzer_id, String vorname, String nachname, String klasse, byte geschlecht) {
		this.nutzer_id = nutzer_id;
		this.vorname = vorname;
		this.nachname = nachname;
		this.klasse = klasse;
		this.geschlecht = geschlecht;
		
		rang = RANG_UNDEFINED;
		klasse_id = -1;
		anrede = convertToAnrede(geschlecht);
	}
	
	// Konstruktor für SearchNutzer (Projekt)
	public Nutzer(int nutzer_id, String vorname, String nachname, String klasse, int klassenstufe, String suffix, String email) {
		this.nutzer_id = nutzer_id;
		this.vorname = vorname;
		this.nachname = nachname;
		this.klasse = klasse;
		this.klassenstufe = klassenstufe;
		this.suffix = suffix;
		this.email = email;
		
		this.rang = RANG_UNDEFINED;
		this.klasse_id = -1;
		anrede = convertToAnrede(geschlecht);
	}
	
	// getNutzer(rang)
	public Nutzer(int nutzer_id, byte geschlecht, String vorname, String nachname) {
		this.nutzer_id = nutzer_id;
		this.vorname = vorname;
		this.nachname = nachname;
		this.geschlecht = geschlecht;
		
		klasse_id = -1;
		anrede = convertToAnrede(geschlecht);
	}
	
	// Konstruktor für Sitzung
	public Nutzer(int rang, int nutzer_id, int klasse_id, String vorname, String nachname, String klassen_name, int klassenstufe, String suffix, byte geschlecht, String email) {
		this.rang = rang;
		this.nutzer_id = nutzer_id;
		this.klasse_id = klasse_id;
		
		this.vorname = vorname;
		this.nachname = nachname;
		this.klasse = klassen_name;
		this.klassenstufe = klassenstufe;
		this.suffix = suffix;
		this.geschlecht = geschlecht;
		this.email = email;
		
		anrede = convertToAnrede(geschlecht);
		unlocker = new Unlocker();
	}
	
	// Gast - Account
	public Nutzer() {
		this.rang = -1;
		this.nutzer_id = -1;
		this.klasse_id = -1;
		unlocker = new Unlocker();
	}
	
	// Lehrer - Chat
	public Nutzer(int nutzerID, String vorname, String nachname, String lastLehrerChatTime) {
		this.rang = -1;
		this.nutzer_id = nutzerID;
		this.vorname = vorname;
		this.nachname = nachname;
		this.lastLehrerChatTime = lastLehrerChatTime;
	}
	
	public String getRangString() {
		switch(rang) {
		case RANG_SCHUELER:
			return "Schüler";
		case RANG_LEHRER:
			return "Lehrer";
		case RANG_ADMIN:
			return "Admin";
		case RANG_GAST:
			return "Gast";
		default: return "Gast (nicht eingeloggt)";
 		}
	}
	
	public static String getRangString(int rang) {
		switch(rang) {
		case RANG_SCHUELER:
			return "Schüler/in";
		case RANG_LEHRER:
			return "Lehrer/in";
		case RANG_ADMIN:
			return "Admin";
		case RANG_GAST:
			return "Gast";
		default: return "Gast (nicht eingeloggt)";
 		}
	}
	
	/**
	 * Gibt die Geschlecht-ID anhand des Strings zurück
	 *
	 * @param  geschlechtString  Geschlecht-String aus dieser Klasse
	 */
	public static int convertToGeschlechtInt(String geschlechtString) {
		switch(geschlechtString) {
		case STR_MANN:
			return GESCHLECHT_MANN;
		case STR_FRAU:
			return GESCHLECHT_FRAU;
		default:
			return GESCHLECHT_UNBEKANNT;
		}
	}
	
	private String convertToAnrede(byte geschlecht) {
		if(geschlecht == 0) {
			return STRING_MANN;
		} else if(geschlecht == 1){
			return STRING_FRAU;
		} else {
			return null;
		}
	}
	
	//Lehrer hat Klasse verlassen/beigetreten
	public void updateKlasse(int klasseID, String klasse, int klassenstufe, String suffix) {
		this.klasse_id = klasseID;
		this.klasse = klasse;
		this.klassenstufe = klassenstufe;
		this.suffix = suffix;
	}
	
	public int getNutzer_id() {
		return nutzer_id;
	}
	
	public int getKlasse_id() {
		return klasse_id;
	}
	
	public int getRang() {
		return rang;
	}
	
	public String getVorname() {
		return vorname;
	}
	
	public void setVorname(String vorname) {
		this.vorname = vorname;
	}
	
	public String getNachname() {
		return nachname;
	}
	
	public void setNachname(String nachname) {
		this.nachname = nachname;
	}
	
	/**
	 * @return Herr Mustermann, Frau Mustermann, Max Mustermann (Geschlecht unbekannt)
	 */
	public String getFormalName() {
		switch(geschlecht) {
		case GESCHLECHT_MANN:
			return "Herr "+nachname;
		case GESCHLECHT_FRAU:
			return "Frau "+nachname;
		default:
			return vorname+" "+nachname;
		}
	}
	
	public byte getGeschlecht() {
		return geschlecht;
	}
	
	public String getKlasse() {
		return klasse;
	}
	
	public Projekt getOpenProjekt() {
		return openProjekt;
	}
	
	public void setOpenProjekt(Projekt openProjekt) {
		this.openProjekt = openProjekt;
	}
	
	public int getKlassenstufe() {
		return klassenstufe;
	}
	
	public String getSuffix() {
		return suffix;
	}
	
	public String getAnrede() {
		return anrede;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setSearchKeyWord(String searchKeyWord) {
		this.searchKeyWord = searchKeyWord;
	}
	
	public String getSearchKeyWord() {
		return searchKeyWord;
	}
	
	public void setSearchResults(Beitrag[] searchResults) {
		this.searchResults = searchResults;
	}
	
	public Beitrag[] getSearchResults() {
		return searchResults;
	}
	
	public void setOpenRubrik(Rubrik openRubrik) {
		this.openRubrik = openRubrik;
	}
	
	public Rubrik getOpenRubrik() {
		return openRubrik;
	}
	
	public Unlocker getUnlocker() {
		return unlocker;
	}
	
	public String getLastLehrerChatTime() {
		return lastLehrerChatTime;
	}
	
	//Util
	
	/**
	 * @return true, falls nutzerID enthalten ist, ansonsten false
	 */
	public static boolean contains(Nutzer[] nutzer, int nutzerID) {
		for(Nutzer n : nutzer) {
			if(n.nutzer_id == nutzerID)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Wichtig: nutzerID darf im Array max. einmal auftreten!
	 * @return neues Nutzer-Array ohne nutzerID, falls nutzerID nicht enthalten ist, return Original
	 */
	public static Nutzer[] remove(Nutzer[] nutzer, int nutzerID) {
		
		boolean found = contains(nutzer, nutzerID);
		if(found) {
			Nutzer[] nutzerNeu = new Nutzer[nutzer.length-1];
			int currIndex = 0;
			for(int i = 0; i < nutzer.length; i++) {
				if(nutzer[i].nutzer_id != nutzerID) {
					nutzerNeu[currIndex] = nutzer[i];
					currIndex++;
				}
			}
			return nutzerNeu;
		} else {
			return nutzer;
		}
	}
	
	/**
	 * @return Nutzer-Objekt, falls nutzerID enthalten ist, ansonsten null
	 */
	public static Nutzer get(Nutzer[] nutzer, int nutzerID) {
		for(Nutzer n : nutzer) {
			if(n.nutzer_id == nutzerID)
				return n;
		}
		
		return null;
	}
	
	public static String toNameString(Nutzer[] nutzer, boolean formal) {
		String s = "";
		for(int i = 0; i < nutzer.length; i++) {
			
			if(formal)
				s += nutzer[i].getFormalName();
			else
				s += nutzer[i].getVorname()+" "+nutzer[i].getNachname();
					
			if(i < nutzer.length-1) {
				s += ", "; //Komma anhängen, falls nicht das letzte Element
			}
		}
		
		return s;
	}
	
	//beim Einloggen / Ausloggen die freigeschalteten Gruppen / Beiträge mitnehmen
	public void setUnlocker(Unlocker unlocker) {
		this.unlocker = unlocker;
	}
	
	//Login
	
	public int getLoginTries() {
		return loginTries;
	}
	
	public void setLoginTries(int loginTries) {
		this.loginTries = loginTries;
	}
	
	public long getUnlockTime() {
		return unlockTime;
	}
	
	public void setUnlockTime(long unlockTime) {
		this.unlockTime = unlockTime;
	}
	
	//key
	
	public String getKey() {
		return key;
	}
	
	public String generateKey() {
		key = Zugangscodes.generateZugangscode(10);
		return key;
	}
	
	@Override
	public String toString() {
		return "["+getRangString()+"] "+vorname+" "+nachname;
	}
	
	//Ordner
	
	public void setOrdnerID(int ordnerID) {
		this.ordnerID = ordnerID;
	}
	
	public int getOrdnerID() {
		return ordnerID;
	}
	
}
