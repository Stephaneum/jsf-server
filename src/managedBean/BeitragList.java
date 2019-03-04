package managedBean;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.menu.MenuModel;

import mysql.Datenbank;
import objects.Beitrag;
import objects.Datei;
import objects.Gruppe;
import objects.Nutzer;
import objects.Pagination;
import objects.Projekt;
import objects.Rubrik;
import sitzung.Sitzung;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class BeitragList {
	
	/*
	 * Anzeige der Beiträge (home.xhtml)
	 * 
	 */
	
	private Beitrag[] beitragArray = null;
	private Pagination pagination = null;
	private Gruppe[] childs = null;
	private Gruppe gruppe;
	private Datei bild;
	private boolean publicHome, privateHome;
	
	//Passwort
	private String password, passwordInput;
	
	//eingeloggt
	private boolean showProjekt;
	private Projekt[] projektArray;
	private Rubrik rubrik;
	private MenuModel model;
	
	@PostConstruct
    public void init() {
		
		if(Sitzung.getNutzer() == null)
			Sitzung.createNutzerGuest();
		
		//Beitrag aus dem Parameter "id" entnehmen
		try {
			Map<String, String> parameterMap = (Map<String, String>) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			String param = parameterMap.get("id");
			String paramPage = parameterMap.get("page");
			String paramPublic = parameterMap.get("public");
			int paramPageInt;
			if(paramPage == null)
				paramPageInt = 1;
			else {
				try {
					paramPageInt = Integer.valueOf(paramPage);
				} catch (NumberFormatException e) {
					paramPageInt = 1;
				}
			}
			
			int id;
			boolean parameterUsed;
			Nutzer nutzer = Sitzung.getNutzer();
			int rang = nutzer == null ? Nutzer.RANG_GAST_NO_LOGIN : nutzer.getRang();
			boolean isPublic = false;
			if(param != null) {
				id = Integer.parseInt(param);
				parameterUsed = true;
			} else {
				id = Datenbank.getDefaultGruppeID();
				parameterUsed = false;
				isPublic = true;
			}
			
			publicHome = (!parameterUsed && rang == Nutzer.RANG_GAST_NO_LOGIN) || paramPublic != null;
			
			if(publicHome)
				privateHome = false;
			else
				privateHome = !parameterUsed && rang != Nutzer.RANG_GAST_NO_LOGIN;
			
			if(publicHome || parameterUsed) {
				
				if(!isPublic) {
					// make it to specific user public
					
					if(rang == Nutzer.RANG_ADMIN)
						isPublic = true; //Admin sieht alles
					else if(rang != Nutzer.RANG_GAST_NO_LOGIN) {
						//im Array suchen, ob Gruppe zur Rubrik gehört
						isPublic = Gruppe.contains(Datenbank.getGruppeArrayRubrik(Sitzung.getNutzer().getNutzer_id(), true), id);
					}
					
					if(!isPublic) {
						isPublic = Datenbank.isGruppePublic(id);
					}
				}
				
				if(isPublic) {
					gruppe = Datenbank.getGruppeBasic(id);
					
					if(gruppe != null) {
						bild = Datenbank.getRubrikBild(gruppe.getGruppe_id());
						beitragArray = Datenbank.getBeitragArray(id, parameterUsed ? -1 : 3);
					} else {
						//Fehlermeldung als Beitrag anzeigen
						gruppe = new Gruppe(-1, "Fehler");
						return;
					}
					
					pagination = new Pagination(beitragArray, paramPageInt, 5, 10);
					beitragArray = pagination.beitrag;
					
					childs = Datenbank.getChilds(id);
					password = Datenbank.getGruppePassword(id);
					if(password != null) {
						if(nutzer == null)
							nutzer = Sitzung.createNutzerGuest();
						else {
							if(nutzer.getUnlocker().isGruppeUnlocked(id)) {
								password = null;
							}
						}
					}
				}
				
				if(gruppe == null) {
					
					//Fehlermeldung als Beitrag anzeigen
					gruppe = new Gruppe(-1, "Fehler");
				}
				showProjekt = false;
			} else {
				//Eingeloggt, auf der Startseite
				
				gruppe = new Gruppe(-1, " Startseite");
				showProjekt = true;
				projektArray = Datenbank.getProjektArray(nutzer.getNutzer_id(),true,false);
				rubrik = Datenbank.getRubrik(Sitzung.getNutzer().getNutzer_id());
				if(rubrik != null) {
					bild = rubrik.getBild();
					model = Gruppe.generateModel(rubrik.getGruppe().getUnterGruppe(), false);
				}
			}
			
		} catch (NumberFormatException e) {
			gruppe = new Gruppe(-1, "fehlerhafte URL");
		}
	}
	
	public int getDefaultGruppe() {
		return Datenbank.getDefaultGruppeID();
	}
	
	public Gruppe getGruppe() {
		return gruppe;
	}
	
	//Beiträge anzeigen	
	public Beitrag[] getBeitrag() {
		return beitragArray;
	}
	
	// stephaneum700.de without parameters and not logged in
	public boolean isPublicHome() {
		return publicHome;
	}
	
	public boolean isPrivateHome() {
		return privateHome;
	}
	
	public Pagination getPagination() {
		return pagination;
	}
	
	public Gruppe[] getChilds() {
		return childs;
	}
	
	public Datei getBild() {
		return bild;
	}
	
	//Passwort
	
	public String getPassword() {
		return password;
	}
	
	public String getPasswordInput() {
		return passwordInput;
	}
	
	public void setPasswordInput(String passwordInput) {
		this.passwordInput = passwordInput;
	}
	
	public void checkPassword() {
		if(password != null && passwordInput != null && password.equals(passwordInput)) {
			Nutzer nutzer = Sitzung.getNutzer();
			if(nutzer == null)
				nutzer = Sitzung.createNutzerGuest();
			nutzer.getUnlocker().unlockGruppe(gruppe.getGruppe_id());
			password = null;
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Zugriff genehmigt",gruppe.getName()) );
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Zugriff verwehrt", "falsches Passwort") );
		}
	}
	
	//Projekte auf der Startseite
	
	public boolean isShowProjekt() {
		return showProjekt;
	}
	
	public Projekt[] getProjektArray() {
		return projektArray;
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
	
	//Rubrik
	
	public Rubrik getRubrik() {
		return rubrik;
	}
	
	public MenuModel getModel() {
		return model;
	}
	
	

}
