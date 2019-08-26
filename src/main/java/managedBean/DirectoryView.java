package managedBean;

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.Datei;
import sitzung.Sitzung;

/*
 * 	Diese Klasse verwaltet die Darstellung von Ordner X und die Navigation zwischen Ordnern
 * 
 */

public class DirectoryView {
	
	final static private String VERZEICHNIS_HOME = "Homeverzeichnis";
	final private boolean ordnerOnly;
	
	final private int nutzer_klasse_projekt_id;
	final private Datei.Mode mode;
	
	private boolean root;
	private List<Datei> pfad;
	private Datei ordner;
	private int ordnerID = Datei.ORDNER_HOME;
	private String verzeichnis;
	private Datei[] array;
	
	//temp
	private StringBuilder builder;
	
	public DirectoryView(int nutzer_klasse_projekt_id, Datei.Mode mode, boolean ordnerOnly) {
		this.nutzer_klasse_projekt_id = nutzer_klasse_projekt_id;
		this.ordnerOnly = ordnerOnly;
		this.mode = mode;
		reset();
	}
	
	void reset() {
		root = true;
		pfad = new ArrayList<Datei>();
		ordner = null;
		ordnerID = Datei.ORDNER_HOME;
		verzeichnis = null;
		
		update();
	}
	
	protected void update() {
		
		if(Sitzung.getNutzer() == null) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Sitzung abgelaufen. Bitte neu anmelden.") );
			return;
		}
		
		if(ordnerOnly)
			array = Datenbank.getFolderRecursive(nutzer_klasse_projekt_id, ordnerID, mode);
		else {
			Datei[] arrayOrdner = Datenbank.getFolderRecursive(nutzer_klasse_projekt_id, ordnerID, mode);
			Datei[] arrayDatei = Datenbank.getFileArray(nutzer_klasse_projekt_id, ordnerID, mode);
			array = new Datei[arrayOrdner.length + arrayDatei.length]; //fusionieren
			for(int i = 0; i < arrayOrdner.length; i++) {
				array[i] = arrayOrdner[i];
			}
			for(int i = 0; i < arrayDatei.length; i++) {
				array[i+arrayOrdner.length] = arrayDatei[i];
			}
		}
		
		verzeichnis = generateVerzeichnis();
		root = pfad.size() == 0;
	}
	
	private String generateVerzeichnis() {
		if(builder == null)
			builder = new StringBuilder();
		
		builder.setLength(0);
		for(int i = 0; i < pfad.size(); i++) {
			builder.append(" / ");
			builder.append(pfad.get(i).getDatei_name());
		}
		
		return builder.toString();
	}
	
	public String getVerzeichnis() {
		if(verzeichnis == null)
			verzeichnis = generateVerzeichnis();
		return verzeichnis;
	}
	
	public String getVerzeichnisNotEmpty() {
		String output = getVerzeichnis();
		
		if(output.equals(""))
			return VERZEICHNIS_HOME;
		else
			return output;
	}
	
	public Datei[] getArray() {
    	if(array == null)
    		update();
    	return array;
    }
	
	public void selectOrdner(Datei ordner) {
		this.ordnerID = ordner.getDatei_id();
		this.ordner = ordner;
		pfad.add(ordner);
		
		update();
	}
	
	public void upVerzeichnis() {
		if(pfad.size() == 1) {
			pfad.clear();
			ordner = null;
			ordnerID = Datei.ORDNER_HOME;
		} else if(pfad.size() > 1) {
			pfad.remove(pfad.size()-1);
			ordner = pfad.get(pfad.size()-1);
			ordnerID = ordner.getDatei_id();
		}
		
		update();
	}
	
	public boolean isRoot() {
		return root;
	}
	
	public int getOrdnerID() {
		return ordnerID;
	}

}
