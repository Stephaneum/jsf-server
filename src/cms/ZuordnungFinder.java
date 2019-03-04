package cms;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import mysql.Datenbank;
import objects.Gruppe;

@ManagedBean
@ViewScoped
public class ZuordnungFinder {
	
	/*
	 * der Nutzer legt mit dieser Klasse eine Gruppe für einen Beitrag fest
	 */
	
	private DefaultMenuModel model;
	private Gruppe selectedGruppe;
	
	@PostConstruct
	public void init() {
		init(-1);
	}
	
	//nutzerID != -1, soll für Rubriken gelten
	public void init(int nutzerID) {
		
		selectedGruppe = null;
		
		Gruppe[] gruppe = nutzerID == -1 ? Datenbank.getGruppeArray() : Datenbank.getGruppeArrayRubrik(nutzerID, true); //Gruppe-Array [1.Ebene]
		model = new DefaultMenuModel();
        
        for(int i = 0; i < gruppe.length; i++) {
        	
	        DefaultSubMenu subMenu = new DefaultSubMenu(gruppe[i].getName());
	        subMenu.setStyleClass("grp-choose grp-choose-id"+gruppe[i].getGruppe_id());
	        
	        addUntergruppen(subMenu,gruppe[i].getUnterGruppe());
	         
	        model.addElement(subMenu);
        }
	}
	
	//Rekursion
	//parent... Gruppe-Array [ab 2.Ebene]
	private void addUntergruppen(DefaultSubMenu parent, Gruppe[] childs) {
		
        for(int n = 0; n < childs.length; n++) {
        	
        	Gruppe currentChild = childs[n];
        	
        	if(!currentChild.containsSammlungen()) {
        		
        		//Dieses Kind hat keine weiteren Kinder mehr
        		
            	if(currentChild.getLink() == null) {
            		//Sammlung
            		DefaultMenuItem item = new DefaultMenuItem(currentChild.getName());
    		        item.setStyle("font-size:10pt");
    		        item.setStyleClass("grp-choose grp-choose-id"+currentChild.getGruppe_id());
    		        parent.addElement(item);
            	}
            	
        	} else {
        		
        		//Dieses Kind hat noch mehr Kinder
        		
    	        DefaultSubMenu subMenu = new DefaultSubMenu(currentChild.getName());
    	        subMenu.setStyle("font-size:10pt");
    	        subMenu.setStyleClass("grp-choose grp-choose-id"+currentChild.getGruppe_id());
    	        
    	        addUntergruppen(subMenu,currentChild.getUnterGruppe()); //neuer Stack
    	        
    	        parent.addElement(subMenu);
    	        
        	}
        }
	}
	
	//Methode wird ausgewählt, wenn auf das TopMenu geklickt wurde
	public void choose() {
		
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		int gruppeID = Integer.valueOf(params.get("gruppeID")); //von Javascript
		
		selectedGruppe = Datenbank.getGruppeBasic(gruppeID);
	}
	
	public MenuModel getModel() {
        return model;
    }
	
	public Gruppe getSelectedGruppe() {
		return selectedGruppe;
	}
	
	public void unselect() {
		selectedGruppe = null;
	}

}
