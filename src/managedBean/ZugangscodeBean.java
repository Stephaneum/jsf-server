package managedBean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.Nutzer;
import tools.Zugangscodes;

@ViewScoped
@ManagedBean
public class ZugangscodeBean {
	
	//für die Seite konfig_admin_zugangscode.xhtml
	
	private String[][] mergedZugangscode;
	private int anzahlLehrer, anzahlSchueler, anzahlGast;
	
	final static private String[] RANG = {"Lehrer/in","Schüler/in","Gast"};
	private String anzahl, rang;
	
	//Text-Modus
	private boolean textMode = false;
	private String codeText;
	
	public ZugangscodeBean() {
		update();
	}
	
	private void update() {
		if(Datenbank.existDatabase() == true) {
			String[] zugangscode_gast = Datenbank.getAllZugangscodes(2, false);
			String[] zugangscode_lehrer = Datenbank.getAllZugangscodes(1,false);
			String[] zugangscode_schueler = Datenbank.getAllZugangscodes(0,false);
			
			anzahlLehrer = zugangscode_lehrer.length;
			anzahlSchueler = zugangscode_schueler.length;
			anzahlGast = zugangscode_gast.length;
			
			int length = zugangscode_schueler.length;
			if(zugangscode_lehrer.length > length)
				length = zugangscode_lehrer.length;
			if(zugangscode_gast.length > length)
				length = zugangscode_gast.length;
			
			mergedZugangscode = new String[length][3];
			for(int i = 0; i < length; i++) {
				
				if(i < zugangscode_lehrer.length)
					mergedZugangscode[i][0] = zugangscode_lehrer[i];
				else
					mergedZugangscode[i][0] = null;
				
				if(i < zugangscode_schueler.length)
					mergedZugangscode[i][1] = zugangscode_schueler[i];
				else
					mergedZugangscode[i][1] = null;
				
				if(i < zugangscode_gast.length)
					mergedZugangscode[i][2] = zugangscode_gast[i];
				else
					mergedZugangscode[i][2] = null;
			}
			
			if(textMode)
				generateText();
		}
	}
	
	public void setTextMode(boolean textMode) {
		this.textMode = textMode;
		
		if(textMode) {
			generateText();
		} else {
			codeText = null;
		}
	}
	
	public boolean isTextMode() {
		return textMode;
	}
	
	private void generateText() {
		StringBuilder builder = new StringBuilder();
		
		for(int r = 0; r < RANG.length; r++) {
			builder.append("--");
			builder.append(RANG[r]);
			builder.append("--\n");
			for(int i = 0; i < mergedZugangscode.length; i++) {
				
				if(mergedZugangscode[i][r] == null)
					break;
				
				builder.append(mergedZugangscode[i][r]);
				builder.append("\n");
			}
			builder.append("\n");
		}
		
		codeText = builder.toString();
	}
	
	//Text-Mode
	
	public String getCodeText() {
		return codeText;
	}
	
	public String[][] getMergedZugangscode() {
		return mergedZugangscode;
	}
	
	public int getAnzahlLehrer() {
		return anzahlLehrer;
	}
	
	public int getAnzahlSchueler() {
		return anzahlSchueler;
	}
	
	public int getAnzahlGast() {
		return anzahlGast;
	}
	
	public void generate() {
		
		int anzahlInt;
		try {
			anzahlInt = Integer.parseInt(anzahl);
		} catch(NumberFormatException e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte Zahlen eingeben") );
			return;
		}
		
		int rangInt;
		if(rang.equals(RANG[0])) //Lehrer
			rangInt = Nutzer.RANG_LEHRER;
		else if(rang.equals(RANG[1])) //Schüler
			rangInt = Nutzer.RANG_SCHUELER;
		else if(rang.equals(RANG[2])) //gast
			rangInt = Nutzer.RANG_GAST;
		else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte Rang eingeben") );
			return;
		}
		
		Datenbank.createZugangscodes(Zugangscodes.STANDARD_LENGTH, anzahlInt, rangInt);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Zugangscodes erstellt","Anzahl: "+anzahlInt+", Rang: "+rang));
		update();
	}
	
	public String[] getRangList() {
		return RANG;
	}
	
	public String getAnzahl() {
		return anzahl;
	}
	
	public void setRang(String rang) {
		this.rang = rang;
	}
	
	public String getRang() {
		return rang;
	}
	
	public void setAnzahl(String anzahl) {
		this.anzahl = anzahl;
	}
	
	public void deleteZugangscode(String code) {
		Datenbank.deleteZugangscode(code);
		update();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Zugangscode gelöscht",code));
	}

}
