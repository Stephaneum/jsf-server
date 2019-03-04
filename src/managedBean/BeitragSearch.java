package managedBean;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.Beitrag;
import objects.Nutzer;
import sitzung.Sitzung;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class BeitragSearch {
	
	/*
	 * Suche und Anzeige der Suchergebnisse (home.xhtml)
	 * 
	 */
	
	private Beitrag[] beitragArray;
	
	private String keyWord;
	private boolean searchBegin = false;
	
	@PostConstruct
	void init() {
		Nutzer nutzer = Sitzung.getNutzer();
		
		if(nutzer != null) {
			if(nutzer.getSearchKeyWord() != null){
				searchBegin = true;
				
				if(FacesContext.getCurrentInstance().getViewRoot().getViewId().equals("/"+URLManager.SUCHE+".xhtml"))
					keyWord = nutzer.getSearchKeyWord(); //z.B. auf Home, sollte nicht das vergangende Suchwort stehen
				beitragArray = nutzer.getSearchResults();
			} else {
				//Suchergebnisse löschen
				nutzer.setSearchKeyWord(null);
				nutzer.setSearchResults(null);
			}
		}
		
	}
	
	//Beiträge anzeigen	
	public Beitrag[] getBeitrag() {
		return beitragArray;
	}
	
	public String search() {
		
		if(keyWord == null || keyWord.trim().equals("")) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte mindestens einen Suchbegriff eingeben.") );
			searchBegin = false;
			keyWord = null;
			beitragArray = null;
			return null;
		}
		
		Konsole.method("BeitragSearch.search()");
		
		keyWord = keyWord.trim().toLowerCase();
		String[] keyWords = keyWord.split(" ");
		int[] keyWordsLength = new int[keyWords.length];
		for(int i = 0; i < keyWords.length; i++) {
			keyWordsLength[i] = keyWords[i].length();
		}
		
		beitragArray = Datenbank.getBeitragArray(true); //Array aktualisieren
		searchBegin = true; //flag auf true setzen, wegen rendered-Attribut
		
		//Counts resetten
		for(int i = 0; i < beitragArray.length; i++) {
			beitragArray[i].setCount(0);
		}
		
		//jeden Suchbegriff...
		int[][] count = new int[keyWords.length][beitragArray.length];
		for(int k = 0; k < keyWords.length; k++) {
			
			//jeden Beitrag...
			for(int i = 0; i < beitragArray.length; i++) {
				
				String text = beitragArray[i].getPlainText();
				
				//Suchergebnisse für den Text
				if(text != null) {
					text = text.toLowerCase();
					count[k][i] = getTreffer(text, keyWords[k]) * keyWordsLength[k];
				}
				
				//Suchergebnisse für den Titel
				text = beitragArray[i].getTitel().toLowerCase();
				count[k][i] += getTreffer(text, keyWords[k]) * keyWordsLength[k];
			}
		}
		
		//Anzahl der Treffer vom count[][]-Array speichern, wenn ALLE Suchbegriffe count[k] != 0 gilt
		
		for(int i = 0; i < beitragArray.length; i++) {
			
			boolean error = false;
			int sum = 0;
			
			for(int k = 0; k < keyWords.length; k++) {
				if(count[k][i] == 0) {
					error = true;
					break;
				} else {
					sum += count[k][i];
				}
			}
			
			if(!error) {
				beitragArray[i].setCount(sum);
			}
		}
		
		//neues Array erstellen, Beiträge mit 0 Übereinstimmungen löschen
		
		int anzahl = 0;
		
		for(int i = 0; i < beitragArray.length; i++) {
			if(beitragArray[i].getCount() > 0) {
				anzahl++;
			}
		}
		
		Beitrag[] beitragArrayNew = new Beitrag[anzahl];
		
		anzahl = 0; //cursor
		for(int i = 0; i < beitragArray.length; i++) {
			if(beitragArray[i].getCount() > 0) {
				beitragArrayNew[anzahl] = beitragArray[i];
				anzahl++;
			}
		}
		
		beitragArray = beitragArrayNew;
		
		if(anzahl == 0) {
			//Sortieren nicht notwendig
			Nutzer nutzer = Sitzung.getNutzer();
			nutzer.setSearchKeyWord(null);
			nutzer.setSearchResults(null);
			
			keyWord = null;
			beitragArray = null;
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Suche durchgeführt","Keine Treffer") );
			return null;
		}
		
		//Prozentsatz
		for(int i = 0; i < beitragArray.length; i++) {
			
			int lengthText = 0;
			if(beitragArray[i].getPlainText() != null) {
				lengthText = beitragArray[i].getPlainText().length();
			}
			
			float percentage = beitragArray[i].getCount() / (float) (lengthText + beitragArray[i].getTitel().length());
			beitragArray[i].setPercentage(percentage);
		}
		
		//sortieren nach Übereinstimmung
		Beitrag current;
		for (int i = 1; i < beitragArray.length; i++) {
			
			current = beitragArray[i];
			int j = i;
			while (j > 0 && beitragArray[j - 1].getPercentage() < current.getPercentage()) {
				//tauschen
				beitragArray[j] = beitragArray[j - 1];
				j--;
			}
			beitragArray[j] = current;
		}
		
		//höchste Übereinstimmung = 100%
		
		float max = beitragArray[0].getPercentage();
		beitragArray[0].setPercentageString("100");
		
		for(int i = 1; i < beitragArray.length; i++) {
			
			float percentage = (beitragArray[i].getPercentage() / max) * 100;
					
			percentage = (float) (Math.round(percentage * 100.0) / 100.0);
			beitragArray[i].setPercentage(percentage);
			
			if(percentage == 100.0f)
				beitragArray[i].setPercentageString("100");
			else
				beitragArray[i].setPercentageString(String.valueOf(percentage).replace('.', ','));
		}
		
		//Vorschau-Text aktualisieren
		for(int i = 0; i < beitragArray.length; i++) {
			beitragArray[i].highlightSuche(keyWords, keyWordsLength);
		}
		
		//in die Sitzung speichern (falls man vom geöffneten Beitrag wieder zurück will
		
		Nutzer nutzer = Sitzung.getNutzer();
		nutzer.setSearchKeyWord(keyWord);
		nutzer.setSearchResults(beitragArray);
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Suche durchgeführt",beitragArray.length+" Ergebnisse") );
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true); //damit message redirect überlebt
		return URLManager.SUCHE+"?faces-redirect=true";
	}
	
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	
	public String getKeyWord() {
		return keyWord;
	}
	
	public boolean isSearchBegin() {
		return searchBegin;
	}
	
	public int getAnzahlSuchergebnisse() {
		if(beitragArray != null)
			return beitragArray.length;
		else
			return 0;
	}
	
	private int getTreffer(String text, String keyWord) {
		
		int lastIndex = 0;
		int treffer = 0;

		while(lastIndex != -1){

		    lastIndex = text.indexOf(keyWord,lastIndex);

		    if(lastIndex != -1){
		    	treffer ++;
		        lastIndex += keyWord.length();
		    }
		}
		
		return treffer;
	}

}
