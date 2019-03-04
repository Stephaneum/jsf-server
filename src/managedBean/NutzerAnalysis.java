package managedBean;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import managedBean.NutzerUpdate.Zeile;
import mysql.Datenbank;
import objects.Datei;
import objects.Nutzer;

@ViewScoped
@ManagedBean
public class NutzerAnalysis {
	
	//Privatspeicher
	@ManagedProperty("#{nutzerUpdate}")
	private NutzerUpdate nutzerUpdate;
	
	private Speicherverbrauch[] verbraucher;
	
	@PostConstruct
	void init() {
		Map<Nutzer,String> map = Datenbank.getSpeicherverbrauch();
		
		Speicherverbrauch[] verbraucher = new Speicherverbrauch[map.size()];
		int index = 0;
		for (Map.Entry<Nutzer, String> mapEntry : map.entrySet()) {
		    verbraucher[index] = new Speicherverbrauch(mapEntry.getKey(), mapEntry.getValue());
		    index++;
		}
		
		this.verbraucher = verbraucher;
	}
	
	public Speicherverbrauch[] getSpeicherverbrauch() {
		return verbraucher;
	}
	
	public void select(Nutzer nutzer) {
		int storage = Datenbank.getStorage(nutzer.getNutzer_id());
		nutzerUpdate.select(new Zeile(nutzer, Datei.convertSizeToString(storage)));
		nutzerUpdate.loadPrivatSpeicher();
	}
	
	public class Speicherverbrauch {
		
		private Nutzer nutzer;
		private String speicher;
		
		public Speicherverbrauch(Nutzer nutzer, String speicher) {
			this.nutzer = nutzer;
			this.speicher = speicher;
		}
		
		public Nutzer getNutzer() {
			return nutzer;
		}
		
		public String getSpeicher() {
			return speicher;
		}
	}
	
	public void setNutzerUpdate(NutzerUpdate nutzerUpdate) {
		this.nutzerUpdate = nutzerUpdate;
	}

}
