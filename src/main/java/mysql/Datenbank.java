package mysql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import managedBean.Statistiken;
import managedBean.TopMenu;
import objects.Beitrag;
import objects.Country;
import objects.Datei;
import objects.Ereignis;
import objects.Gruppe;
import objects.Klasse;
import objects.Nachricht;
import objects.Nutzer;
import objects.ProgressObject;
import objects.Projekt;
import objects.Rubrik;
import objects.Slide;
import objects.StaticFile;
import sitzung.Count;
import sitzung.Sitzung;
import sitzung.UserAgentDetection;
import tools.Action;
import tools.Countdown;
import tools.ImageTools;
import tools.Timeformats;
import tools.Zugangscodes;

public class Datenbank {
	
	static private MySQLManager mysql;
	static private boolean connected = false; //um erneutes Verbinden zu verhindern
	static private boolean exist_database = false; //um erneutes abgragen zu unterbinden, sobald true -> nicht mehr nachfragen
	static private String speicherort = null; //um erneutes Abfragen zu unterbinden, sobald != null -> nicht mehr nachfragen (setSpeicherort ändert es)
	static private String backupDir = null;
	
	static private String kontakt, impressum, copyright, entwicklerInfo, liveTicker, vertretung, vertretungInfo, history, euSa, termine, terminePrepared, koop, koopURL; //(sync durch erstes get; setter ändern es)
	static private boolean historyLink = false, euSaLink = false; //ist history/euSa ein Link?, sync durch erstes get; setter ändern es
	static private ArrayList<Country> koopList;
	static private boolean showSoftware = true, showSysteme = true, showCloud = true, redirectPort = false; //wird nach connect() synchronisiert
	static private ArrayList<StaticFile> staticFiles;
	
	static private boolean showDummy = false; //nach jedem Server-Start deaktiviert, über Konfig (Admin) aktivierbar
	
	static private boolean mergeAktuelles = false; //wird nach connect() synchronisiert
	static private int pictureSize = Datei.convertToBytes(250, Datei.SIZE_KB); //wird nach connect() synchronisiert
	static private int defaultGruppeID; //wird nach connect() synchronisiert
	
	public static void main(String[] args) {
		connect("root", "foradmin", "localhost", "3306", "s4");
	}
	
	//--------------- Admin/Hidden-Funktionen ---------------------------------------------------
	
	//unbenutzte Zugangscodes
	static public String[] getAllZugangscodes(int rang, boolean benutzt) {
		Konsole.method("Datenbank.getAllZugangscodes("+rang+","+benutzt+")");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getAllZugangscodes(rang,benutzt);
	}
	
	static public String[] getAllZugangscodes() {
		Konsole.method("Datenbank.getAllZugangscodes()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getAllZugangscodes();
	}
	
	static public int getAnzahlZugangscodes(int rang, boolean benutzt) {
		Konsole.method("Datenbank.getAnzahlZugangscodes("+rang+","+benutzt+")");
		if(mysql == null) {
			Konsole.noConnection();
			return -1;
		} else
			return mysql.getAnzahlZugangscodes(rang, benutzt);
	}
	
	static public int getAnzahlDatei() {
		Konsole.method("Datenbank.getAnzahlDatei()");
		if(mysql == null) {
			Konsole.noConnection();
			return 0;
		} else
			return mysql.getAnzahlDatei();
	}
	
	static public String getCloudSize() {
		Konsole.method("Datenbank.getCloudSize()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return Datei.convertSizeToString(mysql.getCloudSize());
	}
	
	//vorher getAnzahlDatei() aufrufen
	static public String[] getCloudMime(int anzahl) {
		Konsole.method("Datenbank.getCloudMime()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getCloudMime(anzahl);
	}
	
	//vorher getAnzahlDatei() aufrufen
	static public Map<String,Double> getCloudActivity() {
		Konsole.method("Datenbank.getCloudActivity()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getCloudActivity();
	}
	
	static public Map<Nutzer,String> getSpeicherverbrauch() {
		Konsole.method("Datenbank.getSpeicherverbrauch()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getSpeicherverbrauch();
	}
	
	static public int getAnzahlBeitrag() {
		Konsole.method("Datenbank.getAnzahlBeitrag()");
		if(mysql == null) {
			Konsole.noConnection();
			return 0;
		} else
			return mysql.getAnzahlBeitrag();
	}
	
	static public int getAnzahlBilderBeitrag() {
		Konsole.method("Datenbank.getAnzahlBilderBeitrag()");
		if(mysql == null) {
			Konsole.noConnection();
			return 0;
		} else
			return mysql.getAnzahlBilderBeitrag();
	}
	
	static public int[] getAllKlassenID() {
		Konsole.method("Datenbank.getAllKlassenID()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getAllID(MySQLManager.KLASSE);
	}
	
	static public int[] getAllKlassenstufe() {
		Konsole.method("Datenbank.getAllKlassenstufe()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getAllKlassenstufe();
	}
	
	static public Klasse[] getAllKlassen() {
		Konsole.method("Datenbank.getAllKlassen()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getAllKlassen();
	}
	
	static public Nutzer[] getAllNutzer() {
		Konsole.method("Datenbank.getAllNutzer()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		}
		else
			return mysql.getAllNutzer();
	}
	
	static public Nutzer[] getAllNutzer(int rang) {
		Konsole.method("Datenbank.getAllNutzer(rang)");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		}
		else
			return mysql.getAllNutzer(rang);
	}
	
	static public int[] getAllNutzerID(int rang) {
		Konsole.method("Datenbank.getAllNutzerID("+rang+")");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		}
		else
			return mysql.getAllNutzerId(rang);
	}
	
	static public int getAnzahlNutzer(int rang) {
		Konsole.method("Datenbank.getAnzahlNutzer("+rang+")");
		if(mysql == null) {
			Konsole.noConnection();
			return -1;
		}
		else
			return mysql.getAnzahlNutzer(rang);
	}
	
	static public int[] getAllNutzerID(int rang, int klassenstufe) {
		Konsole.method("Datenbank.getAllNutzerID("+rang+","+klassenstufe+")");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		}
		else
			return mysql.getAllNutzerId(rang,klassenstufe);
	}
	
	static public int getNextProjektID() {
		Konsole.method("Datenbank.getNextProjektID()");
		return mysql.getNextID(MySQLManager.PROJEKT);
	}
	
	static public int getNextGruppeID() {
		Konsole.method("Datenbank.getNextGruppeID()");
		return mysql.getNextID(MySQLManager.GRUPPE);
	}
	
	//von Dummy aufgerufen
	static public void createProjekt(String projekt_name, int leiter_nutzer_id) {
		Konsole.method("Datenbank.createProjekt("+projekt_name+","+leiter_nutzer_id+")");
		
		mysql.createProjekt(leiter_nutzer_id, projekt_name, true, false);
	}
	
	static public void prepareRegisterNutzerFast() {
		Konsole.method("Datenbank.prepareRegisterNutzerFast()");
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.prepareTempStorageTypes();
	}
	
	//Für die Generierung der Dummies benötigt UND NutzerImport
	static public void registerNutzerFast(String code,int klasse_id,String vorname,String nachname,int geschlecht,String email,String passwort) {
		Konsole.method("Datenbank.registerNutzerFast(): "+vorname+" "+nachname);
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.registerNutzerFast(code, klasse_id, vorname, nachname, geschlecht, email, passwort);
	}
	
	//Zugangscodes, die mit keinem Nutzer zugeordnet sind werden gelöscht
	static public void validateZugangscode() {
		Konsole.method("Datenbank.validateZugangscode()");
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.validateZugangscode();
	}
	
	//ALLE Beiträge und ALLE Dateien löschen
	static public void clearAllStorageAndBeitrag() {
		Konsole.method("Datenbank.clearAllStorageAndBeitrag()");
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.clearAllStorageAndBeitrag();
	}
	
	//Alle Dateien eines Nutzers
	static public Datei[] getAllDateiArrayPrivate(int nutzerID) {
		Konsole.method("Datenbank.getFileList("+nutzerID+")");
		if(mysql == null) {
			Konsole.noConnection();
			return new Datei[0];
		} else
			return mysql.getFileArrayPrivate(nutzerID, null);
	}
	
	static public void setNutzerVorname(int id, String value) {
		Konsole.method("Datenbank.setNutzerVorname(...)");
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.setNutzerValue(id, MySQLManager.VORNAME, value);
	}
	
	static public void setNutzerNachname(int id, String value) {
		Konsole.method("Datenbank.setNutzerNachname(...)");
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.setNutzerValue(id, MySQLManager.NACHNAME, value);
	}
	
	static public void setNutzerEmail(int id, String value) {
		Konsole.method("Datenbank.setNutzerEmail(...)");
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.setNutzerValue(id, MySQLManager.EMAIL, value);
	}
	
	//--------------- Datenbank-Konfiguration ---------------------------------------------------
	
	static public boolean connect(String name, String password, String ip, String port, String database_name) {
		Konsole.method("Datenbank.connect()");
		
		if(mysql == null) {
			//passiert nur 1x
			mysql = new MySQLManager(database_name);
			Sicherung.startBackupTimer();
		}
		
		//Instanz existiert nun 100%
		if(connected == true) {
			//bereits verbunden
			Konsole.antwort("Die Verbindung zur MySQL-Datenbank wurde bereits aufgebaut!");
			return true;
		} else {
			//nicht verbunden
			connected = mysql.connect(name, password, ip, port);
			if(connected == true) {
				Konsole.antwort("Verbindung zur MySQL-Datenbank hergestellt.");
				
				if(existDatabase() && !needUpdate()) {
					syncAll();
				}
			} else {
				mysql = null;
			}
			return connected;
		}
	}
	
	public static void syncAll() {
		mysql.deleteCodePasswortVergessen();
		
		syncSpecialText();
		syncVariables();
		syncPlan();

		Statistiken.initData();
	}

	public static void syncSpecialText() {
		kontakt = mysql.getKonfig(MySQLManager.KONFIG_STRING_KONTAKT);
		impressum = mysql.getKonfig(MySQLManager.KONFIG_STRING_IMPRESSUM);
		copyright = mysql.getKonfig(MySQLManager.KONFIG_STRING_BOTTOM);
		entwicklerInfo = mysql.getKonfig(MySQLManager.KONFIG_STRING_ENTWICKLER);
		liveTicker = mysql.getKonfig(MySQLManager.KONFIG_STRING_TICKER);

		history = mysql.getKonfig(MySQLManager.KONFIG_STRING_HISTORY);
		historyLink = history != null && history.startsWith("http");

		euSa = mysql.getKonfig(MySQLManager.KONFIG_STRING_EU_SA);
		euSaLink = euSa != null && euSa.startsWith("http");

		termine = mysql.getKonfig(MySQLManager.KONFIG_TERMINE);
		terminePrepared = prepareTermine(termine);

		koop = mysql.getKonfig(MySQLManager.KONFIG_KOOP);
		koopList = calcKoopList(koop);
		koopURL = mysql.getKonfig(MySQLManager.KONFIG_KOOP_URL);
	}

	public static void syncVariables() {
		defaultGruppeID = mysql.getKonfigInt(MySQLManager.KONFIG_DEFAULT_GRUPPE_ID);
		if(defaultGruppeID == -1) {
			setDefaultGruppeID(mysql.getFirstGruppeID());
		}
		mergeAktuelles = mysql.getKonfigBoolean(MySQLManager.KONFIG_MERGE_AKTUELLES);

		speicherort = mysql.getKonfig(MySQLManager.KONFIG_SPEICHERORT);
		backupDir = mysql.getKonfig(MySQLManager.KONFIG_BACKUP_DIR);
		pictureSize = mysql.getKonfigInt(MySQLManager.KONFIG_PICTURE_SIZE);
		redirectPort = mysql.getKonfigBoolean(MySQLManager.KONFIG_REDIRECT_PORT);

		showSysteme = mysql.getKonfigBoolean(MySQLManager.KONFIG_SHOW_SYSTEM);
		showSoftware = mysql.getKonfigBoolean(MySQLManager.KONFIG_SHOW_SOFTWARE);
		showCloud = mysql.getKonfigBoolean(MySQLManager.KONFIG_SHOW_CLOUD);
	}

	public static void syncPlan() {
		vertretung = mysql.getKonfig(MySQLManager.KONFIG_STRING_VERTRETUNG);
		vertretungInfo = mysql.getKonfig(MySQLManager.KONFIG_STRING_VERTRETUNG_INFO);
	}
	
	static public void updateDatabase() {
		Konsole.method("Datenbank.updateDatabase()");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.updateDatabase();
			syncAll();
		}
	}
	
	static public boolean needUpdate() {
		return mysql == null ? false : mysql.isNeedUpdate();
	}
	
	static public boolean isConnected() {
		return connected;
	}
	
	static public boolean existDatabase() {
		if(mysql == null) {
			Konsole.method("Datenbank.existDatabase()");
			Konsole.noConnection();
			return false;
		} else {
			if(exist_database == true) {
				//Zwischenspeicher: JA - keine Konsolenausgabe
				return true;
			} else {
				//Zwischenspeicher: NEIN - es kann sein, dass es nun JA ist
				Konsole.method("Datenbank.existDatabase()");
				exist_database = mysql.existDatabase();
				Konsole.antwort("Datenbankstruktur-Existenz: "+exist_database);
				return exist_database;
			}
		}
	}
	
	static public String getConnectionMetaData() {
		if(mysql == null)
			return null;
		else
			return mysql.getConnectionMetaData();
	}
	
	static public String getDatabaseLocation() {
		if(mysql == null)
			return "";
		else
			return mysql.getDatabaseLocation();
	}
	
	//0 bis 60%
	static public void createDatabase(ProgressObject progress) {
		Konsole.method("Datenbank.createDatabase()");
		if(mysql == null)
			Konsole.noConnection();
		else
			mysql.createDatabase(progress);
	}
	
	static public void createEmptyDatabase() {
		Konsole.method("Datenbank.createEmptyDatabase()");
		if(mysql == null)
			Konsole.noConnection();
		else
			mysql.createEmptyDatabase();
	}
	
	static public MySQLManager getInstance() {
		return mysql;
	}
	
	static public void createZugangscodes(int laenge, int anzahl_gesamt, int anzahl_lehrer, boolean withAdmin) {
		Konsole.method("Datenbank.createZugangscodes("+laenge+","+anzahl_gesamt+","+anzahl_lehrer+")");
		if(mysql == null)
			Konsole.noConnection();
		else {
			String[] codes = Zugangscodes.generateZugangscodes(anzahl_gesamt, laenge, getAllZugangscodes());
			mysql.createZugangscodes(codes, anzahl_lehrer, withAdmin);
			Konsole.antwort("Zugangscodes fertig generiert und in die Datenbank gespeichert");
		}
	}
	
	static public void createZugangscodes(int laenge, int anzahl, int rang) {
		Konsole.method("Datenbank.createZugangscodes("+laenge+","+anzahl+","+rang+")");
		if(mysql == null)
			Konsole.noConnection();
		else {
			String[] codes = Zugangscodes.generateZugangscodes(anzahl, laenge, getAllZugangscodes());
			mysql.createZugangscodes(codes,rang);
			Konsole.antwort("Zugangscodes fertig generiert und in die Datenbank gespeichert");
		}
	}
	
	static public void deleteZugangscode(String code) {
		Konsole.method("Datenbank.deleteZugangscode("+code+")");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.deleteZugangscode(code);
		}
	}
	
	static public void createKlassen(int[] stufe, String[] suffix) {
		Konsole.method("Datenbank.createKlassen()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.addKlassen(stufe, suffix);
			Konsole.antwort("Neue Klassen in die Datenbank gespeichert");
		}
	}
	
	//wird von NutzerImport und Authentifizierung aufgerufen
	static public void createKlasse(int stufe, String suffix) {
		Konsole.method("Datenbank.createKlasse()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.addKlasse(stufe, suffix);
			Konsole.antwort("Neue Klasse in die Datenbank gespeichert");
		}
	}
	
	static public boolean isShowDummy() {
		return showDummy;
	}
	
	static public void setShowDummy(boolean show) {
		showDummy = show;
	}
	
	static public int getDefaultGruppeID() {
		return defaultGruppeID;
	}
	
	static public void setDefaultGruppeID(int id) {
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfigInt(MySQLManager.KONFIG_DEFAULT_GRUPPE_ID, id);
			defaultGruppeID = id;
		}
	}
	
	static public int getPictureSize() {
		return pictureSize;
	}
	
	static public void setPictureSize(int size) {
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfigInt(MySQLManager.KONFIG_PICTURE_SIZE, size);
			pictureSize = size;
		}
	}
	
	static public boolean isMergeAktuelles() {
		return mergeAktuelles;
	}
	
	static public void setMergeAktuelles(boolean merge) {
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfigBoolean(MySQLManager.KONFIG_MERGE_AKTUELLES, merge);
			mergeAktuelles = merge;
		}
	}
	
	static public boolean isShowSystem() {
		return showSysteme;
	}
	
	static public void setShowSystem(boolean show) {
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfigBoolean(MySQLManager.KONFIG_SHOW_SYSTEM, show);
			showSysteme = show;
		}
	}
	
	static public boolean isShowSoftware() {
		return showSoftware;
	}
	
	static public void setShowSoftware(boolean show) {
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfigBoolean(MySQLManager.KONFIG_SHOW_SOFTWARE, show);
			showSoftware = show;
		}
	}
	
	static public boolean isShowCloud() {
		return showCloud;
	}
	
	static public void setShowCloud(boolean show) {
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfigBoolean(MySQLManager.KONFIG_SHOW_CLOUD, show);
			showCloud = show;
		}
	}
	
	static public boolean isRedirectPort() {
		return redirectPort;
	}
	
	static public void setRedirectPort(boolean redirect) {
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfigBoolean(MySQLManager.KONFIG_REDIRECT_PORT, redirect);
			redirectPort = redirect;
		}
	}
	
	static public void setSpeicherort(String pfad) {
		
		Konsole.method("Datenbank.setSpeicherort("+pfad+")");
		if(mysql == null)
			Konsole.noConnection();
		else {
			pfad = pfad.replace('\\', '/');
			if(pfad.endsWith("/"))
				pfad = pfad.substring(0, pfad.length()-1); //den letzten Slash entfernen
			
			mysql.setKonfig(MySQLManager.KONFIG_SPEICHERORT,pfad);
			if(speicherort != null)
				mysql.updatePath(speicherort, pfad); //in den vorhandenen Dateien den Pfad ändern
			
			speicherort = pfad; //Klassenvariable aktualisieren
			vertretung = mysql.getKonfig(MySQLManager.KONFIG_STRING_VERTRETUNG); //Vertretungsplan aktualisieren
		}
	}
	
	static public String getSpeicherort() {
		return speicherort;
	}
	
	static public void setBackupDir(String pfad) {
		
		Konsole.method("Datenbank.setBackupDir("+pfad+")");
		if(mysql == null)
			Konsole.noConnection();
		else {
			pfad = pfad.replace('\\', '/');
			if(pfad.endsWith("/"))
				pfad = pfad.substring(0, pfad.length()-1); //den letzten Slash entfernen
			
			mysql.setKonfig(MySQLManager.KONFIG_BACKUP_DIR,pfad);
			
			backupDir = pfad; //Klassenvariable aktualisieren
		}
	}
	
	static public String getBackupDir() {
		return backupDir;
	}
	
	static public String getVerwendeterSpeicherplatz() {
		File file = new File(getSpeicherort());
		long space = file.getTotalSpace()-file.getUsableSpace();
		return Datei.convertSizeToString(space);
	}
	
	static public String getFreierSpeicherplatz() {
		long space = new File(getSpeicherort()).getUsableSpace();
		return Datei.convertSizeToString(space);
	}
	
	static public String getSpeicherplatz() {
		long space = new File(getSpeicherort()).getTotalSpace();
		return Datei.convertSizeToString(space);
	}
	
	static public String getUsedRAM(Runtime runtime) {
		long usedMem = runtime.totalMemory() - runtime.freeMemory();
		return Datei.convertSizeToString(usedMem);
	}
	
	static public String getMaxRAM(Runtime runtime) {
		long maxMem = runtime.maxMemory();
		return Datei.convertSizeToString(maxMem);
	}
	
	static public void setKontakt(String text) {
		Konsole.method("Datenbank.setKontakt()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_STRING_KONTAKT,text);
			kontakt = text;
		}
	}
	
	static public String getKontakt() {
		return kontakt;
	}
	
	static public void setImpressum(String text) {
		Konsole.method("Datenbank.setImpressum()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_STRING_IMPRESSUM,text);
			impressum = text;
		}
	}
	
	static public String getImpressum() {
		return impressum;
	}
	
	static public void setBottomText(String text) {
		Konsole.method("Datenbank.setBottomText()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_STRING_BOTTOM,text);
			copyright = text;
		}
	}
	
	static public String getBottomText() {
		return copyright;
	}
	
	static public void setEntwicklerInfo(String text) {
		Konsole.method("Datenbank.setEntwicklerInfo()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_STRING_ENTWICKLER,text);
			entwicklerInfo = text;
		}
	}
	
	static public String getEntwicklerInfo() {
		return entwicklerInfo;
	}
	
	static public void setLiveTicker(String text) {
		Konsole.method("Datenbank.setLiveTicker()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_STRING_TICKER,text);
			liveTicker = text;
		}
	}
	
	static public String getLiveTicker() {
		return liveTicker;
	}
	
	static public void setHistory(String content) {
		Konsole.method("Datenbank.setHistory()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_STRING_HISTORY,content);
			history = content;
			historyLink = content != null && content.startsWith("http"); //in der Bean wird ggf. geparst, falls Link
		}
	}
	
	static public String getHistory() {
		return history;
	}
	
	static public boolean isHistoryLink() {
		return historyLink;
	}
	
	static public void setEuSa(String content) {
		Konsole.method("Datenbank.setEuSa()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_STRING_EU_SA,content);
			euSa = content;
			euSaLink = content != null && content.startsWith("http"); //in der Bean wird ggf. geparst, falls Link
		}
	}
	
	static public String getEuSa() {
		return euSa;
	}
	
	static public boolean isEuSaLink() {
		return euSaLink;
	}
	
	static private String prepareTermine(String raw) {
		if(raw == null)
			return "";

		String[] events = raw.split("\n");
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for(String curr : events) {
			if(curr.equals(""))
				continue;
			
			String[] split = curr.split(",");
			if(split.length >= 2) {
				
				String title = "";
				String url = null;
				if(split.length > 2) {
					// title contains comma itself: include it again
					for(int i = 1; i < split.length; i++) {
						if(i == 1)
							title += split[i];
						else
							title += ", "+split[i];
					}
				} else {
					title = split[1];
				}
				
				int indexHTTP = title.indexOf("http");
				if(indexHTTP != -1) {
					url = title.substring(indexHTTP);
					title = title.substring(0, indexHTTP);
				}
				
				String start = null;
				String end = null;
				
				if(split[0].contains("-")) {
					String[] split2 = split[0].split("-");
					if(split2.length == 2) {
						start = split2[0].trim();
						end = split2[1].trim();
					} else {
						return "";
					}
				} else {
					start = split[0].trim();
				}
				
				if(!first)
					builder.append(",{title:'");
				else
					builder.append("{title:'");
				builder.append(title);
				builder.append("',start:'");
				appendDate(start,builder);
				if(end != null) {
					builder.append("',end:'");
					appendDate(end,builder);
				}
				if(url != null) {
					builder.append("',url:'");
					builder.append(url);
				}
				
				builder.append("'}");
				
				first = false;
			} else {
				continue;
			}
		}
		return builder.toString();
	}
	
	static private void appendDate(String input, StringBuilder builder) {
		
		String[] split = input.split(" ");
		String[] date = split[0].split("\\.");
		
		if(date.length == 3) {
			builder.append(date[2]);
			builder.append("-");
			builder.append(date[1]);
			builder.append("-");
			builder.append(date[0]);
			if(split.length == 2) {
				// include time
				builder.append("T");
				builder.append(split[1]);
			}
		}
	}
	
	static public void setTermine(String termine) {
		Konsole.method("Datenbank.setTermine()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_TERMINE,termine);
			Datenbank.termine = termine;
			Datenbank.terminePrepared = prepareTermine(termine);
		}
	}
	
	static public String getTermine() {
		return termine;
	}
	
	public static String getTerminePrepared() {
		return terminePrepared;
	}
	
	static public void setKoop(String koop) {
		Konsole.method("Datenbank.setKoop()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_KOOP,koop);
			Datenbank.koop = koop;
			Datenbank.koopList = calcKoopList(koop);
		}
	}
	
	static public ArrayList<Country> calcKoopList(String koop) {
		if(koop == null)
			return null;
		
		String[] array = koop.split(";");
		
		ArrayList<Country> list = new ArrayList<>(array.length);
		for(String curr : array) {
			String name = curr;
			String info = null;
			String url = null;
			int startBracket = curr.indexOf('(');
			int endBracket = curr.indexOf(')');
			int startSquareBracket = curr.indexOf('[');
			int endSquareBracket = curr.indexOf(']');
			
			if(startBracket != -1 && endBracket != -1 && startBracket < endBracket) {
				name = curr.substring(0,startBracket);
				info = curr.substring(startBracket+1, endBracket);
			}
			
			if(startSquareBracket != -1 && endSquareBracket != -1 && startSquareBracket < endSquareBracket) {
				if(info == null)
					name = curr.substring(0,startSquareBracket); // Country [URL]
				url = curr.substring(startSquareBracket+1, endSquareBracket);
			}
			
			list.add(new Country(name, info, url));
		}
			
		return list;
	}
	
	static public String getKoop() {
		return koop;
	}
	
	static public ArrayList<Country> getKoopAsList() {
		return koopList;
	}
	
	static public void setKoopURL(String koopURL) {
		Konsole.method("Datenbank.setKoopURL()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_KOOP_URL,koopURL);
			Datenbank.koopURL = koopURL;
		}
	}
	
	static public String getKoopURL() {
		return koopURL;
	}

	/**
	 * nachdem ein neuer Vertretungsplan hochgeladen wurde, wird dessen Pfad in der Datenbank gespeichert.
	 * @param pfad Pfad des Vertretungsplans
	 */
	static public void setVertretung(String pfad) {
		Konsole.method("Datenbank.setVertretung()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_STRING_VERTRETUNG,pfad);
			vertretung = pfad;
		}
	}
	
	/**
	 * 
	 * @return Pfad zum Vertretungsplan (lokal auf dem Server)
	 */
	static public String getVertretung() {
		return vertretung;
	}
	
	static public String getVertretungLastModified() {
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else {
			if(vertretung == null) {
				return null;
			} else {
				File file = new File(vertretung);
				return Timeformats.completeNoSeconds().format(new Date(file.lastModified()));
			}
		}
	}
	
	static public void setVertretungInfo(String info) {
		Konsole.method("Datenbank.setVertretungInfo()");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_STRING_VERTRETUNG_INFO,info);
			vertretungInfo = info;
		}
	}
	
	static public String getVertretungInfo() {
		return vertretungInfo;
	}
	
	static public void setTimeoutPasswortVergessen(int millisec) {
		Konsole.method("Datenbank.setTimeoutPasswortVergessen("+millisec+")");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_TIMEOUT_PASSWORT_VERGESSEN, millisec+"");
		}
	}
	
	static public int getTimeoutPasswortVergessen() {
		Konsole.method("Datenbank.getTimeoutPasswortVergessen()");
		if(mysql == null) {
			Konsole.noConnection();
			return -1;
		} else {
			String string = mysql.getKonfig(MySQLManager.KONFIG_TIMEOUT_PASSWORT_VERGESSEN);
			int dauer = 0;
			if(string == null) {
				Konsole.antwort("Dauer des Passwort-Vergessen-Codes wurde noch nicht festgelegt");
			} else {
				dauer = Integer.parseInt(string);
				Konsole.antwort("Dauer: "+dauer+" ms");
			}
			return dauer;
		}
	}
	
	static public void setSpeicherLehrer(int speicher) {
		Konsole.method("Datenbank.setSpeicherLehrer("+speicher+")");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_STORAGE_LEHRER,""+speicher);
		}
	}
	
	static public String getSpeicherLehrer() {
		Konsole.method("Datenbank.getSpeicherLehrer()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else {
			return mysql.getKonfig(MySQLManager.KONFIG_STORAGE_LEHRER);
		}
	}
	
	static public void setSpeicherSchueler(int speicher) {
		Konsole.method("Datenbank.setSpeicherSchueler("+speicher+")");
		if(mysql == null)
			Konsole.noConnection();
		else {
			mysql.setKonfig(MySQLManager.KONFIG_STORAGE_SCHUELER,""+speicher);
		}
	}
	
	static public String getSpeicherSchueler() {
		Konsole.method("Datenbank.getSpeicherSchueler()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else {
			return mysql.getKonfig(MySQLManager.KONFIG_STORAGE_SCHUELER);
		}
	}
	
	//--------------- Registrierung/Login ---------------------------------------------------
	
	static public int checkZugangscode(String code) {
		Konsole.method("Datenbank.checkZugangscode(\""+code+"\")");
		if(mysql == null)
			Konsole.noConnection();
		else {
			return mysql.checkZugangscode(code);
		}
		return Nutzer.RANG_GAST_NO_LOGIN;
	}
	
	//Rückgabewert: 0 = OK, 1 = E-Mail bereits verwendet, 2 = Klasse existiert nicht, 3 = Unbekannter Error
	static public int registerNutzer(String code,String klasse,String vorname,String nachname,int geschlecht,String email,String passwort) {
		Konsole.method("Datenbank.registerNutzer("+vorname+" "+nachname+")");
		if(mysql == null) {
			Konsole.noConnection();
			return 3;
		}
		else {
			int done = mysql.registerNutzer(code,klasse,vorname,nachname,geschlecht,email,passwort);
			switch(done) {
			case 0:
				Konsole.antwort(vorname+" "+nachname+" ("+klasse+") wurde erfolgreich registriert.");
				if(klasse == null)
					addLog(Ereignis.TYPE_REGISTER, vorname+" "+nachname);
				else
					addLog(Ereignis.TYPE_REGISTER, vorname+" "+nachname+" ("+klasse+")");
				break;
			case 1:
				Konsole.warn("Die E-Mail '"+email+"' wird bereits von einem anderen Account verwendet!");
				break;
			case 2:
				Konsole.warn("Klasse existiert nicht!");
				break;
			default:
				Konsole.warn("Unbekannter Error");
				break;
			}
			return done;
		}
	}
	
	static public void registerAdmin(String vorname, String nachname, int geschlecht, String email, String passwort) {
		Konsole.method("Datenbank.registerAdmin()");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.registerAdmin(vorname, nachname, geschlecht, email, passwort);
			Konsole.antwort("Admin registriert");
		}
	}
	
	//Rückgabewert: -1 gesperrt, 0 falsch, 1 richtig
	static public int login(String email, String passwort) {
		Konsole.method("Datenbank.login(?,?)");
		if(mysql == null) {
			Konsole.noConnection();
			return 0;
		} else {
			int nutzer_id = mysql.login(email, passwort);
			if(nutzer_id == -1) {
				Konsole.antwort("Login fehlgeschlagen");
				return 0;
			} else {
				
				if(mysql.isGesperrt(nutzer_id)) {
					return -1;
				}
				
				Nutzer nutzer = updateNutzerObjekt(nutzer_id,email);
				
				//Systeme: userAgent
				HttpServletRequest request = ((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest());
				String agent = request.getHeader("user-agent").toLowerCase();
				
				Konsole.antwort("Login erfolgreich >> [nutzer_id="+nutzer_id+"],[rang="+nutzer.getRang()+"]");
				addLog(Ereignis.TYPE_LOGIN, nutzer.getVorname()+" "+nutzer.getNachname()+" ("+nutzer.getRangString()+"), "
					+ "IP: "+Sitzung.getIP(null)+", "+UserAgentDetection.BROWSERS[UserAgentDetection.getBrowser(agent)]+", "+UserAgentDetection.OS[UserAgentDetection.getOS(agent)]);
				return 1;
			}
		}
	}
	
	//legt einen Nutzer in die Sitzung ab / aktualisiert diese; gibt für die Weiterverarbeitung das Nutzer-Objekt wieder zurück
	static public Nutzer updateNutzerObjekt(int nutzer_id, String email) {
		
		int rang = mysql.getRang(nutzer_id);
		
		Object klasse_id_object = mysql.getDataTabelleNutzer(MySQLManager.KLASSE_ID, nutzer_id);
		int klasse_id = -1;
		String klasse_name = null;
		int klassenstufe = -1;
		String suffix = null;
		if(klasse_id_object != null) {
			klasse_id = (int) klasse_id_object;
			Object[] klasse = mysql.getKlasse(klasse_id);
			klassenstufe = (int) klasse[0];
			suffix = (String) klasse[1];
			klasse_name = klassenstufe+suffix;
		}
		String vorname = (String) mysql.getDataTabelleNutzer(MySQLManager.VORNAME, nutzer_id);
		String nachname = (String) mysql.getDataTabelleNutzer(MySQLManager.NACHNAME, nutzer_id);
		byte geschlecht = (byte) (int) mysql.getDataTabelleNutzer(MySQLManager.GESCHLECHT, nutzer_id);
		
		Nutzer nutzer = new Nutzer(rang, nutzer_id, klasse_id, vorname, nachname, klasse_name, klassenstufe, suffix, geschlecht,email);
		
		Nutzer old = Sitzung.getNutzer();
		if(old != null) {
			//alte Daten transferieren
			nutzer.setUnlocker(old.getUnlocker());
		}
		
		Sitzung.setNutzer(nutzer);
		
		return nutzer;
	}
	
	static public boolean isGesperrt(int nutzer_id) {
		Konsole.method("Datenbank.isGesperrt("+nutzer_id+")");
		if(mysql == null) {
			Konsole.noConnection();
			return true;
		} else {
			return mysql.isGesperrt(nutzer_id);
		}
	}
	
	static public void setGesperrt(int nutzer_id, boolean gesperrt) {
		Konsole.method("Datenbank.setGesperrt("+nutzer_id+","+gesperrt+")");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.setGesperrt(nutzer_id, gesperrt);
		}
	}
	
	static public boolean isBeitragManager(int nutzer_id) {
		Konsole.method("Datenbank.isBeitragManager("+nutzer_id+")");
		if(mysql == null) {
			Konsole.noConnection();
			return true;
		} else {
			return mysql.isBeitragManager(nutzer_id);
		}
	}
	
	static public void setBeitragManager(int nutzer_id, boolean beitragManager) {
		Konsole.method("Datenbank.setBeitragManager("+nutzer_id+","+beitragManager+")");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			if(beitragManager) {
				//nicht beides
				setRubrikErstellen(nutzer_id, false);
			}
			mysql.setBeitragManager(nutzer_id, beitragManager);
		}
	}
	
	static public boolean isProjektErstellen(int nutzer_id) {
		Konsole.method("Datenbank.isProjektErstellen("+nutzer_id+")");
		if(mysql == null) {
			Konsole.noConnection();
			return true;
		} else {
			return mysql.isProjektErstellen(nutzer_id);
		}
	}
	
	static public void setProjektErstellen(int nutzer_id, boolean projektErstellen) {
		Konsole.method("Datenbank.setProjektErstellen("+nutzer_id+","+projektErstellen+")");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.setProjektErstellen(nutzer_id, projektErstellen);
		}
	}
	
	static public boolean isRubrikErstellen(int nutzer_id) {
		Konsole.method("Datenbank.isRubrikErstellen("+nutzer_id+")");
		if(mysql == null) {
			Konsole.noConnection();
			return true;
		} else {
			return mysql.isRubrikErstellen(nutzer_id); //kein getDataTabelleNutzer, weil komplexer
		}
	}
	
	static public void setRubrikErstellen(int nutzer_id, boolean rubrikErstellen) {
		Konsole.method("Datenbank.setRubrikErstellen("+nutzer_id+","+rubrikErstellen+")");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			if(rubrikErstellen) {
				//nicht beides
				setBeitragManager(nutzer_id, false);
			}
			mysql.setRubrikErstellen(nutzer_id, rubrikErstellen); //kein setDataTabelleNutzer, weil komplexer
		}
	}
	
	static public boolean isVertretung(int nutzer_id) {
		Konsole.method("Datenbank.isVertretung("+nutzer_id+")");
		if(mysql == null) {
			Konsole.noConnection();
			return true;
		} else {
			Object o = mysql.getDataTabelleNutzer(MySQLManager.VERTRETUNGSPLAN, nutzer_id);
			return o != null ? (boolean) o : false;
		}
	}
	
	static public void setVertretung(int nutzer_id, boolean vertretung) {
		Konsole.method("Datenbank.setVertretung("+nutzer_id+","+vertretung+")");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.setDataTabelleNutzer(MySQLManager.VERTRETUNGSPLAN, nutzer_id, vertretung);
		}
	}
	
	static public void deleteAccount(int nutzer_id) {
		Konsole.method("Datenbank.deleteAccount("+nutzer_id+")");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.deleteAccount(nutzer_id);
		}
	}
	
	//Kontrolle des Passworts des BEREITS eingeloggten Nutzers (für mehr Sicherheit)
	static public boolean checkPassword(String passwort) {
		Konsole.method("Datenbank.checkPassword(?,?)");
		if(mysql == null) {
			Konsole.noConnection();
			return false;
		} else {
			String email = Sitzung.getNutzer().getEmail();
			int nutzer_id = mysql.login(email, passwort);
			if(nutzer_id != -1) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	//Kontrolle des Emails des BEREITS eingeloggten Nutzers (für mehr Sicherheit)
		static public boolean checkEmail(String email) {
			Konsole.method("Datenbank.checkPassword(?,?)");
			if(mysql == null) {
				Konsole.noConnection();
				return false;
			} else {
				return email.equals(Sitzung.getNutzer().getEmail());
			}
		}
	
	//Diese Funktion wird durch tien.email.EmailManager.sendCodePasswortVergessen(email) aufgerufen
	//Rückgabewert = Verifizierungscode
	//Wenn null = E-Mail existiert nicht in der Datenbank
	public static String addPasswortVergessen(final String email) {
		Konsole.method("Datenbank.addPasswortVergessen("+email+")");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		}
		else {
			//Nur gültig für eine Zeit lang (festlegbar in der Konfig)
			int timeout = Integer.parseInt(mysql.getKonfig(MySQLManager.KONFIG_TIMEOUT_PASSWORT_VERGESSEN));
			Countdown.start(timeout, new Action() {
				
				@Override
				public void startAction() {
					mysql.deleteCodePasswortVergessen(email);
					Konsole.antwort("Code für 'Passwort-Vergessen' gelöscht");
				}
			}, null);
			return mysql.addPasswortVergessen(email);
		}
	}
	
	public static boolean checkCodePasswortVergessen(String email, String code) {
		Konsole.method("Datenbank.checkCodePasswortVergessen("+email+","+code+")");
		if(mysql == null) {
			Konsole.noConnection();
			return false;
		} else {
			boolean antwort = mysql.checkCodePasswortVergessen(email, code);
			if(antwort == true) {
				Konsole.antwort("Code ist richtig");
				return true;
			} else {
				Konsole.antwort("Code ist NICHT richtig");
				return false;
			}
		}
	}
	
	/**
	 * 
	 * Ändere das Passwort von einem Nutzer
	 * für die Authentifizierung wird NutzerID XOR E-Mail verwendet!
	 * (sobald NutzerID != -1, dann wird NutzerID)
	 * 
	 * @param nutzerID -1, falls E-Mail != null
	 * @param email null, falls NutzerID != -1
	 * @param passwort neues Passwort
	 */
	public static void setPasswort(int nutzerID, String email, String passwort) {
		Konsole.method("Datenbank.setPasswort()");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.setPasswort(nutzerID, email, passwort);
		}
	}
	
	public static void setEmail(String oldEmail, String newEmail) {
		Konsole.method("Datenbank.setEmail("+oldEmail+","+newEmail+")");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.setEmail(oldEmail, newEmail);
			Konsole.antwort("E-Mail gesetzt ( E-Mail = "+newEmail+" )");
		}
	}
	
	public static boolean existEmail(String email) {
		Konsole.method("Datenbank.existEmail("+email+")");
		if(mysql == null) {
			Konsole.noConnection();
			return true;
		} else {
			return mysql.existEmail(email);
		}
	}
	
	//--------------- Nutzer ---------------------------------------------------
	
	//Rang: -1 ALLE, 0 Schüler, 1 Lehrer, 100 Admin
	static public Nutzer[] searchNutzer(String vorname, String nachname, int rang, int klassenstufe) {
		Konsole.method("Datenbank.searchNutzer()");
		
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null) {
			Konsole.noSession();
			return null;
		} else {
			if(vorname != null && vorname.trim().equals(""))
				vorname = null;
			if(nachname != null && nachname.trim().equals(""))
				nachname = null;
			if(vorname == null && nachname == null && rang == -1 && klassenstufe == -1) {
				Konsole.warn("Keine Nutzereingaben");
				return new Nutzer[0];
			}
			
			Nutzer[] founded_nutzer;
				
			if(klassenstufe == -1)
				founded_nutzer = mysql.searchNutzer(vorname, nachname, rang);
			else
				founded_nutzer = mysql.searchNutzer(vorname, nachname, rang, klassenstufe);
			
			if(founded_nutzer != null)
				Konsole.antwort("Nutzer gefunden. Anzahl = "+founded_nutzer.length);
			return founded_nutzer;
		}
	}
	
	static public Nutzer[] getNutzerAllowRubrik() {
		Konsole.method("Datenbank.getNutzerAllowRubrik()");
		return mysql.getNutzerAllowRubrik();
	}
	
	//--------------- Klasse ---------------------------------------------------
	
	static public Nutzer[] getKlassenkameraden() {
		Konsole.method("Datenbank.getKlassenkameraden()");
		Nutzer[] kameraden = null;
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null) {
			Konsole.noSession();
		} else {
			int klasse_id = nutzer.getKlasse_id();
			return mysql.getKlassenkameraden(klasse_id);
		}
		return kameraden;
	}
	
	static public void translateKlassenstufe(int delta) {
		Konsole.method("Datenbank.translateKlassenstufe("+delta+")");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.translateKlassenstufe(delta);
		}
	}
	
	static public void joinKlasse(int nutzer_id, int klasse_id) {
		Konsole.method("Datenbank.joinKlasse("+nutzer_id+","+klasse_id+")");
		
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.joinKlasse(nutzer_id, klasse_id);
			
			String[] name = mysql.getName(nutzer_id);
			String klasse = mysql.getKlassenname(klasse_id);
			addLog(Ereignis.TYPE_JOIN_KLASSE, name[0]+" "+name[1]+", "+klasse);
		}
	}
	
	static public void quitKlasse(int nutzer_id, int klasse_id) {
		Konsole.method("Datenbank.quitKlasse("+nutzer_id+","+klasse_id+")");
		
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.quitKlasse(nutzer_id, klasse_id);
			
			String[] name = mysql.getName(nutzer_id);
			String klasse = mysql.getKlassenname(klasse_id);
			addLog(Ereignis.TYPE_QUIT_KLASSE, name[0]+" "+name[1]+", "+klasse);
		}
	}
	
	static public void cleanKlasse() {
		Konsole.method("Datenbank.cleanKlasse()");
		
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.cleanKlasse();
		}
	}
	
	//--------------- Projekt ---------------------------------------------------
	
	/**
	 * @return Alle Projekte, die mit dem eingeloggten Nutzer assoziiert werden
	 */
	static public Projekt[] getProjektArray(int nutzerID, boolean akzeptiert, boolean lehrerchat) {
		Konsole.method("Datenbank.getProjektArray()");
		return mysql.getProjektArray(nutzerID, akzeptiert, lehrerchat);
	}
	
	static public Projekt[] getAllProjektArray() {
		Konsole.method("Datenbank.getAllProjektArray()");
		Projekt[] projekte = mysql.getAllProjektArray();
		
		if(projekte != null) {
			Konsole.antwort("Alle Projekte von der Datenbank erhalten.");
		}
		return projekte;
	}
	
	/**
	 * @return alle Nutzer, die in dem Projekt sind, mit der Bedingung, dass sie <b>betreuer</b> sind. Wenn betreuer = false, dann ist der Projektleiter mit im Array
	 */
	static public Nutzer[] getNutzerProjektArray(int projekt_id, boolean betreuer) {
		Konsole.method("Datenbank.getNutzerProjektArray()");
		
		return mysql.getNutzerProjektArray(projekt_id, betreuer);
	}

	/**
	 * 
	 * @param projekt_name
	 * @return neu erstelltes Projekt
	 */
	static public Projekt createProjekt(String projekt_name, boolean sofortAkzeptiert, boolean lehrerchat) {
		Konsole.method("Datenbank.createProjekt()");
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null) {
			Konsole.noSession();
			return null;
		} else {
			int nutzer_id = nutzer.getNutzer_id();
			
			Projekt projekt = mysql.createProjekt(nutzer_id, projekt_name, sofortAkzeptiert, lehrerchat);
			
			if(projekt != null) {
				
				if(lehrerchat) {
					Konsole.antwort("Chatraum erstellt");
					addLog(Ereignis.TYPE_CREATE_CHATRAUM, nutzer.getVorname()+" "+nutzer.getNachname()+" ("+nutzer.getRangString()+"), "+projekt_name);
				} else {
					Konsole.antwort("Projekt erstellt");
					addLog(Ereignis.TYPE_CREATE_PROJEKT, nutzer.getVorname()+" "+nutzer.getNachname()+" ("+nutzer.getRangString()+"), "+projekt_name);
				}
			}
			
			return projekt;
		}
	}
	
	/**
	 * @param nutzerID die ID vom Nutzer, der es akzeptiert
	 * @param projektID die ID vom Projekt
	 * @return 0 ok, warte auf andere Betreuer, 1 alle Betreuer haben akzeptiert/Projekt verfügbar
	 */
	static public int projektAkzeptieren(int nutzerID, int projektID) {
		return mysql.projektAkzeptieren(nutzerID, projektID);
	}
	
	static public void forceDeleteProjekt(int projektID) {
		mysql.deleteProjekt(projektID);
	}
	
	static public void renameProjekt(int projektID, String name) {
		mysql.renameProjekt(projektID, name);
	}
	
	static public boolean joinProjekt(Nutzer nutzer, Projekt projekt, boolean betreuer, boolean akzeptiert) {
		Konsole.method("Datenbank.joinProjekt()");
			
		boolean done = mysql.joinProjekt(nutzer.getNutzer_id(), projekt.getProjektID(), betreuer, akzeptiert);
		
		if(done) {
			Konsole.antwort("Projekt/Chatraum beigetreten");
			
			addLog(Ereignis.TYPE_JOIN_PROJEKT, nutzer.getVorname()+" "+nutzer.getNachname()+", "+projekt.getName());
			return true;
		} else {
			return false;
		}
	}
	
	//für Dummy
	static public boolean joinProjekt(int nutzer_id, int projekt_id, boolean betreuer) {
		Konsole.method("Datenbank.joinProjekt()");
			
		boolean done = mysql.joinProjekt(nutzer_id, projekt_id, betreuer, true);
		
		if(done) {
			return true;
		} else {
			return false;
		}
	}
	
	static public boolean existProjekt(int projektID) {
		return mysql.existProjekt(projektID);
	}
	
	/**
	 * 
	 * @param projekt das zuverlassene Projekt
	 * @param nutzer
	 * @return -1 Fehler, 0 OK (verlassen), 1 verlassen und Projekt gelöscht
	 */
	static public int quitNutzerProjekt(Projekt projekt, Nutzer nutzer) {
		Konsole.method("Datenbank.kickNutzerProjekt()");
		
		int done = mysql.quitProjekt(nutzer.getNutzer_id(), projekt.getProjektID());
		
		switch(done) {
		case -1:
			Konsole.warn("Ein unbekannter Fehler ist aufgetreten!");
			return -1;
		case 0:
			Konsole.antwort("Projekt verlassen");
			
			addLog(Ereignis.TYPE_QUIT_PROJEKT, nutzer.getVorname()+" "+nutzer.getNachname()+", "+projekt.getName());
			return 0;
		case 1:
			Konsole.antwort("Projekt verlassen UND Projekt gelöscht (Anzahl Teilnehmer = 0)");
			
			addLog(Ereignis.TYPE_QUIT_PROJEKT, nutzer.getVorname()+" "+nutzer.getNachname()+", "+projekt.getName());
			addLog(Ereignis.TYPE_DELETE_PROJEKT, nutzer.getVorname()+" "+nutzer.getNachname()+", "+projekt.getName());
			return 1;
		default:
			return -1;
		}
	}
	
	//-1 unbekannter fehler, -2 kein Projektleiter, 0 OK
	static public int deleteProjekt(int projekt_id) {
		Konsole.method("Datenbank.deleteProjekt("+projekt_id+")");
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer == null) {
			Konsole.noSession();
			return -1;
		} else {
			int nutzer_id = nutzer.getNutzer_id();
			
			int done;
			if(nutzer.getRang() == 100) {
				mysql.deleteProjekt(projekt_id);
				done = 0;
			} else {
				done = mysql.deleteProjekt(nutzer_id, projekt_id); //Nutzer muss Projektleiter sein
			}
			
			switch(done) {
			case -1:
				Konsole.warn("Ein unbekannter Fehler ist aufgetreten!");
				break;
			case -2:
				Konsole.warn("Der aktuelle Nutzer ist KEIN Projektleiter");
				break;
			case 0:
				Konsole.antwort("Projekt gelöscht");
				break;
			}
			return done;
		}
	}
	
	static public boolean isProjektChat(int projektID) {
		return mysql.isProjektChat(projektID);
	}
	
	static public void setProjektChat(int projektID, boolean chat) {
		mysql.setProjektChat(projektID, chat);
	}
	
	/**
	 * Hinweis: das Projekt <b>muss</b> mit dieser Gruppe verknüpft sein! Vorher also überprüfen!
	 * @param projektID Die ID des Projektes
	 * @return Gruppe
	 */
	static public Gruppe getProjektGruppe(int projektID) {
		Konsole.method("Datenbank.getProjektGruppe()");
		return mysql.getProjektGruppe(projektID);
	}
	
	/**
	 * 
	 * @param projektID Die ID des Projektes
	 * @return Beitrag-Array der verknüpften Gruppe.<p>NULL, wenn keine Gruppe verknüpft
	 */
	static public Beitrag[] getProjektBeitrag(int projektID) {
		Konsole.method("Datenbank.getProjektBeitrag()");
		return mysql.getProjektBeitrag(projektID);
	}
	
	static public void clearProjektGruppe(int projektID) {
		Konsole.method("Datenbank.clearProjektGruppe()");
		mysql.clearProjektGruppe(projektID);
	}
	
	static public void addProjektGruppe(int projektID, int gruppeID) {
		Konsole.method("Datenbank.addProjektGruppe()");
		mysql.addProjektGruppe(projektID, gruppeID);
	}
	
	//--------------- Speicher ---------------------------------------------------
	
	//Ordner und alle Unterordnern, es gibt nicht nur "diese Ordnerebene", weil die Größe des Ordners selbst rekursiv ist
	static public Datei[] getFolderRecursive(int nutzer_klasse_projekt_id, int ordner, Datei.Mode mode) {
		Konsole.method("Datenbank.getFolderRecursive()");
		
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else {
			return mysql.getFolderRecursive(nutzer_klasse_projekt_id, ordner, mode, true);
		}
	}
	
	static public void addFolder(String name, int ordnerID, int nutzer_id, int klasse_projekt_id, Datei.Mode mode) {
		Konsole.method("Datenbank.addFolder()");
		
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.addFolder(nutzer_id, klasse_projekt_id, name, ordnerID, mode);
		}
	}
	
	static public void renameFolder(int ordnerID, String neu) {
		Konsole.method("Datenbank.renameFolder()");
		
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.renameFolder(ordnerID, neu);
		}
	}
	
	static public void deleteFolder(int ordnerID) {
		Konsole.method("Datenbank.deleteFolder()");
		
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.deleteFolder(ordnerID);
		}
	}
	
	//false... Fehler aufgetreten
	static public boolean moveFile(int datei_id, int ordner_id) {
		Konsole.method("Datenbank.moveFile()");
		
		if(mysql == null) {
			Konsole.noConnection();
			return false;
		} else {
			return mysql.moveFile(datei_id, ordner_id);
		}
	}
	
	//false... Fehler aufgetreten
	static public boolean moveFolder(int ordner_id, int parent) {
		Konsole.method("Datenbank.moveFolder()");
		
		if(mysql == null) {
			Konsole.noConnection();
			return false;
		} else {
			return mysql.moveOrdner(ordner_id, parent);
		}
	}
	
	//Dateien einer Ordnerebene
	static public Datei[] getFileArray(int nutzer_klasse_projekt_id, int ordner, Datei.Mode mode) {
		Konsole.method("Datenbank.getFileArray()");
		
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else {
			return mysql.getFileArray(nutzer_klasse_projekt_id, ordner, mode);
		}
	}
	
	static public void setPublic(int datei_id, boolean isPublic) {
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.setPublic(datei_id, isPublic);
	}
	
	static public boolean isPublic(int datei_id) {
		if(mysql == null) {
			Konsole.noConnection();
			return false;
		} else
			return mysql.isPublic(datei_id);
	}
	
	static public Datei[] getDateiArrayAllImages(int nutzer_id) {
		Konsole.method("Datenbank.getDateiArrayAllImages()");
		
		Datei[] datei = mysql.getFileArrayAccess(nutzer_id, "image");
		if(datei != null) {
			return datei;
		} else {
			Konsole.warn("Ein Fehler ist bei der Methode Datenbank.getDateiArrayAllImages() aufgetreten");
		}
		return datei;
	}
	
	/**
	 * maximale Speicherplatz eines Nutzers
	 * 
	 * @param nutzer_id
	 * @return
	 */
	static public int getStorage(int nutzer_id) {
		if(mysql == null) {
			Konsole.noConnection();
			return -1;
		} else
			return mysql.getStorage(nutzer_id);
		
	}
	
	static public int getStorageUsed(int nutzer_id) {
		Konsole.method("Datenbank.getStorageUsed()");
		if(mysql == null) {
			Konsole.noConnection();
			return -1;
		} else
			return mysql.getStorageUsed(nutzer_id);
		
	}
	
	//storage in bytes, false...zu wenig Speicher
	static public boolean editStorage(int nutzer_id, int storage) {
		Konsole.method("Datenbank.editStorage()");
		return mysql.editStorage(nutzer_id, storage);
	}
	
	static public void editStorageKlasse(int klasseID, int storage) {
		Konsole.method("Datenbank.editStorageKlasse()");
		mysql.editStorageKlasse(klasseID, storage);
	}
	
	static public void deleteDatei(Datei datei, Nutzer nutzer) {
		Konsole.method("Datenbank.deleteDatei()");
		
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.deleteFile(datei.getDatei_id(), datei.getPfad());
			addLog(Ereignis.TYPE_DELETE_FILE, nutzer.getVorname()+" "+nutzer.getNachname()+" ("+nutzer.getRangString()+"), "+datei.getDatei_name());
		}
	}
	
	static public void renameDatei(Datei datei, String neu) {
		Konsole.method("Datenbank.renameDatei(): neu = "+neu);
		
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.renameFile(datei, neu);
		}
	}
	
	static public void clearStorage(int nutzer_id) {
		Konsole.method("Datenbank.clearStorage()");
		
		if(mysql == null)
			Konsole.noConnection();
		else
			mysql.clearStorage(nutzer_id);
		
	}
	
	static public Datei getDatei(int dateiID) {
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getFile(dateiID);
	}
	
	//Download
	static public StreamedContent getDateiInhalt(Datei datei) {
		Konsole.method("Datenbank.getDateiInhalt()");
		try {
	        return new DefaultStreamedContent(new FileInputStream(datei.getPfad()), datei.getMime(), datei.getDatei_name());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static public void deleteVertretung() {
		String path = Datenbank.getVertretung();
		if(path != null) {
			try {
				Datenbank.setVertretung(null);
				Files.delete(Paths.get(path)); //auch wenn Datei nicht existiert, wird vorher setVertretung(null) aufgerufen
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	synchronized static public void addFileVertretung(UploadedFile file) {
		Konsole.method("Datenbank.addFileVertretung()");
		InputStream inputStream = null;
		OutputStream outputStream = null;
		
		try {
			String datei_name = Datei.DATEI_VERTRETUNG;
			
			String main_pfad = mysql.getKonfig(MySQLManager.KONFIG_SPEICHERORT);
			String pfad = main_pfad+"/"+datei_name;
			
			//Sitzung überprüfen
			Nutzer nutzer = Sitzung.getNutzer();
			if(Sitzung.isLoggedIn() == false) {
				Konsole.noSession();
				return;
			}
			
			Datenbank.setVertretung(pfad);
			
			//neue Datei schreiben
			inputStream = file.getInputstream();
			outputStream = new FileOutputStream(new File(pfad));

			int read = 0;
			byte[] bytes = new byte[1024];
			
			//JETZT neue Datei schreiben
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.close();
			Konsole.antwort("Datei gespeichert.");
			addLog(Ereignis.TYPE_UPLOAD, "[Vertretungsplan]  "+nutzer.getVorname()+" "+nutzer.getNachname()+" ("+nutzer.getRangString()+"), "+datei_name);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 
	 * @param file Datei von Primefaces-API (das was der Nutzer hochlädt)
	 * @param mode z.B. Datei.MODE_PRIVATSPEICHER
	 * @param klasse_projekt_rubrik_id wird verwendet, falls Projekt- oder Rubrik-Modus
	 * @param ordner_id Ordner ID oder Datei.ORDNER_HOME
	 * @return Datei-Objekt: ENTHÄLT NUR DIE ID!
	 * @throws Exception
	 */
	synchronized static public Datei addFile(UploadedFile file, int ordner_id, int klasse_projekt_rubrik_id, Datei.Mode mode) throws Exception {
		Konsole.method("Datenbank.addFile()");
		boolean success = false;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		
		int next_id = -1;
		try {
			next_id = mysql.getNextID(MySQLManager.DATEI);
			String datei_name = new File(file.getFileName()).getName();
			
			String main_pfad = mysql.getKonfig(MySQLManager.KONFIG_SPEICHERORT);
			String pfad = main_pfad+"/"+next_id+"_"+datei_name;
			
			//Sitzung überprüfen
			Nutzer nutzer = Sitzung.getNutzer();
			if(Sitzung.isLoggedIn() == false) {
				Konsole.noSession();
				throw new Exception("Sitzung abgelaufen");
			}
			
			//Speicherplatz überprüfen
			int storage = Datenbank.getStorage(nutzer.getNutzer_id());
			int storageUsed = Datenbank.getStorageUsed(nutzer.getNutzer_id());
			int storageFree = storage - storageUsed;
			
			if(storageFree < file.getSize()) {
				Konsole.warn("zu wenig Speicherplatz (Server-Validierung)");
				return null;
			}
			
			//Modus überprüfen und ggf. Werte einsetzen
			int eigtum_nutzer_id = (mode == Datei.Mode.RUBRIK) ? -1 : nutzer.getNutzer_id(); //bei Rubrik wird kein Eigentümer gespeichert
			
			//Pfad der Datei inkl. weitere Informationen in die Datenbank speichern
			mysql.addFile(eigtum_nutzer_id, klasse_projekt_rubrik_id, mode, pfad, (int) file.getSize(), file.getContentType(), ordner_id);
			
			//neue Datei schreiben
			inputStream = file.getInputstream();
			outputStream = new FileOutputStream(new File(pfad));

			int read = 0;
			byte[] bytes = new byte[1024];
			
			//JETZT neue Datei schreiben
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.close();
			Konsole.antwort("Datei gespeichert.");
			addLog(Ereignis.TYPE_UPLOAD, "["+Datei.toModeString(mode)+"]  "+nutzer.getVorname()+" "+nutzer.getNachname()+" ("+nutzer.getRangString()+"), "+datei_name);
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		//Datei-Objekt
		if(success == true) {
			return new Datei(next_id);
		} else {
			return null;
		}
	}
	
	static public void updateFileSizeAndPath(int dateiID, int size, String path) {
		mysql.updateFileSizeAndPath(dateiID, size, path);
	}
	
	//--------------- Neuigkeiten / Homepage ---------------------------------------------------
	
	// false...Fehler
	static public boolean createGruppe(int gruppeID, String name, int priory, String link, String password) {
		Konsole.method("Datenbank.createGruppe()");
		Nutzer nutzer = Sitzung.getNutzer();
		if(mysql == null || nutzer == null) {
			Konsole.noConnection();
			return false;
		} else {
			TopMenu.triggerChanged();
			addLog(Ereignis.CREATE_MENU, nutzer.getVorname()+" "+nutzer.getNachname()+" ("+nutzer.getRangString()+"), "+name);
			return mysql.createGruppe(gruppeID,name,priory,link,password);
		}
	}
	
	static public void createRubrik(int nutzerID, String name) {
		Konsole.method("Datenbank.createRubrik()");
		if(mysql == null) {
			Konsole.noConnection();
			return;
		} else
			mysql.createRubrik(nutzerID, name);
	}
	
	static public boolean isGruppePublic(int gruppeID) {
		if(mysql == null) {
			Konsole.noConnection();
			return false;
		} else
			return mysql.isGruppePublic(gruppeID);
	}
	
	//null, falls Nutzer hat keine Rubrik
	static public Rubrik getRubrik(int nutzerID) {
		Konsole.method("Datenbank.getRubrik()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getRubrik(nutzerID);
	}
	
	static public void renameRubrik(int gruppeID, String name) {
		TopMenu.triggerChanged();
		mysql.renameRubrik(gruppeID, name);
	}
	
	static public Datei getRubrikBild(int gruppeID) {
		Konsole.method("Datenbank.getRubrikBild()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getRubrikBild(gruppeID);
	}
	
	static public void deleteRubrikBild(int gruppeID, Nutzer nutzer) {
		Konsole.method("Datenbank.deleteRubrikBild()");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.deleteRubrikBild(gruppeID);
			addLog(Ereignis.TYPE_DELETE_FILE, nutzer.getVorname()+" "+nutzer.getNachname()+" ("+nutzer.getRangString()+"), Rubrik-Bild");
		}
	}

	/***
	 *
	 * @param gruppeID id of group to be deleted
	 * @param name name of the group for logging purposes
	 */
	static public void deleteGruppe(int gruppeID, String name) {
		Konsole.method("Datenbank.deleteGruppe("+gruppeID+")");
		Nutzer nutzer = Sitzung.getNutzer();
		if(mysql == null || nutzer == null) {
			Konsole.noConnection();
		} else {
			mysql.deleteGruppe(gruppeID);
			addLog(Ereignis.DELETE_MENU, nutzer.getVorname()+" "+nutzer.getNachname()+" ("+nutzer.getRangString()+"), "+name);
			TopMenu.triggerChanged();
		}
	}
	
	// false...Fehler
	static public boolean editGruppe(int gruppeID, int parentID, String name, int priory, String link, String password, boolean genehmigt) {
		Konsole.method("Datenbank.editGruppe()");
		Nutzer nutzer = Sitzung.getNutzer();
		if(mysql == null || nutzer == null) {
			Konsole.noConnection();
			return false;
		} else {
			TopMenu.triggerChanged();
			addLog(Ereignis.EDIT_MENU, nutzer.getVorname()+" "+nutzer.getNachname()+" ("+nutzer.getRangString()+"), "+name);
			return mysql.editGruppe(gruppeID, parentID, name,priory,link,password,genehmigt);
		}
	}
	
	static public Gruppe getGruppeBasic(int gruppeID) {
		Konsole.method("Datenbank.getGruppeBasic()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else {
			return mysql.getGruppeBasic(gruppeID);
		}
	}
	
	static public String getGruppePassword(int gruppeID) {
		Konsole.method("Datenbank.getGruppePassword()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else {
			return mysql.getGruppePasswort(gruppeID);
		}
	}
	
	static public Gruppe[] getChilds(int gruppeID) {
		Konsole.method("Datenbank.hasChilds()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else {
			Gruppe[] gruppe = mysql.getGruppeArrayRekursion(gruppeID, null, true);
			Gruppe.sortGruppeRekursive(gruppe);
			return gruppe;
		}
	}
	
	//Rückgabe = ID des hinzugefügten Beitrages
	static public int createBeitrag(int untergruppeID, Nutzer nutzer, String titel, String text, int vorschau, int layoutBeitrag, int layoutVorschau, boolean showAutor) {
		Konsole.method("Datenbank.createBeitrag()");
		if(mysql == null) {
			Konsole.noConnection();
			return -1;
		}
		else {
			mysql.createBeitrag(untergruppeID,nutzer.getNutzer_id(),titel,text,vorschau,layoutBeitrag,layoutVorschau,showAutor);
			
			addLog(Ereignis.TYPE_CREATE_BEITRAG, nutzer.getVorname()+" "+nutzer.getNachname()+" ("+nutzer.getRangString()+"), "+titel);
			return mysql.getNextID(MySQLManager.BEITRAG)-1;
		}
	}
	
	//für Dummy
	static public int createBeitrag(int untergruppeID, int nutzer_id, String titel, String text, int vorschau, boolean showAutor) {
		Konsole.method("Datenbank.createBeitrag()");
		if(mysql == null) {
			Konsole.noConnection();
			return -1;
		}
		else {
			mysql.createBeitrag(untergruppeID,nutzer_id,titel,text,vorschau,ThreadLocalRandom.current().nextInt(0, 4),ThreadLocalRandom.current().nextInt(0, 4),showAutor);
			
			return mysql.getNextID(MySQLManager.BEITRAG)-1;
		}
	}
	
	static public void editBeitrag(int nutzer_id, int beitrag_id, String titel, String text, int vorschau, int layoutBeitrag, int layoutVorschau, boolean showAutor, boolean updateDatum) {
		Konsole.method("Datenbank.editBeitrag()");
		
		mysql.editBeitrag(nutzer_id, beitrag_id, titel, text, vorschau, layoutBeitrag, layoutVorschau, showAutor, updateDatum);
			
		Nutzer nutzer = Sitzung.getNutzer();
		addLog(Ereignis.TYPE_EDIT_BEITRAG, nutzer.getVorname()+" "+nutzer.getNachname()+", IP: "+Sitzung.getIP(null)+", "+titel);
	}
	
	static public void editUnapprovedBeitrag(int beitrag_id, String titel, String text, int vorschau, int layoutBeitrag, int layoutVorschau, boolean showAutor) {
		Konsole.method("Datenbank.editUnapprovedBeitrag()");

		mysql.editUnapprovedBeitrag(beitrag_id, titel, text, vorschau, layoutBeitrag, layoutVorschau, showAutor);
			
		Nutzer nutzer = Sitzung.getNutzer();
		addLog(Ereignis.TYPE_EDIT_BEITRAG, nutzer.getVorname()+" "+nutzer.getNachname()+" ("+nutzer.getRangString()+"), "+titel);
	}
	
	static public void approveBeitrag(Nutzer nutzer, int beitrag_id, int unterGruppeID, String titel, String text, int vorschau, int layoutBeitrag, int layoutVorschau, boolean showAutor, boolean update) {
		Konsole.method("Datenbank.approveBeitrag()");

		mysql.approveBeitrag(nutzer.getNutzer_id(), beitrag_id, unterGruppeID, titel, text, vorschau, layoutBeitrag, layoutVorschau, showAutor, update);
		addLog(Ereignis.TYPE_APPROVE_BEITRAG, nutzer.getVorname()+" "+nutzer.getNachname()+", IP: "+Sitzung.getIP(null)+", "+titel);
	}
	
	static public void connectDateiBeitrag(int datei_id, int beitrag_id) {
		Konsole.method("Datenbank.connectDateiBeitrag("+datei_id+","+beitrag_id+")");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.connectDateiBeitrag(datei_id, beitrag_id);
		}
	}
	
	static public void disconnectDateiBeitrag(int beitrag_id) {
		Konsole.method("Datenbank.disconnectDateiBeitrag("+beitrag_id+")");
		if(mysql == null) {
			Konsole.noConnection();
		} else {
			mysql.disconnectDateiBeitrag(beitrag_id);
		}
	}
	
	//Alle Dateien löschen, die keine NutzerID haben und mit keinem Beitrag verknüpft sind
	static public void cleanDateiBeitrag() {
		Konsole.method("Datenbank.cleanDateiBeitrag()");
		mysql.cleanDateiBeitrag();
	}
	
	static public void deleteBeitrag(int beitragID) {
		Konsole.method("Datenbank.deleteBeitrag("+beitragID+")");
		if(mysql == null) {
			Konsole.noConnection();
			return;
		}
		else
			mysql.deleteBeitrag(beitragID);
	}
	
	static public boolean checkPasswordBeitrag(int beitragID, String password) {
		if(mysql == null) {
			Konsole.noConnection();
			return false;
		}
		else
			return mysql.checkPasswordBeitrag(beitragID, password);
	}
	
	static public void setPasswordBeitrag(int beitragID, String password) {
		Konsole.method("Datenbank.setPasswordBeitrag("+beitragID+",?)");
		if(mysql == null)
			Konsole.noConnection();
		else
			mysql.setPasswordBeitrag(beitragID, password);
	}
	
	//Alle Gruppen und Untergruppen (ABER KEINE BEITRÄGE)
	static public Gruppe[] getGruppeArray() {
		Konsole.method("Datenbank.getGruppeArray()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		}
		else
			return mysql.getGruppeArray();
	}
	
	//Rubrik und dessen Untergruppen (ABER KEINE BEITRÄGE)
	static public Gruppe[] getGruppeArrayRubrik(int nutzerID, boolean includeHome) {
		Konsole.method("Datenbank.getGruppeArrayRubrik()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		}
		else
			return mysql.getGruppeArrayRubrik(nutzerID, includeHome);
	}
	
	static public Rubrik[] getRubrikArray(boolean genehmigt) {
		Konsole.method("Datenbank.getRubrikArray()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		}
		else
			return mysql.getRubrikArray(genehmigt);
	}
	
	static public Beitrag[] getBeitragArray(int unterGruppeID, int limit) {
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else if(unterGruppeID == -1) {
			return null;
		} else
			return mysql.getBeitragArray(unterGruppeID, limit);
	}
	
	static public Beitrag getBeitrag(int beitragID, boolean genehmigt) {
		Konsole.method("Datenbank.getBeitrag("+beitragID+","+genehmigt+")");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else if(beitragID <= 0) {
			return null;
		} else
			return mysql.getBeitrag(beitragID, genehmigt);
	}
	
	static public Beitrag[] getBeitragArray(boolean genehmigt) {
		Konsole.method("Datenbank.getBeitragArray("+genehmigt+")");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getBeitragArray(genehmigt);
	}
	
	static public int getBeitragAnzahl(boolean genehmigt) {
		Konsole.method("Datenbank.getBeitragAnzahl("+genehmigt+")");
		if(mysql == null) {
			Konsole.noConnection();
			return 0;
		} else
			return mysql.getBeitragAnzahl(genehmigt);
	}
	
	static public Beitrag[] getUnapprovedBeitragArray(int nutzer_id) {
		Konsole.method("Datenbank.getUnapprovedBeitragArray("+nutzer_id+")");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getUnapprovedBeitragArray(nutzer_id);
	}
	
	static public boolean isApproved(int beitrag_id) {
		Konsole.method("Datenbank.getApproved("+beitrag_id+")");
		if(mysql == null) {
			Konsole.noConnection();
			return true;
		} else
			return mysql.isApproved(beitrag_id);
	}
	
	static public boolean existBeitrag(int beitrag_id) {
		Konsole.method("Datenbank.existBeitrag("+beitrag_id+")");
		if(mysql == null) {
			Konsole.noConnection();
			return true;
		} else
			return mysql.existBeitrag(beitrag_id);
	}
	
	static public String getGruppeName(int gruppeID) {
		Konsole.method("Datenbank.getGruppeName("+gruppeID+")");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else if(gruppeID == -1) {
			return null;
		} else
			return mysql.getGruppeName(gruppeID);
	}
	
	//--------------- Logs ---------------------------------------------------
	
	static public void addLog(Ereignis type, String ereignis) {
		Konsole.method("Datenbank.addLog()");
		mysql.addLog(ereignis, type.id);
	}
	
	//--------------- Stats ---------------------------------------------------
	
	static public long getStatsTotal() {
		Konsole.method("Datenbank.getStatsTotal()");
		if(mysql == null) {
			Konsole.noConnection();
			return -1;
		} else
			return mysql.getKonfigLong(MySQLManager.KONFIG_TOTAL_VIEWS);
	}
	
	static public void setStatsTotal(long views) {
		Konsole.method("Datenbank.setStatsTotal()");
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.setKonfigLong(MySQLManager.KONFIG_TOTAL_VIEWS,views);
	}
	
	static public Count[] getStatsTage() {
		Konsole.method("Datenbank.getStatsTage()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getStatsTage();
	}
	
	static public long[] getStatsStunden() {
		Konsole.method("Datenbank.getStatsStunden()");
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getStatsStunden();
	}
	
	static public void resetStatsTage() {
		Konsole.method("Datenbank.resetStatsTage()");
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.resetStatsTage();
	}
	
	static public void resetStatsStunden() {
		Konsole.method("Datenbank.resetStatsStunden()");
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.resetStatsStunden();
	}
	
	static public int getStatsStundenIndex() {
		if(mysql == null) {
			Konsole.noConnection();
			return -1;
		} else
			return mysql.getKonfigInt(MySQLManager.KONFIG_STATS_STUNDEN_INDEX);
	}
	
	static public void setStatsStundenIndex(int index) {
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.setKonfigInt(MySQLManager.KONFIG_STATS_STUNDEN_INDEX, index);
	}
	
	static public int saveStats(String dateString, int amountDay, int indexHour, int hour) {
		Konsole.method("Datenbank.saveStats()");
		if(mysql == null) {
			Konsole.noConnection();
			return -1;
		} else
			return mysql.saveStats(dateString, amountDay, indexHour, hour);
	}
	
	static public void saveStats(int dateID, int amountDay, int indexHour, int hour) {
		Konsole.method("Datenbank.saveStats()");
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.saveStats(dateID, amountDay, indexHour, hour);
	}
	
	//--------------- Chat ---------------------------------------------------
	
	static public Nachricht[] getNachricht(int projektID, boolean lehrerchat, int maxAnzahlNutzer) {
		Konsole.method("Datenbank.getNachricht()");
		return mysql.getNachricht(projektID, lehrerchat, maxAnzahlNutzer);
	}
	
	static public void clearNachrichten(int projektID) {
		Konsole.method("Datenbank.clearNachrichten()");
		mysql.clearNachrichten(projektID);
	}
	
	static public void deleteNachricht(int nachrichtID) {
		Konsole.method("Datenbank.deleteNachricht()");
		mysql.deleteNachricht(nachrichtID);
	}
	
	/**
	 * Polling
	 * @param nutzerID NutzerID, damit der Zeitstempel gesetzt werden kann
	 * @param projektID chat im Projekt
	 * @param lehrerchat wird gerade aus dem Lehrerchat gepollt?
	 * @return Anzahl der Nachrichten
	 */
	static public int getNachrichtAnzahl(int nutzerID, int projektID, boolean lehrerchat, boolean updateZeitstempel) {
		Konsole.method("Datenbank.getNachrichtAnzahl()");
		return mysql.getNachrichtAnzahl(nutzerID, projektID, lehrerchat, updateZeitstempel);
	}
	
	static public void addNachricht(int nutzerID, int projektID, boolean lehrerchat, String text) {
		Konsole.method("Datenbank.addNachricht()");
		mysql.addNachricht(nutzerID, projektID, lehrerchat, text);
	}
	
	static public void editNachricht(int nachrichtID, String text) {
		Konsole.method("Datenbank.editNachricht()");
		mysql.editNachricht(nachrichtID, text);
	}
	
	static public Nutzer[] getLehrerChat() {
		Konsole.method("Datenbank.getLehrerChat()");
		return mysql.getLehrerChat();
	}
	
	//--------------- Slides ---------------------------------------------------
	
	static public ArrayList<Slide> getSlides() {
		if(mysql == null) {
			Konsole.noConnection();
			return null;
		} else
			return mysql.getSlides();
	}
	
	synchronized static public void addSlide(UploadedFile file, int index, String title, String sub, String direction) {
		Konsole.method("Datenbank.addSlide()");
		InputStream inputStream = null;
		OutputStream outputStream = null;
		
		try {
			
	        String randomUUIDString = UUID.randomUUID().toString().replace('-', '_');
			String datei_name = "slider_"+randomUUIDString+".jpg";
			
			String main_pfad = mysql.getKonfig(MySQLManager.KONFIG_SPEICHERORT);
			String pfad = main_pfad+"/"+datei_name;
			
			//Sitzung überprüfen
			Nutzer nutzer = Sitzung.getNutzer();
			if(Sitzung.isLoggedIn() == false) {
				Konsole.noSession();
				return;
			}
			
			mysql.addSlide(index, pfad, title, sub, direction);
			
			//neue Datei schreiben
			inputStream = file.getInputstream();
			outputStream = new FileOutputStream(new File(pfad));

			int read = 0;
			byte[] bytes = new byte[1024];
			
			//JETZT neue Datei schreiben
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			
			outputStream.close();
			ImageTools.resize(pfad, 1600, 1000);
			
			Konsole.antwort("Datei gespeichert.");
			addLog(Ereignis.TYPE_UPLOAD, "[Diashow]  "+nutzer.getVorname()+" "+nutzer.getNachname()+" ("+nutzer.getRangString()+"), "+datei_name);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	static public void modifySlide(int index, String title, String sub, String direction) {
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.modifySlide(index, title, sub, direction);
	}
	
	static public void swapSlides(Slide slide1, Slide slide2) {
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.swapSlides(slide1, slide2);
	}
	
	static public void deleteSlide(int index) {
		if(mysql == null) {
			Konsole.noConnection();
		} else
			mysql.deleteSlide(index);
	}
}
