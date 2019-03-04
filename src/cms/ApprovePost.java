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

import managedBean.SideMenu;
import mysql.Datenbank;
import objects.Beitrag;
import objects.Datei;
import objects.Nutzer;
import objects.Rubrik;
import sitzung.Sitzung;
import tools.ImageTools;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class ApprovePost {
	
	private Rubrik rubrik;
	
	//Zuordnung
	private Beitrag[] beitrag;
	private boolean selected = false; //die anderen Tabs werden erst angezeigt, wenn true
	private Beitrag selectedBeitrag;
	
	@ManagedProperty("#{zuordnungFinder}")
	private ZuordnungFinder zuordnungFinder;
	
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
	
	//SideMenu aktualisieren
	@ManagedProperty("#{sideMenu}")
	private SideMenu sideMenu;
	
	@PostConstruct
	void init() {
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer != null){
			fileHandler = new FileHandler(nutzer.getNutzer_id());
			hint = null;
			
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
			
			//Beiträge
			beitrag = Datenbank.getBeitragArray(false);
			selectedBeitrag = null;
			selected = false;
		}
	}
	
	public void selectBeitrag(Beitrag beitrag) {
		
		if(!Datenbank.existBeitrag(beitrag.getBeitrag_id())) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Beitrag wurde von "+beitrag.getNutzer().getVorname()+" "+beitrag.getNutzer().getNachname()+" bereits gelöscht.") );
			init();
			return;
		}
		
		fileHandler = new FileHandler(Sitzung.getNutzer().getNutzer_id());
		
		selectedBeitrag = beitrag;
		selected = true;
		
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
		fileHandler.setModified(false); //als Originalzustand bezeichnen
		updateHint();
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Beitrag ausgewählt",titel) );
	}
	
	public String finish() {
		//Error-Suche
		FacesContext context = FacesContext.getCurrentInstance();
		
		if(!Datenbank.existBeitrag(selectedBeitrag.getBeitrag_id())) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Beitrag wurde von "+selectedBeitrag.getNutzer().getVorname()+" "+selectedBeitrag.getNutzer().getNachname()+" bereits gelöscht.") );
			init();
			return null;
		}
		
		if(zuordnungFinder.getSelectedGruppe() == null) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Eine Zuordnung wurde nicht korrekt festgelegt.") );
			postManagerJS.setScrollUp(false);
			return null;
		} else if(titel == null || titel.trim().equals("")) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Leerer Titel") );
			postManagerJS.setScrollUp(false);
	        return null;
		}
		
		if(text != null && (text.trim().equals("") || Jsoup.parse(text).text().trim().equals("")))
			text = null;
		
		//IDs
		int beitrag_id = selectedBeitrag.getBeitrag_id();
		Nutzer me = Sitzung.getNutzer();
		
		if(me == null)
			return URLManager.LOGIN;
		
		//Genehmigen
		boolean edited = true;
		if(titel.equals(selectedBeitrag.getTitel()) 
				&& ((text == null && selectedBeitrag.getText() == null) || text.equals(selectedBeitrag.getText()))
				&& vorschauLength == selectedBeitrag.getVorschau()
				&& showAutor == selectedBeitrag.isShowAutor()
				&& layout.getBeitragLayout() == selectedBeitrag.getLayoutBeitrag()
				&& layout.getVorschauLayout() == selectedBeitrag.getLayoutVorschau()
				&& fileHandler.isModified() == false) {
			
			edited = false;
		}
		Datenbank.approveBeitrag(me, beitrag_id, zuordnungFinder.getSelectedGruppe().getGruppe_id(), titel, text, vorschauLength,
										layout.getBeitragLayout(), layout.getVorschauLayout(), showAutor, edited); //Beitrag genehmigen
		Datenbank.disconnectDateiBeitrag(beitrag_id); //alle alten Verknüpfungen löschen
		
		Datei[] added_files = fileHandler.getFilesAdded();
		
		if(added_files != null) {
			
			for(Datei datei : added_files) {
				
				Datenbank.connectDateiBeitrag(datei.getDatei_id(), beitrag_id); //alle Dateien mit diesem Beitrag verknüpfen
				
				if(datei.getSizeInt() > Datenbank.getPictureSize())
					ImageTools.resize(datei, Datei.DEFAULT_MAX_WIDTH, Datei.DEFAULT_MAX_HEIGHT);
			}
		}
		
		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Beitrag veröffentlicht",titel) );
		
		init(); //Reset
		Datenbank.cleanDateiBeitrag(); //Alle Dateien löschen, die keine NutzerID haben und mit keinem Beitrag verknüpft sind
		sideMenu.init();
		postManagerJS.setScrollUp(true);
		return null;
	}
	
	public void deleteBeitrag() {
		
		if(Datenbank.existBeitrag(selectedBeitrag.getBeitrag_id())) {
			Datenbank.deleteBeitrag(selectedBeitrag.getBeitrag_id()); //LÖSCHEN
		}
		Datenbank.cleanDateiBeitrag(); //Alle Dateien löschen, die keine NutzerID haben und mit keinem Beitrag verknüpft sind
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Beitrag gelöscht",selectedBeitrag.getTitel()) );
		init();
		sideMenu.init();
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
		return beitrag;
	}
	
	public int getBeitragLength() {
		return beitrag.length;
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
	
	//ManagedProperty
	
	public void setZuordnungFinder(ZuordnungFinder zuordnungFinder) {
		this.zuordnungFinder = zuordnungFinder;
	}
	
	public void setVorschauPost(VorschauPost vorschauPost) {
		this.vorschauPost = vorschauPost;
	}
	
	public void setSideMenu(SideMenu sideMenu) {
		this.sideMenu = sideMenu;
	}
	
	public void setPostManagerJS(PostManagerJS postManagerJS) {
		this.postManagerJS = postManagerJS;
	}

}
