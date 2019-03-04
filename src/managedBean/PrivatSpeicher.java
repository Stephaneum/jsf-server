package managedBean;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

@ViewScoped
@ManagedBean
public class PrivatSpeicher{
	
	//Dateien
	@ManagedProperty("#{dateien}")
	private Dateien dateien;
	
	@PostConstruct
	public void init() {
		dateien.initPrivatspeicher();
	}
	
	// damit diese Klasse instanziert wird, da ansonsten kein einziger call kommt
	public String getTitle() {
		return "Dein persönlicher Online-Speicher";
	}
	
	
	// ManagedProperty
	public void setDateien(Dateien dateien) {
		this.dateien = dateien;
	}
    
}
