package cms;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.jsoup.Jsoup;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import mysql.Datenbank;
import objects.Beitrag;
import objects.Datei;
import objects.Nutzer;
import objects.Rubrik;
import sitzung.Sitzung;
import tools.ImageTools;

@ViewScoped
@ManagedBean
public class EditPost {
	
	private Rubrik rubrik;
	
	//Zuordnung
	private Beitrag[] beitrag;
	private boolean selected;
	private Beitrag selectedBeitrag;
	
	@ManagedProperty("#{zuordnungFinder}")
	private ZuordnungFinder zuordnungFinder;
	private int currentGruppeID;
	
	//Text
	private String titel, text;
	
	//Bilder
	private FileHandler fileHandler;
	private String hint;
	
	//Eigenschaften
	private int vorschauLength;
	private boolean showAutor;
	
	//Layout
	private Layout layout;

	//Info für Javascript: Scroll to top
	@ManagedProperty("#{postManagerJS}")
	private PostManagerJS postManagerJS;
	
	//Vorschau
	@ManagedProperty("#{vorschauPost}")
	private VorschauPost vorschauPost;
	
	//Password
	private Beitrag selectedBeitragPW;
	private String password;
	
	@PostConstruct
	void init() {
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer != null){
			fileHandler = new FileHandler(nutzer.getNutzer_id());
			
			rubrik = Datenbank.getRubrik(nutzer.getNutzer_id());
			
			if(rubrik != null)
				zuordnungFinder.init(nutzer.getNutzer_id());
			else
				zuordnungFinder.init(); //Admin, Beitrag-Manager
			titel = null;
			text = null;
			vorschauLength = Beitrag.STANDARD_VORSCHAU_LENGTH;
			showAutor = Beitrag.STANDARD_SHOW_AUTOR;
			layout = new Layout();
			
			currentGruppeID = -1;
			beitrag = null;
			selected = false;
			selectedBeitrag = null;
		}
		
	}
	
	public void selectBeitrag(Beitrag beitrag) {
		fileHandler = new FileHandler(Sitzung.getNutzer().getNutzer_id());
		
		selected = true;
		selectedBeitrag = beitrag;
		
		titel = beitrag.getTitel();
		text = beitrag.getText();
		vorschauLength = beitrag.getVorschau();
		showAutor = beitrag.isShowAutor();
		layout = new Layout(beitrag.getLayoutBeitrag(), beitrag.getLayoutVorschau());
		Datei[] bilder = beitrag.getBilder();
		for(int i = 0; i < bilder.length; i++) {
			fileHandler.addFile(bilder[i]);
		}
		fileHandler.reload();
		
		updateHint();
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Beitrag ausgewählt",titel) );
	}
	
	public void deleteBeitrag(Beitrag beitrag) {
		selected = false;
		selectedBeitrag = null;
		Datenbank.deleteBeitrag(beitrag.getBeitrag_id()); //LÖSCHEN
		this.beitrag = Datenbank.getBeitragArray(currentGruppeID, -1); //Beitrag-Tabelle aktualisieren
		Datenbank.cleanDateiBeitrag(); //Alle Dateien löschen, die keine NutzerID haben und mit keinem Beitrag verknüpft sind
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Beitrag gelöscht",beitrag.getTitel()) );
	}
	
	public void edit(boolean updateDatum) {
		
		FacesContext context = FacesContext.getCurrentInstance();
		if(titel == null || titel.trim().equals("")) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Leerer Titel") );
			postManagerJS.setScrollUp(false);
	        return;
		}
		
		if(text != null && (text.trim().equals("") || Jsoup.parse(text).text().trim().equals("")))
			text = null;
		
		int beitrag_id = selectedBeitrag.getBeitrag_id();
		int nutzer_id = Sitzung.getNutzer().getNutzer_id();
		
		Datenbank.editBeitrag(nutzer_id, beitrag_id, titel, text, vorschauLength, layout.getBeitragLayout(), layout.getVorschauLayout(), showAutor, updateDatum); //Titel und Text aktualisieren
		Datenbank.disconnectDateiBeitrag(beitrag_id); //alle alten Verknüpfungen löschen
		
		Datei[] added_files = fileHandler.getFilesAdded();
		if(added_files != null) {
			
			for(Datei datei : added_files) {
				
				Datenbank.connectDateiBeitrag(datei.getDatei_id(), beitrag_id); //alle Dateien mit diesem Beitrag verknüpfen
				
				if(datei.getSizeInt() > Datenbank.getPictureSize())
					ImageTools.resize(datei, Datei.DEFAULT_MAX_WIDTH, Datei.DEFAULT_MAX_HEIGHT);
			}
		}
		
		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Änderungen gespeichert",titel) );
		
		init(); //Reset
		Datenbank.cleanDateiBeitrag(); //Alle Dateien löschen, die keine NutzerID haben und mit keinem Beitrag verknüpft sind
		postManagerJS.setScrollUp(true);
	}
	
	private void updateHint() {
		hint = null;
		
		//Dateigröße
		Datei[] files = fileHandler.getFilesAdded();
		
		if(files != null) {
			for(int i = 0; i < files.length; i++) {
				if(files[i].getSizeInt() > Datenbank.getPictureSize()) {
					hint = "Hinweis: Mindestens ein Bild ist groß (> "+Datei.convertSizeToString(Datenbank.getPictureSize())+"). Zu große Bilder werden automatisch verkleinert.";
					return;
				}
			}
		}
	}
	
	public void upload(FileUploadEvent e) {
    	UploadedFile file = e.getFile();
        Datei uploaded;
		try {
			uploaded = Datenbank.addFile(file, Datei.ORDNER_HOME, -1, Datei.Mode.PRIVATSPEICHER);
		} catch (Exception e1) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Sitzung abgelaufen. Bitte neu anmelden.") );
			return;
		}
		
		if(uploaded == null) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Nicht genügend Speicherplatz") );
		} else {
			fileHandler.addFile(uploaded);
	        fileHandler.reload();
	        updateHint();
	        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"hochgeladen und angehängt",file.getFileName()) );
		}
    }
	
	public void addFile(Datei datei) {
		fileHandler.addFile(datei);
		fileHandler.reload();
		updateHint();
	}
	
	public void removeFile(Datei datei) {
		fileHandler.removeFile(datei);
		fileHandler.reload();
		updateHint();
	}
	
	public void prepareVorschau() {
		vorschauPost.load(titel, text, fileHandler, showAutor, vorschauLength, layout.getBeitragLayout(), layout.getVorschauLayout(), selectedBeitrag.getNutzer());
	}
	
	public void setTitel(String titel) {
		this.titel = titel;
	}
	
	public String getTitel() {
		return titel;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public FileHandler getFileHandler() {
		return fileHandler;
	}
	
	public Beitrag[] getBeitrag() {
		
		if(zuordnungFinder.getSelectedGruppe() != null && currentGruppeID != zuordnungFinder.getSelectedGruppe().getGruppe_id()) {
			//update
			
			currentGruppeID = zuordnungFinder.getSelectedGruppe().getGruppe_id();
			
			if(currentGruppeID != -1) {
				beitrag = Datenbank.getBeitragArray(currentGruppeID, -1);
			} else {
				beitrag = null;
			}
			selectedBeitrag = null;
			selected = false;
			titel = null;
			text = null;
		}
		return beitrag;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public int getVorschauLength() {
		return vorschauLength;
	}
	
	public void setVorschauLength(int vorschauLength) {
		this.vorschauLength = vorschauLength;
	}
	
	public boolean isShowAutor() {
		return showAutor;
	}
	
	public void setShowAutor(boolean showAutor) {
		this.showAutor = showAutor;
	}
	
	public String getHint() {
		return hint;
	}
	
	public Layout getLayout() {
		return layout;
	}
	
	//Password
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void updatePassword() {
		Datenbank.setPasswordBeitrag(selectedBeitragPW.getBeitrag_id(), password);
		this.beitrag = Datenbank.getBeitragArray(currentGruppeID, -1); //Beitrag-Tabelle aktualisieren
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Beitrag passwortgeschützt",selectedBeitragPW.getTitel()) );
	}
	
	public void togglePassword(Beitrag beitrag) {
		if(beitrag.isPassword()) {
			Datenbank.setPasswordBeitrag(beitrag.getBeitrag_id(), null);
			this.beitrag = Datenbank.getBeitragArray(currentGruppeID, -1); //Beitrag-Tabelle aktualisieren
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Beitrag öffentlich",beitrag.getTitel()) );
		} else {
			//passwort festlegen
			password = null;
			selectedBeitragPW = beitrag;
		}
	}
	
	//ManagedProperty
	
	public void setZuordnungFinder(ZuordnungFinder zuordnungFinder) {
		this.zuordnungFinder = zuordnungFinder;
	}
	
	public void setVorschauPost(VorschauPost vorschauPost) {
		this.vorschauPost = vorschauPost;
	}
	
	public void setPostManagerJS(PostManagerJS postManagerJS) {
		this.postManagerJS = postManagerJS;
	}

}
