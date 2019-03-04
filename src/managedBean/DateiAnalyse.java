package managedBean;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import mysql.Datenbank;
import objects.Datei;
import tools.URLManager;

@ManagedBean
@ViewScoped
public class DateiAnalyse {
	
	/*
	 * URL-Analyse unter Nutzerverwaltung
	 */
	
	private String placeholder, placeholderNoWWW;
	private String url, id;
	private boolean noResult = false;
	private boolean found = false;
	
	//Ergebnis
	private Datei datei;
	
	@PostConstruct
	public void init() {
		placeholder = URLManager.getMainURL(null)+"/public/?file=";
		placeholderNoWWW = placeholder.replaceFirst("http://www.", "http://");
	}
	
	public void analyseURL() {
		
		if(url == null || url.equals("")) {
			noResult = true;
			return;
		}
		
		String temp = URLManager.convertUTF8(url, URLManager.CONVERT_FROM_URL);
		temp = temp.replace("http://www.", "http://");
		
		if(temp.startsWith(placeholderNoWWW)) {
			
			// 'http://....?file=' entfernen
			temp = temp.substring(placeholderNoWWW.length());
			
			// 3_Name in 3 und Name unterteilen: id_string
			String[] id_string = temp.split("_",2);
			
			if(id_string.length == 0) {
				noResult = true;
				return;
			}
			
			int id = -1;
			try {
				id = Integer.parseInt(id_string[0]);
			} catch (NumberFormatException e) {
				noResult = true;
				return;
			}
			
			datei = Datenbank.getDatei(id);
			if(datei == null || !datei.isPublicity() || !datei.getDatei_name().equals(id_string[1])) {
				noResult = true;
				return;
			}
			noResult = false;
			found = true;
		} else {
			noResult = true;
		}
	}
	
	public void analyseID() {
		if(id == null || id.equals("")) {
			noResult = true;
			return;
		}
		
		try {
			int idInt = Integer.parseInt(id);
			
			datei = Datenbank.getDatei(idInt);
			if(datei == null) {
				noResult = true;
				
				return;
			}
			
			noResult = false;
			found = true;
			
		} catch (NumberFormatException e) {
			noResult = true;
			return;
		}
		
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getPlaceholder() {
		return placeholder;
	}
	
	public boolean isNoResult() {
		return noResult;
	}
	
	public boolean isFound() {
		return found;
	}
	
	public Datei getDatei() {
		return datei;
	}
}

