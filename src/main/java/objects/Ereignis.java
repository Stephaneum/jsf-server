package objects;

public class Ereignis {
	
	public final static int TYPE_LOGIN = 0, TYPE_REGISTER = 1,
							TYPE_UPLOAD = 2, TYPE_CREATE_BEITRAG = 3, TYPE_EDIT_BEITRAG = 4,
							TYPE_CREATE_PROJEKT = 5, TYPE_JOIN_PROJEKT = 6, TYPE_QUIT_PROJEKT = 7, TYPE_DELETE_PROJEKT = 8,
							TYPE_JOIN_KLASSE = 9, TYPE_QUIT_KLASSE = 10,
							TYPE_DELETE_FILE = 11, TYPE_APPROVE_BEITRAG = 12,
							TYPE_CREATE_CHATRAUM = 13;
	public final static int ANZAHL_TYPES = 14;
	public final static String[] TYPES = {"Login","Registrierung",
										"Datei hochgeladen","Beitrag erstellt","Beitrag bearbeitet",
										"Projekt erstellt","Projekt beigetreten","Projekt verlassen","Projekt gelöscht",
										"Klasse festgelegt","Klasse verlassen",
										"Datei gelöscht", "Beitrag genehmigt",
										"Chatraum erstellt"};
	
	final private String datum, text, typString;
	final private int typ;
	
	public Ereignis(String datum, String text, int typ) {
		this.datum = datum;
		this.text = text;
		this.typ = typ;
		
		switch(typ) {
		case TYPE_LOGIN:
			typString = " [Login] ";
			break;
		case TYPE_REGISTER:
			typString = " [Registrierung] ";
			break;
		case TYPE_UPLOAD:
			typString = " [Datei hochgeladen] ";
			break;
		case TYPE_CREATE_BEITRAG:
			typString = " [Beitrag erstellt] ";
			break;
		case TYPE_EDIT_BEITRAG:
			typString = " [Beitrag bearbeitet] ";
			break;
		case TYPE_CREATE_PROJEKT:
			typString = " [Projekt erstellt] ";
			break;
		case TYPE_JOIN_PROJEKT:
			typString = " [Projekt beigetreten] ";
			break;
		case TYPE_QUIT_PROJEKT:
			typString = " [Projekt verlassen] ";
			break;
		case TYPE_DELETE_PROJEKT:
			typString = " [Projekt gelöscht] ";
			break;
		case TYPE_JOIN_KLASSE:
			typString = " [Klasse festgelegt] ";
			break;
		case TYPE_QUIT_KLASSE:
			typString = " [Klasse verlassen] ";
			break;
		case TYPE_DELETE_FILE:
			typString = " [Datei gelöscht] ";
			break;
		case TYPE_APPROVE_BEITRAG:
			typString = " [Beitrag genehmigt] ";
			break;
		case TYPE_CREATE_CHATRAUM:
			typString = " [Chatraum erstellt] ";
			break;
		default: typString = " [unbekannter Typ] ";
		}
	}
	
	public String getDatum() {
		return datum;
	}
	
	public String getText() {
		return text;
	}
	
	public int getTyp() {
		return typ;
	}
	
	public String getTypString() {
		return typString;
	}

}
