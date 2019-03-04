package cms;

import java.util.Arrays;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.jsoup.Jsoup;

import objects.Beitrag;
import objects.Datei;
import objects.Nutzer;

@ViewScoped
@ManagedBean
public class VorschauPost {
	
	/*
	 * kümmert sich um die Anzeige der Vorschau, während man gerade den Beitrag bearbeitet/erstellt
	 * 
	 */
	
	private String titel, text, vorschauText;
	private boolean showAutor;
	private int layoutBeitrag, layoutVorschau;
	private FileHandler fileHandler;
	private Nutzer autor;
	private List<Datei> dateiList;
	private Datei[] bilder5;
	
	void load(String titel, String text, FileHandler fileHandler, boolean showAutor, int vorschau, int layoutBeitrag, int layoutVorschau, Nutzer autor) {
		
		if(text != null && (text.trim().equals("") || Jsoup.parse(text).text().trim().equals(""))) {
			text = null;
		}
		
		this.titel = titel;
		this.text = text;
		this.fileHandler = fileHandler;
		this.showAutor = showAutor;
		this.autor = autor;
		this.layoutBeitrag = layoutBeitrag;
		this.layoutVorschau = layoutVorschau;
		
		if(fileHandler.getFilesAdded() != null && fileHandler.getFilesAdded().length != 0) {
			dateiList = Arrays.asList(fileHandler.getFilesAdded());
			
			if(layoutVorschau == Beitrag.LAYOUT_VORSCHAU_BILDREIHE || layoutVorschau == Beitrag.LAYOUT_VORSCHAU_BILDREIHE_TEXT) {
				
				Datei[] bilder = fileHandler.getFilesAdded();
				
				int anzahl = bilder.length >= 5 ? 5 : bilder.length;
				
				bilder5 = new Datei[anzahl];
				for(int i = 0; i < bilder5.length; i++)
					bilder5[i] = bilder[i];
			}
		}
		
		if(text != null && !text.equals("")) {
			vorschauText = Jsoup.parse(text).text();
	    	if(vorschauText.length() > vorschau)
	    		vorschauText = vorschauText.substring(0, vorschau);
	    	vorschauText = vorschauText+"..."; //Text kürzen und "..." anhängen
		}
	}
	
	public String getTitel() {
		return titel;
	}
	
	public String getText() {
		return text;
	}
	
	public Datei[] getAddedFiles() {
		if(fileHandler == null)
			return null;
		return fileHandler.getFilesAdded();
	}
	
	public int getBilderAnzahl() {
		if(fileHandler != null && fileHandler.getBildReiheAdded() != null)
			return fileHandler.getFilesAdded().length;
		else
			return 0;
	}
	
	public Datei[] getBilder5() {
		return bilder5;
	}
	
	public Nutzer getAutor() {
		return autor;
	}
	
	public String getAutorString() {
		if(autor.getRang() == Nutzer.RANG_SCHUELER) {
			return autor.getVorname()+" "+autor.getNachname();
		} else {
			return autor.getFormalName();
		}
	}
	
	public boolean isShowAutor() {
		return showAutor;
	}
	
	public String getVorschauText() {
		return vorschauText;
	}
	
	public int getLayoutBeitrag() {
		return layoutBeitrag;
	}
	
	public int getLayoutVorschau() {
		return layoutVorschau;
	}
	
	public List<Datei> getDateiList() {
		return dateiList;
	}

}
