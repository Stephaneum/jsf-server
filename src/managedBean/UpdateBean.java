package managedBean;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import tools.URLManager;

@ManagedBean
@ViewScoped
public class UpdateBean {
	
	@PostConstruct
	public void init() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Datenbank.updateDatabase();
			}
		}).start();
	}
	
	public boolean isNeedUpdate() {
		return Datenbank.needUpdate();
	}
	
	public void poll() {
		if(!Datenbank.needUpdate()) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(URLManager.HOME+".xhtml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

