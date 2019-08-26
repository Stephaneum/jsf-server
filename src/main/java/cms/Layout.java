package cms;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class Layout {
	
	/*
	 * Legt Layout von Vorschau & Beitrag fest
	 */
	
	private boolean[] layoutBeitrag = new boolean[3];
	private boolean[] layoutVorschau = new boolean[3];
	
	Layout() {
		//Initialisierung: erstes Layout standardmäßig aktivieren
		layoutBeitrag[0] = true;
		layoutVorschau[0] = true;
	}
	
	Layout(int indexBeitrag, int indexVorschau) {
		//Initialisierung: bestimmte Layout standardmäßig aktivieren
		layoutBeitrag[indexBeitrag] = true;
		layoutVorschau[indexVorschau] = true;
	}

	int getBeitragLayout() {
		for(int i = 0; i < layoutBeitrag.length; i++) {
			if(layoutBeitrag[i])
				return i;
		}
		
		return 0;
	}
	
	int getVorschauLayout() {
		for(int i = 0; i < layoutVorschau.length; i++) {
			if(layoutVorschau[i])
				return i;
		}
		
		return 0;
	}
	
	//Layout[index] aktivieren, alles andere deaktivieren
	public void selectLayoutBeitrag(int index) {
		
		if(layoutBeitrag[index])
			return; //aktueller Layout bereits aktiviert
		
		for(int i = 0; i < layoutBeitrag.length; i++) {
			layoutBeitrag[i] = false;
		}
		
		layoutBeitrag[index] = true;
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Layout festgelegt","Beitrag-Layout "+(index+1)) );
	}
	
	public void selectLayoutVorschau(int index) {
		
		if(layoutVorschau[index])
			return; //aktueller Layout bereits aktiviert
		
		for(int i = 0; i < layoutVorschau.length; i++) {
			layoutVorschau[i] = false;
		}
		
		layoutVorschau[index] = true;
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Layout festgelegt","Vorschau-Layout "+(index+1)) );
	}
	
	//Additum zum .layout-div
	private String getStyleClassBeitrag(int index) {
		if(!layoutBeitrag[index])
			return "not-selected";
		else
			return "";
	}
	
	public String getStyleClassBeitrag0() {
		return getStyleClassBeitrag(0);
	}
	
	public String getStyleClassBeitrag1() {
		return getStyleClassBeitrag(1);
	}
	
	public String getStyleClassBeitrag2() {
		return getStyleClassBeitrag(2);
	}
	
	//Additum zum .layout-div
	private String getStyleClassVorschau(int index) {
		if(!layoutVorschau[index])
			return "not-selected";
		else
			return "";
	}
	
	public String getStyleClassVorschau0() {
		return getStyleClassVorschau(0);
	}
	
	public String getStyleClassVorschau1() {
		return getStyleClassVorschau(1);
	}
	
	public String getStyleClassVorschau2() {
		return getStyleClassVorschau(2);
	}
}
