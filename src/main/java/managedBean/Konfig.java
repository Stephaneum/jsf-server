package managedBean;

import java.util.ArrayList;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import mysql.Datenbank;
import objects.Country;
import objects.Nutzer;
import objects.Slide;
import sitzung.Sitzung;
import tools.URLManager;

//eager bedeutet, dass diese Klasse beim Programmstart sofort instanziert wird
@ManagedBean(name="konfig", eager = true)
@ApplicationScoped
public class Konfig {
	
	/*
	 * diese Klasse ist auf jeder XHTML-Datei zu finden
	 */
	
	final static private String SUFFIX_TITLE = " - Stephaneum";
	final static private String PREFIX_TITLE = "Stephaneum: ";
	final static private String FILE_CSS = "style_v13.css"; //wird von xhtml-Dateien verwiesen
	final static private String FILE_CSS_MATERIAL = "style_material_v2.css"; //wird von xhtml-Dateien verwiesen
	final static private String STAND = "9.Mai 2020"; //informationen
	final static public boolean RESTORE_VERSION = false;
	
	//Version
	private static String dbase, dbaseVer, server, serverVer;
	
	//Caching kann gefährlich sein, wenn Nutzer A etwas sieht, was Nutzer B nur sehen dürfte
	//tritt vor allem beim Schulnetzwerk über den Proxy auf
	public void addHeader() {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletResponse response = (HttpServletResponse) context.getResponse();
		
		response.addHeader("Cache-Control", "private");
	}

	public boolean getDatenbank_exist() {
		return Datenbank.existDatabase();
	}
	
	//Panels ausblenden, wenn nicht Admin
	public boolean isAdmin() {
		return Sitzung.isAdmin();
	}
	
	public boolean isLehrer() {
		return Sitzung.isLehrer();
	}
	
	public boolean isLehrerOrAdmin() {
		return Sitzung.isLehrer() || Sitzung.isAdmin();
	}
	
	//Panels ausblenden, wenn nicht eingeloggt
	public boolean isLoggedIn() {
		return Sitzung.isLoggedIn();
	}
	
	public boolean isVertretungVisible() {
		return Datenbank.getVertretung() != null;
	}
	
	public String getVertretungURL() {
		return "/"+URLManager.VERTRETUNG;
	}
	
	public int getLogoY() {
		return isVertretungVisible() ? 270 : 180;
	}
	
	public String getVertretungInfo() {
		return Datenbank.getVertretungInfo();
	}
	
	public String getKontakt() {
		return Datenbank.getKontakt();
	}
	
	public String getImpressum() {
		return Datenbank.getImpressum();
	}
	
	public String getBottomText() {
		return Datenbank.getBottomText();
	}
	
	public String getLiveTicker() {
		return Datenbank.getLiveTicker();
	}
	
	public boolean isHistoryLink() {
		return Datenbank.isHistoryLink();
	}
	
	public String getHistory() {
		return Datenbank.getHistory();
	}
	
	public boolean isEuSaLink() {
		return Datenbank.isEuSaLink();
	}
	
	public String getEuSa() {
		return Datenbank.getEuSa();
	}
	
	public String getTerminePrepared() {
		return Datenbank.getTerminePrepared();
	}
	
	public String getKoop() {
		return Datenbank.getKoop();
	}
	
	public ArrayList<Country> getKoopAsList() {
		return Datenbank.getKoopAsList();
	}
	
	public String getKoopURL() {
		return Datenbank.getKoopURL();
	}
	
	public ArrayList<Slide> getSlides() {
		return Datenbank.getSlides();
	}
	
	public String getServerOS() {
		String s = System.getProperty("os.name");
		return s.equals("Linux") ? s+" (Distro nicht erkannt)" : s;
	}
	
	public boolean isWindows() {
		return Konfig.isWindowsOS();
	}
	
	public static boolean isWindowsOS() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}
	
	public String getDatabase() {
		if(dbase != null)
			return dbase;
		else {
			String s = Datenbank.getConnectionMetaData();
			if(s != null) {
				if(s.contains("MariaDB")) {
					s = "MariaDB";
				} else if(s.contains("MySQL")) {
					s = "MySQL";
				}
				dbase = s;
				return s;
			} else {
				return "unbekannt";
			}
		}
		
	}
	
	public String getDatabaseVer() {
		if(dbaseVer != null)
			return dbaseVer;
		else {
			String s = Datenbank.getConnectionMetaData();
			if(s != null) {
				if(s.contains("MariaDB")) {
					String[] split = s.split("-");
					
					s = split.length-2 >= 0 ? split[1] : "unbekannt";
				} else if(s.contains("MySQL")) {
					s = "MySQL";
					
					String[] split = s.split("-");
					
					s = split.length-2 >= 0 ? split[split.length-2] : "unbekannt";
				}
				dbaseVer = s;
				return s;
			} else {
				return "unbekannt";
			}
		}
	}
	
	public String getWebServer() {
		if(server != null)
			return server;
		else {
			String s = ((ServletContext) FacesContext
				    .getCurrentInstance().getExternalContext().getContext()).getServerInfo();
			if(s != null) {
				if(s.contains("Apache Tomcat")) {
					s = "Apache Tomcat";
				}
				server = s;
				return s;
			} else {
				return "unbekannt";
			}
		}
	}
	
	public String getWebServerVer() {
		if(serverVer != null)
			return serverVer;
		else {
			String s = ((ServletContext) FacesContext
				    .getCurrentInstance().getExternalContext().getContext()).getServerInfo();
			if(s != null) {
				if(s.contains("Apache Tomcat")) {
					String[] split = s.split("/");
					
					s = split.length > 0 ? split[split.length-1] : "unbekannt";
				}
				serverVer = s;
				return s;
			} else {
				return "unbekannt";
			}
		}
	}
	
	public String getSuffixTitle() {
		return SUFFIX_TITLE;
	}
	
	public String getPrefixTitle() {
		return PREFIX_TITLE;
	}
	
	public String getCss() {
		return FILE_CSS;
	}
	
	public String getCssMaterial() {
		return FILE_CSS_MATERIAL;
	}
	
	public String getStand() {
		return STAND;
	}
	
	public String getFullName() {
		Nutzer nutzer = Sitzung.getNutzer();
		return nutzer != null && nutzer.getRang() != Nutzer.RANG_GAST_NO_LOGIN ? nutzer.getVorname() + " " + nutzer.getNachname() : "unbekannt";
	}
	
	public Nutzer getNutzer () {
		return Sitzung.getNutzer();
	}
	
	//diese methode ist ein platzhalter, damit die XHTML-Komponente funktioniert (z.B. p:selectBooleanButton in admin_konfig.xhtml)
	public void toggle() { }
	
	//wie oben, nur mit anderen Namen
	public void doNothing() {}
	
}


