package managedBean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.Klasse;
import objects.Nutzer;
import tools.Zugangscodes;

@ViewScoped
@ManagedBean
public class NutzerImport {
	
	final static private String STANDARD_PASSWORD_NOT_AVAILABLE = "nicht verfügbar";
	final static private String TYPE_0 = "Vorname ; Nachname ; Anmeldename{@stephaneum.de} ; Passwort ; Klasse";
	final static private String TYPE_1 = "Anmeldename{@stephaneum.de} ; Anrede ; Nachname ; Vorname";
	final static private String TYPE_2 = "Klasse ; Nachname ; Vorname";
	final static private String[] TYPES = {TYPE_0, TYPE_1, TYPE_2};
	final static private int[] ANZAHL_SPALTEN = {5,4,3};
	
	final static private String TRENNER_0 = "Semikolon [ ; ]";
	final static private String TRENNER_1 = "Tab [   ]";
	final static private String TRENNER_2 = "Schrägstrich [ / ]";
	final static private String[] TRENNER = {TRENNER_0,TRENNER_1,TRENNER_2};
	
	final static private String BSP_0 = "Max;Mustermann;m.mustermann;meinPasswort;7c";
	final static private String BSP_1 = "m.mustermann;Herr;Mustermann;Max";
	final static private String BSP_2 = "7c;Mustermann;Max";
	final static private String[] BSP = {BSP_0,BSP_1,BSP_2};
	
	//Indezes (typ-spezifisch)
	final static private int[] INDEX_VORNAME = {0,3,2}, INDEX_NACHNAME = {1,2,1}, INDEX_SUB_EMAIL = {2,0,-1}, INDEX_KLASSE = {4,-1,0};
	final static private int INDEX_PASSWORT = 3, INDEX_ANREDE = 1; //bei allen Typen gleich bzw. nur bei einem Typ verfügbar
	
	final static private String EMAIL_PROVIDER = "@stephaneum.de";
	
	final static private String RANG_SCHUELER = "Schüler/in", RANG_LEHRER = "Lehrer/in";
	final static private String[] RANG = {RANG_SCHUELER,RANG_LEHRER};
	
	private String beispiel;
	private String type;
	private String trenner = TRENNER_0;
	private String rang;
	private String data;
	private String standardPassword = STANDARD_PASSWORD_NOT_AVAILABLE;
	private int percent = 0;
	
	public void setData(String data) {
		this.data = data;
	}
	
	public String getData() {
		return data;
	}
	
	public void setRang(String rang) {
		this.rang = rang;
	}
	
	public String getRang() {
		return rang;
	}
	
	public String[] getRangArray() {
		return RANG;
	}
	
	public String[] getTypes() {
		return TYPES;
	}
	
	public String getStandardPassword() {
		return standardPassword;
	}
	
	public void setStandardPassword(String standardPassword) {
		if(standardPassword != null && standardPassword.trim().equals(""))
			return;
		
		this.standardPassword = standardPassword;
	}
	
	public int getPercent() {
		return percent;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String[] getTrenners() {
		return TRENNER;
	}
	
	public String getTrenner() {
		return trenner;
	}
	
	public void setTrenner(String trenner) {
		this.trenner = trenner;
	}
	
	public void updateType() {
		int type = 0;
		if(this.type != null) {
			switch(this.type) {
			case TYPE_0:
				standardPassword = STANDARD_PASSWORD_NOT_AVAILABLE;
				type = 0;
				break;
			case TYPE_1:
				standardPassword = "Schule";
				type = 1;
				break;
			case TYPE_2:
				standardPassword = "Eule";
				type = 2;
				break;
			default:
				standardPassword = STANDARD_PASSWORD_NOT_AVAILABLE;
			}
		}
		
		if(this.trenner != null) {
			switch(this.trenner) {
			case TRENNER_0:
				beispiel = BSP[type];
				break;
			case TRENNER_1:
				beispiel = BSP[type].replace(";", "&nbsp;&nbsp;&nbsp;&nbsp;");
				break;
			case TRENNER_2:
				beispiel = BSP[type].replace(";", "/");
				break;
			}
		}
	}
	
	public String getBeispiel() {
		return beispiel;
	}
	
	public int getTypeIndex() {
		if(this.type == null)
			return 0;
		
		switch(this.type) {
		case TYPE_0:
			return 0;
		case TYPE_1:
			return 1;
		case TYPE_2:
			return 2;
		default:
			return 0;
		}
	}
	
	public void updateData() {
		
		Konsole.method("updateData");
		
		percent = 0;
		
		//Type
		int type;
		switch(this.type) {
		case TYPE_0:
			type = 0;
			break;
		case TYPE_1:
			type = 1;
			break;
		case TYPE_2:
			type = 2;
			break;
		default:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte eine Syntax auswählen") );
			Konsole.warn("Validierung: kein Syntax ausgewählt");
			return;
		}
		
		//Rang
		int rang;
		switch(this.rang) {
		case RANG_SCHUELER:
			rang = Nutzer.RANG_SCHUELER;
			break;
		case RANG_LEHRER:
			rang = Nutzer.RANG_LEHRER;
			break;
		default:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte einen Rang auswählen") );
			Konsole.warn("Validierung: falscher Rang");
			return;
		}
		
		//Trenner
		char trenner;
		switch(this.trenner) {
		case TRENNER_0:
			trenner = ';';
			break;
		case TRENNER_1:
			trenner = '\t';
			break;
		case TRENNER_2:
			trenner = '/';
			break;
		default:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte einen Trennzeichen auswählen") );
			Konsole.warn("Validierung: falsches Trennzeichen");
			return;
		}
		
		String[] linesOrig = data.split("\\r?\\n");
		
		
		//leere Zeilen löschen
		int anzahlLeer = 0;
		for(int i = 0; i < linesOrig.length; i++) {
			String withoutTab = linesOrig[i].replace('\t', ' ');
			
			if(withoutTab.trim().equals("")) {
				//leere Zeilen überspringen
				anzahlLeer++;
			}
		}
		
		if(anzahlLeer == linesOrig.length) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Es wurden keine Eingaben erkannt") );
			Konsole.warn("Validierung: leere Eingabe");
			return;
		}
		
		Konsole.antwort("leere Zeilen = "+anzahlLeer);
		
		String[] lines = new String[linesOrig.length-anzahlLeer];
		int current = 0;
		for(int i = 0; i < linesOrig.length; i++) {
			String withoutTab = linesOrig[i].replace('\t', ' ');
			
			if(!withoutTab.trim().equals("")) {
				lines[current] = linesOrig[i];
				current++;
			}
		}
		
		//Validierung (das gleiche wie weiter unten, nur keine Datenbankänderungen)
		
		int[] klassenstufen = new int[lines.length];
		String[] suffixe = new String[lines.length];
		
		for(int i = 0; i < lines.length; i++) {
			
			percent = 10+(int)(20*((i+1)/(float)lines.length));
			
			String[] currentLine = lines[i].split(String.valueOf(trenner));
			
			if(currentLine.length != ANZAHL_SPALTEN[type]) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "In der "+(i+1)+". Zeile sind "+currentLine.length+" Spalten statt "+ANZAHL_SPALTEN[type]) );
				Konsole.warn("Validierung: falsche Syntax: In der "+(i+1)+". Zeile sind "+currentLine.length+" Spalten statt "+ANZAHL_SPALTEN[type]);
				percent = 100;
				return;
			}
			
			//Klasse
			if(type != 1) { //nur Syntax 1 unterstützt keine Klassen
				String[] string = currentLine[INDEX_KLASSE[type]].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
				for(int n = 2; n < string.length; n++) {
					string[1] += string[n];
				}
				
				if(string.length >= 2) {
					
					try {
						klassenstufen[i] = Integer.parseInt(string[0]);
						suffixe[i] = string[1];
					} catch(NumberFormatException e) {
						
						//Zahl (von der Klassenstufe) unbekannt
						FacesContext.getCurrentInstance().addMessage(
								null, new FacesMessage(
										FacesMessage.SEVERITY_ERROR,
										"Fehler",
										"In der "+(i+1)+". Zeile wurde auf die Klasse \""+currentLine[INDEX_KLASSE[type]]+"\" verwiesen, die nicht erstellt werden kann"
								)
						);
						Konsole.warn("Validierung: Klassensyntax ungültig");
						percent = 100;
						return; //Klassenstufe ungültig (keine Zahl)
					}
					
				} else {
					
					//string.length ist zu klein: mindestens 2!
					
					FacesContext.getCurrentInstance().addMessage(
							null, new FacesMessage(
									FacesMessage.SEVERITY_ERROR,
									"Fehler",
									"In der "+(i+1)+". Zeile wurde auf die Klasse \""+currentLine[INDEX_KLASSE[type]]+"\" verwiesen, die nicht erstellt werden kann"
							)
					);
					Konsole.warn("Validierung: Klassensyntax ungültig");
					percent = 100;
					return; //ungültige Syntax der Klasse
				}

			}
		}
		
		Konsole.antwort("Validierung: keine Fehler");
		
		//überprüfen, ob genügend Zugangscodes vorhanden sind
		
		int delta = lines.length - Datenbank.getAnzahlZugangscodes(rang, false);
		
		if(delta > 0) {
			if(rang == Nutzer.RANG_LEHRER)
				Datenbank.createZugangscodes(Zugangscodes.STANDARD_LENGTH, delta, delta, false); //nur Lehrer
			else
				Datenbank.createZugangscodes(Zugangscodes.STANDARD_LENGTH, delta, 0, false); //keine Lehrer
		}
		
		String[] freeZugangscodes = Datenbank.getAllZugangscodes(rang, false);
		
		Konsole.antwort("lines = "+lines.length+"; delta-zugangscode = "+delta);
		
		//Nutzer registrieren
		
		Klasse[] klassen = Datenbank.getAllKlassen();
		String[] emails = new String[lines.length]; //bei type=2 wird auf eindeutigkeit überprüft
		
		StringBuilder builder = new StringBuilder();
		
		Datenbank.prepareRegisterNutzerFast();
		for(int i = 0; i < lines.length; i++) {
			
			percent = 30+(int)(70*((i+1)/(float)lines.length));
			
			String[] currentLine = lines[i].split(String.valueOf(trenner));
			
			//Klasse
			
			int klasseID;
			switch(type) {
			case 0:
			case 2:
				//0 und 2 haben klassen
				//wir nutzen die vorher berechnete Klassenstufe und Suffix
				klasseID = getID(klassenstufen[i], suffixe[i], klassen);
				if(klasseID == -1) {
					//neue Klasse erstellen
					Datenbank.createKlasse(klassenstufen[i], suffixe[i]);
					klassen = Datenbank.getAllKlassen(); //Array aktualisieren
					klasseID = getID(klassenstufen[i], suffixe[i], klassen);
				}
				break;
				
			default:
				//1 hat keine Klassen
				klasseID = -1;
			}
			
			//Passwörter
			String passwort;
			if(type == 0) {
				//nur erste Syntax hat Passwort
				passwort = currentLine[INDEX_PASSWORT];
			} else {
				passwort = standardPassword;
			}
			
			//Geschlecht
			int geschlecht;
			switch(type) {
			case 1:
				if(currentLine[INDEX_ANREDE].equals("Herr")) {
					geschlecht = Nutzer.GESCHLECHT_MANN;
				} else if(currentLine[INDEX_ANREDE].equals("Frau")) {
					geschlecht = Nutzer.GESCHLECHT_FRAU;
				} else {
					geschlecht = Nutzer.GESCHLECHT_UNBEKANNT;
				}
				break;
			default:
				//0 und 2 kein Geschlecht angegeben
				geschlecht = Nutzer.GESCHLECHT_UNBEKANNT;
				break;
			}
			
			//E-Mail
			String email;
			switch(type) {
			case 0:
			case 1:
				//0 und 1 haben E-Mail explizit angegeben
				email = currentLine[INDEX_SUB_EMAIL[type]]+EMAIL_PROVIDER;
				break;
			case 2:
				//bei 2: erster buchstabe von vornahme PUNKT nachname: Max Mustermann -> m.mustermann
				//falls email bereits vorhanden, dann eine Zahl anhängen
				
				String prefix = currentLine[INDEX_VORNAME[type]].substring(0, 1).toLowerCase() + "." + currentLine[INDEX_NACHNAME[type]].toLowerCase();
				email = prefix + EMAIL_PROVIDER;

				int number = 1;
				while(contains(emails, i, email)) {
					email = prefix + number + EMAIL_PROVIDER; // z.B. m.mustermann2@stephaneum.de
					number++;
				}
				
				emails[i] = email;
				break;
			default:
				email = "Error";
			}
			
			//endgültige Registrierung
			Datenbank.registerNutzerFast(freeZugangscodes[i], klasseID, currentLine[INDEX_VORNAME[type]], currentLine[INDEX_NACHNAME[type]], geschlecht, email, passwort);
			
			//reset
			builder.setLength(0);
		}
		
		Datenbank.validateZugangscode();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Nutzer erstellt","Anzahl: "+lines.length) );
		Konsole.antwort(lines.length+" Nutzer erstellt");
	}
	
	//erhalte die KlasseID vom Klassen-Array (spart Datenbank-Abfragen)
	private int getID(int stufe, String suffix, Klasse[] klassen) {
		
		for(Klasse klasse : klassen) {
			if(klasse.getStufe() == stufe && klasse.getSuffix().equals(suffix)) {
				return klasse.getId();
			}
		}
		return -1;
	}
	
	private boolean contains(String[] array, int untilIndex, String current) {
		for(int i = 0; i <= untilIndex && i < array.length; i++) {
			if(array[i] != null && current.equals(array[i]))
				return true;
		}
		
		return false;
	}

}
