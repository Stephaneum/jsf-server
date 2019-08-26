package managedBean;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.TabChangeEvent;

import mysql.Datenbank;
import objects.Nachricht;
import objects.Nutzer;
import objects.Projekt;
import sitzung.Sitzung;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class LehrerChat {
	
	//allgemeine Daten
	private int nutzerID; //fürs rendered-Attribut benötigt (Löschen anzeigen)
	private boolean admin = false;
	
	//Chaträume
	// ID = 0 -> Allgemein, ID = -1 -> neuer Chatraum
	private Projekt[] projekte;
	private Projekt selectedProjekt;
	private boolean projektLeiter;
	private Nutzer[] projektNutzer;
	private String name;
	
	//Chaträume : Lehrer hinzufügen
	private Nutzer[] lehrerAddObjList; //alle Lehrer
	private String[] lehrerAddList; //alle Lehrer als String
	private String[] lehrerAddSelectedList; //alle ausgewählten Lehrer als String
	
	//Lehrer
	int lehrerAnzahl;
	Nutzer[] allLehrer;
	Nutzer[] onlineLehrer;
	Nutzer[] offlineLehrer;
	
	//Chat
	private Nachricht[] nachricht; //Rohdaten aus der Datenbank
	private ArrayList<Chat> chat; //speziell formatiert für die Ausgabe
	private String newMessage;
	private Chat selectedChatZeile;
	
	//Dateien
	@ManagedProperty("#{dateien}")
	private Dateien dateien;
	
	@PostConstruct
	public void init() {
		Konsole.method("LehrerChat.init()");
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer != null && nutzer.getRang() != Nutzer.RANG_GAST_NO_LOGIN) {
			nutzerID = nutzer.getNutzer_id();
			admin = nutzer.getRang() == Nutzer.RANG_ADMIN;
			
			//projekte (Chaträume)
			Projekt[] realProjekte = Datenbank.getProjektArray(nutzer.getNutzer_id(), true, true);
			Projekt allgemein = new Projekt(0, "Allgemein");
			Projekt neuerRaum = new Projekt(-1, "Neuer Chatraum");
			
			projekte = new Projekt[realProjekte.length+2];
			projekte[0] = allgemein;
			projekte[projekte.length-1] = neuerRaum;
			for(int i = 0; i < realProjekte.length; i++) {
				projekte[i+1] = realProjekte[i];
			}
			selectedProjekt = allgemein;
			
			updateChat();
			updateLehrer();
			dateien.initLehrerChat();
		} else {
			nutzerID = 0;
		}
	}
	
	//Tabs
	
	public void onTabChange(TabChangeEvent event) {
		String tabTitle = event.getTab().getTitle();
		
		int projektIndex = 0;
		for(int i = 0; i < projekte.length; i++)
			if(projekte[i].getName().equals(tabTitle))
				projektIndex = i;
		
		selectedProjekt = projekte[projektIndex];
		
		Konsole.method("onTabChange: "+selectedProjekt.getProjektID());
		
		if(selectedProjekt.getProjektID() == 0)
			dateien.initLehrerChat();
		else if(selectedProjekt.getProjektID() > 0){
			projektNutzer = Datenbank.getNutzerProjektArray(selectedProjekt.getProjektID(),false);
			dateien.initLehrerChatraum(selectedProjekt.getProjektID(), selectedProjekt.getName());
			name = selectedProjekt.getName();
			projektLeiter = selectedProjekt.getProjektLeiter().getNutzer_id() == nutzerID;
		} else {
			//ID = -1
			name = null;
			updateLehrerAdd();
		}
		
		if(selectedProjekt.getProjektID() != -1) {
			//vorher sollte projektNutzer aktualisiert werden, deswegen updateChat, updateLehrer zum Schluss
			updateChat();
			updateLehrer();
			
			if(projektIndex > 0) {
				//Lehrer hinzufügen: Arrays aktualisieren
				updateLehrerAdd();
			}
		}
    }
	
	public Projekt[] getProjekte() {
		return projekte;
	}
	
	public int getSelectedProjektID() {
		if(selectedProjekt == null)
			return 0;
		return selectedProjekt.getProjektID();
	}
	
	//Chat
	
	public void updateChat() {
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null || (nutzer.getRang() != Nutzer.RANG_LEHRER && nutzer.getRang() != Nutzer.RANG_ADMIN))
			return;
		
		int newAnzahl = selectedProjekt.getProjektID() > 0 ?
				Datenbank.getNachrichtAnzahl(nutzerID,selectedProjekt.getProjektID(),false,true) : Datenbank.getNachrichtAnzahl(nutzerID,-1,true,true);
		
		if(nachricht == null || nachricht.length != newAnzahl) {
			
			//nur aktualisieren, wenn Änderungen da sind
			nachricht = selectedProjekt.getProjektID() > 0 ?
					Datenbank.getNachricht(selectedProjekt.getProjektID(), false, projektNutzer.length) : Datenbank.getNachricht(-1, true, lehrerAnzahl);
					
			chat = new ArrayList<>((int) (nachricht.length*1.5f));
			
			String lastDate = null;
			for(int i = 0; i < nachricht.length; i++) {
				
				if(lastDate == null || !lastDate.equals(nachricht[i].getDate())) {
					
					String date = "<p style=\"text-align:center\">"+nachricht[i].getDate()+"</p>";
					chat.add(new Chat(date));
					
					lastDate = nachricht[i].getDate();
				}
				
				boolean editable = admin || nachricht[i].getSenderID() == nutzerID;
				
				chat.add(new Chat(editable, nachricht[i]));
			}
		}
	}
	
	public void updateLehrer() {
		
		if(selectedProjekt == null)
			return;
		
		allLehrer = Datenbank.getLehrerChat();
		Nutzer[] lehrer = allLehrer; //allLehrer sollte nicht mit einer anderen Instanz überschrieben werden
		
		//davon filtern wir ggf. die Lehrer aus, die nicht im Projekt sind
		if(selectedProjekt.getProjektID() > 0) {
			
			Nutzer[] filtered = new Nutzer[projektNutzer.length];
			int currIndex = 0;
			for(Nutzer n : allLehrer) {
				if(Nutzer.contains(projektNutzer, n.getNutzer_id())) {
					filtered[currIndex] = n;
					currIndex++;
				}
			}
			
			lehrer = filtered;
		}
		
		int anzahlOnline = 0;
		for(Nutzer n : lehrer) {
			if(n.getLastLehrerChatTime().equals("$")) // das Dollar-Zeichen ist ein Flag für "gerade online"
				anzahlOnline++;
		}
		
		lehrerAnzahl = lehrer.length;
		onlineLehrer = new Nutzer[anzahlOnline];
		offlineLehrer = new Nutzer[lehrerAnzahl-anzahlOnline];
		int currOnline = 0, currOffline = 0;
		for(Nutzer n : lehrer) {
			if(n.getLastLehrerChatTime().equals("$")) {
				onlineLehrer[currOnline] = n;
				currOnline++;
			} else {
				offlineLehrer[currOffline] = n;
				currOffline++;
			}
		}
	}
	
	public void updateLehrerAdd() {
		
		if(selectedProjekt.getProjektID() == -1) {
			//neuer Chatraum bzw. Projekt
			lehrerAddObjList = new Nutzer[allLehrer.length - 1];
			lehrerAddList = new String[lehrerAddObjList.length];
			lehrerAddSelectedList = null;
			
			//alle außer mich
			int currIndex = 0;
			for(Nutzer lehrer : allLehrer) {
				if(lehrer.getNutzer_id() != nutzerID) {
					lehrerAddObjList[currIndex] = lehrer;
					lehrerAddList[currIndex] = lehrer.getVorname()+" "+lehrer.getNachname();
					currIndex++;
				}
			}
			
		} else {
			//im Projekt neue Lehrer hinzufügen
			lehrerAddObjList = new Nutzer[allLehrer.length - projektNutzer.length];
			lehrerAddList = new String[lehrerAddObjList.length];
			lehrerAddSelectedList = null;
			
			int currIndex = 0;
			for(Nutzer lehrer : allLehrer) {
				if(!Nutzer.contains(projektNutzer, lehrer.getNutzer_id())) {
					lehrerAddObjList[currIndex] = lehrer;
					lehrerAddList[currIndex] = lehrer.getVorname()+" "+lehrer.getNachname();
					currIndex++;
				}
			}
		}
	}
	
	public ArrayList<Chat> getChat() {
		return chat;
	}
	
	public int getNachrichtenAnzahl() {
		if(nachricht != null)
			return nachricht.length;
		else
			return 0;
	}
	
	public String getNewMessage() {
		return newMessage;
	}
	
	public void setNewMessage(String newMessage) {
		this.newMessage = newMessage;
	}
	
	public void sendMessage() {
		if(newMessage == null || newMessage.trim().equals("")) {
			return;
		}
		
		if(selectedProjekt.getProjektID() > 0)
			Datenbank.addNachricht(nutzerID, selectedProjekt.getProjektID(), false, newMessage);
		else
			Datenbank.addNachricht(nutzerID, -1, true, newMessage);
		newMessage = null;
		updateChat();
	}
	
	public Nutzer[] getOnlineLehrer() {
		return onlineLehrer;
	}
	
	public int getOnlineAmount() {
		return onlineLehrer.length;
	}
	
	public Nutzer[] getOfflineLehrer() {
		return offlineLehrer;
	}
	
	public int getOfflineAmount() {
		return offlineLehrer.length;
	}
	
	public void deleteNachricht(Chat chatZeile) {
		Datenbank.deleteNachricht(chatZeile.nachricht.getId());
		nachricht = null; // force updateChat
		updateChat();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Gelöscht", "Die Nachricht wurde für immer gelöscht.") );
	}
	
	public void prepareEditNachricht(Chat chatZeile) {
		newMessage = chatZeile.nachricht.getText();
		selectedChatZeile = chatZeile;
	}
	
	public Chat getSelectedChatZeile() {
		return selectedChatZeile;
	}
	
	public void editNachricht() {
		if(newMessage == null || newMessage.trim().equals("")) {
			return;
		}
		
		Datenbank.editNachricht(selectedChatZeile.nachricht.getId(), newMessage);
		selectedChatZeile = null;
		nachricht = null; // force updateChat
		updateChat();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Änderungen gespeichert", newMessage) );
		newMessage = null;
	}
	
	public int getNutzerID() {
		return nutzerID;
	}
	
	public boolean isProjektLeiter() {
		return projektLeiter;
	}
	
	//Konfiguration Projekt (bzw. Chatraum)
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void renameProjekt() {
    	
    	if(selectedProjekt.getProjektID() <= 0)
    		return;
    	
    	if(name == null || name.trim().equals("")) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Der Name muss aus mindestens einem Zeichen bestehen.") );
			return;
		}
    	
    	Datenbank.renameProjekt(selectedProjekt.getProjektID(), name);
    	selectedProjekt.update(name);
    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Chatraum umbenannt",name));
    }
	
	//Konfiguration Projekt (bzw. Chatraum) : Lehrer hinzufügen
	
	public String[] getLehrerAddList() {
		return lehrerAddList;
	}
	
	public String[] getLehrerAddSelectedList() {
		return lehrerAddSelectedList;
	}
	
	public void setLehrerAddSelectedList(String[] lehrerAddSelectedList) {
		this.lehrerAddSelectedList = lehrerAddSelectedList;
	}
	
	public void addLehrer() {
		
		if(selectedProjekt.getProjektID() == 0)
    		return;
		
		for(int i = 0; i < lehrerAddSelectedList.length; i++) {
			for(int x = 0; x < lehrerAddList.length; x++) {
				if(lehrerAddSelectedList[i].equals(lehrerAddList[x])) {
					Datenbank.joinProjekt(lehrerAddObjList[x], selectedProjekt, false, true);
					break;
				}
			}
		}
		
		if(selectedProjekt.getProjektID() == -1) {
			//mich selber noch hinzufügen
			Datenbank.joinProjekt(Sitzung.getNutzer(), selectedProjekt, false, true);
		}
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Lehrer/innen hinzugefügt","Anzahl: +"+lehrerAddSelectedList.length));
		
		projektNutzer = Datenbank.getNutzerProjektArray(selectedProjekt.getProjektID(),false);
		updateLehrer();
		updateLehrerAdd();
	}
	
	public String createProjekt() {
		if(selectedProjekt.getProjektID() != -1)
    		return null;
    	
    	if(name == null || name.trim().equals("")) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Der Name muss aus mindestens einem Zeichen bestehen.") );
			return null;
		}
    	
    	selectedProjekt = Datenbank.createProjekt(name, true, true);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Chatraum erstellt",name));
		addLehrer();
		
		return URLManager.LEHRER_CHAT;
	}
	
	public String quitProjekt() {
		
		Nutzer nutzer = Sitzung.getNutzer();
		
		if(nutzer == null) {
			return URLManager.LOGIN+"?faces-redirect=true";
		}
		
		int antwort = Datenbank.quitNutzerProjekt(selectedProjekt, nutzer);
		switch(antwort) {
		case -1:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "unbekannter Fehler") );
			return null;
		case 0:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Chatraum verlassen",selectedProjekt.getName()) );
			return URLManager.LEHRER_CHAT;
		case 1:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Chatraum verlassen",selectedProjekt.getName()) );
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Chatraum gelöscht",selectedProjekt.getName()) );
			return URLManager.LEHRER_CHAT;
		default:
			return null;
		}
	}
	
	//ManagedProperty
	public void setDateien(Dateien dateien) {
		this.dateien = dateien;
	}
	
	public static class Chat {
		
		//repräsentiert eine Zeile im Chatverlauf
		
		private Nachricht nachricht;
		private boolean editable;
		private String date;
		private String shortRaw; //dynamisch zugewiesen
		
		private Chat(boolean editable, Nachricht nachricht) {
			this.nachricht = nachricht;
			this.editable = editable;
			this.date = null;
		}
		
		private Chat(String date) {
			this.nachricht = null;
			this.editable = false;
			this.date = date;
		}
		
		public Nachricht getNachricht() {
			return nachricht;
		}
		
		public String getDate() {
			return date;
		}
		
		public String getShortRaw() {
			if(shortRaw == null) {
				int actualLength = nachricht.getText().length();
				if(actualLength >= 20) {
					shortRaw = nachricht.getText().substring(0, 20)+" [...]";
				} else {
					shortRaw = nachricht.getText();
				}
				
			}
			return shortRaw;
		}
		
		public boolean isEditable() {
			return editable;
		}
	}
	
}