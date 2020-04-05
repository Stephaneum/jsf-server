package managedBean;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import mysql.Datenbank;
import objects.Nutzer;
import sitzung.Sitzung;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class SideMenu {

	private MenuModel model;
	
    @PostConstruct
    public void init() {
        model = new DefaultMenuModel();
        
        if(Sitzung.isLoggedIn()) {
        	
        	Nutzer nutzer = Sitzung.getNutzer();
        	
        	boolean admin = false, beitragManager = false, rubrikErstellen = false, vertretungsplan = false;
    		if(nutzer != null) {
    			beitragManager = Datenbank.isBeitragManager(nutzer.getNutzer_id());
    			rubrikErstellen = Datenbank.isRubrikErstellen(nutzer.getNutzer_id());
    			vertretungsplan = Datenbank.isVertretung(nutzer.getNutzer_id());
    			admin = isAdmin();
    		}
        	
        	//Menü
            DefaultSubMenu submenuOption = new DefaultSubMenu(Sitzung.getNutzer().getVorname()+" "+Sitzung.getNutzer().getNachname());
            
            DefaultMenuItem itemHome = new DefaultMenuItem("Home");
            itemHome.setHref("/"+URLManager.HOME+".xhtml");
            itemHome.setIcon("ui-icon-home");
            submenuOption.addElement(itemHome);
            
            DefaultMenuItem itemLogout = new DefaultMenuItem("Abmelden");
            itemLogout.setHref("/"+URLManager.LOGOUT+".xhtml");
            itemLogout.setIcon("ui-icon-power");
            submenuOption.addElement(itemLogout);
            
            if(Datenbank.getVertretung() != null) {
            	DefaultMenuItem itemVertretung = new DefaultMenuItem("Vertretungsplan");
                itemVertretung.setHref("/"+URLManager.VERTRETUNG);
                itemVertretung.setIcon("ui-icon-document");
                itemVertretung.setTarget("_blank");
                submenuOption.addElement(itemVertretung);
            }
            
            DefaultMenuItem itemInformation = new DefaultMenuItem("Statistiken");
            itemInformation.setHref("/"+URLManager.STATISTIKEN+".xhtml");
            itemInformation.setIcon("ui-icon-info");
            submenuOption.addElement(itemInformation);
             
            model.addElement(submenuOption);
            
            //Admin
            if(admin) {
            	DefaultSubMenu submenuAdmin = new DefaultSubMenu("Admin");
            	
            	DefaultMenuItem itemKonfig = new DefaultMenuItem("Konfiguration");
            	itemKonfig.setHref("/"+URLManager.KONFIG+".xhtml");
            	itemKonfig.setIcon("ui-icon-gear");
                submenuAdmin.addElement(itemKonfig);
                
                DefaultMenuItem itemStatic = new DefaultMenuItem("Benutzerdef. Seiten");
                itemStatic.setHref("/"+URLManager.STATIC+".xhtml");
                itemStatic.setIcon("ui-icon-document ");
                submenuAdmin.addElement(itemStatic);
                
                DefaultMenuItem itemRubrik = new DefaultMenuItem("Rubriken");
                itemRubrik.setHref("/"+URLManager.ADMIN_RUBRIK+".xhtml");
                itemRubrik.setIcon("ui-icon-bookmark");
                submenuAdmin.addElement(itemRubrik);
                
                DefaultMenuItem itemCodes = new DefaultMenuItem("Zugangscodes");
                itemCodes.setHref("/"+URLManager.ZUGANGSCODE+".xhtml");
                itemCodes.setIcon("ui-icon-key");
                submenuAdmin.addElement(itemCodes);
                
                if(Datenbank.isShowDummy()) {
                	DefaultMenuItem itemDummy = new DefaultMenuItem("Dummys");
                    itemDummy.setHref("/"+URLManager.DUMMY+".xhtml");
                    itemDummy.setIcon("ui-icon-tag");
                    submenuAdmin.addElement(itemDummy);
                }
                
                DefaultMenuItem itemDatenbank = new DefaultMenuItem("Nutzer");
                itemDatenbank.setHref("/"+URLManager.NUTZER+".xhtml");
                itemDatenbank.setIcon("ui-icon-person");
                submenuAdmin.addElement(itemDatenbank);
                
                DefaultMenuItem itemLogs = new DefaultMenuItem("Logbuch");
                itemLogs.setHref("/"+URLManager.LOGS+".xhtml");
                itemLogs.setIcon("ui-icon-note");
                submenuAdmin.addElement(itemLogs);
                 
                model.addElement(submenuAdmin);
            }
            
            //Konfiguration
            if(vertretungsplan && !admin) {
            	DefaultSubMenu submenuKonfig = new DefaultSubMenu("Konfiguration");
            	
            	DefaultMenuItem itemKonfig = new DefaultMenuItem("Vertretungsplan");
            	itemKonfig.setHref("/"+URLManager.KONFIG_VERTRETUNG+".xhtml");
            	itemKonfig.setIcon("ui-icon-gear");
            	submenuKonfig.addElement(itemKonfig);
            	
            	model.addElement(submenuKonfig);
            }
            
            //Nutzer
            DefaultSubMenu submenuNutzer = new DefaultSubMenu("Nutzer");
            
            if(rubrikErstellen) {
            	DefaultMenuItem itemRubrik = new DefaultMenuItem("Rubrik");
                itemRubrik.setHref("/"+URLManager.RUBRIK+".xhtml");
                itemRubrik.setIcon("ui-icon-bookmark");
                submenuNutzer.addElement(itemRubrik);
            }
            
            DefaultMenuItem itemBeitrag;
            if(admin || beitragManager) {
            	int anzahl = Datenbank.getBeitragAnzahl(false);
            	if(anzahl == 0)
            		itemBeitrag = new DefaultMenuItem("Beiträge");
            	else
            		itemBeitrag = new DefaultMenuItem("Beiträge ("+anzahl+")");
            } else
            	itemBeitrag = new DefaultMenuItem("Beiträge");
            itemBeitrag.setHref("/"+URLManager.BEITRAG+".xhtml");
            itemBeitrag.setIcon("ui-icon-pencil");
            submenuNutzer.addElement(itemBeitrag);
             
            DefaultMenuItem itemSpeicher = new DefaultMenuItem("Dateien");
            itemSpeicher.setHref("/"+URLManager.DATEIEN+".xhtml");
            itemSpeicher.setIcon("ui-icon-folder-collapsed");
            submenuNutzer.addElement(itemSpeicher);
            
            DefaultMenuItem itemDaten = new DefaultMenuItem("Account");
            itemDaten.setHref("/"+URLManager.ACCOUNT+".xhtml");
            itemDaten.setIcon("ui-icon-key");
            submenuNutzer.addElement(itemDaten);
             
            model.addElement(submenuNutzer);
            
            //Gruppen
            DefaultSubMenu submenuGruppe = new DefaultSubMenu("Gruppen");
            
            //Projekt
            DefaultMenuItem itemMeineProjekte = new DefaultMenuItem("Gruppen");
            itemMeineProjekte.setHref("/"+URLManager.PROJEKT+".xhtml");
            itemMeineProjekte.setIcon("ui-icon-star");
            submenuGruppe.addElement(itemMeineProjekte);
             
            model.addElement(submenuGruppe);
        } else {
        	//nicht angemeldet
            DefaultSubMenu submenuOption = new DefaultSubMenu("Menü");
             
            DefaultMenuItem itemHome = new DefaultMenuItem("Home");
            itemHome.setHref("/"+URLManager.HOME+".xhtml");
            itemHome.setIcon("ui-icon-home");
            submenuOption.addElement(itemHome);
            
            DefaultMenuItem itemLogin = new DefaultMenuItem("Anmelden");
            itemLogin.setHref("/"+URLManager.LOGIN+".xhtml");
            itemLogin.setIcon("ui-icon-key");
            submenuOption.addElement(itemLogin);
            
            DefaultMenuItem itemInformation = new DefaultMenuItem("Statistiken");
            itemInformation.setHref("/"+URLManager.STATISTIKEN+".xhtml");
            itemInformation.setIcon("ui-icon-info");
            submenuOption.addElement(itemInformation);
             
            model.addElement(submenuOption);
        }
        
    }
 
    public MenuModel getModel() {
        return model;
    }
    
    public boolean isAdmin() {
    	Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer != null && nutzer.getRang() == 100) {
			return true;
		} else {
			return false;
		}
    }
    
    public boolean isLehrer() {
    	Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer != null && nutzer.getRang() == 1) {
			return true;
		} else {
			return false;
		}
    }
    
    public boolean isSchueler() {
    	Nutzer nutzer = Sitzung.getNutzer();
    	if(nutzer != null && nutzer.getRang() == 0) {
    		return true;
    	} else{
    		return false;
    	}
    }
  	
  	public String getWelcomeNutzer() {
		
		if(!Sitzung.isLoggedIn()) {
			return null;
		}
		
		if(Sitzung.getNutzer().getRang() == 0 || Sitzung.getNutzer().getAnrede() == null) {
			return "Hallo, "+Sitzung.getNutzer().getVorname();
		} else
			return "Hallo, "+Sitzung.getNutzer().getAnrede()+" "+Sitzung.getNutzer().getNachname();
	}
  	
  	public boolean isLoggedIn() {
  		return Sitzung.isLoggedIn();
  	}
	
}
