package cms;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;

import managedBean.TopMenu;
import mysql.Datenbank;
import objects.Gruppe;
import objects.Nutzer;
import objects.Rubrik;
import sitzung.Sitzung;

@ViewScoped
@ManagedBean
public class EditGruppe {
	
	/*
	 * Bearbeiten des oberen Menüs UND
	 * Bearbeiten der Rubrik
	 * Diese Klasse bezieht sich auf keine anderen ManagedBean-Klassen in diesem Package
	 * 
	 */
	
	private Gruppe[] gruppe;
	private DefaultMenuModel model;
	
	@ManagedProperty("#{topMenu}")
	private TopMenu topMenu;
	
	//Auswahl (konstant während des Overlays)
	private String rubrikLeiter;
	private String selectGruppe;
	private String header; //falls Hauptgruppe, dann keine Eltern
	private int selectGruppeID = -1;
	private boolean passwordProtected;
	
	//Auswahl (editierbar)
	private String newName, newURL, newPassword;
	private int newPriory;
	
	//Kontrolle
	private boolean correctPassword = false, errorPassword = false;
	private String password;
	
	//standardmäßige Gruppe
	private Gruppe defaultGruppe;
	
	//Rubrik
	private boolean rubrikMode = false;
	private int nutzerID; //nur wenn rubrikMode == true
	private Rubrik rubrik;
	
	//Rubrik einfügen
	private Rubrik[] rubriken;
	private String[] rubrikenString;
	private String selectedRubrik;
	
	@PostConstruct
	public void init() {
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer != null) {
			
			if(nutzer.getOpenRubrik() != null) {
				//als einen anderen Nutzer simulieren
				nutzerID = nutzer.getOpenRubrik().getNutzerID();
				rubrikMode = true;
			} else {
				nutzerID = nutzer.getNutzer_id();
				rubrikMode = Datenbank.isRubrikErstellen(nutzerID);
			}
		}
		
		model = new DefaultMenuModel();
		
		if(rubrikMode) {
			gruppe = Datenbank.getGruppeArrayRubrik(nutzerID, false);
			rubrik = Datenbank.getRubrik(nutzerID);
		} else {
			gruppe = Datenbank.getGruppeArray();
			
			rubriken = Datenbank.getRubrikArray(false);
			rubrikenString = new String[rubriken.length];
			for(int i = 0; i < rubriken.length; i++) {
				rubrikenString[i] = rubriken[i].toString();
			}
		}
		
		for(int x = 0; x < gruppe.length; x++) {
			
			DefaultSubMenu submenu = new DefaultSubMenu(gruppe[x].getName());
			submenu.setStyle("font-size:12pt");
			submenu.setStyleClass("top-menu-edit top-menu-grp"+gruppe[x].getGruppe_id());
			
			//Untergruppen
			Gruppe[] untergruppe = gruppe[x].getUnterGruppe();
			
			addChild(submenu,untergruppe);
			
			//+ neue Untergruppe
			DefaultMenuItem item_new_zweig = new DefaultMenuItem("neue Gruppe");
			item_new_zweig.setIcon("ui-icon-plusthick");
			item_new_zweig.setStyle("font-weight: bold;font-size:10pt");
			item_new_zweig.setStyleClass("top-menu-edit top-menu-ngrp"+gruppe[x].getGruppe_id());
			submenu.addElement(item_new_zweig);
			
			//+ neuer Link
			DefaultMenuItem item_new_link = new DefaultMenuItem("neuer Link");
			item_new_link.setIcon("ui-icon-plusthick");
			item_new_link.setStyle("font-weight: bold;font-size:10pt");
			item_new_link.setStyleClass("top-menu-edit top-menu-nlink"+gruppe[x].getGruppe_id());
			submenu.addElement(item_new_link);
			
			if(!rubrikMode) {
				//+ neue Rubrik
				DefaultMenuItem item_new_rubrik = new DefaultMenuItem("Rubrik einfügen");
				item_new_rubrik.setIcon("ui-icon-plusthick");
				item_new_rubrik.setStyle("font-weight: bold;font-size:10pt");
				item_new_rubrik.setStyleClass("top-menu-edit top-menu-nrubrik"+gruppe[x].getGruppe_id());
				submenu.addElement(item_new_rubrik);
			}
			
			model.addElement(submenu);
		}
		//+ neue Hauptgruppe
		DefaultMenuItem item_new_obergruppe = new DefaultMenuItem("neue Hauptgruppe");
		item_new_obergruppe.setIcon("ui-icon-plusthick");
		item_new_obergruppe.setOncomplete("PF('overlayNewGruppe').show()");
		item_new_obergruppe.setUpdate("panelNewGruppe");
		item_new_obergruppe.setCommand("#{editGruppe.openNewHauptgruppe}");
		item_new_obergruppe.setStyle("font-weight: bold;font-size:12pt");
		model.addElement(item_new_obergruppe);
		
		defaultGruppe = Datenbank.getGruppeBasic(Datenbank.getDefaultGruppeID());
	}
	
	private void addChild(DefaultSubMenu parent, Gruppe[] childs) {
		
		final int fontSize = 10;
		
		for(int n = 0; n < childs.length; n++) {
        	
        	Gruppe currentChild = childs[n];
        	
        	if(currentChild.getLink() == null) {
        		
        		DefaultSubMenu subMenu = new DefaultSubMenu(currentChild.getName());
    	        subMenu.setStyle("font-size:"+fontSize+"pt");
    	        subMenu.setStyleClass("top-menu-edit top-menu-grp"+currentChild.getGruppe_id());
        		
        		if(currentChild.getUnterGruppe().length != 0) {
            		addChild(subMenu,currentChild.getUnterGruppe()); //neuer Stack
            	}
        		
        		//+ neuer Zweig
    			DefaultMenuItem item_new_zweig = new DefaultMenuItem("neue Gruppe");
    			item_new_zweig.setIcon("ui-icon-plusthick");
    			item_new_zweig.setStyle("font-weight: bold;font-size:"+fontSize+"pt");
    			item_new_zweig.setStyleClass("top-menu-edit top-menu-ngrp"+currentChild.getGruppe_id());
    			subMenu.addElement(item_new_zweig);
    			
    			//+ neuer Link
    			DefaultMenuItem item_new_link = new DefaultMenuItem("neuer Link");
    			item_new_link.setIcon("ui-icon-plusthick");
    			item_new_link.setStyle("font-weight: bold;font-size:"+fontSize+"pt");
    			item_new_link.setStyleClass("top-menu-edit top-menu-nlink"+currentChild.getGruppe_id());
    			subMenu.addElement(item_new_link);
    			
    			if(!rubrikMode) {
    				//+ neue Rubrik
    				DefaultMenuItem item_new_rubrik = new DefaultMenuItem("Rubrik einfügen");
    				item_new_rubrik.setIcon("ui-icon-plusthick");
    				item_new_rubrik.setStyle("font-weight: bold;font-size:"+fontSize+"pt");
    				item_new_rubrik.setStyleClass("top-menu-edit top-menu-nrubrik"+currentChild.getGruppe_id());
    				subMenu.addElement(item_new_rubrik);
    			}
    	        
    	        parent.addElement(subMenu);
        	} else {
        		
        		//Links dürfen keine Untergruppen besitzen
        		
        		DefaultMenuItem item = new DefaultMenuItem(currentChild.getName());
        		item.setStyle("font-size:"+fontSize+"pt");
        		item.setStyleClass("top-menu-edit top-menu-link"+currentChild.getGruppe_id());
				item.setIcon("ui-icon-arrowthick-1-n");
				
            	parent.addElement(item);
        	}
        }
	}
	
	private void updateAll() {
		init();
		topMenu.init();
	}
	
	//gruppe muss bereits initialisiert sein!
	private Gruppe getGruppe(Gruppe[] gruppe, int id) {
		for(int i = 0; i < gruppe.length; i++) {
			if(gruppe[i].getGruppe_id() == id) {
				return gruppe[i];
			}
			
			if(gruppe[i].getUnterGruppe().length != 0) {
				Gruppe founded = getGruppe(gruppe[i].getUnterGruppe(),id);
				if(founded != null)
					return founded;
			}
		}
		
		return null;
	}
	
	public void openEdit() {
		
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int gruppeID = Integer.valueOf(params.get("gruppeID")); //von Javascript
		
		Gruppe gruppe = getGruppe(this.gruppe,gruppeID);
		rubrikLeiter = gruppe.getRubrikLeiter();
		selectGruppe = gruppe.getName();
		selectGruppeID = gruppe.getGruppe_id();
		
		header = gruppe.getParent() == null ? "Neue Hauptgruppe" : selectGruppe+" / Neue Gruppe"; //wichtig nur bei neue (Haupt)Gruppe
		newName = selectGruppe;
		newURL = gruppe.getLink();
		newPriory = gruppe.getPriory();
		newPassword = gruppe.getPassword();
		passwordProtected = gruppe.getPassword() != null;
		
		correctPassword = false;
		errorPassword = false;
	}
	
	public void openNew() {
		
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int gruppeID = Integer.valueOf(params.get("gruppeID"));
		
		Gruppe gruppe = getGruppe(this.gruppe,gruppeID);
		selectGruppe = gruppe.getName();
		selectGruppeID = gruppe.getGruppe_id();
		
		header = selectGruppe+" / Neue Gruppe";
		newName = null;
		newURL = null;
		newPassword = gruppe.getPassword();
		
		Gruppe[] kinder = gruppe.getUnterGruppe();
		if(kinder != null && kinder.length != 0)
			newPriory = Gruppe.getMin(kinder).getPriory()-1;
		else
			newPriory = Gruppe.STANDARD_PRIORITY;
	}
	
	//Spezialfall
	public void openNewHauptgruppe() {
		
		selectGruppe = null;
		selectGruppeID = rubrik != null ? rubrik.getGruppeID() : -1; //Rubrikmode oder nicht
		header = rubrik != null ? rubrik.getName()+" / Neue Hauptgruppe" : "Neue Hauptgruppe"; //Rubrikmode oder nicht
		newName = null;
		newURL = null;
		newPassword = null;
		
		if(gruppe.length != 0)
			newPriory = Gruppe.getMin(gruppe).getPriory()-1;
		else
			newPriory = Gruppe.STANDARD_PRIORITY;
	}
	
	//Gruppe / Link: Änderungen speichern
	public void save() {
		
		if(newPassword != null && newPassword.trim().equals(""))
			newPassword = null;
		
		//ist immer genehmigt, als Nicht-Admin
		boolean ok = Datenbank.editGruppe(selectGruppeID, -1, newName, newPriory, newURL, newPassword, true);
		
		if(ok) {
			if(newURL == null)
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Gruppe geändert",newName) );
			else
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Link geändert",newName) );
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Ein Fehler ist aufgetreten.") );
		}
		
		
		updateAll();
	}
	
	// (Haupt)gruppe, Link löschen
	public void delete() {
		
		Datenbank.deleteGruppe(selectGruppeID);
		
		if(newURL != null)
			Datenbank.cleanDateiBeitrag(); //Alle Dateien löschen, die keine NutzerID haben und mit keinem Beitrag verknüpft sind
		
		if(newURL == null)
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Gruppe gelöscht", selectGruppe) );
		else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Link gelöscht", selectGruppe) );
		
		updateAll();
	}
	
	// (Haupt)gruppe, Link erstellen
	public void create() {
		
		if(newURL != null && !newURL.equals("")) {
			//Validierung Link
			if(!newURL.contains("http")) {
				newURL = "http://"+newURL;
			}
		}
		
		if(newPassword != null && newPassword.trim().equals(""))
			newPassword = null;
		
		boolean ok = Datenbank.createGruppe(selectGruppeID, newName, newPriory, newURL, newPassword);
		
		if(ok) {
			if(newURL == null)
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Gruppe erstellt", newName) );
			else
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Link erstellt", newName) );
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Ein Fehler ist aufgetreten.") );
		}
		
		
		updateAll();
	}
	
	public void createRubrik() {
		
		//index herausfinden
		int selectedIndex = -1;
		for(int i = 0; i < rubriken.length; i++) {
			if(rubriken[i].toString().equals(selectedRubrik)) {
				selectedIndex = i;
				break;
			}
		}
		
		if(selectedIndex == -1) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Keine Rubrik ausgewählt.") );
			return;
		}
		
		Datenbank.editGruppe(rubriken[selectedIndex].getGruppeID(), selectGruppeID, rubriken[selectedIndex].getName(), newPriory, null, null, true);
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Rubrik genehmigt", newName) );
		
		updateAll();
	}
	
	//Passwort kontrollieren (Haupt- und Untergruppe löschen)
	public void checkPassword() {
		correctPassword = Datenbank.checkPassword(password);
		errorPassword = !correctPassword; //errorPasswort kann nur nach dem ersten Versuch false sein
	}
	
	//AB HIER: GET/SET-----------------------
	
	public DefaultMenuModel getModel() {
		return model;
	}
	
	public String getSelectGruppe() {
		return selectGruppe;
	}
	
	public String getRubrikLeiter() {
		return rubrikLeiter;
	}
	
	public String getHeader() {
		return header;
	}
	
	public int getSelectGruppeID() {
		return selectGruppeID;
	}
	
	public void setNewName(String newName) {
		this.newName = newName;
	}
	
	public String getNewName() {
		return newName;
	}
	
	public void setNewURL(String newURL) {
		this.newURL = newURL;
	}
	
	public String getNewURL() {
		return newURL;
	}
	
	public boolean isPasswordProtected() {
		return passwordProtected;
	}
	
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
	public String getNewPassword() {
		return newPassword;
	}
	
	public int getNewPriory() {
		return newPriory;
	}
	
	public void setNewPriory(int newPriory) {
		this.newPriory = newPriory;
	}

	public boolean isCorrectPassword() {
		return correctPassword;
	}
	
	public boolean isErrorPassword() {
		return errorPassword;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String[] getRubrikenString() {
		return rubrikenString;
	}
	
	public void setSelectedRubrik(String selectedRubrik) {
		this.selectedRubrik = selectedRubrik;
	}
	
	public String getSelectedRubrik() {
		return selectedRubrik;
	}
	
	//standardmäßige Gruppe, Merge
	
	public void setDefaultGruppe() {
		Datenbank.setDefaultGruppeID(selectGruppeID);
		
		defaultGruppe = Datenbank.getGruppeBasic(selectGruppeID);
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Einstellungen gespeichert","Startseite: "+defaultGruppe.getName()) );
	}
	
	public Gruppe getDefaultGruppe() {
		return defaultGruppe;
	}
	
	public boolean isMergeAktuelles() {
		return Datenbank.isMergeAktuelles();
	}
	
	public void setMergeAktuelles(boolean mergeAktuelles) {
		Datenbank.setMergeAktuelles(mergeAktuelles);
		
		if(mergeAktuelles)
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Einstellungen gespeichert","Es werden alle Beiträge in "+defaultGruppe.getName()+" angezeigt.") );
		else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Einstellungen gespeichert","Es werden nur Beiträge von "+defaultGruppe.getName()+" angezeigt.") );
	}
	
	//ManagedProperty
	public void setTopMenu(TopMenu topMenu) {
		this.topMenu = topMenu;
	}
}
