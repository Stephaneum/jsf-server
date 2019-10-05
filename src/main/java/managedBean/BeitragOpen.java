package managedBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.Beitrag;
import objects.Datei;
import objects.Gruppe;
import objects.Nutzer;
import sitzung.Sitzung;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class BeitragOpen {
	
	/*
	 * wenn ein Beitrag geöffnet wird (beitrag.xhtml)
	 * 
	 */
	
	private Beitrag openBeitrag;
	private Datei bild; //ggf. Bild der Rubrik
	private String password;
	private boolean passwordOK;
	private boolean fromSearchResults;
	
	@PostConstruct
    public void init() {
		
		fromSearchResults = false;
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null)
			nutzer = Sitzung.createNutzerGuest();
		
		//Beitrag aus dem Parameter "id" entnehmen
		try {
			Map<String, String> parameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			String paramID = parameterMap.get("id");
			String paramSearch = parameterMap.get("suche");
			
			if(paramID != null) {
				
				int id = Integer.parseInt(paramID);
				
				openBeitrag = Datenbank.getBeitrag(id,true);
				
				if(openBeitrag != null && nutzer.getRang() != Nutzer.RANG_ADMIN && !Datenbank.isGruppePublic(openBeitrag.getGruppe().getGruppe_id())) {
					//Gruppe des Beitrages gehört zu einer nicht öffentlichen Rubrik
					
					//gehört diese Rubrik dem Nutzer?
					Gruppe[] gruppeRubrik = Datenbank.getGruppeArrayRubrik(Sitzung.getNutzer().getNutzer_id(), true);
					if(!Gruppe.contains(gruppeRubrik, openBeitrag.getGruppe().getGruppe_id())) {
						openBeitrag = null;
					}

				}
				
				if(openBeitrag == null) {
					
					//Fehlermeldung als Beitrag anzeigen
					openBeitrag = new Beitrag(id, null, "Ein Fehler ist aufgetreten",
													"Ich finde diesen Beitrag nicht...<br/>Es kann sein, dass es unter einer anderen Adresse erneut veröffentlicht wurde.<br/>Oder dieser Beitrag ist bereits erstellt - nur noch nicht öffentlich.<br/>",
													new Nutzer(-1,"LG System","",(byte) 2), getDate(), null, 0, Beitrag.LAYOUT_BEITRAG_STANDARD, Beitrag.LAYOUT_VORSCHAU_STANDARD, true, false, false);
				} else {
					
					//ZWEIG, WO BIS JETZT ALLES OK IST
					
					bild = Datenbank.getRubrikBild(openBeitrag.getGruppe().getGruppe_id());
					
					//Passwort
					if(openBeitrag.isPassword() && nutzer.getUnlocker().isBeitragUnlocked(openBeitrag.getBeitrag_id())) {
						passwordOK = true;
					}
					
					if(paramSearch != null && paramSearch.equals("1")) {
						fromSearchResults = true;
					}
				}
			} else {
				openBeitrag = new Beitrag(-1, null, "Ein Fehler ist aufgetreten",
						"Irgendetwas stimmt mit der URL nicht...<br/>",
						new Nutzer(-1,"LG System","",(byte) 2), getDate(), null, 0, Beitrag.LAYOUT_BEITRAG_STANDARD, Beitrag.LAYOUT_VORSCHAU_STANDARD, true, false, false);
			}
		} catch (NumberFormatException e) {
			openBeitrag = new Beitrag(-1, null, "Ein Fehler ist aufgetreten",
					"Irgendetwas stimmt mit der URL nicht...<br/>",
					new Nutzer(-1,"LG System","",(byte) 2), getDate(), null, 0, Beitrag.LAYOUT_BEITRAG_STANDARD, Beitrag.LAYOUT_VORSCHAU_STANDARD, true, false, false);
			return;
		}
	}
	
	public Beitrag getOpenBeitrag() {
		return openBeitrag;
	}
	
	public Datei getBild() {
		return bild;
	}
	
	public boolean isFromSearchResult() {
		return fromSearchResults;
	}
	
	private static String getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");    
		Date resultdate = new Date(System.currentTimeMillis());
		return sdf.format(resultdate);
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void checkPassword() {
		passwordOK = Datenbank.checkPasswordBeitrag(openBeitrag.getBeitrag_id(), password);
		if(passwordOK) {
			Nutzer nutzer = Sitzung.getNutzer();
			if(nutzer == null)
				nutzer = Sitzung.createNutzerGuest();
			nutzer.getUnlocker().unlockBeitrag(openBeitrag.getBeitrag_id());
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Zugriff genehmigt",openBeitrag.getTitel()) );
		} else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Zugriff verwehrt", "falsches Passwort") );
	}
	
	public boolean isPasswordOK() {
		return passwordOK;
	}
	
	public void resetPasswordOK() {
		password = null;
		passwordOK = false;
	}
	
	//zurück auf die Homepage, und UntergruppeID aktualisieren
	public String back() {
		if(openBeitrag.getGruppe() == null) //keine Ahnung welcher Gruppe der Beitrag angehört
			return URLManager.HOME+"?faces-redirect=true";
		else
			return URLManager.HOME+"?faces-redirect=true&id=" + openBeitrag.getGruppe().getGruppe_id(); //zurück zur Gruppe (= Sammlung)
	}

}
