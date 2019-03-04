package managedBean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import mysql.Datenbank;
import objects.Datei;
import objects.Nutzer;
import sitzung.Sitzung;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class Dateien {
	
	//allgemeine Daten
	private int nutzerID;
	private Datei.Mode mode;
	private int nutzerKlasseProjektID = -1;
	private String titel;
	private boolean deleteEverything;
	
	//Viewer
	private DirectoryView directoryView, ordnerSelektor;
	
	//Speicherplatz
	private String storageString, storageUsedString, storageFreeString;
	private int storageFree;
	
	//öffentlicher Link
	private String mainURL;
	private String selectedLink;
	
	//Datei bearbeiten
	private Datei selectedDatei;
	private String newName;
	
	public void initPrivatspeicher() {
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null)
			return;
		
		this.mode = Datei.Mode.PRIVATSPEICHER;
		this.nutzerKlasseProjektID = nutzer.getNutzer_id();
		this.titel = null;
		this.nutzerID = nutzer.getNutzer_id();
		this.deleteEverything = true;
		
		postInit(nutzer);
	}
	
	public void initKlassenspeicher(int klasseID, boolean lehrerOrAdmin) {
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null)
			return;
		
		this.mode = Datei.Mode.KLASSENSPEICHER;
		this.nutzerKlasseProjektID = klasseID;
		this.titel = "Klassenspeicher";
		this.nutzerID = nutzer.getNutzer_id();
		this.deleteEverything = lehrerOrAdmin;
		
		postInit(nutzer);
	}
	
	public void initProjektspeicher(int projektID, boolean projektLeiterOrBetreuer) {
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null)
			return;
		
		this.mode = Datei.Mode.PROJEKTSPEICHER;
		this.nutzerKlasseProjektID = projektID;
		this.titel = "Projektspeicher";
		this.nutzerID = nutzer.getNutzer_id();
		this.deleteEverything = projektLeiterOrBetreuer;
		
		postInit(nutzer);
	}
	
	public void initLehrerChat() {
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null)
			return;
		
		this.mode = Datei.Mode.LEHRERCHAT;
		this.nutzerKlasseProjektID = -1;
		this.titel = "Lehrerspeicher";
		this.nutzerID = nutzer.getNutzer_id();
		this.deleteEverything = nutzer.getRang() == Nutzer.RANG_ADMIN;
		
		postInit(nutzer);
	}
	
	public void initLehrerChatraum(int projektID, String projektName) {
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null)
			return;
		
		this.mode = Datei.Mode.PROJEKTSPEICHER;
		this.nutzerKlasseProjektID = projektID;
		this.titel = "Speicher zum Chatraum \""+projektName+"\"";
		this.nutzerID = nutzer.getNutzer_id();
		this.deleteEverything = false;
		
		postInit(nutzer);
	}
	
	private void postInit(Nutzer nutzer) {
		directoryView = new DirectoryView(nutzerKlasseProjektID, mode, false);
		ordnerSelektor = new DirectoryView(nutzerKlasseProjektID, mode, true);
		updateStorage(nutzer);
	}
	
	public int getNutzerID() {
		return nutzerID;
	}
	
	public String getTitel() {
		return titel;
	}
	
	public boolean isDeleteEverything() {
		return deleteEverything;
	}
	
	public DirectoryView getDirectoryView() {
		return directoryView;
	}
	
	
	//neben der Überprüfung, ob der Nutzer existiert, gibt noch weitere spezifische Bedingungen
	private boolean check(Nutzer nutzer) {
		if(mode == null)
			return false;
		
		switch(mode) {
			case PRIVATSPEICHER:
				return true;
			case KLASSENSPEICHER:
				return true;
			case PROJEKTSPEICHER:
				return nutzerKlasseProjektID != -1;
			case LEHRERCHAT:
				return nutzer.getRang() == Nutzer.RANG_LEHRER || nutzer.getRang() == Nutzer.RANG_ADMIN;
			default:
				return false;
		}
	}
	
	private void update() {
		
		if(mode == null)
			return;
		
		Nutzer nutzer = Sitzung.getNutzer();
		
		if(nutzer != null && check(nutzer)) {
			directoryView.update();
			updateStorage(nutzer);
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Sitzung abgelaufen. Bitte neu anmelden.") );
		}
	}
	
	private void updateStorage(Nutzer nutzer) {
		int storage = Datenbank.getStorage(nutzer.getNutzer_id());
		int storageUsed = Datenbank.getStorageUsed(nutzer.getNutzer_id());
		storageFree = storage - storageUsed;
		
		storageString = Datei.convertSizeToString(storage);
		storageUsedString = Datei.convertSizeToString(storageUsed);
		storageFreeString = Datei.convertSizeToString(storageFree);
	}
	
	public int getStorageFree() {
		return storageFree;
	}

	public String getStorageString() {
		return storageString;
	}
	
	public String getStorageUsedString() {
		return storageUsedString;
	}
	
	public String getStorageFreeString() {
		return storageFreeString;
	}
	
	public void saveOrdnerID() {
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer != null)
			nutzer.setOrdnerID(directoryView.getOrdnerID());
	}
	
	public synchronized void upload(FileUploadEvent event) {
		
		if(mode == null)
			return;
		
    	UploadedFile file = event.getFile();
    	
    	Nutzer me = Sitzung.getNutzer();
    	
    	if(me != null && check(me)) {
    		
    		int realOrdnerID = me.getOrdnerID();
    		
    		try {
				Datenbank.addFile(file, realOrdnerID, nutzerKlasseProjektID, mode);
			} catch (Exception e) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Sitzung abgelaufen. Bitte neu anmelden.") );
				return;
			}
    		
    		if(realOrdnerID == directoryView.getOrdnerID()) {
    			//nur updaten, falls der Nutzer sich immer noch im selben Ordner befindet
            	update();
    		}
    		
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Hochgeladen",file.getFileName()) );
    	} else {
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Sitzung abgelaufen. Bitte neu anmelden.") );
    	}
        
    }
	
	public StreamedContent download(Datei datei) {
    	return Datenbank.getDateiInhalt(datei);
    }
    
    public String delete(Datei datei) {
    	
    	Nutzer me = Sitzung.getNutzer();
		
		if(me == null || !check(me)) {
			return URLManager.LOGIN;
		}
    	
    	if(datei.isOrdner()) {
    		Datenbank.deleteFolder(datei.getDatei_id());
    	} else {
    		Datenbank.deleteDatei(datei,me);
    	}
    	
    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Gelöscht",datei.getDatei_name()) );
    	update();
    	
    	return null;
    }
    
    //neuer Ordner
    
    public void prepareNewOrdner() {
    	this.newName = null;
    }
    
    public String addOrdner() {
    	
    	if(mode == null)
    		return null;
    	
    	Nutzer me = Sitzung.getNutzer();
		
		if(me == null || !check(me)) {
			return URLManager.LOGIN;
		}
    	
    	if(newName == null || newName.trim().equals("")) {
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Name darf nicht leer sein.") );
    		return null;
    	}
    	
    	if(newName.contains("/") || newName.contains("\\")) {
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Name enthält unerlaubte Zeichen.") );
    		return null;
    	}
    	
    	Datenbank.addFolder(newName, directoryView.getOrdnerID(), me.getNutzer_id(), nutzerKlasseProjektID, mode);
    	
    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Ordner erstellt",newName) );
    	update();
    	
    	return null;
    }
    
    public void togglePublicity(Datei datei) {
    	datei.setPublicity(!datei.isPublicity());
    	Datenbank.setPublic(datei.getDatei_id(), datei.isPublicity());
    	
    	if(datei.isPublicity())
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Öffentlich","Die Datei \""+datei.getDatei_name()+"\" ist ab sofort öffentlich.") );
    	else
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Privat","Die Datei \""+datei.getDatei_name()+"\" ist ab sofort privat.") );
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
    
    //Datei bearbeiten
    
    //gilt für Dateien und Ordner
    public void select(Datei datei) {
    	this.selectedDatei = datei;
    	this.newName = datei.getDatei_name_ohne_endung();
    	ordnerSelektor.reset();
    }
    
    public Datei getSelectedDatei() {
		return selectedDatei;
	}
    
    public void setNewName(String newName) {
		this.newName = newName;
	}
    
    public String getNewName() {
		return newName;
	}
    
    public void renameDatei() {
    	
    	if(newName == null || newName.trim().equals("")) {
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Name darf nicht leer sein.") );
    		return;
    	}
    		
    	String endung = selectedDatei.getDatei_name().substring(selectedDatei.getDatei_name().lastIndexOf('.')+1, selectedDatei.getDatei_name().length()).toLowerCase();
    	Datenbank.renameDatei(selectedDatei, newName+"."+endung);
    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Datei umbenannt",newName+"."+endung) );
    	update();
    }
    
    public void moveDatei() {
    	boolean ok = Datenbank.moveFile(selectedDatei.getDatei_id(), ordnerSelektor.getOrdnerID());
    	
    	if(ok)
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Datei verschoben",ordnerSelektor.getVerzeichnisNotEmpty()) );
    	else
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "unbekannter Fehler") );
    	update();
    }
    
    //Ordner bearbeiten
    
    public void renameOrdner() {
    	
    	if(newName == null || newName.trim().equals("")) {
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Name darf nicht leer sein.") );
    		return;
    	}
    	
    	if(newName.contains("/") || newName.contains("\\")) {
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Name enthält unerlaubte Zeichen.") );
    		return;
    	}
    		
    	Datenbank.renameFolder(selectedDatei.getDatei_id(), newName);
    	
    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Ordner umbenannt",newName) );
    	update();
    }
    
    //unterordner -> parent
    //selectedDatei -> ordnerSelektor.getOrdnerID()
    public void moveOrdner() {
    	if(selectedDatei.getDatei_id() == ordnerSelektor.getOrdnerID()) {
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Ordner stimmen überein.") );
    		return;
    	}
    	
    	if(contains(selectedDatei, ordnerSelektor.getOrdnerID())) {
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Ordner dürfen nicht voneinander abhängig sein.") );
    		return;
    	}
    	
    	boolean ok = Datenbank.moveFolder(selectedDatei.getDatei_id(), ordnerSelektor.getOrdnerID());
    	if(ok)
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Ordner verschoben",ordnerSelektor.getVerzeichnisNotEmpty()) );
    	else
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "unbekannter Fehler") );
    	update();
    }
    
    private boolean contains(Datei ordner, int kind_id) {
    	
    	if(ordner.getDatei_id() == kind_id)
    		return true;
    	
    	for(int i = 0; i < ordner.getKinder().length; i++) {
    		if(contains(ordner.getKinder()[i],kind_id))
    			return true;
    	}
    	
    	return false;
    }
    
    public DirectoryView getOrdnerSelektor() {
		return ordnerSelektor;
	}
	
}