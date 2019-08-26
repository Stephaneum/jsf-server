package managedBean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.Klasse;
import objects.Nutzer;
import sitzung.Sitzung;

@ViewScoped
@ManagedBean
public class KlasseBean {
	
	@ManagedProperty(value = "#{sideMenu}")
	private SideMenu sideMenu; //die Klasse wird im Seitenmenü angezeigt und muss deswegen aktualisiert werden
	
	//allgemeine Daten
	private int nutzerID;
	private boolean lehrer = false;
	private Nutzer nutzer;
	private Nutzer[] klassenkameraden;
	private String lehrerNames;
	
	//Klasse beitreten
	private String klasse, selectedKlasse;
	private String[] klassen;
	private Klasse[] klassenObjekte;
	private int klasseID;
	
	//Dateien
	@ManagedProperty("#{dateien}")
	private Dateien dateien;
	
	@PostConstruct
    public void init() {
		
		if(!Sitzung.isLoggedIn()) {
			return;
		}
		
		nutzer = Sitzung.getNutzer();
		klasse = nutzer.getKlasse();
		klasseID = nutzer.getKlasse_id();
		nutzerID = nutzer.getNutzer_id();
		
		lehrer = Sitzung.isLehrer() || Sitzung.isAdmin();
		
		if(klasse == null) {
			//keine Klasse zugeordnet: Liste aller Klassen erstellen
			
			klassenObjekte = Datenbank.getAllKlassen();
			
			klassen = new String[klassenObjekte.length];
			
			for(int i = 0; i < klassen.length; i++) {
				klassen[i] = klassenObjekte[i].getString();
			}
		} else {
			Nutzer[] klassenkameraden = Datenbank.getKlassenkameraden();
			
			//Lehrer und Schüler unterteilen
			List<Nutzer> listKamerad = new ArrayList<Nutzer>(klassenkameraden.length);
			List<Nutzer> listLehrer = new ArrayList<Nutzer>(1);
			for(int i = 0; i < klassenkameraden.length; i++) {
				if(klassenkameraden[i].getRang() == Nutzer.RANG_SCHUELER) {
					listKamerad.add(klassenkameraden[i]);
				} else {
					listLehrer.add(klassenkameraden[i]);
				}
			}
			
			this.klassenkameraden = listKamerad.toArray(new Nutzer[listKamerad.size()]);
			
			if(listLehrer.size() == 1) {
				Nutzer current = listLehrer.get(0);
				switch(current.getGeschlecht()) {
				case Nutzer.GESCHLECHT_MANN:
					this.lehrerNames = "Klassenlehrer: "+current.getFormalName();
					break;
				case Nutzer.GESCHLECHT_FRAU:
					this.lehrerNames = "Klassenlehrerin: "+current.getFormalName();
					break;
				default:
					this.lehrerNames = "Klassenlehrer/in: "+current.getFormalName();
					break;
				}
			} else {
				//mehrere Lehrer
				StringBuilder builder = new StringBuilder();
				builder.append("Klassenlehrer/in: ");
				for(int i = 0; i < listLehrer.size(); i++) {
					
					Nutzer current = listLehrer.get(i);
					
					builder.append(current.getFormalName());
					
					if(i < listLehrer.size()-1) {
						builder.append(", ");
					}
				}
				
				if(listLehrer.size() == 0) {
					builder.append("Klassenlehrer/in: noch nicht zugeordnet");
				}
				this.lehrerNames = builder.toString();
			}
			
			dateien.initKlassenspeicher(klasseID, lehrer);
		}
	}
	
	public String getKlassenname() {
		if(klasse == null) {
			return "Klassen";
		} else {
			return "Klasse "+klasse;
		}
		
	}
	
	public Nutzer[] getKlassenkameraden() {
		return klassenkameraden;
	}
	
	public String getLehrerNames() {
		return lehrerNames;
	}
	
	public int getNutzerID() {
		return nutzerID;
	}
	
	//Lehrer----------------------------------------------------------------
	
	public String[] getKlassen() {
		return klassen;
	}
	
	public String getSelectedKlasse() {
		return selectedKlasse;
	}
	
	public void setSelectedKlasse(String selectedKlasse) {
		this.selectedKlasse = selectedKlasse;
	}
	
	public boolean isLehrer() {
		return lehrer;
	}
	
	public boolean isInsideKlasse() {
		return klasse != null;
	}
	
	public void quit() {
		Datenbank.quitKlasse(nutzerID, klasseID);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Klasse verlassen",klasse));
		Datenbank.updateNutzerObjekt(nutzerID, nutzer.getEmail());
		init();
		sideMenu.init();
	}
	
	public void join() {
		
		if(selectedKlasse == null) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte eine Klasse auswählen") );
			return;
		}
		
		//ID suchen
		
		for(int i = 0; i < klassenObjekte.length; i++) {
			if(klassenObjekte[i].getString().equals(selectedKlasse)) {
				Datenbank.joinKlasse(nutzerID, klassenObjekte[i].getId());
				Datenbank.updateNutzerObjekt(nutzerID, nutzer.getEmail());
				init();
				sideMenu.init();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Klasse festgelegt",klasse));
				return;
			}
		}
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte eine Klasse auswählen") );
	}
    
    //managedProperty
    public void setSideMenu(SideMenu sideMenu) {
		this.sideMenu = sideMenu;
	}
    
    public void setDateien(Dateien dateien) {
		this.dateien = dateien;
	}
}
