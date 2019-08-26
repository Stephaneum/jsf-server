package managedBean;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import cms.EditGruppe;
import mysql.Datenbank;
import objects.Datei;
import objects.Nutzer;
import objects.Rubrik;
import sitzung.Sitzung;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class RubrikBean {
	
	private Rubrik rubrik;
	private boolean allowed = false; //hat dieser Nutzer das Recht, Rubriken zu erstellen
	
	//neu erstellen
	private String newRubrik;
	
	@ManagedProperty("#{topMenu}")
	private TopMenu topMenu; //beim Verlassen ggf. TopMenu aktualisieren
	
	@ManagedProperty("#{editGruppe}")
	private EditGruppe editGruppe; //beim Verlassen ggf. EditGruppe aktualisieren
	
	//Admin: Rubrikliste
	private Rubrik[] rubriken;
	
	@PostConstruct
	private void init() {
		
		Nutzer nutzer = Sitzung.getNutzer();
		
		if(nutzer != null) {
			
			newRubrik = null;
			if(nutzer.getOpenRubrik() != null) {
				//Admin
				rubrik = Datenbank.getRubrik(nutzer.getOpenRubrik().getNutzerID());
				allowed = true;
			} else {
				//normaler Nutzer
				rubrik = Datenbank.getRubrik(nutzer.getNutzer_id());
				allowed = Datenbank.isRubrikErstellen(nutzer.getNutzer_id());
			}
			
		}
	}
	
	public boolean isAllowed() {
		return allowed;
	}
	
	public Rubrik getRubrik() {
		return rubrik;
	}
	
	public String deleteRubrik() {
		if(rubrik != null) {
			Datenbank.deleteGruppe(rubrik.getGruppeID());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Rubrik gelöscht",rubrik.getName()) );
			
			Nutzer nutzer = Sitzung.getNutzer();
			
			if(nutzer.getOpenRubrik() != null) {
				return URLManager.ADMIN_RUBRIK+"?faces-redirect=true";
			} else {
				init();
				editGruppe.init();
				topMenu.init();
			}
		}
		
		return null;
	}
	
	//Titelbild
	
	public void upload(FileUploadEvent event) {
		
		UploadedFile file = event.getFile();
		
		if(file == null || file.getFileName().equals("")) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Datei auswählen.") );
			return;
		}
		
		String filename = file.getFileName().toLowerCase();
		if(!Datei.isImage(file.getContentType()) && !(filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".gif"))) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Nur Bilder sind erlaubt.") );
			return;
		}
    	
        try {
			Datenbank.addFile(file, Datei.ORDNER_HOME, rubrik.getGruppeID(), Datei.Mode.RUBRIK);
		} catch (Exception e1) {
			e1.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Sitzung abgelaufen. Bitte neu anmelden.") );
			return;
		}
        
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Hochgeladen",file.getFileName()) );
        
        init();
    }
	
	public void deleteBild() {
		
		Nutzer nutzer = Sitzung.getNutzer();
		
		if(nutzer != null) {
			Datenbank.deleteRubrikBild(rubrik.getGruppeID(),nutzer);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Entfernt","Es wird das Standard-Bild angezeigt.") );
			init();
		}
	}
	
	public void prepareRenameRubrik() {
		if(rubrik != null)
			newRubrik = rubrik.getName();
	}
	
	public void renameRubrik() {
		if(rubrik == null)
			return;
		
		if(newRubrik == null || newRubrik.trim().equals("")) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "leere Eingabe") );
			return;
		}
		
		Datenbank.renameRubrik(rubrik.getGruppeID(), newRubrik);
		rubrik.setName(newRubrik);
		topMenu.init();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Umbenannt",newRubrik) );
	}
	
	//neue Rubrik
	
	public void setNewRubrik(String newRubrik) {
		this.newRubrik = newRubrik;
	}
	
	public String getNewRubrik() {
		return newRubrik;
	}
	
	public void createRubrik() {
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer != null && nutzer.getNutzer_id() != Nutzer.RANG_GAST_NO_LOGIN) {
			
			if(newRubrik == null || newRubrik.trim().equals("")) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fehler", "Geben Sie einen Namen ein.") );
				return;
			}
			
			Datenbank.createRubrik(nutzer.getNutzer_id(), newRubrik);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Rubrik erstellt",newRubrik) );
			init();
			editGruppe.init();
			topMenu.init();
		}
	}
	
	//Admin
	
	private void initRubriken() {
		Nutzer[] nutzerAllowed = Datenbank.getNutzerAllowRubrik();
		rubriken = new Rubrik[nutzerAllowed.length];
		
		for(int i = 0; i < nutzerAllowed.length; i++) {
			
			int currentNutzerID = nutzerAllowed[i].getNutzer_id();
			
			rubriken[i] = Datenbank.getRubrik(currentNutzerID);
			
			if(rubriken[i] == null) {
				rubriken[i] = new Rubrik(-1, null, currentNutzerID, nutzerAllowed[i].getVorname()+" "+nutzerAllowed[i].getNachname(), null, null, false);
			}
		}
	}
	
	public Rubrik[] getRubriken() {
		
		if(rubriken == null) {
			initRubriken();
		}
		
		return rubriken;
	}
	
	public void deleteRubrik(Rubrik rubrik) {
		if(rubrik != null) {
			Datenbank.deleteGruppe(rubrik.getGruppeID());
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Rubrik gelöscht",rubrik.getName()) );
			initRubriken();
			topMenu.init();
		}
	}
	
	public String openRubrik(Rubrik rubrik) {
		Nutzer nutzer = Sitzung.getNutzer(); //Nutzer-Objekt erhalten
		if(nutzer != null) {
			nutzer.setOpenRubrik(rubrik);
			return URLManager.RUBRIK+"?faces-redirect=true";
		} else {
			return URLManager.LOGIN+"?faces-redirect=true"; //Sitzung abgelaufen
		}
	}
	
	//ManagedProperty
	
	public void setTopMenu(TopMenu topMenu) {
		this.topMenu = topMenu;
	}
	
	public void setEditGruppe(EditGruppe editGruppe) {
		this.editGruppe = editGruppe;
	}
	
}
