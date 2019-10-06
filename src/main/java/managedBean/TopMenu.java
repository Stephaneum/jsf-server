package managedBean;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.menu.MenuModel;

import mysql.Datenbank;
import objects.Gruppe;
import objects.Nutzer;
import objects.Rubrik;
import sitzung.Sitzung;

@ManagedBean
@ViewScoped
public class TopMenu {
	
	static private MenuModel cachedModel;
	static private Gruppe[] cachedTopLevel; // materialize
	static private boolean changed = true;
	
	private MenuModel model;
	
	// materialize
	private Gruppe[] topLevel; 
	private boolean admin, beitragManager, rubrikErstellen, vertretungsplan;
	private int beitragAnzahl; //ungenehmigt
	private int flagDepth;
	
	@PostConstruct
	public void init() {
		
		if(!Datenbank.existDatabase()) {
			return;
		}
		
		Nutzer nutzer = Sitzung.getNutzer();
		boolean loggedIn = nutzer != null && nutzer.getRang() != Nutzer.RANG_GAST_NO_LOGIN;
		
		if(loggedIn) {
			
			//Zweig für eingeloggte Nutzer
			
			Rubrik rubrik = Datenbank.getRubrik(nutzer.getNutzer_id());
			
			if(rubrik != null && !rubrik.isGenehmigt()) {
				
				Gruppe[] hauptgruppe = Datenbank.getGruppeArray(); //enthält ALLE Gruppen
				
				Gruppe[] merged = new Gruppe[hauptgruppe.length+1];
				for(int i = 0; i < hauptgruppe.length; i++)
					merged[i] = hauptgruppe[i];
				merged[hauptgruppe.length] = rubrik.getGruppe();
				
				model = Gruppe.generateModel(merged, true);
				topLevel = merged;
			} else {
				
				if(!changed) {
					topLevel = cachedTopLevel;
					model = cachedModel;
				} else {
					cachedTopLevel = Datenbank.getGruppeArray();
					cachedModel = Gruppe.generateModel(cachedTopLevel, true);
					
					topLevel = cachedTopLevel;
					model = cachedModel;
					changed = false;
				}
			}
			
			// MaterializeCSS Design
			this.beitragManager = Datenbank.isBeitragManager(nutzer.getNutzer_id());
			this.rubrikErstellen = Datenbank.isRubrikErstellen(nutzer.getNutzer_id());
			this.vertretungsplan = Datenbank.isVertretung(nutzer.getNutzer_id());
			this.admin = nutzer.getRang() == Nutzer.RANG_ADMIN;
			this.beitragAnzahl = Datenbank.getBeitragAnzahl(false);
			this.flagDepth = (admin || vertretungsplan) ? 3 : 2;
    		
		} else {
			
			//Zweig für öffentliche Nutzer
			
			if(!changed) {
				topLevel = cachedTopLevel;
				model = cachedModel;
			} else {
				cachedTopLevel = Datenbank.getGruppeArray();
				cachedModel = Gruppe.generateModel(cachedTopLevel, true);
				
				topLevel = cachedTopLevel;
				model = cachedModel;
				changed = false;
			}
		}
	}
	
	public MenuModel getModel() {
        return model;
    }
	
	public Gruppe[] getTopLevel() {
		return topLevel;
	}
	
	public static void triggerChanged() {
		changed = true;
	}
	
	public int getBeitragAnzahl() {
		return beitragAnzahl;
	}
	
	public int getFlagDepth() {
		return flagDepth;
	}
	
	public boolean isAdmin() {
		return admin;
	}
	
	public boolean isBeitragManager() {
		return beitragManager;
	}
	
	public boolean isRubrikErstellen() {
		return rubrikErstellen;
	}
	
	public boolean isVertretungsplan() {
		return vertretungsplan;
	}

	public String getUrl(Gruppe gruppe) {
		if(gruppe.getLink() != null)
			return gruppe.getLink();
		else
			return "/home.xhtml?id="+gruppe.getGruppe_id();
	}

	public String getTarget(Gruppe gruppe) {
		if(gruppe.getLink() != null)
			return "_blank";
		else
			return "_self";
	}
}

