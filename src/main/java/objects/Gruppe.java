package objects;

import java.io.Serializable;
import java.util.Arrays;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

import tools.URLManager;

public class Gruppe implements Serializable, Comparable<Gruppe>{
	private static final long serialVersionUID = 7050036941972676357L;
	
	final public static int STANDARD_PRIORITY = 10;
	
	final private int gruppe_id; //ID dieser Gruppe
	final private String name, rubrikLeiter;
	final private Gruppe[] unterGruppe; //Alle Kinder
	private Gruppe parent; //Elterngruppe
	final private String link; //kann 0 sein
	final private int priory; //fürs Sortieren
	final private String password;
	final private int depth; // amount of childs in childs in childs... (starting at 0)
	
	//TopMenu und Sitemap
	public Gruppe(int id, String name, String rubrikLeiter, Gruppe[] gruppe, Gruppe parent, String link, int priory, String password) {
		this.gruppe_id = id;
		this.name = name;
		this.rubrikLeiter = rubrikLeiter;
		this.unterGruppe = gruppe;
		this.parent = parent;
		this.link = link;
		this.priory = priory;
		this.password = password;
		this.depth = calcDepth(unterGruppe, 0);
	}
	
	//Jedes Beitrag verweist auf eine Sammlung (BeitragOpen und BeitragList)
	public Gruppe(int id, String name) {
		this.gruppe_id = id;
		this.name = name;
				
		//undefined
		this.unterGruppe = null;
		this.parent = null;
		this.link = null;
		this.priory = -1;
		this.rubrikLeiter = null;
		this.password = null;
		this.depth = 0;
	}
	
	public int getGruppe_id() {
		return gruppe_id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getRubrikLeiter() {
		return rubrikLeiter;
	}
	
	public Gruppe[] getUnterGruppe() {
		return unterGruppe;
	}
	
	public String getLink() {
		return link;
	}
	
	public String getPassword() {
		return password;
	}
	
	public Gruppe getParent() {
		return parent;
	}
	
	public void setParent(Gruppe parent) {
		this.parent = parent;
	}
	
	public void printToConsole() {
		System.out.println(name);
		if(unterGruppe != null) {
			for(int i = 0; i < unterGruppe.length; i++) {
				unterGruppe[i].printToConsole();
			}
		}
	}
	
	public int getPriory() {
		return priory;
	}
	
	public boolean containsSammlungen() {
		if(unterGruppe == null || unterGruppe.length == 0)
			return false;
		else {
			for(int i = 0; i < unterGruppe.length; i++) {
				if(unterGruppe[i].link == null)
					return true;
			}
			return false; //not found
		}
	}
	
	public int getDepth() {
		return depth;
	}
	
	private int calcDepth(Gruppe[] childs, int currDepth) {
		
		if(childs == null || childs.length == 0)
			return currDepth;
		
		int max = 0;
		for(Gruppe g : childs) {
			int curr = calcDepth(g.unterGruppe, currDepth+1);
			if(curr > max)
				max = curr;
		}
		
		return max;
	}
	
	public static void sortGruppeRekursive(Gruppe[] gruppe) {
		Arrays.sort(gruppe);
		
		for(Gruppe g : gruppe) {
			if(g.unterGruppe.length != 0)
				sortGruppeRekursive(g.unterGruppe);
		}
	}
	
	//geringste Priorität
	public static Gruppe getMin(Gruppe[] gruppe) {
		
		int min = Integer.MAX_VALUE;
		Gruppe minG = null;
		for(Gruppe g : gruppe) {
			if(g.priory < min) {
				min = g.priory;
				minG = g;
			}
		}
		
		return minG;
	}

	@Override
	public int compareTo(Gruppe o) {
		return o.priory-priory;
	}
	
	public static boolean contains(Gruppe gruppe, int id) {
		return gruppe.gruppe_id == id || contains(gruppe.unterGruppe, id);
	}
	
	public static boolean contains(Gruppe[] gruppen, int id) {
		
		for(Gruppe current : gruppen) {
			if(current.getGruppe_id() == id) {
				return true;
			}
		}
		
		boolean found = false;
		
		//weiter nach unten suchen
		for(Gruppe current : gruppen) {
			found = Gruppe.contains(current.getUnterGruppe(), id);
			if(found)
				return true; //sofort abbrechen, falls gefunden
		}
		
		return false;
	}
	
	//Gruppe[] -> MenuModel fürs TopMenu, Menubar bei Home (Rubrik)
	public static MenuModel generateModel(Gruppe[] gruppe, boolean italicOnRubrik) {
		
		MenuModel model = new DefaultMenuModel();
        
        for(int i = 0; i < gruppe.length; i++) {
        	
	        DefaultSubMenu subMenu = new DefaultSubMenu(gruppe[i].getName());
	        if(!italicOnRubrik || gruppe[i].getRubrikLeiter() == null)
	        	subMenu.setStyle("font-size:12pt");
	        else
	        	subMenu.setStyle("font-size:12pt;font-style:italic"); //nur ungenehmigte Rubriken landen auf die erste Ebene
	        subMenu.setStyleClass("grpClick grpID"+gruppe[i].getGruppe_id()); //Javascript übernimmt den OnClick-Event
	        
	        Gruppe[] childs = gruppe[i].getUnterGruppe();
	        addUntergruppen(subMenu,childs);
	         
	        model.addElement(subMenu);
        }
        
        return model;
	}
	
	//Rekursion
	//parent... Gruppe-Array [ab 2.Dimension]
	private static void addUntergruppen(DefaultSubMenu parent, Gruppe[] childs) {
		
        for(int n = 0; n < childs.length; n++) {
        	
        	Gruppe currentChild = childs[n];
        	
        	if(currentChild.getUnterGruppe().length == 0) {
        		
        		//Dieses Kind hat keine weiteren Kinder mehr
        		
        		DefaultMenuItem item = new DefaultMenuItem(currentChild.getName());
        		item.setStyle("font-size:10pt;");
        		
            	if(currentChild.getLink() != null) { 
            		//Link
            		item.setHref(currentChild.getLink());
            		item.setIcon("ui-icon-arrowthick-1-n");
            		if(!currentChild.getLink().endsWith(URLManager.LEHRER_CHAT+".xhtml")) //Lehrerchats soll im selben Tab geöffnet werden
            			item.setTarget("_blank");
            		item.setStyleClass("grpClick grpLink"); //Javascript übernimmt den OnClick-Event
            	} else {
            		item.setStyleClass("grpClick grpID"+currentChild.getGruppe_id()); //Javascript übernimmt den OnClick-Event
            	}
            	
            	parent.addElement(item);
            	
        	} else {
        		
        		//Dieses Kind hat noch mehr Kinder
        		
    	        DefaultSubMenu subMenu = new DefaultSubMenu(currentChild.getName());
    	        subMenu.setStyle("font-size:10pt;");
    	        subMenu.setStyleClass("grpClick grpID"+currentChild.getGruppe_id()); //Javascript übernimmt den OnClick-Event
    	        
    	        addUntergruppen(subMenu,currentChild.getUnterGruppe()); //neuer Stack
    	        
    	        parent.addElement(subMenu);
    	        
        	}
        }
	}

}
