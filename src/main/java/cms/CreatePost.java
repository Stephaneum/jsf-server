package cms;

import java.io.IOException;

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
import tools.URLManager;

@ViewScoped
@ManagedBean
public class CreatePost {
	
	//Diese Klasse teilen sich Admins und Nicht-Admins
	private boolean adminMode = false;
	private boolean realAdmin = false; //Spezielle Texte, sollte nur der Admin verändern können
	private Rubrik rubrik;
	
	//Zuordnung
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
	
	public CreatePost() {
		Nutzer nutzer = Sitzung.getNutzer();
		//einmalige Setzung des adminMode-Wertes
		
		if(nutzer != null) {
			int rang = nutzer.getRang();
			if(rang == Nutzer.RANG_ADMIN) {
				adminMode = true;
				realAdmin = true;
			} else {
				adminMode = Datenbank.isBeitragManager(nutzer.getNutzer_id());
			}
			rubrik = Datenbank.getRubrik(nutzer.getNutzer_id());
		}
	}
	
	@PostConstruct
	void init() {
		titel = null;
		text = null;
		hint = null;
		
		if(rubrik != null) {
			Nutzer nutzer = Sitzung.getNutzer();
			if(nutzer != null)
				zuordnungFinder.init(nutzer.getNutzer_id());
		} else
			zuordnungFinder.init(); //Admin, Beitrag-Manager
		vorschauLength = Beitrag.STANDARD_VORSCHAU_LENGTH;
		showAutor = Beitrag.STANDARD_SHOW_AUTOR;
		fileHandler = new FileHandler(Sitzung.getNutzer().getNutzer_id());
		fileHandler.reload();
		layout = new Layout();
	}
	
	public String create() {
		FacesContext context = FacesContext.getCurrentInstance(); //später für message benötigt
		
		Nutzer me = Sitzung.getNutzer();
		
		if(me == null || me.getRang() == Nutzer.RANG_GAST_NO_LOGIN) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Bitte loggen Sie sich neu ein.") );
			return URLManager.LOGIN;
		}
		
		int untergruppeID = -1;
		
		//Admins/BeitragManager/Rubrikleiter können Zuordnung auswählen
		if(adminMode || rubrik != null)
			untergruppeID = zuordnungFinder.getSelectedGruppe() == null ? -1 : zuordnungFinder.getSelectedGruppe().getGruppe_id();
		
		//Errors finden
		if(adminMode && untergruppeID == -1) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Es wurde keine Zuordnung gefunden.") );
			postManagerJS.setScrollUp(false);
			return null;
		} else if(titel == null || titel.trim().equals("")) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Leerer Titel") );
			postManagerJS.setScrollUp(false);
			return null;
		}
		
		if(text != null && (text.trim().equals("") || Jsoup.parse(text).text().trim().equals(""))) {
			text = null;
		}
		
		//Versuch starten einen Beitrag zu erstellen (untergruppeID = -1 bei NICHT-Admin)
		int beitrag_id = Datenbank.createBeitrag(untergruppeID,me, titel, text, vorschauLength, layout.getBeitragLayout(), layout.getVorschauLayout(), showAutor); //neuen Beitrag erstellen
		
		if(beitrag_id != -1) {
			Datei[] added_files = fileHandler.getFilesAdded();
			if(added_files != null) {
				
				for(Datei datei : added_files) {
					
					Datenbank.connectDateiBeitrag(datei.getDatei_id(), beitrag_id); //alle Dateien mit diesem Beitrag verknüpfen
					
					if(datei.getSizeInt() > Datenbank.getPictureSize())
						ImageTools.resize(datei, Datei.DEFAULT_MAX_WIDTH, Datei.DEFAULT_MAX_HEIGHT);
				}
			}
			
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Ein neuer Beitrag wurde erstellt",titel) );
			
			if(untergruppeID != -1) {
				//falls Rubrikleiter / Admin --> Beitrag nach dem Erstellen anzeigen
				try {
					FacesContext.getCurrentInstance().getExternalContext().redirect(URLManager.HOME_BEITRAG+".xhtml?id="+beitrag_id);
				} catch (IOException e) {
					e.printStackTrace();
				}
				postManagerJS.setScrollUp(false);
				return null;
			}

			postManagerJS.setScrollUp(true);
	        init(); //Reset
		} else {
			postManagerJS.setScrollUp(false);
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Ein Fehler ist bei der Erstellung des Beitrages aufgetreten") );
		}
		
		return null;
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
		vorschauPost.load(titel, text, fileHandler, showAutor, vorschauLength, layout.getBeitragLayout(), layout.getVorschauLayout(), Sitzung.getNutzer());
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
	
	public boolean isAdminMode() {
		return adminMode;
	}
	
	public boolean isRealAdmin() {
		return realAdmin;
	}
	
	public Rubrik getRubrik() {
		return rubrik;
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
	
	//managedProperty
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
