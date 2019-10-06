package managedBean;

import java.io.IOException;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.Nutzer;
import sitzung.Sitzung;
import tools.Jwt;
import tools.URLManager;

@ApplicationScoped
@ManagedBean
public class Weiterleitung {
	
	//Diese Klasse verwaltet alle Weiterleitungen (mit wenigen Außnahmen, z.B. MySQLConnection)
	
	//home, beitrag, login, kontakt, impressum, sitemap, information
	public String index() {
		
		//erste Einrichtung - MySQL-Verbindung (wird einmal ausgeführt, dann immer übersprungen)
		if(!Datenbank.isConnected()) {
			Konsole.antwort("Es wurde noch keine Verbindung zu MySQL/MariaDB aufgebaut. Leite nach sql.xhtml weiter.");
			return URLManager.SQL; //Weiterleiten
		}
		
		if(Datenbank.needUpdate()) {
			Konsole.antwort("Datenbank erfordert Update. Leite nach update.xhtml weiter.");
			return URLManager.UPDATE; //Weiterleiten
		}
		
		//erste Einrichtung - Datenbankstruktur
		if(!Datenbank.existDatabase()) {
			Konsole.antwort("Es wurde noch keine Datenbankstruktur erstellt. Leite nach einrichtung.xhtml weiter.");
			return URLManager.EINRICHTUNG; //Weiterleiten
		}
		
		//Weiterleitung auf Port 80
		if(Datenbank.isRedirectPort() && URLManager.getPort() != 80) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(URLManager.translateURL(80));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null)
			nutzer = Sitzung.createNutzerGuest(); //ViewCounter

		return null;
	}
	
	public String databaseExist() {
		if(Datenbank.existDatabase()) {
			Konsole.antwort("Es wurde bereits eine Datenbankstruktur erstellt. Leite nach home.xhtml weiter.");
			return URLManager.HOME; //Weiterleiten
		} else {
			return null;
		}
	}
	
	public String noAdmin() {
		
		if(Datenbank.needUpdate()) {
			//Spezialfall: admin_backup.xhtml
			
			Konsole.antwort("Datenbank erfordert Update. Leite nach update.xhtml weiter.");
			return URLManager.UPDATE; //Weiterleiten
		}
		
		if(!Sitzung.isAdmin()) {
			Konsole.antwort("Noch nicht eingeloggt oder falscher Rang! Leite nach login.xhtml weiter.");
			return URLManager.HOME; //Weiterleiten
		} else {
			Sitzung.getNutzer().setOpenRubrik(null); //die Nutzer-Rubrik-Seite wurde verlassen, null setzen damit EditGruppe wieder für Admin funktioniert
			return null;
		}
	}
	
	public String noLehrer() {
		if( !(Sitzung.isAdmin() || Sitzung.isLehrer()) ) {
			Konsole.antwort("Noch nicht eingeloggt oder falscher Rang! Leite nach login.xhtml weiter.");
			return URLManager.HOME; //Weiterleiten
		} else {
			return null;
		}
	}
	
	public String login() {
		if(!Sitzung.isLoggedIn()) {
			Konsole.antwort("Noch nicht eingeloggt! Leite nach login.xhtml weiter.");
			return URLManager.LOGIN; //Weiterleiten
		} else {
			return null;
		}
	}

	public String planManager() throws IOException {
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null || nutzer.getRang() == Nutzer.RANG_GAST_NO_LOGIN || (nutzer.getRang() != Nutzer.RANG_ADMIN && !Datenbank.isVertretung(nutzer.getNutzer_id()))) {
			Konsole.antwort("Noch nicht eingeloggt! Leite nach login.xhtml weiter.");
			return URLManager.LOGIN; //Weiterleiten
		} else {
			Map<String, String> parameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			String oldParam = parameterMap.get("old");
			if(oldParam == null) {
				// redirect if not ?old
				String token = Jwt.generateToken(Sitzung.getNutzer().getNutzer_id());
				FacesContext.getCurrentInstance().getExternalContext().redirect("vertretungsplan-manager?key=" + token);
			}
			return null;
		}
	}

	public String beitragManager() throws IOException {
		if(!Sitzung.isLoggedIn()) {
			Konsole.antwort("Noch nicht eingeloggt! Leite nach login.xhtml weiter.");
			return URLManager.LOGIN; //Weiterleiten
		} else {
			Map<String, String> parameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			String oldParam = parameterMap.get("old");
			if(oldParam == null) {
				// redirect if not ?old
				String token = Jwt.generateToken(Sitzung.getNutzer().getNutzer_id());
				FacesContext.getCurrentInstance().getExternalContext().redirect("beitrag-manager?key=" + token);
			}
			return null;
		}
	}
	
	public String logout() {
  		Nutzer nutzer = Sitzung.getNutzer();
  		
  		if(nutzer != null) {
  			
  			Sitzung.deleteNutzer();
  			
  			Nutzer gast = Sitzung.createNutzerGuest();
  			//daten transferieren
  			gast.setUnlocker(nutzer.getUnlocker());
  			
  	  		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Abgemeldet",nutzer.getVorname()+" "+nutzer.getNachname()));
  		}
  		return URLManager.HOME+"?faces-redirect=true";
  	}
	
	public String search() {
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null || nutzer.getSearchResults() == null || nutzer.getSearchResults().length == 0) {
			Konsole.antwort("Noch keine Suche gestartet! Leite nach home.xhtml weiter.");
			return URLManager.HOME; //Weiterleiten
		} else {
			return null;
		}
	}
	
	public String noRubrik() {
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null || nutzer.getRang() == Nutzer.RANG_GAST_NO_LOGIN)
			return URLManager.LOGIN;
		else if(nutzer.getOpenRubrik() != null || Datenbank.isRubrikErstellen(nutzer.getNutzer_id()))
			return null;
		else
			return URLManager.HOME;
	}
	
	public String noVertretungKonfig() {
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null || nutzer.getRang() == Nutzer.RANG_GAST_NO_LOGIN)
			return URLManager.LOGIN;
		else if(nutzer.getRang() == Nutzer.RANG_ADMIN || Datenbank.isVertretung(nutzer.getNutzer_id()))
			return null;
		else
			return URLManager.HOME;
	}
	
	public String noProjekt() {
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null || nutzer.getRang() == Nutzer.RANG_GAST_NO_LOGIN)
			return URLManager.LOGIN;
		else if(nutzer.getOpenProjekt() == null)
			return URLManager.PROJEKT;
		else
			return null;
	}

}
