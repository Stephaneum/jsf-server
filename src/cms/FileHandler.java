package cms;

import mysql.Datenbank;
import objects.BildReihe;
import objects.Datei;

public class FileHandler {
	
	/*
	 * Dateiliste eines Beitrages
	 * wird von ApprovePost, CreatePost und EditPost JEWEILS instanziert (pro Nutzer, der sich auf der Webseite befindet)
	 */
	
	final private int NUTZER_ID;
	private Datei[] files_added, files_available;
	private boolean modified = false; //sobald Dateien hinzugefügt werden --> true
	
	//Bilderreihe
	final static private int REIHE_ANZAHL = 4; //4 Bilder pro Zeile
	private BildReihe[] reiheAdded, reiheAvailable;
	private boolean vorschauAnsicht = true;
	
	FileHandler(int nutzer_id) {
		this.NUTZER_ID = nutzer_id;
	}
	
	/**
	 *  Aktualisiert files_available.
	 *  files_added erhält volle Eigenschaften (Größe, Pfad, usw.)
	 *  
	 *  wird bei jeder Modifikation aufgerufen (upload,add,remove)
	 */
	void reload() {
		Datei[] files_available_all = Datenbank.getDateiArrayAllImages(NUTZER_ID);
		
		if(files_added == null) { //falls noch keine Dateien hinzugefügt wurden
			files_available = files_available_all;
			
			if(vorschauAnsicht)
				updateBildReihe();
			return;
		}
		
		//added Files löschen
		
		//Anzahl - files_available - files_added
		int anzahl_available = files_available_all.length;
		for(int x = 0; x < files_available_all.length; x++) { //jede verfügbare Datei
			for(int y = 0; y < files_added.length; y++) { //mit jeder vorhandenen Datei vergleichen
				if(files_available_all[x].getDatei_id() == files_added[y].getDatei_id()) {
					anzahl_available--;
					break;
				}
			}
		}
		
		//Aussortieren - files_available
		files_available = new Datei[anzahl_available];
		int current = 0;
		for(int x = 0; x < files_available_all.length; x++) { //jede verfügbare Datei
			boolean found = false;
			for(int y = 0; y < files_added.length; y++) { //mit jeder vorhandenen Datei vergleichen
				if(files_available_all[x].getDatei_id() == files_added[y].getDatei_id()) {
					found = true;
					break;
				}
			}
			//zum neuen array hinzufügen
			if(found == false) {
				files_available[current] = files_available_all[x];
				current++;
			}
		}
		
		//files_added mit vollen Daten befüllen (vorher eventuell nur mit datei_id)
		for(int x = 0; x < files_available_all.length; x++) { //jede verfügbare Datei
			for(int y = 0; y < files_added.length; y++) { //mit jeder vorhandenen Datei vergleichen
				if(files_available_all[x].getDatei_id() == files_added[y].getDatei_id()) {
					files_added[y] = files_available_all[x];
					break;
				}
			}
		}
		
		if(vorschauAnsicht)
			updateBildReihe();
	}
	
	void addFile(Datei datei) {
		
		if(datei == null)
			return;
		
		if(files_added == null) {
			//erstes mal
			files_added = new Datei[0];
		}
		
		Datei[] files_added_new = new Datei[files_added.length+1];
		for(int i = 0; i < files_added.length; i++) {
			files_added_new[i] = files_added[i];
		}
		files_added_new[files_added.length] = datei;
		
		files_added = files_added_new;
		modified = true;
		
		if(vorschauAnsicht)
			updateBildReihe();
	}
	
	void removeFile(Datei datei) {
		if(files_added.length == 1) {
			files_added = null;
		} else {
			Datei[] files_added_new = new Datei[files_added.length-1];
			int current = 0;
			for(int i = 0; i < files_added.length; i++) { //jedes Element des alten Arrays
				if(files_added[i].getDatei_id() != datei.getDatei_id()) {
					files_added_new[current] = files_added[i]; //fügt das Element hinzu, wenn es NICHT mit der ID übereinstimmt
					current++;
				}
			}
			files_added = files_added_new;
		}
		modified = true;
		
		if(vorschauAnsicht)
			updateBildReihe();
	}
	
	private void updateBildReihe() {
		if(files_added == null)
			reiheAdded = new BildReihe[0];
		else
			reiheAdded = new BildReihe[(int) Math.ceil(files_added.length/(float)REIHE_ANZAHL)];
		
		for(int i = 0; i < reiheAdded.length; i++) {
			Datei[] bilder = new Datei[REIHE_ANZAHL];
			
			for(int x = 0; x < REIHE_ANZAHL; x++) {
				
				int index = i * REIHE_ANZAHL + x;
				
				if(index < files_added.length) {
					bilder[x] = files_added[index];
				} else {
					break;
				}
			}
			
			reiheAdded[i] = new BildReihe(bilder);
		}
		
		if(files_available == null)
			reiheAvailable = new BildReihe[0];
		else
			reiheAvailable = new BildReihe[(int) Math.ceil(files_available.length/(float)REIHE_ANZAHL)];
		
		for(int i = 0; i < reiheAvailable.length; i++) {
			Datei[] bilder = new Datei[REIHE_ANZAHL];
			
			for(int x = 0; x < REIHE_ANZAHL; x++) {
				
				int index = i * REIHE_ANZAHL + x;
				
				if(index < files_available.length) {
					bilder[x] = files_available[index];
				} else {
					break;
				}
				
			}
			
			reiheAvailable[i] = new BildReihe(bilder);
		}
	}
	
	public Datei[] getFilesAdded() {
		return files_added;
	}
	
	public Datei[] getFilesAvailable() {
		return files_available;
	}
	
	public BildReihe[] getBildReiheAdded() {
		return reiheAdded;
	}
	
	public BildReihe[] getBildReiheAvailable() {
		return reiheAvailable;
	}
	
	public boolean isModified() {
		return modified;
	}
	
	public void setModified(boolean modified) {
		this.modified = modified;
	}
	
	public void setVorschauAnsicht(boolean vorschauAnsicht) {
		if(vorschauAnsicht)
			updateBildReihe();
		this.vorschauAnsicht = vorschauAnsicht;
	}
	
	public boolean isVorschauAnsicht() {
		return vorschauAnsicht;
	}

}
