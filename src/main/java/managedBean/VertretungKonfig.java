package managedBean;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import mysql.Datenbank;
import objects.Datei;
import objects.Nutzer;
import sitzung.Sitzung;

@ViewScoped
@ManagedBean
public class VertretungKonfig {
	
	final private static String[] WOCHENTAGE = {"Montag","Dienstag","Mittwoch","Donnerstag","Freitag","Samstag","Sonntag"};
	final private static String[] WOCHENTAG_KURZ = {"Mo","Di","Mi","Do","Fr","Sa","So"};
	final private static String[] MONATE = {"Januar","Februar","März","April","Mai","Juni","Juli","August","September","Oktober","November","Dezember"};
	
	private boolean allowed;
	private String newInfo;
	private String lastModified;
	private boolean suggested;
	
	@PostConstruct
	private void init() {
		
		Nutzer nutzer = Sitzung.getNutzer();
		
		if(nutzer != null) {
			//normaler Nutzer
			allowed = nutzer.getRang() == Nutzer.RANG_ADMIN || Datenbank.isVertretung(nutzer.getNutzer_id());
			newInfo = Datenbank.getVertretungInfo();
			lastModified = Datenbank.getVertretungLastModified();
			suggested = false;
		} else {
			allowed = false;
		}
	}
	
	public boolean isAllowed() {
		return allowed;
	}
	
	public void upload(FileUploadEvent event) {
		
		UploadedFile file = event.getFile();
		
		if(file == null || file.getFileName().equals("")) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Datei auswählen.") );
			return;
		}
		
		String filename = file.getFileName().toLowerCase();
		if(!filename.endsWith(".pdf")) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Nur PDF-Dateien erlaubt.") );
			return;
		}
    	
        try {
			Datenbank.addFileVertretung(file);
			lastModified = Datenbank.getVertretungLastModified();
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Sitzung abgelaufen. Bitte neu anmelden.") );
			return;
		}
        
        java.util.logging.Logger.getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.SEVERE);
        
        String vorschlagAusAnalyse = null;
        try {
			PDDocument document = PDDocument.load(new File(Datenbank.getVertretung()));
			PDFTextStripper pdfStripper = new PDFTextStripper();
			String text = pdfStripper.getText(document);
			
			String[] splitted = text.split(System.lineSeparator());
			
			
			for(String line : splitted) {
				String wochentag = null; //gekürzte Version
				String monat = null; //0 = Januar, 1 Februar, ...
				String tag = null;
				String jahr = null;
				
				for(int i = 0; i < WOCHENTAGE.length; i++) {
					
					int occurenceAt = line.indexOf(WOCHENTAGE[i]);
					if(occurenceAt != -1) {
						//Wochentag gefunden
						System.out.println("[Vertretungsplan-Analyse] "+WOCHENTAGE[i]);
						wochentag = WOCHENTAG_KURZ[i];
						
						for(int x = 0; x < MONATE.length; x++) {
							
							int occurenceMonatAt = line.indexOf(MONATE[x]);
							if(occurenceMonatAt != -1) {
								
								//Monat gefunden
								System.out.println("[Vertretungsplan-Analyse] "+MONATE[x]);
								
								monat = x >= 9 ? String.valueOf(x+1) : "0"+(x+1);
								
								int indexTagBegin = occurenceAt+WOCHENTAGE[i].length()+2;
								int indexTagEnd = occurenceMonatAt-2;
								System.out.println("[Vertretungsplan-Analyse] tag: "+indexTagBegin+" bis "+indexTagEnd);
								
								if(indexTagEnd > indexTagBegin && indexTagEnd < line.length())
									tag = line.substring(indexTagBegin,indexTagEnd);
								
								if(tag.length() == 1)
									tag = "0"+tag;
								
								int indexJahrBegin = occurenceMonatAt+MONATE[x].length()+1;
								int indexJahrEnd = occurenceMonatAt+MONATE[x].length()+1+4;
								System.out.println("[Vertretungsplan-Analyse] jahr: "+indexJahrBegin+" bis "+indexJahrEnd);
								
								if(tag != null && indexJahrEnd > indexJahrBegin && indexJahrEnd < line.length())
									jahr = line.substring(indexJahrBegin,indexJahrEnd);
								
								if(jahr != null) {
									System.out.println("\""+line+"\" >> "+wochentag+", "+tag+"."+monat+"."+jahr);
									vorschlagAusAnalyse = wochentag+", "+tag+"."+monat+"."+jahr;
								}
								
								break;
							}
						}
						break;
					}
				}
				
				if(vorschlagAusAnalyse != null)
					break;
			}
			
			
			
			document.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Hochgeladen",Datei.DATEI_VERTRETUNG) );
        
        if(vorschlagAusAnalyse == null) {
			suggested = false;
		} else {
			suggested = true;
			
			newInfo = vorschlagAusAnalyse;
			updateInfo();
		}
    }
	
	public void delete() {
		Datenbank.deleteVertretung();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Gelöscht",Datei.DATEI_VERTRETUNG) );
	}
	
	public String getNewInfo() {
		return newInfo;
	}
	
	public String getLastModified() {
		return lastModified;
	}
	
	public void setNewInfo(String newInfo) {
		this.newInfo = newInfo;
	}
	
	public boolean isSuggested() {
		return suggested;
	}
	
	public void updateInfo() {
		Datenbank.setVertretungInfo(newInfo);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Aktualisiert",newInfo) );
	}
	
}


