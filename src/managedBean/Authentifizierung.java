package managedBean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.Klasse;
import objects.Nutzer;
import sitzung.Sitzung;
import tools.EmailManager;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class Authentifizierung {
	
	final static private int MAX_TRIES = 3, UNLOCK_TIME = 10000;
	final static private String[] GESCHLECHT = {"männlich","weiblich","keine Angabe"};
	
	//Login
	private String loginEmail, loginPasswort;
	private boolean renderLogin = true;
	
	//Register
	private String registerVorname, registerNachname, registerEmail, registerCode, registerPasswort, registerPasswortWdh, registerKlasse, registerGeschlecht;
	private boolean correctCode = false;
	private int rang = Nutzer.RANG_GAST_NO_LOGIN;
	private String rangString = Nutzer.getRangString(rang);
	
	//Passwort-Vergessen
	private String codePasswortVergessen;
	private boolean renderCheckCode = false, renderCheckCode2 = false, renderCheckCode3 = false;
	
	//Zugangscode prüfen---------------------------------------------------
	public void setRegisterCode(String registerCode) {
		this.registerCode = registerCode;
	}
	
	public String getRegisterCode() {
		return registerCode;
	}
	
	public String checkZugangscode() {
		
		rang = Datenbank.checkZugangscode(registerCode);
		rangString = Nutzer.getRangString(rang);
		correctCode = rang != Nutzer.RANG_GAST_NO_LOGIN;
		if(!correctCode) {
			try {
				Thread.sleep(2000); //bei falsche Eingabe 2 Sekunden warten
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Falscher oder entwerteter Zugangscode") );
		}
		return null;
	}
	
	public boolean isCorrectCode() {
		return correctCode;
	}

	//Registrierung---------------------------------------------------------
	
	//Rang des aktuellen Zugangscodes
	public int getRang() {
		return rang;
	}
	
	public String getRangString() {
		return rangString;
	}
	
	public void setRegisterVorname(String registerVorname) {
		this.registerVorname = registerVorname;
	}
	
	public String getRegisterVorname() {
		return registerVorname;
	}
	
	public void setRegisterNachname(String registerNachname) {
		this.registerNachname = registerNachname;
	}
	
	public String getRegisterNachname() {
		return registerNachname;
	}
	
	public void setRegisterEmail(String registerEmail) {
		this.registerEmail = registerEmail;
	}
	
	public String getRegisterEmail() {
		return registerEmail;
	}
	
	public void setRegisterGeschlecht(String registerGeschlecht) {
		this.registerGeschlecht = registerGeschlecht;
	}
	
	public String getRegisterGeschlecht() {
		return registerGeschlecht;
	}
	
	public void setRegisterKlasse(String registerKlasse) {
		this.registerKlasse = registerKlasse;
	}
	
	public String getRegisterKlasse() {
		return registerKlasse;
	}
	
	public void setRegisterPasswort(String registerPasswort) {
		this.registerPasswort = registerPasswort;
	}
	
	public String getRegisterPasswort() {
		return registerPasswort;
	}
	
	public void setRegisterPasswortWdh(String registerPasswortWdh) {
		this.registerPasswortWdh = registerPasswortWdh;
	}
	
	public String getRegisterPasswortWdh() {
		return registerPasswortWdh;
	}
	
	public String[] getGeschlechtVorgaben() {
		return GESCHLECHT;
	}
	
//Register Button
	public String register() {
	
		if(!registerPasswort.equals(registerPasswortWdh)) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Passwörter stimmen nicht überein") );
			return null;
		}
		
		if(registerEmail != null) {
			if(!registerEmail.contains("@")) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Die E-Mail-Adresse ist ungültig.") );
				return null;
			}
		}
		
		// Datenwert in int umwandeln
		int geschlecht_int = -1;
		switch(registerGeschlecht) {
		case "männlich":
			geschlecht_int = 0;
			break;
		case "weiblich":
			geschlecht_int = 1;
			break;
		case "keine Angabe":
			geschlecht_int = 2;
			break;
		default:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Geschlecht angeben") );
			return null;
		}
		
		//Klasse
		if(registerKlasse != null && registerKlasse.trim().equals("")) {
			registerKlasse = null;
		}
		
		if(registerKlasse != null) {
			
			registerKlasse = registerKlasse.trim();
			
			Klasse[] klassen = Datenbank.getAllKlassen();
			boolean found = false;
			for(Klasse klasse : klassen) {
				if(klasse.getString().equals(registerKlasse)) {
					found = true;
					break;
				}
			}
			
			//neue Klasse erstellen
			if(!found) {
				String[] string = registerKlasse.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
				for(int n = 2; n < string.length; n++) {
					string[1] += string[n];
				}
				
				if(string.length >= 2) {
					int klassenstufe = 0;
					try {
						klassenstufe = Integer.parseInt(string[0]);
					} catch (NumberFormatException e) {
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Syntax der Klasse überprüfen") );
						return null;
					}
					
					String suffix = string[1];
					
					if( (klassenstufe < 10 && suffix.contains("-")) || (klassenstufe >= 10 && !suffix.contains("-")) ) {
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Syntax der Klasse überprüfen") );
						return null;
					}
					
					Datenbank.createKlasse(klassenstufe, suffix);
				}
			}
		}
		
		//Rückgabewert: 0 = OK, 1 = E-Mail bereits verwendet, 2 = Klasse existiert nicht, 3 = Unbekannter Error
		int antwort = Datenbank.registerNutzer(registerCode, registerKlasse, registerVorname, registerNachname, geschlecht_int, registerEmail, registerPasswort);
		switch(antwort) {
		case 0:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Registriert!", registerVorname+" "+registerNachname) );
			correctCode = false;
			rang = Nutzer.RANG_GAST_NO_LOGIN;
			rangString = Nutzer.getRangString(rang);
			registerCode = null;
			break;
		case 1:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "E-Mail bereits verwendet") );
			break;
		case 2:
			if(registerKlasse != null)
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Klasse "+registerKlasse+" existiert nicht") );
			else
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte als Schüler/in eine Klasse angeben.") );
			break;
		default:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "unbekannter Fehler") );
			break;
		}
		
		return null;
	}
	
	//Login
	
	public boolean isBlockLogin() {
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null)
			nutzer = Sitzung.createNutzerGuest();
		
		return nutzer.getUnlockTime() > System.currentTimeMillis();
	}
	
	public long getBlockLoginTime() {
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null)
			nutzer = Sitzung.createNutzerGuest();
		
		return ((nutzer.getUnlockTime() - System.currentTimeMillis())/1000)+1;
	}
	
	public String getLoginEmail() {
		return loginEmail;
	}
	
	public String getLoginPasswort() {
		return loginPasswort;
	}
	
	public void setLoginEmail(String loginEmail) {
		this.loginEmail = loginEmail;
	}
	
	public void setLoginPasswort(String loginPasswort) {
		this.loginPasswort = loginPasswort;
	}
	
	public String login() {
		
		if(isBlockLogin()) {
			return null;
		}
		
		int antwort = Datenbank.login(loginEmail, loginPasswort);
		
		switch(antwort) {
		case -1:
			Konsole.antwort("Nutzer gesperrt");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Gesperrt!", "Dein Account wurde vom Administrator gesperrt.") );
			return null;
		case 0:
			Konsole.antwort("Fehler beim login! Falsche e-mail oder Passwort");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Login fehlgeschlagen.") );
			
			Nutzer nutzer = Sitzung.getNutzer();
			if(nutzer == null)
				nutzer = Sitzung.createNutzerGuest();
			nutzer.setLoginTries(nutzer.getLoginTries()+1);
			
			try {
				Thread.sleep(2000); //bei falsche Eingabe 2 Sekunden warten
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(nutzer.getLoginTries() > MAX_TRIES)
				nutzer.setUnlockTime(System.currentTimeMillis() + UNLOCK_TIME);
			return null;
		case 1:
			Konsole.antwort("Login erfolgreich");
			nutzer = Sitzung.getNutzer();
			nutzer.setLoginTries(0);
			
			if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/"+URLManager.LEHRER_CHAT+".xhtml"))
				return URLManager.LEHRER_CHAT+"?faces-redirect=true";
			else
				return URLManager.HOME+"?faces-redirect=true";
		default:
			return null;
		}
	}

//E-Mail vergessen
	
	// Email vergessen Button
	public void startMissingPassword() {
		renderCheckCode = true;
		renderLogin = false;
		Konsole.antwort("Ein Nutzer hat sein Passwort vergessen");
	}
	
	public boolean isRenderLogin() {
		return renderLogin;
	}
	
	public boolean isRenderCheckCode() {
		return renderCheckCode;
	}
	
	public boolean isRenderCheckCode2() {
		return renderCheckCode2;
	}
	
	public boolean isRenderCheckCode3() {
		return renderCheckCode3;
	}
	
	// Senden Button
	public void checkEmailExist() {
		int status = EmailManager.sendCodePasswortVergessen(loginEmail);
		
		switch(status) {
		case EmailManager.STATUS_OK:
			renderCheckCode2 = true;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Code gesendet!", loginEmail) );
			break;
		case EmailManager.STATUS_EMAIL_NOT_EXIST:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "E-Mail nicht registriert") );
			break;
		case EmailManager.STATUS_ADDRESS_ERR:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "intern: ADDRESS-ERROR") );
			break;
		case EmailManager.STATUS_MESSAGING_ERR:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "intern: MESSAGING-ERROR") );
			break;
		}
	}
	
	public String getCodePasswortVergessen() {
		return codePasswortVergessen;
	}
	
	public void setCodePasswortVergessen(String codePasswortVergessen) {
		this.codePasswortVergessen = codePasswortVergessen;
	}
	
	// Prüfen Button
	public void checkCodePasswortVergessen() {
		String code = codePasswortVergessen.trim();
		if(Datenbank.checkCodePasswortVergessen(loginEmail, code)) {
			renderCheckCode3 = true;
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Code akzeptiert", code) );
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "falscher Code") );
		}
	}
	
	//Bestätigen Button
	public void setNewPasswort() {
		Datenbank.setPasswort(-1, loginEmail, loginPasswort);
		renderLogin = true;
		renderCheckCode = false;
		renderCheckCode2 = false;
		renderCheckCode3 = false;
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Passwort geändert!", loginEmail) );
	}
	
}
