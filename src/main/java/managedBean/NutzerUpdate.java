package managedBean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.StreamedContent;

import mysql.Datenbank;
import objects.Datei;
import objects.Klasse;
import objects.Nutzer;
import sitzung.Sitzung;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class NutzerUpdate {
	
	final static private String SIZE_B = "Bytes", SIZE_KB = "KB", SIZE_MB = "MB", SIZE_GB = "GB";
	final static private String[] SIZES = {SIZE_B,SIZE_KB,SIZE_MB,SIZE_GB};
	final static private String RANG_SCHUELER = "Schüler/in", RANG_LEHRER = "Lehrer/in", RANG_GAST = "Gast";
	final static private String[] RANG = {RANG_SCHUELER,RANG_LEHRER,RANG_GAST};
	
	private String critVorname, critNachname, critKlassenstufe, critRang; //Suche (Kriterien)
	private Zeile selectedZeile;
	
	//Eigenschaften
	
	//Quotas
	private String storageString;
	private String sizeType;
	
	//Quotas klassenweise ändern
	private String[] klassen, selectedKlassen;
	private Klasse[] klassenObj;
	
	//Daten ändern
	private String dVorname, dNachname, dEmail, dPasswort;
	
	//Passwort-überprüfen
	private String passwort;
	private boolean passwortCorrect;
	
	//Privatspeicher
	private String selectedLink, mainURL;
	private DirectoryView directoryView;
	
	private Zeile[] tabelle;
	
	private int percent = 0;
	
	public int getPercent() {
		return percent;
	}
	
	public String getCritVorname() {
		return critVorname;
	}
	
	public void setCritVorname(String critVorname) {
		this.critVorname = critVorname;
	}
	
	public String getCritNachname() {
		return critNachname;
	}
	
	public void setCritNachname(String critNachname) {
		this.critNachname = critNachname;
	}
	
	public String getCritKlassenstufe() {
		return critKlassenstufe;
	}
	
	public void setCritKlassenstufe(String critKlassenstufe) {
		this.critKlassenstufe = critKlassenstufe;
	}
	
	public String getCritRang() {
		return critRang;
	}
	
	public void setCritRang(String critRang) {
		this.critRang = critRang;
	}
	
	public String[] getRang() {
		return RANG;
	}
	
	public Zeile[] getTabelle() {
		return tabelle;
	}
	
	public Zeile getSelectedZeile() {
		return selectedZeile;
	}
	
	public void search() {
		
		Konsole.method("search()");
		
		//Rang
		int rang;
		
		if(critRang == null) {
			rang = -1;
		} else {
			switch(critRang) {
			case RANG_SCHUELER:
				rang = Nutzer.RANG_SCHUELER;
				break;
			case RANG_LEHRER:
				rang = Nutzer.RANG_LEHRER;
				break;
			case RANG_GAST:
				rang = Nutzer.RANG_GAST;
				break;
			default:
				rang = -1;
				break;
			}
		}
		
		int klassenstufe;
		try {
			klassenstufe = Integer.parseInt(critKlassenstufe);
		} catch (NumberFormatException e) {
			klassenstufe = -1;
		}
		
		if(critVorname != null) {
			critVorname = critVorname.trim();
			if(critVorname.equals(""))
				critVorname = null;
		}
			
		
		if(critNachname != null) {
			critNachname = critNachname.trim();
			if(critNachname.equals(""))
				critNachname = null;
		}
			
		
		Nutzer[] nutzer;
		
		if(critVorname == null && critNachname == null && rang == -1 && klassenstufe == -1)
			nutzer = Datenbank.getAllNutzer();
		else
			nutzer = Datenbank.searchNutzer(critVorname, critNachname, rang, klassenstufe);
		
		if(nutzer != null) {
			Konsole.antwort("Anzahl Suchergebnisse = "+nutzer.length);
			
			tabelle = new Zeile[nutzer.length];
			for(int i = 0; i < nutzer.length; i++) {
				int storage = Datenbank.getStorage(nutzer[i].getNutzer_id());
				tabelle[i] = new Zeile(nutzer[i],Datei.convertSizeToString(storage));
			}
		}
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Suche durchgeführt",nutzer.length+" Nutzer gefunden.") );
	}
	
	public void cancel() {
		tabelle = null;
	}
	
	public void select(Zeile zeile) {
		selectedZeile = zeile;
		
		selectedZeile.speicherUsed = Datenbank.getStorageUsed(zeile.nutzer.getNutzer_id());
		selectedZeile.speicherUsedString = Datei.convertSizeToString(selectedZeile.speicherUsed);
	}
	
	public String getSizeType() {
		return sizeType;
	}
	
	public void setSizeType(String sizeType) {
		this.sizeType = sizeType;
	}
	
	public void editStorage() {
		
		float storage;
		try {
			storage = Float.parseFloat(storageString.replace(',', '.'));
		} catch(NumberFormatException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte eine Zahl eingeben") );
			return;
		}
		
		int storageTranslated;
		switch(sizeType) {
		case SIZE_B:
			storageTranslated = Datei.convertToBytes(storage, Datei.SIZE_B);
			break;
		case SIZE_KB:
			storageTranslated = Datei.convertToBytes(storage, Datei.SIZE_KB);
			break;
		case SIZE_MB:
			storageTranslated = Datei.convertToBytes(storage, Datei.SIZE_MB);
			break;
		case SIZE_GB:
			storageTranslated = Datei.convertToBytes(storage, Datei.SIZE_GB);
			break;
		default:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte eine Größenordnung wählen") );
			return;
		}
		
		if(Datenbank.editStorage(selectedZeile.nutzer.getNutzer_id(), storageTranslated)) {
			//Daten aktualisieren
			selectedZeile.speicherString = Datei.convertSizeToString(storageTranslated);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Speicher geändert",selectedZeile.nutzer.getVorname()+" "+selectedZeile.nutzer.getNachname()+" hat nun "+selectedZeile.speicherString+" zur Verfügung") );
		} else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Speicherplatz muss größer als "+selectedZeile.speicherUsedString+" sein.") );
	}
	
	public void clearStorage() {
		Datenbank.clearStorage(selectedZeile.nutzer.getNutzer_id());
		select(selectedZeile);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Speicher geleert",selectedZeile.nutzer.getVorname()+" "+selectedZeile.nutzer.getNachname()) );
	}
	
	public String getStorageString() {
		return storageString;
	}
	
	public void setStorageString(String storageString) {
		this.storageString = storageString;
	}
	
	public String[] getSizes() {
		return SIZES;
	}
	
	public void incrementKlassenstufe() {
		Datenbank.translateKlassenstufe(1);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Klassenstufen erhöht","Alle Klassenstufen jeweils um eins erhöht.") );
		if(tabelle != null)
			search();
	}
	
	public void decrementKlassenstufe() {
		Datenbank.translateKlassenstufe(-1);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Klassenstufen verringert","Alle Klassenstufen jeweils um eins verringert.") );
		if(tabelle != null)
			search();
	}
	
	public void prepareQuotasKlasse() {
		klassenObj = Datenbank.getAllKlassen();
		
		klassen = new String[klassenObj.length];
		
		for(int i = 0; i < klassen.length; i++) {
			klassen[i] = klassenObj[i].getString();
		}
		
		selectedKlassen = null;
		storageString = null;
		sizeType = null;
	}
	
	public String[] getSelectedKlassen() {
		return selectedKlassen;
	}
	
	public void setSelectedKlassen(String[] selectedKlassen) {
		this.selectedKlassen = selectedKlassen;
	}
	
	public String[] getKlassen() {
		return klassen;
	}
	
	public void editStorageKlasse() {
	
		//String in float
		float storage;
		try {
			storage = Float.parseFloat(storageString.replace(',', '.'));
		} catch(NumberFormatException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte eine Zahl eingeben") );
			return;
		}
		
		//in Bytes umwandeln
		int storageTranslated;
		switch(sizeType) {
		case SIZE_B:
			storageTranslated = Datei.convertToBytes(storage, Datei.SIZE_B);
			break;
		case SIZE_KB:
			storageTranslated = Datei.convertToBytes(storage, Datei.SIZE_KB);
			break;
		case SIZE_MB:
			storageTranslated = Datei.convertToBytes(storage, Datei.SIZE_MB);
			break;
		case SIZE_GB:
			storageTranslated = Datei.convertToBytes(storage, Datei.SIZE_GB);
			break;
		default:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte eine Größenordnung wählen") );
			return;
		}
		
		//Klassen-String-Array in Klassen-Objekt-Array
		Klasse[] selectedKlassenObj = new Klasse[selectedKlassen.length];
		int currIndex = 0;
		for(int i = 0; i < selectedKlassen.length; i++) {
			boolean found = false;
			for(int x = 0; x < klassen.length; x++) {
				if(selectedKlassen[i].equals(klassen[x])) {
					selectedKlassenObj[currIndex] = this.klassenObj[x];
					currIndex++;
					found = true;
					break;
				}
			}
			
			if(!found) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte mindestens eine Klasse wählen") );
				return;
			}
		}
		
		if(selectedKlassenObj.length == 0) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte mindestens eine Klasse wählen") );
			return;
		}
			
		
		for(int i = 0; i < selectedKlassenObj.length; i++) {
			Datenbank.editStorageKlasse(selectedKlassenObj[i].getId(), storageTranslated);
		}
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Speicher geändert", storageString+" "+sizeType) );
		
		if(tabelle != null)
			search();
	}
	
	public void resetSchuelerPassword() {
		int[] id = Datenbank.getAllNutzerID(Nutzer.RANG_SCHUELER);
		for(int curr : id) {
			Datenbank.setPasswort(curr, null, "schule");
		}
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Passwort aktualisiert",""));
	}
	
	//Rechte
	
	public void loadRechte() {
		if(selectedZeile != null) {
			int nutzerID = selectedZeile.nutzer.getNutzer_id();
			
			selectedZeile.gesperrt = Datenbank.isGesperrt(nutzerID);
			selectedZeile.beitragManager = Datenbank.isBeitragManager(nutzerID);
			selectedZeile.projektErstellen = Datenbank.isProjektErstellen(nutzerID);
			selectedZeile.rubrikErstellen = Datenbank.isRubrikErstellen(nutzerID);
			selectedZeile.vertretungsplan = Datenbank.isVertretung(nutzerID);
		}
	}
	
	public void toggleSperrung() {
		
		if(selectedZeile.nutzer.getNutzer_id() == Sitzung.getNutzer().getNutzer_id()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Sie können sich nicht selber aussperren.") );
			return;
		}
		
		selectedZeile.gesperrt = !selectedZeile.gesperrt;
		Datenbank.setGesperrt(selectedZeile.nutzer.getNutzer_id(), selectedZeile.gesperrt);
		if(selectedZeile.gesperrt)
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Nutzer gesperrt: ",selectedZeile.nutzer.getVorname()+" "+selectedZeile.nutzer.getNachname()) );
		else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Nutzer entsperrt: ",selectedZeile.nutzer.getVorname()+" "+selectedZeile.nutzer.getNachname()) );
	}
	
	public void toggleBeitragManager() {
		
		if(selectedZeile.nutzer.getNutzer_id() == Sitzung.getNutzer().getNutzer_id()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Sie sind Admin.") );
			Datenbank.setBeitragManager(selectedZeile.nutzer.getNutzer_id(), true);
			return;
		}
		
		selectedZeile.beitragManager = !selectedZeile.beitragManager;
		Datenbank.setBeitragManager(selectedZeile.nutzer.getNutzer_id(), selectedZeile.beitragManager);
		if(selectedZeile.beitragManager) {
			selectedZeile.rubrikErstellen = false;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Rechte aktualisiert: ",selectedZeile.nutzer.getVorname()+" "+selectedZeile.nutzer.getNachname()+" kann Beiträge genehmigen und bearbeiten.") );
		} else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Rechte aktualisiert: ",selectedZeile.nutzer.getVorname()+" "+selectedZeile.nutzer.getNachname()+" kann nur Beiträge erstellen und nicht veröffentlichte bearbeiten"));
	}
	
	public void toggleProjektErstellen() {
		
		if(selectedZeile.nutzer.getNutzer_id() == Sitzung.getNutzer().getNutzer_id()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Sie sind Admin.") );
			Datenbank.setProjektErstellen(selectedZeile.nutzer.getNutzer_id(), true);
			return;
		}
		
		selectedZeile.projektErstellen = !selectedZeile.projektErstellen;
		Datenbank.setProjektErstellen(selectedZeile.nutzer.getNutzer_id(), selectedZeile.projektErstellen);
		if(selectedZeile.projektErstellen)
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Rechte aktualisiert: ",selectedZeile.nutzer.getVorname()+" "+selectedZeile.nutzer.getNachname()+" kann Projekte erstellen.") );
		else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Rechte aktualisiert: ",selectedZeile.nutzer.getVorname()+" "+selectedZeile.nutzer.getNachname()+" kann keine Projekte erstellen."));
	}
	
	public void toggleVertretung() {
		
		if(selectedZeile.nutzer.getNutzer_id() == Sitzung.getNutzer().getNutzer_id()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Sie sind Admin.") );
			Datenbank.setVertretung(selectedZeile.nutzer.getNutzer_id(), true);
			return;
		}
		
		selectedZeile.vertretungsplan = !selectedZeile.vertretungsplan;
		Datenbank.setVertretung(selectedZeile.nutzer.getNutzer_id(), selectedZeile.vertretungsplan);
		if(selectedZeile.vertretungsplan)
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Rechte aktualisiert: ",selectedZeile.nutzer.getVorname()+" "+selectedZeile.nutzer.getNachname()+" kann Vertretungspläne ändern.") );
		else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Rechte aktualisiert: ",selectedZeile.nutzer.getVorname()+" "+selectedZeile.nutzer.getNachname()+" kann keine Vertretungspläne ändern."));
	}
	
	public void toggleRubrikErstellen() {
		
		if(selectedZeile.nutzer.getNutzer_id() == Sitzung.getNutzer().getNutzer_id()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Sie sind Admin.") );
			Datenbank.setRubrikErstellen(selectedZeile.nutzer.getNutzer_id(), false);
			return;
		}
		
		selectedZeile.rubrikErstellen = !selectedZeile.rubrikErstellen;
		Datenbank.setRubrikErstellen(selectedZeile.nutzer.getNutzer_id(), selectedZeile.rubrikErstellen);
		if(selectedZeile.rubrikErstellen) {
			selectedZeile.beitragManager = false;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Rechte aktualisiert: ",selectedZeile.nutzer.getVorname()+" "+selectedZeile.nutzer.getNachname()+" kann Rubriken erstellen.") );
		} else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Rechte aktualisiert: ",selectedZeile.nutzer.getVorname()+" "+selectedZeile.nutzer.getNachname()+" kann keine Rubriken erstellen."));
	}
	
	//Daten ändern
	
	public void resetNutzerData() {
		dVorname = null;
		dNachname = null;
		dPasswort = null;
		dEmail = null;
	}
	
	public void saveNutzerData() {
		
		boolean changed = false;
		
		if(dVorname != null && !dVorname.trim().equals("")) {
			changed = true;
			Datenbank.setNutzerVorname(selectedZeile.nutzer.getNutzer_id(), dVorname.trim());
			selectedZeile.nutzer.setVorname(dVorname.trim());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Vorname aktualisiert",dVorname));
		}
		
		if(dNachname != null && !dNachname.trim().equals("")) {
			changed = true;
			Datenbank.setNutzerNachname(selectedZeile.nutzer.getNutzer_id(), dNachname.trim());
			selectedZeile.nutzer.setNachname(dNachname);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Nachname aktualisiert",dNachname));
		}
		
		if(dPasswort != null && !dPasswort.trim().equals("")) {
			changed = true;
			Datenbank.setPasswort(selectedZeile.nutzer.getNutzer_id(), null, dPasswort);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Passwort aktualisiert",""));
		}
		
		if(dEmail != null && !dEmail.trim().equals("")) {
			if(Datenbank.existEmail(dEmail.trim())) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "E-Mail bereits vergeben.") );
			} else {
				changed = true;
				Datenbank.setNutzerEmail(selectedZeile.nutzer.getNutzer_id(), dEmail.trim());
				selectedZeile.nutzer.setEmail(dEmail);
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"E-Mail aktualisiert",dEmail));
			}
			
		}
		
		if(!changed) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Keine Änderungen durchgeführt",""));
		}
	}
	
	public String getdVorname() {
		return dVorname;
	}
	
	public void setdVorname(String dVorname) {
		this.dVorname = dVorname;
	}
	
	public String getdNachname() {
		return dNachname;
	}
	
	public void setdNachname(String dNachname) {
		this.dNachname = dNachname;
	}
	
	public String getdEmail() {
		return dEmail;
	}
	
	public void setdEmail(String dEmail) {
		this.dEmail = dEmail;
	}
	
	public String getdPasswort() {
		return dPasswort;
	}
	
	public void setdPasswort(String dPasswort) {
		this.dPasswort = dPasswort;
	}
	
	//Account Löschen
	
	public void prepareDeleteAccount() {
		passwort = null;
		passwortCorrect = false;
	}
	
	public void deleteAllSchueler() {
		percent = 0;
		int[] nutzer_id = Datenbank.getAllNutzerID(Nutzer.RANG_SCHUELER);
		
		for(int i = 0; i < nutzer_id.length; i++) {
			percent = (int) ((i*100)/(float)nutzer_id.length);
			Datenbank.deleteAccount(nutzer_id[i]);
		}
		
		Datenbank.cleanKlasse(); //unnötige Klassen löschen
		percent = 100;
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Nutzer gelöscht",nutzer_id.length+" Schüler gelöscht.") );
		
		if(tabelle != null)
			search();
	}
	
	public void deleteAccount() {
		
		if(selectedZeile.nutzer.getNutzer_id() == Sitzung.getNutzer().getNutzer_id()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Sie können sich nicht selber löschen.") );
			return;
		}
		
		Datenbank.deleteAccount(selectedZeile.nutzer.getNutzer_id());
		Datenbank.cleanKlasse(); //unnötige Klassen löschen
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Nutzer gelöscht",selectedZeile.nutzer.getVorname()+" "+selectedZeile.nutzer.getNachname()) );
		
		if(tabelle != null)
			search();
	}
	
	public void checkPasswort() {
		passwortCorrect = Datenbank.checkPassword(passwort);
		if(passwortCorrect == false)
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Falsches Passwort!","") );
	}
	
	public boolean isPasswortCorrect() {
		return passwortCorrect;
	}
	
	public String getPasswort() {
		return passwort;
	}
	
	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}
	
	//Privatspeicher
	
	public void loadPrivatSpeicher() {
		directoryView = new DirectoryView(selectedZeile.nutzer.getNutzer_id(), Datei.Mode.PRIVATSPEICHER, false);
	}
	
	public DirectoryView getDirectoryView() {
		return directoryView;
	}
	
	public StreamedContent download(Datei datei) {
    	return Datenbank.getDateiInhalt(datei);
    }
    
    public void deleteDatei(Datei datei) {
    	Nutzer me = Sitzung.getNutzer();
    	
    	if(me == null)
    		return;
    	
    	if(datei.isOrdner()) {
    		Datenbank.deleteFolder(datei.getDatei_id());
    	} else {
    		Datenbank.deleteDatei(datei,me);
    	}
    	
    	//update
    	selectedZeile.speicherUsed = Datenbank.getStorageUsed(selectedZeile.nutzer.getNutzer_id());
    	selectedZeile.speicherUsedString = Datei.convertSizeToString(selectedZeile.speicherUsed);
    	directoryView.update();
    }
    
    //Link vorbereiten
    public void selectLink(Datei datei) {
    	selectedLink = getMainURL()+datei.getPublicLink();
    }
    
    public String getSelectedLink() {
		return selectedLink;
	}
    
    private String getMainURL() {
    	if(mainURL == null) {
    		mainURL = URLManager.getMainURL(null);
    	}
		return mainURL;
	}
	
	public static class Zeile {
		
		final Nutzer nutzer;
		String speicherString;
		
		//weitere Eigenschaften, wenn man ein Nutzer auswählt
		boolean gesperrt, beitragManager, projektErstellen, rubrikErstellen, vertretungsplan;
		
		int speicherUsed;
		String speicherUsedString;
		
		/**
		 * 
		 * @param nutzer ausgewählter Nutzer
		 * @param speicherString maximale Speicherplatz als String
		 */
		public Zeile(Nutzer nutzer, String speicherString) {
			this.speicherString = speicherString;
			this.nutzer = nutzer;
		}
		
		public Nutzer getNutzer() {
			return nutzer;
		}
		
		public String getSpeicherString() {
			return speicherString;
		}
		
		public int getSpeicherUsed() {
			return speicherUsed;
		}
		
		public String getSpeicherUsedString() {
			return speicherUsedString;
		}
		
		public String getLoginString() {
			if(gesperrt)
				return "NEIN";
			else
				return "JA";
		}
		
		public String getBeitragManagerString() {
			if(beitragManager)
				return "JA";
			else
				return "NEIN";
		}
		
		public String getProjektErstellenString() {
			if(projektErstellen)
				return "JA";
			else
				return "NEIN";
		}
		
		public String getRubrikErstellenString() {
			if(rubrikErstellen)
				return "JA";
			else
				return "NEIN";
		}
		
		public String getVertretungString() {
			if(vertretungsplan)
				return "JA";
			else
				return "NEIN";
		}
	}

}
