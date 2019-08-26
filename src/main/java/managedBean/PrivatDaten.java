package managedBean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.Datei;
import objects.Nutzer;
import sitzung.Sitzung;

@ViewScoped
@ManagedBean
public class PrivatDaten {
	
	private String oldEmail, newEmail, passwordCheck; //E-Mail ändern
	private String oldPassword, newPassword, newPasswordWdh; //Passwort ändern
	
	private String vorname, nachname, rang, klasse, storageString, storageUsedString;
	private int nutzerID = -1;
	
	public PrivatDaten() {
		Nutzer nutzer = Sitzung.getNutzer();
		
		if(nutzer != null) {
			vorname = nutzer.getVorname();
			nachname = nutzer.getNachname();
			rang = nutzer.getRangString();
			nutzerID = nutzer.getNutzer_id();
			
			int storage = Datenbank.getStorage(nutzer.getNutzer_id());
			int storageUsed = Datenbank.getStorageUsed(nutzer.getNutzer_id());
			
			storageString = Datei.convertSizeToString(storage);
			storageUsedString = Datei.convertSizeToString(storageUsed);
			
			klasse = nutzer.getKlasse();
			if(klasse == null) {
				klasse = "keine Klasse zugewiesen";
			}
		}
	}
	
	public void changeEmail() {
		
		if(newEmail != null) {
			if(!newEmail.contains("@")) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Die neue E-Mail-Adresse ist ungültig.") );
				return;
			}
		}
		
		if(nutzerID == -1) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "interner Fehler") );
			return;
		}
			
		if(Datenbank.checkPassword(passwordCheck)) {
			if(Datenbank.checkEmail(oldEmail)) {
				if(Datenbank.existEmail(newEmail)) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Die neue E-Mail existiert bereits.") );
				} else {
					Datenbank.setEmail(oldEmail, newEmail);
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"E-Mail geändert",newEmail) );
					Sitzung.getNutzer().setEmail(newEmail); //Nutzer-Objekt aktualisieren
					oldEmail = null;
					newEmail = null;
					passwordCheck = null;
				}
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Falsche E-Mail oder Passwort") );
			}
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Falsche E-Mail oder Passwort") );
		}
	}
	
	public void changePassword() {
		if(!newPassword.equals(newPasswordWdh)) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Passwörter stimmen nicht überein") );
			return;
		}
		
		if(nutzerID == -1) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "interner Fehler") );
			return;
		}
		
		if(Datenbank.checkPassword(oldPassword)) {
			Datenbank.setPasswort(nutzerID, null, newPassword);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Passwort geändert",null) );
			oldPassword = null;
			newPassword = null;
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Falsches Passwort") );
		}
	}
	
	public String getOldEmail() {
		return oldEmail;
	}
	
	public void setOldEmail(String oldEmail) {
		this.oldEmail = oldEmail;
	}
	
	public String getNewEmail() {
		return newEmail;
	}
	
	public void setNewEmail(String newEmail) {
		this.newEmail = newEmail;
	}
	
	public String getPasswordCheck() {
		return passwordCheck;
	}
	
	public void setPasswordCheck(String passwordCheck) {
		this.passwordCheck = passwordCheck;
	}
	
	public String getOldPassword() {
		return oldPassword;
	}
	
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	
	public String getNewPassword() {
		return newPassword;
	}
	
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
	public String getNewPasswordWdh() {
		return newPasswordWdh;
	}
	
	public void setNewPasswordWdh(String newPasswordWdh) {
		this.newPasswordWdh = newPasswordWdh;
	}
	
	public String getVorname() {
		return vorname;
	}
	
	public String getNachname() {
		return nachname;
	}
	
	public String getRang() {
		return rang;
	}
	
	public String getKlasse() {
		return klasse;
	}
	
	public String getStorageString() {
		return storageString;
	}
	
	public String getStorageUsedString() {
		return storageUsedString;
	}

}
