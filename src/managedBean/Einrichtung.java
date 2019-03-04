package managedBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import mysql.Datenbank;
import objects.Nutzer;
import objects.ProgressObject;
import tools.URLManager;
import tools.Zugangscodes;

@ViewScoped
@ManagedBean
public class Einrichtung {
	
	final static private String[] GESCHLECHT = {Nutzer.STR_MANN,Nutzer.STR_FRAU,Nutzer.STR_UNBEKANNT};
	
	private String speicherort, backupDir, vorname, nachname, passwort, passwortwdh, email, geschlecht;
	private double password_timeout = 2;
	private static boolean creating = false;
	
	private boolean pathFinished = false;
	
	private ProgressObject progress = new ProgressObject(); //dieses Objekt wird übergeben, damit andere Funktionen den INT verändern können
	
	//Autofill Geschlecht
	public String[] getGeschlechtVorgaben() {
		return GESCHLECHT;
	}
	
	//Speicherort für datenbank festlegen
	public void setSpeicherort(String speicherort) {
		this.speicherort = speicherort;
	}
	
	public String getSpeicherort() {
		return speicherort;
	}
	
	public String getBackupDir() {
		return backupDir;
	}
	
	public void setBackupDir(String backupDir) {
		this.backupDir = backupDir;
	}
	
	public String getVorname() {
		return vorname;
	}
	
	public void setVorname(String vorname) {
		this.vorname = vorname;
	}
	
	public String getNachname() {
		return nachname;
	}
	
	public void setNachname(String nachname) {
		this.nachname = nachname;
	}
			
	public String getPasswort() {
		return passwort;
	}
	
	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}
	
	public String getPasswortwdh() {
		return passwortwdh;
	}
	
	public void setPasswortwdh(String passwortwdh) {
		this.passwortwdh = passwortwdh;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getGeschlecht() {
		return geschlecht;
	}
	
	public void setGeschlecht(String geschlecht) {
		this.geschlecht = geschlecht;
	}
	
	public int getPercent() {
		return progress.progress;
	}
	
	public String createDatabase() {
		
		if(Datenbank.existDatabase()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Die Datenbank wurde bereits erstellt.") );
			return URLManager.HOME;
		}
		
		if(creating) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Die Datenbank wird gerade erstellt. Bitte haben Sie Geduld.") );
			creating = false;
			return null;
		}
		
		creating = true;
		
		//Stimmen die Eingabedaten?
		
		if(passwort.equals("") || !passwort.equals(passwortwdh)) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Passwörter stimmen nicht überein") );
			creating = false;
			return null;
		}
		
		if(password_timeout == 0) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Timeout festlegen") );
			creating = false;
			return null;
		}
		
		int geschlecht_int = Nutzer.convertToGeschlechtInt(geschlecht);
		
		Datenbank.createDatabase(progress);
		progress.progress = 60;
		Datenbank.createZugangscodes(Zugangscodes.STANDARD_LENGTH, 200, 20, true);
		progress.progress = 70;
		int[] stufe_low = {5,6,7,8,9};
		int[] stufe_high = {10,11,12};
		String[] suffix_low = {"a","b","c","d","e"};
		String[] suffix_high = {"-1","-2","-3","-4","-5"};
		Datenbank.createKlassen(stufe_low, suffix_low);
		Datenbank.createKlassen(stufe_high, suffix_high);
		progress.progress = 80;
		Datenbank.setSpeicherort(speicherort);
		Datenbank.setBackupDir(backupDir);
		progress.progress = 90;
		Datenbank.setTimeoutPasswortVergessen((int) (password_timeout*3600000));
		progress.progress = 95;
		
		//Registrierung Admin
		Datenbank.registerAdmin(vorname, nachname, geschlecht_int, email, passwort);
		progress.progress = 100;
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Datenbank erstellt","Admin: "+email) );
		creating = false;
		return "home";
	}
	
	public boolean isPathFinished() {
		return pathFinished;
	}
	
	public void finishPath() {
		pathFinished = true;
	}
	
	public void upload(FileUploadEvent event) {
		
		UploadedFile file = event.getFile();
		
		InputStream inputStream;
		OutputStream outputStream;
		try {
			String path = backupDir+"/"+file.getFileName();
			inputStream = file.getInputstream();
			outputStream = new FileOutputStream(new File(path));
			
			int read = 0;
			byte[] bytes = new byte[1024];
			
			//neue Datei schreiben
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			
			BackupBean.restore(path, backupDir, speicherort);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public void setPassword_timeout(double passwort_timeout) {
		this.password_timeout = passwort_timeout;
	}
	
	public double getPassword_timeout() {
		return password_timeout;
	}

}
