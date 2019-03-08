package managedBean;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import mysql.Datenbank;
import servlet.StaticServlet;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class StaticBean {
	
	private String mainURL, staticPath;
	
	@PostConstruct
	public void init() {
		mainURL = URLManager.getMainURL(null);
		staticPath = Datenbank.getSpeicherort()+"/"+StaticServlet.STATIC_FOLDER_NAME;
	}
	
	public String getMainURL() {
		return mainURL;
	}
	
	public String getStaticPath() {
		return staticPath;
	}
	
}
