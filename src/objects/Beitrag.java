package objects;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;

public class Beitrag implements Serializable {
	private static final long serialVersionUID = -5337650248771635000L;
	
	public final static int STANDARD_VORSCHAU_LENGTH = 200;
	public final static boolean STANDARD_SHOW_AUTOR = false;
	
	public final static int LAYOUT_BEITRAG_STANDARD = 0, LAYOUT_BEITRAG_COLLAGE = 1, LAYOUT_BEITRAG_DIASHOW = 2;
	public final static int LAYOUT_VORSCHAU_STANDARD = 0, LAYOUT_VORSCHAU_BILDREIHE = 1, LAYOUT_VORSCHAU_BILDREIHE_TEXT = 2;
	
	private int beitrag_id;
	private String titel;
	private String text, plainText, vorschauText;
	private Nutzer nutzer;
	private String datum;
	private Datei[] bilder, bilder5;
	private List<Datei> dateiList;
	private Gruppe gruppe; //verweist auf Sammlung
	
	private int vorschau;
	private boolean showAutor;
	private boolean hideUntergruppe = true; //nur wichtig bei Aktuelles
	private boolean password = false;
	
	private int layoutBeitrag, layoutVorschau;
	
	//search
	private int count;
	private float percentage;
	private String percentageString;
	
	//EditPost: Anzeige in der Tabelle der Beiträge
	final static private String STYLE_PASSWORD = "background:#eae5c8", STYLE_NO_PASSWORD="";
	
	//Konstruktor für MySQL.getBeitragArray
	public Beitrag(int id, Gruppe gruppe, String titel, String text, Nutzer nutzer, String datum, Datei[] bilder,
					int vorschau, int layoutBeitrag, int layoutVorschau, boolean showAutor, boolean password, boolean preCalcVorschau) {
		this.beitrag_id = id;
		this.gruppe = gruppe;
		this.titel = titel;
		this.text = text;
		this.nutzer = nutzer;
		this.datum = datum;
		this.bilder = bilder;
		this.vorschau = vorschau;
		this.layoutBeitrag = layoutBeitrag;
		this.layoutVorschau = layoutVorschau;
		this.showAutor = showAutor;
		this.password = password;
		
		if(preCalcVorschau) {
			
			if(text != null){
				plainText = Jsoup.parse(text).text(); //HTML in Text
				
				
	        	vorschauText = plainText;
	        	if(vorschauText.length() > vorschau)
	        		vorschauText = vorschauText.substring(0, vorschau);
	        	vorschauText = vorschauText+"..."; //Text kürzen und "..." anhängen
			}
			
			if(bilder != null && bilder.length != 0) {
				//Vorschaubilder oder Galerie
				dateiList = Arrays.asList(bilder);
				
				if(layoutVorschau == LAYOUT_VORSCHAU_BILDREIHE || layoutVorschau == LAYOUT_VORSCHAU_BILDREIHE_TEXT) {
					
					int anzahl = bilder.length >= 5 ? 5 : bilder.length;
					
					bilder5 = new Datei[anzahl];
					for(int i = 0; i < bilder5.length; i++)
						bilder5[i] = bilder[i];
				}
			}
		}
	}
	
	//Konstruktor für Dummy
	public Beitrag(String titel, String text) {
		this.titel = titel;
		this.text = text;
	}
	
	public int getBeitrag_id() {
		return beitrag_id;
	}
	
	public String getTitel() {
		return titel;
	}
	
	public String getText() {
		return text;
	}
	
	public Nutzer getNutzer() {
		return nutzer;
	}
	
	public String getNutzerString() {
		if(nutzer.getRang() == Nutzer.RANG_SCHUELER) {
			return nutzer.getVorname()+" "+nutzer.getNachname();
		} else {
			return nutzer.getFormalName();
		}
	}
	
	public String getDatum() {
		return datum;
	}
	
	public Datei[] getBilder() {
		return bilder;
	}
	
	public Datei[] getBilder5() {
		return bilder5;
	}
	
	public int getVorschau() {
		return vorschau;
	}
	
	public int getLayoutBeitrag() {
		return layoutBeitrag;
	}
	
	public int getLayoutVorschau() {
		return layoutVorschau;
	}
	
	public boolean isShowAutor() {
		return showAutor;
	}
	
	public List<Datei> getDateiList() {
		return dateiList;
	}
	
	public Gruppe getGruppe() {
		return gruppe;
	}
	
	public boolean isHideUntergruppe() {
		return hideUntergruppe;
	}
	
	public void setHideUntergruppe(boolean hideUntergruppe) {
		this.hideUntergruppe = hideUntergruppe;
	}
	
	//Password
	
	public boolean isPassword() {
		return password;
	}
	
	public String getPasswordSymbol() {
		if(password)
			return "fa fa-lock";
    	else
    		return "fa fa-unlock-alt";
	}
	
	//editpost
	public String getStylePassword() {
		if(password)
			return STYLE_PASSWORD;
		else
			return STYLE_NO_PASSWORD;
	}
	
	//Vorschau
	
	public void setVorschauText(String vorschauText) {
		this.vorschauText = vorschauText;
	}
	
	public String getVorschauText() {
		return vorschauText;
	}
	
	public int getBilderAnzahl() {
		if(bilder != null)
			return bilder.length;
		else
			return 0;
	}
	
	//ab hier: hauptsächlich Suche
	
	public String getPlainText() {
		return plainText;
	}
	
	public void setPlainText(String plainText) {
		this.plainText = plainText;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public float getPercentage() {
		return percentage;
	}
	
	public void setPercentage(float percentage) {
		this.percentage = percentage;
	}
	
	public void setPercentageString(String percentageString) {
		this.percentageString = percentageString;
	}
	
	public String getPercentageString() {
		return percentageString;
	}
	
	public void highlightSuche(String[] keyWord, int[] keyWordLength) {
		
		titel = titel.toLowerCase();
		
		if(plainText != null) {
			String plainText = this.plainText.toLowerCase();
			vorschauText = getSnippetKeyWord(plainText, keyWord[0], keyWordLength[0], vorschau/2);
		}
		
		for(int i = 0; i < keyWord.length; i++) {
			if(vorschauText != null)
				vorschauText = vorschauText.replaceAll(keyWord[i], "<span style=\"color:green;font-weight:bold;\">"+keyWord[i]+"</span>");
			titel = titel.replaceAll(keyWord[i], "<span style=\"color:green;font-weight:bold;\">"+keyWord[i]+"</span>");
		}
	}
	
	private String getSnippetKeyWord(String text, String keyWord, int keyWordLength, int radius) {
		
		int lastIndex = text.indexOf(keyWord);
		
		if(lastIndex == -1) {
			return vorschauText;
		}
		
		//von -radius bis +radius
		
		if(lastIndex - radius >= 0) {
			if(lastIndex+keyWordLength+radius < text.length()) {
				//nach links und nach rechts gültig
				return "..."+text.substring(lastIndex-radius, lastIndex+keyWordLength+radius)+"...";
			} else {
				//nach links gültig
				return "..."+text.substring(lastIndex-radius);
			}
		} else {
			if(lastIndex+keyWordLength+radius < text.length()) {
				//nach rechts gültig
				return text.substring(0, lastIndex+keyWordLength+radius)+"...";
			} else {
				//beide seiten eingegrentzt
				return text;
			}
		}
	}
}
