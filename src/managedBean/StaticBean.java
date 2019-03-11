package managedBean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.StaticFile;
import servlet.StaticServlet;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class StaticBean {
	
	private String mainURL, staticPath;
	private ArrayList<StaticFile> files;
	
	@PostConstruct
	public void init() {
		mainURL = URLManager.getMainURL(null);
		staticPath = Datenbank.getSpeicherort()+"/"+StaticServlet.STATIC_FOLDER_NAME;
		Datenbank.updateStaticFiles();
		files = Datenbank.getStaticFiles();
	}
	
	public String getMainURL() {
		return mainURL;
	}
	
	public String getStaticPath() {
		return staticPath;
	}
	
	public ArrayList<StaticFile> getFiles() {
		return files;
	}
	
	public void changeMode(StaticFile file) {
		int mode = file.getMode();
		mode++;
		if(mode > StaticFile.MODE_FULL_SCREEN)
			mode = 0;
		
		Datenbank.editStaticFile(file.getPath(), mode);		
		Datenbank.updateStaticFiles();
		files = Datenbank.getStaticFiles();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Modus aktualisiert", StaticFile.MODE_STRING[mode]));
	}
	
	public void delete(StaticFile file) {
		try {
			Files.delete(Paths.get(staticPath+"/"+file.getPath()));
			
			Datenbank.updateStaticFiles();
			files = Datenbank.getStaticFiles();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Datei gelöscht", file.getPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
