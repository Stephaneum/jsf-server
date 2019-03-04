package cms;

import javax.annotation.PostConstruct;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.jsoup.Jsoup;

import mysql.Datenbank;

/*
 * Spezielle Texte
 */

@ViewScoped
@ManagedBean
public class EditImpressum {
	
	final static private String MODE_KONTAKT = "Kontakt",
								MODE_IMPRESSUM = "Impressum",
								MODE_COPYRIGHT = "Copyright",
								MODE_ENTWICKLER = "Entwicklung",
								MODE_TICKER = "Live-Ticker",
								MODE_HISTORY = "Geschichte",
								MODE_EU_SA = "EU und Sachsen-Anhalt",
								MODE_TERMINE = "Termine",
								MODE_KOOP = "Kooperationspartner",
								MODE_KOOP_URL = "Kooperationspartner (URL)";
	final static private String[] modes = {MODE_KONTAKT,MODE_IMPRESSUM,MODE_COPYRIGHT,MODE_ENTWICKLER,MODE_TICKER,MODE_HISTORY,MODE_EU_SA,MODE_TERMINE,MODE_KOOP,MODE_KOOP_URL};
	
	//Zuordnung
	private String selectedMode;
	private boolean selected;
	private int selectedInt;
	
	//Text
	private String text;
	
	@PostConstruct
	void init() {
		text = null;
		selected = false;
		selectedMode = null;
		selectedInt = -1;
	}
	
	public void select() {
		switch(selectedMode) {
		case MODE_KONTAKT:
			text = Datenbank.getKontakt();
			selectedMode = MODE_KONTAKT;
			selected = true;
			selectedInt = 0;
			break;
		case MODE_IMPRESSUM:
			text = Datenbank.getImpressum();
			selectedMode = MODE_IMPRESSUM;
			selected = true;
			selectedInt = 1;
			break;
		case MODE_COPYRIGHT:
			text = Datenbank.getBottomText();
			selectedMode = MODE_COPYRIGHT;
			selected = true;
			selectedInt = 2;
			break;
		case MODE_ENTWICKLER:
			text = Datenbank.getEntwicklerInfo();
			selectedMode = MODE_ENTWICKLER;
			selected = true;
			selectedInt = 3;
			break;
		case MODE_TICKER:
			text = Datenbank.getLiveTicker();
			selectedMode = MODE_TICKER;
			selected = true;
			selectedInt = 4;
			break;
		case MODE_HISTORY:
			text = Datenbank.getHistory();
			selectedMode = MODE_HISTORY;
			selected = true;
			selectedInt = 5;
			break;
		case MODE_EU_SA:
			text = Datenbank.getEuSa();
			selectedMode = MODE_EU_SA;
			selected = true;
			selectedInt = 6;
			break;
		case MODE_TERMINE:
			text = Datenbank.getTermine();
			selectedMode = MODE_TERMINE;
			selected = true;
			selectedInt = 7;
			break;
		case MODE_KOOP:
			text = Datenbank.getKoop();
			selectedMode = MODE_KOOP;
			selected = true;
			selectedInt = 8;
			break;
		case MODE_KOOP_URL:
			text = Datenbank.getKoopURL();
			selectedMode = MODE_KOOP_URL;
			selected = true;
			selectedInt = 9;
			break;
		default:
			init();
		}
	}
	
	public void setMode(String mode) {
		selectedMode = mode;
	}
	
	public String getMode() {
		return selectedMode;
	}
	
	public int getSelectedInt() {
		return selectedInt;
	}
	
	public void edit() {
		
		String clearText = Jsoup.parse(text).text();
		
		if(text != null && (text.trim().equals("") || clearText.trim().equals("")))
			text = null;
		
		switch(selectedMode) {
		case MODE_KONTAKT:
			Datenbank.setKontakt(text);
			break;
			
		case MODE_IMPRESSUM:
			Datenbank.setImpressum(text);
			break;
			
		case MODE_COPYRIGHT:
			Datenbank.setBottomText(text);
			break;
			
		case MODE_ENTWICKLER:
			Datenbank.setEntwicklerInfo(text);
			break;
			
		case MODE_TICKER:
			Datenbank.setLiveTicker(clearText);
			break;
			
		case MODE_HISTORY:
			clearText = clearText.replace("\u00A0", "").trim();
			if(clearText.startsWith("http")) {
				Datenbank.setHistory(clearText);
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Ein Link",clearText) );
			} else {
				Datenbank.setHistory(text);
			}
			break;
			
		case MODE_EU_SA:
			
			clearText = clearText.replace("\u00A0", "").trim();
			if(clearText.startsWith("http")) {
				Datenbank.setEuSa(clearText);
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Ein Link",clearText) );
			} else {
				Datenbank.setEuSa(text);
			}
			break;
			
		case MODE_TERMINE:
			text = text.replace("&nbsp;", " ").replace("&quot;", "\"").replace("&amp;", "&");
			Datenbank.setTermine(text);
			break;
			
		case MODE_KOOP:
			Datenbank.setKoop(clearText);
			break;
			
		case MODE_KOOP_URL:
			Datenbank.setKoopURL(clearText);
			break;
			
		default:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte einen Text auswählen.") );
			return;
		}
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Änderungen gespeichert",selectedMode) );
		init();
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public String[] getModes() {
		return modes;
	}
	
	public boolean isSelected() {
		return selected;
	}

}
