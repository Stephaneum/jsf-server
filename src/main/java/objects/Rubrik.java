package objects;

import java.io.Serializable;

public class Rubrik implements Serializable{
	private static final long serialVersionUID = -4298685017956807975L;
	
	final private int nutzerID; //Rubrikleiter
	final private int gruppeID; //ID dieser Gruppe
	private String name;
	final private String nutzerName;
	final private Gruppe gruppe; //Diese Gruppe und noch alle Kinder
	final private Datei bild;
	final private boolean genehmigt;
	
	//TopMenu und Sitemap
	public Rubrik(int id, String name, int leiterID, String nutzerName, Gruppe gruppe, Datei bild, boolean genehmigt) {
		this.gruppeID = id;
		this.nutzerID = leiterID;
		this.name = name;
		this.nutzerName = nutzerName;
		this.gruppe = gruppe;
		this.bild = bild;
		this.genehmigt = genehmigt;
	}
	
	public int getGruppeID() {
		return gruppeID;
	}
	
	public int getNutzerID() {
		return nutzerID;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Gruppe getGruppe() {
		return gruppe;
	}
	
	public boolean isGenehmigt() {
		return genehmigt;
	}
	
	public String getNutzerName() {
		return nutzerName;
	}
	
	public Datei getBild() {
		return bild;
	}
	
	@Override
	public String toString() {
		return name+" ("+nutzerName+")";
	}

}
