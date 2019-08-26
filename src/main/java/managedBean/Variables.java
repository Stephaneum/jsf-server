package managedBean;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.Datei;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class Variables {
	
	/*
	 * globale Variablen auf der Seite Konfig
	 * 
	 */
	
	@ManagedProperty("#{sideMenu}")
	private SideMenu sideMenu; //showDummy
	
	private String speicherort, backupDir;
	private double emailTimeout;
	
	final static private String SIZE_B = "Bytes", SIZE_KB = "KB", SIZE_MB = "MB", SIZE_GB = "GB";
	final static private String[] SIZES = {SIZE_B,SIZE_KB,SIZE_MB,SIZE_GB};
	private int speicherLehrer, speicherSchueler;
	private String sizeTypeSchueler, sizeTypeLehrer;
	private String lastSpeicherLehrer, lastSpeicherSchueler;
	
	private int pictureSize;
	private String lastPictureSize, sizeTypePictureSize;
	
	@PostConstruct
    public void init() {
		if(Datenbank.existDatabase()) {
			speicherort = Datenbank.getSpeicherort();
			backupDir = Datenbank.getBackupDir();
			emailTimeout = Datenbank.getTimeoutPasswortVergessen() / 3600000;
		}
	}
	
	//Speicherplatz
	
	public int getSpeicherLehrer() {
		return speicherLehrer;
	}
	
	public void setSpeicherLehrer(int speicherLehrer) {
		this.speicherLehrer = speicherLehrer;
	}
	
	public int getSpeicherSchueler() {
		return speicherSchueler;
	}
	
	public void setSpeicherSchueler(int speicherSchueler) {
		this.speicherSchueler = speicherSchueler;
	}
	
	public String getLastSpeicherLehrer() {
		if(lastSpeicherLehrer == null)
			lastSpeicherLehrer = Datei.convertSizeToString(Integer.parseInt(Datenbank.getSpeicherLehrer()));
		return lastSpeicherLehrer;
	}
	
	public String getLastSpeicherSchueler() {
		if(lastSpeicherSchueler == null)
			lastSpeicherSchueler = Datei.convertSizeToString(Integer.parseInt(Datenbank.getSpeicherSchueler()));
		return lastSpeicherSchueler;
	}
	
	public String[] getSizes() {
		return SIZES;
	}
	
	public String getSizeTypeLehrer() {
		return sizeTypeLehrer;
	}
	
	public void setSizeTypeLehrer(String sizeTypeLehrer) {
		this.sizeTypeLehrer = sizeTypeLehrer;
	}
	
	public String getSizeTypeSchueler() {
		return sizeTypeSchueler;
	}
	
	public void setSizeTypeSchueler(String sizeTypeSchueler) {
		this.sizeTypeSchueler = sizeTypeSchueler;
	}
	
	public void updateSpeicherPlatz() {
		
		//Validierung
		if(speicherSchueler < 0 || speicherLehrer < 0) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Zahl muss größer oder gleich null sein.") );
			return;
		}
		
		//Konvertierung (Lehrer)
		switch(sizeTypeLehrer) {
		case SIZE_B:
			break;
		case SIZE_KB:
			speicherLehrer = Datei.convertToBytes(speicherLehrer, Datei.SIZE_KB);
			break;
		case SIZE_MB:
			speicherLehrer = Datei.convertToBytes(speicherLehrer, Datei.SIZE_MB);
			break;
		case SIZE_GB:
			speicherLehrer = Datei.convertToBytes(speicherLehrer, Datei.SIZE_GB);
			break;
		default:
			//Reset
			speicherSchueler = 0;
			speicherLehrer = 0;
			sizeTypeLehrer = null;
			sizeTypeSchueler = null;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte eine Größenordnung wählen") );
			return;
		}
		
		//Konvertierung (Schüler)
		switch(sizeTypeSchueler) {
		case SIZE_B:
			break;
		case SIZE_KB:
			speicherSchueler = Datei.convertToBytes(speicherSchueler, Datei.SIZE_KB);
			break;
		case SIZE_MB:
			speicherSchueler = Datei.convertToBytes(speicherSchueler, Datei.SIZE_MB);
			break;
		case SIZE_GB:
			speicherSchueler = Datei.convertToBytes(speicherSchueler, Datei.SIZE_GB);
			break;
		default:
			//Reset
			speicherSchueler = 0;
			speicherLehrer = 0;
			sizeTypeLehrer = null;
			sizeTypeSchueler = null;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte eine Größenordnung wählen") );
			return;
		}
		
		//Datenbank-Eintrag aktualisieren
		Datenbank.setSpeicherLehrer(speicherLehrer);
		Datenbank.setSpeicherSchueler(speicherSchueler);
		
		//vorher aktualisieren
		lastSpeicherLehrer = Datei.convertSizeToString(speicherLehrer);
		lastSpeicherSchueler = Datei.convertSizeToString(speicherSchueler);
		
		//Reset
		speicherSchueler = 0;
		speicherLehrer = 0;
		sizeTypeLehrer = null;
		sizeTypeSchueler = null;
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Speicherplatz gesetzt","Zukünftige Lehrer/innen und Schüler/innen haben einen anderen Speicherplatz.") );
	}
	
	//Speicherort
	
	public void setSpeicherort(String speicherort) {
		this.speicherort = speicherort;
	}
	
	public String getSpeicherort() {
		return speicherort;
	}
	
	public void updateSpeicherort() {
		Datenbank.setSpeicherort(speicherort);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Variable aktualisiert","Speicherort aktualisiert") );
	}
	
	//E-Mail-Timeout
	
	public void setEmailTimeout(double emailTimeout) {
		this.emailTimeout = emailTimeout;
	}
	
	public double getEmailTimeout() {
		return emailTimeout;
	}
	
	public void updateEmailTimeout() {
		Datenbank.setTimeoutPasswortVergessen((int) (emailTimeout*3600000));
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Variable aktualisiert","E-Mail-Timeout aktualisiert") );
	}
	
	//Vertretungsplan
	
	public String openKonfigVertretung() {
		return URLManager.KONFIG_VERTRETUNG+"?faces-redirect=true";
	}
	
	//Picture Size
	
	public void setPictureSize(int pictureSize) {
		this.pictureSize = pictureSize;
	}
	
	public int getPictureSize() {
		return pictureSize;
	}
	
	public String getLastPictureSize() {
		if(lastPictureSize == null)
			lastPictureSize = Datei.convertSizeToString(Datenbank.getPictureSize());
		return lastPictureSize;
	}
	
	public void setSizeTypePictureSize(String sizeTypePictureSize) {
		this.sizeTypePictureSize = sizeTypePictureSize;
	}
	
	public String getSizeTypePictureSize() {
		return sizeTypePictureSize;
	}
	
	public void updatePictureSize() {
		
		//Validierung
		if(pictureSize < 0) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Zahl muss größer oder gleich null sein.") );
			return;
		}
		
		//Konvertierung (Lehrer)
		switch(sizeTypePictureSize) {
		case SIZE_B:
			break;
		case SIZE_KB:
			pictureSize = Datei.convertToBytes(pictureSize, Datei.SIZE_KB);
			break;
		case SIZE_MB:
			pictureSize = Datei.convertToBytes(pictureSize, Datei.SIZE_MB);
			break;
		case SIZE_GB:
			pictureSize = Datei.convertToBytes(pictureSize, Datei.SIZE_GB);
			break;
		default:
			//Reset
			pictureSize = 0;
			sizeTypePictureSize = null;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte eine Größenordnung wählen") );
			return;
		}
		
		//Datenbank-Eintrag aktualisieren
		Datenbank.setPictureSize(pictureSize);
		
		//vorher aktualisieren
		lastPictureSize = Datei.convertSizeToString(pictureSize);
		
		//Reset
		pictureSize = 0;
		sizeTypePictureSize = null;
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Bildgröße festgelegt",lastPictureSize) );
	}
	
	//Port
	
	public boolean isRedirectPort() {
		return Datenbank.isRedirectPort();
	}
	
	public void setRedirectPort(boolean redirect) {
		Datenbank.setRedirectPort(redirect);
		
		if(redirect)
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Einstellungen gespeichert","Nutzer werden auf Port 80 weitergeleitet.") );
		else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Einstellungen gespeichert","Es findet keine Weiterleitung statt.") );
	}
	
	//showDummy
	
	public boolean isShowDummy() {
		return Datenbank.isShowDummy();
	}
	
	public void setShowDummy(boolean show) {
		Datenbank.setShowDummy(show);
		sideMenu.init();
		
		if(show)
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Einstellungen gespeichert","Dummys aktiviert") );
		else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Einstellungen gespeichert","Dummys deaktiviert") );
	}
	
	//Backup
	
	public void setBackupDir(String backupDir) {
		this.backupDir = backupDir;
	}
	
	public String getBackupDir() {
		return backupDir;
	}
	
	public void updateBackupDir() {
		Datenbank.setBackupDir(backupDir);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Variable aktualisiert","Backup-Ordner aktualisiert") );
	}
	
	//ManagedProperty
	
	public void setSideMenu(SideMenu sideMenu) {
		this.sideMenu = sideMenu;
	}
	
}


