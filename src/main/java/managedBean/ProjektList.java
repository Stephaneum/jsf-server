package managedBean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.Nutzer;
import objects.Projekt;
import sitzung.Sitzung;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class ProjektList {
	
	private Projekt[] projektArrayAkzeptieren; //alle Projekte, die vom angemeldeten Betreuer noch akzeptiert werden müssen
	
	private Projekt[] projektArray, projektArrayAll;
	private boolean isAdmin, isSchueler; //seperate Variablen, weil in XHTML nicht validiert wird (z.B. kein Zugriff auf Nutzer.RANG_SCHUELER exist.)
	private boolean allowCreateProjekt;
	
	//Projekt erstellen
	private String projektName;
	
	//Projekt erstellen : Schüler only
	private String[] betreuerArray, selectedBetreuer;
	private Nutzer[] betreuerObj;
	
	@ManagedProperty("#{projektOpen}")
	private ProjektOpen projektOpen; //Vorschau
	
	public ProjektList() {
		updateTabelle();
		updateTabelleAll();
		updateTabelleAkzeptiert();
		
		Nutzer nutzer = Sitzung.getNutzer();
		
		if(nutzer != null) {
			isAdmin = nutzer.getRang() == Nutzer.RANG_ADMIN;
			isSchueler = nutzer.getRang() == Nutzer.RANG_SCHUELER;
			allowCreateProjekt = Datenbank.isProjektErstellen(nutzer.getNutzer_id());
			
			if(allowCreateProjekt && isSchueler) {
				betreuerObj = Datenbank.getAllNutzer(Nutzer.RANG_LEHRER);
				betreuerArray = new String[betreuerObj.length];
				
				for(int i = 0; i < betreuerObj.length; i++) {
					betreuerArray[i] = betreuerObj[i].getFormalName();
				}
			}
		}
	}
	
	public Projekt[] getProjektArray() {
    	return projektArray;
    }
	
	public String getProjektName() {
		return projektName;
	}
	
	public void setProjektName(String projektName) {
		this.projektName = projektName;
	}
	
	public boolean isAllowCreateProjekt() {
		return allowCreateProjekt;
	}
	
	public String[] getBetreuerArray() {
		return betreuerArray;
	}
	
	public String[] getSelectedBetreuer() {
		return selectedBetreuer;
	}
	
	public void setSelectedBetreuer(String[] selectedBetreuer) {
		this.selectedBetreuer = selectedBetreuer;
	}
	
	public String createProjekt() {
		
		if(projektName == null || projektName.trim().length() < 4) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Der Name muss aus mindestens vier Zeichen bestehen.") );
			return null;
		}
		
		if(isSchueler && (selectedBetreuer == null || selectedBetreuer.length == 0)) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Du musst mindestens ein/e Betreuer/in auswählen.") );
			return null;
		}
		
		Projekt projekt;
		
		if(isSchueler) {
			projekt = Datenbank.createProjekt(projektName.trim(),false,false);
			//Betreuer hinzufügen
			for(int i = 0; i < selectedBetreuer.length; i++) {
				for(int x = 0; x < betreuerArray.length; x++) {
					if(selectedBetreuer[i].equals(betreuerArray[x])) {
						Datenbank.joinProjekt(betreuerObj[x], projekt, true, false);
						break;
					}
				}
			}
		} else {
			projekt = Datenbank.createProjekt(projektName.trim(),true,false);
		}
		
		if(projekt == null) {
			return URLManager.LOGIN; //eventuell Sitzung abgelaufen
		}
		
		updateTabelle();
		updateTabelleAll();
		updateTabelleAkzeptiert();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Projekt erstellt",projektName.trim()));
		projektName = null;
		selectedBetreuer = null;
		return null;
	}
	
	public String openProjekt(Projekt projekt) {
		Nutzer nutzer = Sitzung.getNutzer(); //Nutzer-Objekt erhalten
		if(nutzer != null) {
			nutzer.setOpenProjekt(projekt); //in dem Nutzer-Objekt die Variablen ändern
			return URLManager.PROJEKT_OPEN+"?faces-redirect=true";
		} else {
			return URLManager.LOGIN+"?faces-redirect=true"; //Sitzung abgelaufen
		}
	}
	
	//Betreuer-akzeptieren
	
	public Projekt[] getProjektArrayAkzeptieren() {
		return projektArrayAkzeptieren;
	}
	
	private void updateTabelleAkzeptiert() {
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer != null)
			projektArrayAkzeptieren = Datenbank.getProjektArray(nutzer.getNutzer_id(),false,false);
	}
	
	public String akzeptieren(Projekt projekt) {
		Nutzer nutzer = Sitzung.getNutzer(); //Nutzer-Objekt erhalten
		if(nutzer != null) {
			int status = Datenbank.projektAkzeptieren(nutzer.getNutzer_id(), projekt.getProjektID());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Akzeptiert",projekt.getName()));
			if(status == 0) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Neuer Status","Warten auf andere Betreuer"));
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Neuer Status","Projekt verfügbar"));
			}
			updateTabelleAkzeptiert();
			updateTabelle();
			updateTabelleAll();
			return null;
		} else {
			return URLManager.LOGIN+"?faces-redirect=true"; //Sitzung abgelaufen
		}
	}
	
	public String ablehnen(Projekt projekt) {
		Nutzer nutzer = Sitzung.getNutzer(); //Nutzer-Objekt erhalten
		if(nutzer != null) {
			Datenbank.forceDeleteProjekt(projekt.getProjektID());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Abgelehnt",projekt.getName()));
			updateTabelleAkzeptiert();
			return null;
		} else {
			return URLManager.LOGIN+"?faces-redirect=true"; //Sitzung abgelaufen
		}
	}
	
	//admin-only
	public void previewProjekt(Projekt projekt) {
		Nutzer nutzer = Sitzung.getNutzer(); //Nutzer-Objekt erhalten
		if(nutzer != null) {
			nutzer.setOpenProjekt(projekt); //in dem Nutzer-Objekt die Variablen ändern
			projektOpen.init(); //neu laden
		}
	}
	
	public Projekt[] getProjektArrayAll() {
    	return projektArrayAll;
    }
	
	private void updateTabelle() {
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer != null)
			projektArray = Datenbank.getProjektArray(nutzer.getNutzer_id(),true,false);
	}
	
	private void updateTabelleAll() {
		projektArrayAll = Datenbank.getAllProjektArray();
	}
	
	public boolean isAdmin() {
		return isAdmin;
	}
	
	public boolean isSchueler() {
		return isSchueler;
	}
	
	public void deleteProjekt(objects.Projekt projekt) {
		
		Datenbank.deleteProjekt(projekt.getProjektID());
		updateTabelle();
		updateTabelleAll();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Projekt gelöscht",projekt.getName()));
	}
	
	public void toggleChat(Projekt projekt) {
		boolean chat = !projekt.isChat();
		
		Datenbank.setProjektChat(projekt.getProjektID(), chat);
		updateTabelleAll();
		
		if(chat)
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Änderung gespeichert","Chat aktiviert: "+projekt.getName()));
		else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Änderung gespeichert","Chat gesperrt: "+projekt.getName()));
	}
	
	//managedProperty
	
	public void setProjektOpen(ProjektOpen projektOpen) {
		this.projektOpen = projektOpen;
	}
    
	
}