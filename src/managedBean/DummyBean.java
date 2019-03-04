package managedBean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import objects.Nutzer;
import tools.Dummy;

@ViewScoped
@ManagedBean
public class DummyBean {
	
	final static private String[] RANG = {"Schüler/in","Lehrer/in","Gast"};
	
	private String createNutzer_anzahl, createNutzer_rang; //für Nutzererstellung
	private String projekt_anzahl, projekt_min_nutzer, projekt_max_nutzer; //für Projekte
	private String min_beitrag_pro_grp, max_beitrag_pro_grp; //für Beiträge
	
	@ManagedProperty("#{topMenu}")
	private TopMenu topMenu;
	
	//Neue Nutzer erstellen
	public String getCreate_nutzer_anzahl() {
		return createNutzer_anzahl;
	}
	
	public String getCreate_nutzer_rang() {
		return createNutzer_rang;
	}
	
	public void setCreate_nutzer_anzahl(String createNutzer_anzahl) {
		this.createNutzer_anzahl = createNutzer_anzahl;
	}
	
	public void setCreate_nutzer_rang(String createNutzer_rang) {
		this.createNutzer_rang = createNutzer_rang;
	}
	
	public String[] getRangList() {
		return RANG;
	}
	
	public void createNutzer() {
		int int_nutzer_rang = 0, int_nutzer_anzahl = 0;
		try {
			int_nutzer_anzahl = Integer.parseInt(createNutzer_anzahl);
		} catch(NumberFormatException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte Zahlen eingeben") );
			return;
		}
		
		if(createNutzer_rang.equals(RANG[1])) { //Lehrer
			int_nutzer_rang = Nutzer.RANG_LEHRER;
		} else if(createNutzer_rang.equals(RANG[0])){ //Schüler
			int_nutzer_rang = Nutzer.RANG_SCHUELER;
		} else if(createNutzer_rang.equals(RANG[2])) { //Gast
			int_nutzer_rang = Nutzer.RANG_GAST;
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte einen Rang angeben") );
			return;
		}
		
		boolean ok = Dummy.createNutzer(int_nutzer_anzahl, int_nutzer_rang);
		
		if(ok) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Nutzer erstellt","Anzahl: "+int_nutzer_anzahl+", Rang: "+int_nutzer_rang) );
			Konsole.antwort("Es wurden Nutzer erstellt, mit Anzahl: "+int_nutzer_anzahl+", Rang: "+int_nutzer_rang);
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "zu wenige Zugangscodes") );
		}
	}
	
	//Neue Projekte erstellen
	public String getProjekt_anzahl() {
		return projekt_anzahl;
	}
	
	public void setProjekt_anzahl(String projekt_anzahl) {
		this.projekt_anzahl = projekt_anzahl;
	}
	
	public String getProjekt_max_nutzer() {
		return projekt_max_nutzer;
	}
	
	public void setProjekt_max_nutzer(String projekt_max_nutzer) {
		this.projekt_max_nutzer = projekt_max_nutzer;
	}
	
	public String getProjekt_min_nutzer() {
		return projekt_min_nutzer;
	}
	
	public void setProjekt_min_nutzer(String projekt_min_nutzer) {
		this.projekt_min_nutzer = projekt_min_nutzer;
	}
	
	public void createProjekt() {
		int int_projekt_anzahl = 0, int_min_nutzer = 0, int_max_nutzer = 0;
		try {
			int_projekt_anzahl = Integer.parseInt(projekt_anzahl);
			int_max_nutzer = Integer.parseInt(projekt_max_nutzer);
			int_min_nutzer = Integer.parseInt(projekt_min_nutzer);
		} catch(NumberFormatException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte Zahlen eingeben") );
			return;
		}
		
		if(int_min_nutzer > int_max_nutzer) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "MAX muss größer sein als MIN") );
			return;
		}
		
		int antwort = Dummy.createProjekt(int_projekt_anzahl, int_min_nutzer, int_max_nutzer);
		
		switch(antwort) {
		case 0:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Projekte erstellt","Anzahl: "+int_projekt_anzahl+", "+int_min_nutzer+" - "+int_max_nutzer+" pro Projekt") );
			Konsole.antwort("Es wurden "+ projekt_anzahl
					+ " Projekte erstellt, mit min. Schüler " + projekt_min_nutzer
					+ " und max. Schüler " + projekt_max_nutzer);
			break;
		case 1:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Mindestens 2 Nutzer benötigt ein Projekt") );
			break;
		case 2:
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Zu wenige Schüler registriert") );
			break;
		}
		
	}
		
	//Gruppen und Beiträge erstellen
	public String getMin_beitrag_pro_grp() {
		return min_beitrag_pro_grp;
	}
	
	public void setMin_beitrag_pro_grp(String min_beitrag_pro_grp) {
		this.min_beitrag_pro_grp = min_beitrag_pro_grp;
	}
	
	public String getMax_beitrag_pro_grp() {
		return max_beitrag_pro_grp;
	}
	
	public void setMax_beitrag_pro_grp(String max_beitrag_pro_grp) {
		this.max_beitrag_pro_grp = max_beitrag_pro_grp;
	}
	
	public void createPost() {
		int int_min_beitrag_pro_grp = 0, int_max_beitrag_pro_grp = 0;
		try {
			int_min_beitrag_pro_grp = Integer.parseInt(min_beitrag_pro_grp);
			int_max_beitrag_pro_grp = Integer.parseInt(max_beitrag_pro_grp);
		} catch(NumberFormatException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte Zahlen eingeben") );
			return;
		}
		Dummy.createBeitrag(int_min_beitrag_pro_grp, int_max_beitrag_pro_grp);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Beiträge erstellt","Anzahl: "+int_min_beitrag_pro_grp+" - "+int_max_beitrag_pro_grp+" pro Untergruppe") );
		Konsole.antwort("Es wurden Gruppen und Beiträge erstellt");
		topMenu.init(); //Aktualisieren
	}
	
	//ManagedProperty
	public void setTopMenu(TopMenu topMenu) {
		this.topMenu = topMenu;
	}
}
