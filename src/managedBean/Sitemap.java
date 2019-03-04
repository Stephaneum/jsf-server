package managedBean;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import mysql.Datenbank;
import objects.Gruppe;
import objects.Nutzer;
import sitzung.Sitzung;
import tools.URLManager;

@ManagedBean
@ViewScoped
public class Sitemap {
	
	private TreeNode root;
	private boolean isLoggedIn;
	
	@PostConstruct
	public void init() {
		
		if(Datenbank.existDatabase() == false) {
			return;
		}
		
		isLoggedIn = Sitzung.isLoggedIn();
		
		Gruppe[] gruppe = Datenbank.getGruppeArray(); //Gruppe-Array [1.Dimension]
		
		root = new DefaultTreeNode(new Knoten(null, "ROOT", null, 12), null);
		
		TreeNode home = new DefaultTreeNode(new Knoten(null, "Home", URLManager.HOME, 12), root);
		
		for(int i = 0; i < gruppe.length; i++) {
			
			TreeNode hauptzweig = new DefaultTreeNode(new Knoten(gruppe[i], gruppe[i].getName(), gruppe[i].getLink(), 12), home);
			
			addUntergruppen(hauptzweig, gruppe[i].getUnterGruppe());
		}
		
		root.getChildren().add(new DefaultTreeNode(new Knoten(null, "Login", URLManager.LOGIN, 12), root));
		
		if(Datenbank.getVertretung() != null)
			root.getChildren().add(new DefaultTreeNode(new Knoten(null, "Vertretungsplan", URLManager.VERTRETUNG, true, 12), root));
		
		root.getChildren().add(new DefaultTreeNode(new Knoten(null, "Termine", URLManager.TERMINE, false, 12), root));
		
		root.getChildren().add(new DefaultTreeNode(new Knoten(null, "Statistiken", URLManager.STATISTIKEN, 12), root));
		
		if(Datenbank.getHistory() != null) {
			if(Datenbank.isHistoryLink())
				root.getChildren().add(new DefaultTreeNode(new Knoten(null, "Geschichte", Datenbank.getHistory(), true, 12), root));
			else
				root.getChildren().add(new DefaultTreeNode(new Knoten(null, "Geschichte", URLManager.GESCHICHTE, 12), root));
		}
		
		if(isLoggedIn) {
			
			if(Sitzung.isAdmin()) {
				
				TreeNode admin = new DefaultTreeNode(new Knoten(null, "Admin", null, 12), root);
				
				admin.getChildren().add(new DefaultTreeNode(new Knoten(null, "Konfiguration", URLManager.KONFIG, 11), admin));
				admin.getChildren().add(new DefaultTreeNode(new Knoten(null, "Zugangscodes", URLManager.ZUGANGSCODE, 11), admin));
				admin.getChildren().add(new DefaultTreeNode(new Knoten(null, "Dummys", URLManager.DUMMY, 11), admin));
				admin.getChildren().add(new DefaultTreeNode(new Knoten(null, "Nutzer", URLManager.NUTZER, 11), admin));
				admin.getChildren().add(new DefaultTreeNode(new Knoten(null, "Logbuch", URLManager.LOGS, 11), admin));
			}
			
			TreeNode nutzer = new DefaultTreeNode(new Knoten(null, "Nutzer", null, 12), root);
			
			nutzer.getChildren().add(new DefaultTreeNode(new Knoten(null, "Beiträge", URLManager.BEITRAG, 11), nutzer));
			nutzer.getChildren().add(new DefaultTreeNode(new Knoten(null, "Dateien", URLManager.DATEIEN, 11), nutzer));
			nutzer.getChildren().add(new DefaultTreeNode(new Knoten(null, "Account", URLManager.ACCOUNT, 11), nutzer));
			
			TreeNode gruppen = new DefaultTreeNode(new Knoten(null, "Gruppen", null, 12), root);
			
			if(Sitzung.getNutzer().getRang() != Nutzer.RANG_GAST)
				gruppen.getChildren().add(new DefaultTreeNode(new Knoten(null, "Klasse", URLManager.KLASSE, 11), gruppen));
			gruppen.getChildren().add(new DefaultTreeNode(new Knoten(null, "Projekte", URLManager.PROJEKT, 11), gruppen));
		}
		
		root.getChildren().add(new DefaultTreeNode(new Knoten(null, "Kontakt", URLManager.KONTAKT, 12), root));
		root.getChildren().add(new DefaultTreeNode(new Knoten(null, "Impressum", URLManager.IMPRESSUM, 12), root));
		root.getChildren().add(new DefaultTreeNode(new Knoten(null, "Sitemap", URLManager.SITEMAP, 12), root));
		
		
		collapsingORexpanding(root, true);
	}
	
	//Rekursion
	//parent... Gruppe-Array [ab 2.Ebene]
	private void addUntergruppen(TreeNode parentNode, Gruppe[] childs) {
		
        for(int n = 0; n < childs.length; n++) {
        	
        	Gruppe currentChild = childs[n];
        	
        	if(currentChild.getUnterGruppe().length != 0) {
        		TreeNode child = new DefaultTreeNode(new Knoten(currentChild, currentChild.getName(), currentChild.getLink(), 10), parentNode);
        		addUntergruppen(child,currentChild.getUnterGruppe()); //neuer Stack
        	} else {
        		boolean isLink = currentChild.getLink() != null;
        		parentNode.getChildren().add(new DefaultTreeNode(new Knoten(currentChild, currentChild.getName(), currentChild.getLink(), isLink, 10), parentNode));
        	}
        	
        }
	}
	
	public TreeNode getRoot() {
		return root;
	}
	
	public void collapsingORexpanding(TreeNode n, boolean option) {
	    if(n.getChildren().size() == 0) {
	        n.setSelected(false);
	    }
	    else {
	        for(TreeNode s: n.getChildren()) {
	            collapsingORexpanding(s, option);
	        }
	        n.setExpanded(option);
	        n.setSelected(false);
	    }
	}
	
	public boolean isLoggedIn() {
		return isLoggedIn;
	}
	
	/*
	 * Reiter z.B. Nutzer, Gruppen:     gruppe == null, link == null, externalLink == false
	 * funktionale Seite z.B. Projekte: gruppe == null, link != null, externalLink == false
	 * Vertretungsplan, Geschichte:     gruppe == null, link != null, externalLink == true
	 * Gruppe z.B. Aktuelles:           gruppe != null, link == null, externalLink == false
	 * Link:                            gruppe != null, link != null, externalLink == true
	 */
	public class Knoten {
		
		private Gruppe gruppe;
		private String name, link;
		private boolean externalLink;
		private int fontSize;
		
		Knoten(Gruppe gruppe, String name, String link, int fontSize) {
			this.gruppe = gruppe;
			this.name = name;
			this.link = link;
			this.externalLink = false;
			this.fontSize = fontSize;
		}
		
		Knoten(Gruppe gruppe, String name, String link, boolean externalLink, int fontSize) {
			this.gruppe = gruppe;
			this.name = name;
			this.link = link;
			this.externalLink = externalLink;
			this.fontSize = fontSize;
		}
		
		public String getName() {
			return name;
		}
		
		public Gruppe getGruppe() {
			return gruppe;
		}
		
		public int getFontSize() {
			return fontSize;
		}
		
		public boolean isExternalLink() {
			return externalLink;
		}
		
		public String getLink() {
			return link;
		}
		
	}
}

