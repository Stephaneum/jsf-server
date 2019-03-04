package managedBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.TabChangeEvent;

import cms.ZuordnungFinder;
import mysql.Datenbank;
import objects.Beitrag;
import objects.Gruppe;
import objects.Nachricht;
import objects.Nutzer;
import objects.Projekt;
import sitzung.Sitzung;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class ProjektOpen {
	
	final static private int TAB_BEITRAG = 0, TAB_DATEIEN = 1, TAB_KONFIG = 2;
	
	//allgemeine Daten
	private int nutzerID; //fürs rendered-Attribut benötigt (Löschen anzeigen)
	private boolean projektLeiter;
	private Projekt openProjekt;
	private Nutzer[] projektNutzer; //enthält KEINE Betreuer
	private Nutzer[] projektBetreuer;
	private String leiterString, betreuerString;
	
	//Chat
	private boolean chatAllowed;
	private Nachricht[] nachricht;
	private String chat, newMessage;
	
	private int tab; //0...Beiträge, 1... Dateien
	
	//Dateien
	@ManagedProperty("#{dateien}")
	private Dateien dateien;
	
	//Suche
	private boolean search_begin;
	private Nutzer[] nutzerSearch;
	private String vorname, nachname;
	
	//Beiträge
	private Gruppe gruppe;
	private Beitrag[] beitragArray; //Liste der Beiträge
	private Beitrag openBeitrag; //ein Beitrag wurde geöffnet
	
	//Konfig
	private String newName;
	
	@ManagedProperty("#{zuordnungFinder}")
	private ZuordnungFinder zuordnungFinder;
	private boolean addGruppe; //Projektleiter && Admin || Rubrik
	
	@PostConstruct
	public void init() {
		
		openBeitrag = null;
		addGruppe = false;
		gruppe = null;
		tab = 0; //der erste Tab
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer != null) {
			nutzerID = nutzer.getNutzer_id();
			
			openProjekt = nutzer.getOpenProjekt();
			
			if(openProjekt != null) {
				int projektID = openProjekt.getProjektID();
				
				projektBetreuer = Datenbank.getNutzerProjektArray(projektID,true);
				projektLeiter = openProjekt.getProjektLeiter().getNutzer_id() == nutzerID || Nutzer.contains(projektBetreuer, nutzerID);
				
				projektNutzer = Datenbank.getNutzerProjektArray(projektID,false);
				
				leiterString = openProjekt.getProjektLeiter().getVorname()+" "+openProjekt.getProjektLeiter().getNachname();
				betreuerString = Nutzer.toNameString(projektBetreuer, true);
				
				chatAllowed = Datenbank.isProjektChat(projektID);
				beitragArray = Datenbank.getProjektBeitrag(projektID);
				if(beitragArray != null)
					gruppe = Datenbank.getProjektGruppe(projektID);
				
				if(projektLeiter) {
					
					if(Datenbank.getRubrik(nutzerID) != null) {
						zuordnungFinder.init(nutzer.getNutzer_id()); //speziell für Projektleiter, die Rubrik führen, wird neu initialisiert
						addGruppe = true;
					} else if(nutzer.getRang() == Nutzer.RANG_ADMIN) {
						addGruppe = true;
						//für Admins wurde schon initialisiert (init(-1))
					}
					
					newName = openProjekt.getName();	
				}
				
				updateChat();
				
				dateien.initProjektspeicher(projektID, projektLeiter);
			} else {;
				projektLeiter = false;
				projektNutzer = null;
			}
		} else {
			nutzerID = 0;
			openProjekt = null;
			projektLeiter = false;
			projektNutzer = null;
		}
	}
	
	public void onTabChange(TabChangeEvent event) {
		String tabID = event.getTab().getId();
		
		if(tabID.equals("beitragTab")) {
			tab = TAB_BEITRAG;
		} else if(tabID.equals("dateiTab")){
			tab = TAB_DATEIEN;
		} else {
			tab = TAB_KONFIG;
		}
    }
	
	public int getTab() {
		return tab;
	}
	
	public void setTab(int tab) {
		this.tab = tab;
	}
	
	//Chat
	
	public boolean isChatAllowed() {
		return chatAllowed;
	}
	
	public void updateChat() {
		
		if(openProjekt == null)
			return;
		
		boolean exist = Datenbank.existProjekt(openProjekt.getProjektID());
		if(!exist) {
			try {
				Nutzer nutzer = Sitzung.getNutzer();
				if(nutzer != null)
					nutzer.setOpenProjekt(null);
				
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Weiterleitung","Projekt wurde gelöscht."));
				FacesContext.getCurrentInstance().getExternalContext().redirect(URLManager.PROJEKT+".xhtml");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		int newAnzahl = Datenbank.getNachrichtAnzahl(-1, openProjekt.getProjektID(), false, false);
		
		if(nachricht == null || nachricht.length != newAnzahl) {
			
			//nur aktualisieren, wenn Änderungen da sind
			nachricht = Datenbank.getNachricht(openProjekt.getProjektID(), false, projektNutzer.length);
			
			StringBuilder builder = new StringBuilder();
			String lastDate = null;
			for(int i = 0; i < nachricht.length; i++) {
				
				if(lastDate == null || !lastDate.equals(nachricht[i].getDate())) {
					builder.append("<p style=\"text-align:center\">");
					builder.append(nachricht[i].getDate());
					builder.append("</p>");
					lastDate = nachricht[i].getDate();
				}
				
				builder.append("<p title=\""+nachricht[i].getTime()+"\"><b><u>");
				builder.append(nachricht[i].getSender());
				builder.append("</u></b> ");
				builder.append(nachricht[i].getText());
				builder.append("</p>");
			}
			chat = builder.toString();
		}
	}
	
	public String getChat() {
		return chat;
	}
	
	public int getChatHeight() {
		return projektLeiter ? 410 : 200;
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
		if(newMessage == null || newMessage.trim().equals("") || openProjekt == null) {
			return;
		}
		Datenbank.addNachricht(nutzerID, openProjekt.getProjektID(), false, newMessage);
		newMessage = null;
		updateChat();
	}
	
	public void clearNachrichten() {
		if(openProjekt == null)
			return;
		
		Datenbank.clearNachrichten(openProjekt.getProjektID());
		updateChat();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Gelöscht","Alle Nachrichten gelöscht."));
	}
    
    //allgemeine Funktionen
    
    public Projekt getProjekt() {
		return openProjekt;
	}
    
	public boolean isProjektLeiter() {
		return projektLeiter;
	}
    
	public Nutzer[] getNutzer_projekt() {
		return projektNutzer;
	}
	
	public String getLeiterString() {
		return leiterString;
	}
	
	public String getBetreuerString() {
		return betreuerString;
	}
	
	public String kick(Nutzer nutzer) {
		
		if(Sitzung.getNutzer() == null || openProjekt == null) {
			return URLManager.LOGIN;
		}
		
		Datenbank.quitNutzerProjekt(openProjekt, nutzer);
		projektNutzer = Datenbank.getNutzerProjektArray(openProjekt.getProjektID(),false);
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Teilnehmer entfernt",nutzer.getVorname()+" "+nutzer.getNachname()) );
		
		return null;
	}
	
	public String quit() {
		
		Nutzer me = Sitzung.getNutzer();
		
		if(me == null) {
			return URLManager.LOGIN;
		}
		
		int antwort = Datenbank.quitNutzerProjekt(openProjekt, me);
		switch(antwort) {
		case -1:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "unbekannter Fehler") );
			return null;
		case 0:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Projekt verlassen",openProjekt.getName()) );
			return URLManager.PROJEKT+"?faces-redirect=true";
		case 1:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Projekt verlassen",openProjekt.getName()) );
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Projekt gelöscht",openProjekt.getName()) );
			return URLManager.PROJEKT+"?faces-redirect=true";
		default:
			return null;
		}
	}
	
	public int getNutzerID() {
		return nutzerID;
	}
    
    //Teilnehmer hinzufügen
    
    public void setVorname(String vorname) {
    	this.vorname = vorname;
    }
    
    public String getVorname() {
    	return vorname;
    }
    
    public void setNachname(String nachname) {
    	this.nachname = nachname;
    }
    
    public String getNachname() {
    	return nachname;
    }
    
    public boolean getSearch_begin() {
    	return search_begin;
    }
    
    public void search() {
    	if(vorname != null && vorname.trim().equals(""))
    		vorname = null;
    	
    	if(nachname != null && nachname.trim().equals(""))
    		nachname = null;
    	
    	if(vorname == null && nachname == null) {
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Keine Eingabe") );
    		return;
    	}
    	
    	nutzerSearch = Datenbank.searchNutzer(vorname, nachname, -1, -1);
    	if(nutzerSearch == null) {
    		search_begin = false;
    	} else {
    		search_begin = true;
    	}
    	
    	//nutzer im Projekt aussortieren
    	List<Nutzer> cleaned = new ArrayList<Nutzer>(nutzerSearch.length);
    	
    	for(Nutzer currentSearch : nutzerSearch) {
    		
    		cleaned.add(currentSearch);
    		
    		boolean removed = false;
    		
    		for(Nutzer current : projektNutzer) {
    			if(currentSearch.getNutzer_id() == current.getNutzer_id()) {
    				cleaned.remove(cleaned.size()-1); //letzten wieder löschen
    				removed = true;
    				break;
    			}
    		}
    		
    		if(!removed) {
    			for(Nutzer current : projektBetreuer) {
        			if(currentSearch.getNutzer_id() == current.getNutzer_id()) {
        				cleaned.remove(cleaned.size()-1); //letzten wieder löschen
        				removed = true;
        				break;
        			}
        		}
    		}
    		
    	}
    	
    	nutzerSearch = cleaned.toArray(new Nutzer[cleaned.size()]);
    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Suche durchgeführt",nutzerSearch.length+" Nutzer gefunden.") );
    }
    
    public void cancel() {
    	nutzerSearch = null;
    	vorname = null;
    	nachname = null;
    	search_begin = false;
    }
    
    public Nutzer[] getNutzerSearch() {
    	return nutzerSearch;
    }
    
    public String addNutzer(Nutzer nutzer) {
    	
    	Nutzer me = Sitzung.getNutzer();
    	
    	if(me != null && openProjekt != null) {
    		Datenbank.joinProjekt(nutzer, me.getOpenProjekt(), false, true);
        	projektNutzer = Datenbank.getNutzerProjektArray(openProjekt.getProjektID(),false);
        	search();
        	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Teilnehmer hinzugefügt",nutzer.getVorname()+" "+nutzer.getNachname()) );
        	return null;
    	} else {
    		return URLManager.LOGIN;
    	}
    	
    }
    
    // Gruppen / Beiträge
    
    public Gruppe getGruppe() {
		return gruppe;
	}
    
    public Beitrag getOpenBeitrag() {
		return openBeitrag;
	}
    
    public Beitrag[] getBeitragArray() {
		return beitragArray;
	}
    
    public boolean isAddGruppe() {
		return addGruppe;
	}
    
    public void selectBeitrag(Beitrag beitrag) {
    	this.openBeitrag = beitrag;
    }
    
    public void addGruppe() {
    	int gruppeID = zuordnungFinder.getSelectedGruppe() == null ? -1 : zuordnungFinder.getSelectedGruppe().getGruppe_id();
    	
    	if(gruppeID == -1)
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte Gruppe auswählen") );
    	else if(openProjekt != null){
    		int projektID = openProjekt.getProjektID();
    		Datenbank.clearProjektGruppe(projektID);
    		Datenbank.addProjektGruppe(projektID, gruppeID);
    		beitragArray = Datenbank.getProjektBeitrag(projektID);
    		gruppe = Datenbank.getProjektGruppe(projektID);
    		tab = TAB_BEITRAG;
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Verknüpfung gespeichert",zuordnungFinder.getSelectedGruppe().getName()));
    	}
    }
    
    public void removeGruppe() {
    	if(openProjekt == null)
    		return;
    	
    	Datenbank.clearProjektGruppe(openProjekt.getProjektID());
    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Änderung gespeichert","Verknüpfung aufgehoben."));
    	beitragArray = null;
    	gruppe = null;
    	zuordnungFinder.unselect();
    }
    
    public String getNewName() {
		return newName;
	}
    
    public void setNewName(String newName) {
		this.newName = newName;
	}
    
    public void renameProjekt() {
    	
    	if(openProjekt == null)
    		return;
    	
    	if(newName == null || newName.trim().length() < 4) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Der Name muss aus mindestens vier Zeichen bestehen.") );
			return;
		}
    	
    	Datenbank.renameProjekt(openProjekt.getProjektID(), newName);
    	openProjekt.update(newName);
    	FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Projekt umbenannt",newName));
    }
    
    //managedProperty
  	public void setZuordnungFinder(ZuordnungFinder zuordnungFinder) {
  		this.zuordnungFinder = zuordnungFinder;
  	}
  	
  	public void setDateien(Dateien dateien) {
		this.dateien = dateien;
	}
	
}