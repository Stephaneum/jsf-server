package mysql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import objects.Beitrag;
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
import sitzung.ViewCounter;
import tools.Timeformats;

public class MySQLManager {
	
	/*
	 * Java <-> Datenbank (MySQL, MariaDB)
	 */
	
	private long lastSQLSended;
	static private long MAX_INTERVAL = 3600000; //1 Stunde
	private String DATABASE_NAME; //Wird bei instanzierung festgelegt
	final static private String PEPPER = "A43w8pa0M245qga4293zt9o4mc3z98TA3nQ9mzvTa943cta43mTaoz47tz3loIhbiKh";
	
	//KONFIG
	final static String KONFIG_SPEICHERORT = "speicherort";
	final static String KONFIG_TIMEOUT_PASSWORT_VERGESSEN = "timeout_passwort_vergessen"; //millisec
	final static String KONFIG_VERSION = "version";
	final static String KONFIG_STORAGE_LEHRER = "storage_lehrer";
	final static String KONFIG_STORAGE_SCHUELER = "storage_schueler";
	final static String KONFIG_STRING_KONTAKT = "str_kontakt";
	final static String KONFIG_STRING_IMPRESSUM = "str_impressum";
	final static String KONFIG_STRING_BOTTOM = "str_bottom";
	final static String KONFIG_STRING_ENTWICKLER = "str_entwickler";
	final static String KONFIG_STRING_TICKER = "str_liveticker";
	final static String KONFIG_STRING_VERTRETUNG = "str_vertretung";
	final static String KONFIG_MERGE_AKTUELLES = "merge_aktuelles";
	final static String KONFIG_PICTURE_SIZE = "picture_size";
	final static String KONFIG_SHOW_SYSTEM = "show_system";
	final static String KONFIG_SHOW_SOFTWARE = "show_software";
	final static String KONFIG_STRING_HISTORY = "str_history";
	final static String KONFIG_REDIRECT_PORT = "redirect_port";
	final static String KONFIG_DEFAULT_GRUPPE_ID = "default_gruppe_id";
	final static String KONFIG_STRING_VERTRETUNG_INFO = "str_vertretung_info";
	final static String KONFIG_SHOW_CLOUD = "show_cloud";
	final static String KONFIG_TOTAL_VIEWS = "total_views";
	final static String KONFIG_STATS_STUNDEN_INDEX = "stats_stunden_index";
	final static String KONFIG_STRING_EU_SA = "str_eu_sa";
	final static String KONFIG_BACKUP_DIR = "backup_dir";
	
	final static String KONFIG_TERMINE = "str_termine";
	final static String KONFIG_KOOP = "str_koop";
	final static String KONFIG_KOOP_URL = "str_koop_url";
	final static int VALUE_VERSION = 35; //AKTUELLE VERSION
	final static int VALUE_TIMEOUT_PASSWORT_VERGESSEN = 60*60*1000; //1 Stunde in Millisekunden STANDARD
	final static int VALUE_STORAGE_LEHRER = Datei.convertToBytes(200, Datei.SIZE_MB); //200 MB
	final static int VALUE_STORAGE_SCHUELER = Datei.convertToBytes(100, Datei.SIZE_MB); //100 MB
	final static int VALUE_MERGE_AKTUELLES = 0; //nein
	final static int VALUE_PICTURE_SIZE = Datei.convertToBytes(250, Datei.SIZE_KB);
	final static int VALUE_SHOW_SYSTEM = 1; //ja
	final static int VALUE_SHOW_SOFTWARE = 1; //ja
	final static int VALUE_REDIRECT_PORT = 0; //nein
	final static int VALUE_DEFAULT_GRUPPE_ID = -1; //noch keine Gruppe
	final static int VALUE_SHOW_CLOUD = 1; //ja
	final static int VALUE_TOTAL_VIEWS = 0; //fängt bei einem neuen System bei 0 an
	final static int VALUE_STATS_STUNDEN_INDEX = 0; //fängt bei einem neuen System bei 0 an
	final static String VALUE_BACKUP_DIR = "/opt/backup";
	final static String VALUE_TERMINE = "beitrag.xhtml?id=266";
	final static String VALUE_KOOP = "Dänemark;Finnland;Frankreich;Italien;Niederlande;Norwegen;Polen";
	final static String VALUE_KOOP_URL = "http://www.czechbc.de/contao/index.php/kooperationspartner.html";
	
	//SEND QUERY
	final static int TYPE_INT = 1, TYPE_STRING = 2, TYPE_BOOLEAN = 3, TYPE_BYTE = 4;
	final static int TYPE_INT_NULL = -1, TYPE_STRING_NULL = -2, TYPE_BOOLEAN_NULL = -3;
	
	//TABELLEN-NAMEN
	final static int ZUGANGSCODE = 0,
					KLASSE = 1,
					GRUPPE = 2,
					NUTZER = 3,
					PROJEKT = 4,
					BEITRAG = 5,
					ORDNER = 6,
					DATEI = 7,
					NACHRICHT = 8,
					NUTZER_PROJEKT = 9,
					PROJEKT_GRUPPE = 10,
					DATEI_BEITRAG = 11,
					KONFIG = 12,
					LOG = 13,
					STATS_CLOUD = 14,
					STATS_TAGE = 15,
					STATS_STUNDEN = 16,
					SLIDER = 17,
					STATIC_FILES = 18;
	//SPALTEN-NAMEN
	final static int ID = 0,//zugangscode
					CODE = 1,
					RANG = 2,
					BENUTZT = 3,
					KLASSENSTUFE = 1,//klasse
					SUFFIX = 2,
					NAME = 1, //gruppe
					GRP_ID = 2, 
					PRIORY = 3,
					LINK = 4,
					RUBRIK_LEITER = 5,
					DATEI_ID = 6,
					PASSWORT = 7,
					GENEHMIGT = 8,
					CODE_ID = 1,//nutzer
					KLASSE_ID = 4,
					VORNAME = 2,
					NACHNAME = 3,
					GESCHLECHT = 5,
					EMAIL = 6,
					CODE_PASSWORT_VERGESSEN = 8,
					STORAGE = 9,
					GESPERRT = 10,
					BEITRAG_MANAGER = 11,
					PROJEKT_ERSTELLEN = 12,
					RUBRIK_ERSTELLEN = 13,
					VERTRETUNGSPLAN = 14,
					LEHRERCHAT_DATUM = 15,
					LEITER_NUTZER_ID = 2, //projekt
					AKZEPTIERT = 3,
					CHAT = 4,
					PROJEKT_LEHRERCHAT = 5,
					TITEL = 3, //beitrag
					TEXT = 4,
					DATUM = 5,
					NUTZER_ID_UPDATE = 6,
					VORSCHAU = 9,
					SHOW_AUTOR = 10,
					LAYOUT_BEITRAG = 11,
					LAYOUT_VORSCHAU = 12,
					NUTZER_ID = 1,//datei
					PFAD = 2,
					PROJEKT_ID = 3,
					SIZE = 6,
					MIME = 7,
					PUBLIC = 8,
					DATEI_LEHRERCHAT = 9,
					ORDNER_ID = 10,
					STRING = 2, //nachricht
					NACHRICHT_LEHRERCHAT = 4,
					MN_PROJEKT_ID = 0,//nutzer_projekt
					MN_BETREUER = 2,
                    MN_NUTZER_PROJEKT_ID = 4,
					MN_GRUPPE_ID = 1, //projekt_gruppe
					MN_DATEI_ID = 1,//datei_beitrag
					MN_BEITRAG_ID = 2,
					VARIABLE = 1,//konfig
					KONFIG_WERT = 2,
					LOG_DATUM = 1, //LOG
					LOG_TYP = 2,
					LOG_EREIGNIS = 3,
					EIGENTUM = 2, //ordner
					ORDNER_LEHRERCHAT = 5,
					PARENT = 6,
					STATS_DATE = 0, //stats_cloud
					STATS_SIZE = 1,
                    STATS_CLOUD_ID = 2,
					STATS_TAGE_DATUM = 1, //stats_tage
					STATS_TAGE_ANZAHL = 2,
					STATS_STUNDEN_INDEX = 0,
					STATS_STUNDEN_UHRZEIT = 1, //stats_stunden
					SLIDER_INDEX = 0, //slider
					SLIDER_PATH = 1,
					SLIDER_TITLE = 2,
					SLIDER_SUB = 3,
					SLIDER_DIRECTION = 4,
					STATIC_PATH = 0,
					STATIC_MODE = 1;
					
	final static String[] TABLE = {
			"zugangscode",
			"klasse",
			"gruppe",
			"nutzer",
			"projekt",
			"beitrag",
			"ordner",
			"datei",
			"nachricht",
			"nutzer_projekt",
			"projekt_gruppe",
			"datei_beitrag",
			"konfig",
			"log",
			"stats_cloud",
			"stats_tage",
			"stats_stunden",
			"slider",
			"static_files"
	};
	final static String[][][] DB = {
			{//zugangscode
				{"id","INT NOT NULL AUTO_INCREMENT"},
				{"code","VARCHAR(32)"},
				{"rang","INT"},
				{"benutzt","TINYINT(1)"},
				
				{"PRIMARY KEY(id)",""} //CODE.ID
			},{ //klasse
				{"id", "INT NOT NULL AUTO_INCREMENT"},
				{"stufe","INT"},
				{"suffix","VARCHAR(3)"},
				
				{"PRIMARY KEY(id)",""} //KLASSE.ID
			},{ //Gruppe
				{"id","INT NOT NULL AUTO_INCREMENT"},
				{"name","VARCHAR(32)"},
				{"grp_id","INT"},
				{"priory","INT"},
				{"link","TEXT"},
				{"rubrik_leiter","INT"},
				{"datei_id","INT"},
				{"passwort","CHAR(32)"},
				{"genehmigt","TINYINT(1)"},
				
				{"PRIMARY KEY(id)",""}, //GRP.ID
				{"FOREIGN KEY (grp_id) REFERENCES gruppe(id) ON DELETE CASCADE",""}, //-->GRP.ID
				{"FOREIGN KEY (rubrik_leiter) REFERENCES nutzer(id) ON DELETE SET NULL",""},  //-->NUTZER:ID
				{"FOREIGN KEY (datei_id) REFERENCES datei(id) ON DELETE SET NULL",""}, //-->DATEI.ID
			},{ //nutzer
				{"id","INT NOT NULL AUTO_INCREMENT"},
				{"code_id","INT"},
				{"vorname","VARCHAR(100)"},
				{"nachname","VARCHAR(100)"},
				{"klasse_id","INT"},
				{"geschlecht","TINYINT"}, //0 = weiblich, 1 = männlich
				{"email","VARCHAR(100)"},
				{"passwort","VARCHAR(255)"},
				{"code_passwort_vergessen","VARCHAR(32)"},
				{"storage","INT"}, //in Bytes
				{"gesperrt","TINYINT(1)"},
				{"beitrag_manager","TINYINT(1)"},
				{"projekt_erstellen","TINYINT(1)"},
				{"rubrik_erstellen","TINYINT(1)"},
				{"vertretungsplan","TINYINT(1)"},
				{"lehrerchat_datum","DATETIME"},
				
				{"PRIMARY KEY(id)",""}, //NUTZER.ID
				{"FOREIGN KEY (code_id) REFERENCES zugangscode(id) ON DELETE CASCADE",""}, //-->CODE:ID
				{"FOREIGN KEY (klasse_id) REFERENCES klasse(id) ON UPDATE CASCADE",""} //-->KLASSE:ID
			},{ //projekt
				{"id","INT NOT NULL AUTO_INCREMENT"},
				{"name","VARCHAR(100)"},
				{"leiter_nutzer_id","INT"},
				{"akzeptiert","TINYINT(1)"},
				{"chat","TINYINT(1) DEFAULT 1"},
				{"lehrerchat","TINYINT(1) DEFAULT 0"},
				
				{"PRIMARY KEY(id)",""}, //PROJEKT.ID
				{"FOREIGN KEY (leiter_nutzer_id) REFERENCES nutzer(id) ON DELETE CASCADE",""}  //-->NUTZER:ID
			},{ //Beitrag
				{"id","INT NOT NULL AUTO_INCREMENT"},
				{"nutzer_id","INT"},
				{"grp_id","INT"},
				{"titel","VARCHAR(128)"},
				{"text","TEXT"},
				{"datum","DATETIME DEFAULT CURRENT_TIMESTAMP"},
				{"nutzer_id_update","INT"},
				{"passwort","CHAR(32)"},
				{"genehmigt","TINYINT(1)"},
				{"vorschau","INT"},
				{"show_autor","TINYINT(1)"},
				{"layout_beitrag","INT DEFAULT 0"},
				{"layout_vorschau","INT DEFAULT 0"},
				
				{"PRIMARY KEY(id)",""}, //BEITRAG.ID
				{"FOREIGN KEY (nutzer_id) REFERENCES nutzer(id) ON DELETE CASCADE",""}, //-->NUTZER.ID
				{"FOREIGN KEY (grp_id) REFERENCES gruppe(id) ON DELETE CASCADE",""}, //-->UNTERGRP.ID
				{"FOREIGN KEY (nutzer_id_update) REFERENCES nutzer(id) ON DELETE CASCADE",""}, //-->NUTZER.ID
			},{ //ordner
				{"id","INT NOT NULL AUTO_INCREMENT"},
				{"name","TEXT"},
				{"eigentum","INT"},
				{"projekt_id","INT"},
				{"klasse_id","INT"},
				{"lehrerchat","TINYINT(1)"},
				{"parent","INT"},
				
				{"PRIMARY KEY(id)",""}, //ORDNER.ID
				{"FOREIGN KEY (eigentum) REFERENCES nutzer(id) ON DELETE CASCADE",""}, //-->NUTZER.ID
				{"FOREIGN KEY (projekt_id) REFERENCES projekt(id) ON DELETE CASCADE",""}, //-->PROJEKT.ID
				{"FOREIGN KEY (klasse_id) REFERENCES klasse(id) ON DELETE CASCADE",""}, //-->KLASSE.ID
				{"FOREIGN KEY (parent) REFERENCES ordner(id) ON DELETE CASCADE",""}, //-->ORDNER.ID
			},{ //datei
				{"id","INT NOT NULL AUTO_INCREMENT"},
				{"nutzer_id","INT"},
				{"pfad","VARCHAR(1024)"},
				{"projekt_id","INT"},
				{"klasse_id","INT"},
				{"datum","DATETIME DEFAULT CURRENT_TIMESTAMP"},
				{"size","INT"}, //in Bytes
				{"mime_type","VARCHAR(255)"},
				{"public","TINYINT(1)"},
				{"lehrerchat","TINYINT(1)"},
				{"ordner_id","INT"},
				
				{"PRIMARY KEY(id)",""}, //DATEI.ID
				{"FOREIGN KEY (nutzer_id) REFERENCES nutzer(id) ON DELETE CASCADE",""}, //-->NUTZER.ID
				{"FOREIGN KEY (projekt_id) REFERENCES projekt(id) ON DELETE CASCADE",""}, //-->PROJEKT.ID
				{"FOREIGN KEY (klasse_id) REFERENCES klasse(id) ON DELETE CASCADE",""}, //-->KLASSE.ID
				{"FOREIGN KEY (ordner_id) REFERENCES ordner(id) ON DELETE SET NULL",""}, //-->ORDNER.ID
			},{ //nachricht
				{"id","INT NOT NULL AUTO_INCREMENT"},
				{"nutzer_id","INT"},
				{"string","TEXT"},
				{"projekt_id","INT"},
				{"lehrerchat","TINYINT(1)"},
				{"datum","DATETIME DEFAULT CURRENT_TIMESTAMP"},
				
				{"PRIMARY KEY(id)",""}, //NACHRICHT.ID
				{"FOREIGN KEY (nutzer_id) REFERENCES nutzer(id) ON DELETE CASCADE",""}, //-->NUTZER.ID
				{"FOREIGN KEY (projekt_id) REFERENCES projekt(id) ON DELETE CASCADE",""}, //-->PROJEKT.ID
			},{ //nutzer_projekt
				{"projekt_id","INT"},
				{"nutzer_id","INT"},
				{"betreuer","TINYINT(1)"},
				{"akzeptiert","TINYINT(1)"},
                {"id", "INT"},

                {"PRIMARY KEY(id)",""}, //NUTZER_PROJEKT.ID
				{"FOREIGN KEY (projekt_id) REFERENCES projekt(id) ON DELETE CASCADE",""}, //-->PROJEKT.ID
				{"FOREIGN KEY (nutzer_id) REFERENCES nutzer(id) ON DELETE CASCADE",""},  //-->NUTZER.ID
			},{ //projekt_gruppe
				{"projekt_id","INT"},
				{"gruppe_id","INT"},
				{"id","INT"},

				{"PRIMARY KEY(id)",""}, //PROJEKT_GRUPPE.ID
				{"FOREIGN KEY (projekt_id) REFERENCES projekt(id) ON DELETE CASCADE",""}, //-->PROJEKT.ID
				{"FOREIGN KEY (gruppe_id) REFERENCES gruppe(id) ON DELETE CASCADE",""},  //-->GRUPPE.ID
			},{ //datei_beitrag
                {"id", "INT"},
				{"datei_id","INT"},
				{"beitrag_id","INT"},

                {"PRIMARY KEY(id)",""}, //DATEI_BEITRAG.ID
				{"FOREIGN KEY (datei_id) REFERENCES datei(id) ON DELETE CASCADE",""}, //-->DATEI.ID
				{"FOREIGN KEY (beitrag_id) REFERENCES beitrag(id) ON DELETE CASCADE",""},  //-->BEITRAG.ID	
			},{ //konfig
				{"id","INT"},
				{"variable","VARCHAR(64)"},
				{"wert","TEXT"},

                {"PRIMARY KEY(id)",""} //KONFIG.ID
			},{ //log
				{"id", "INT"},
				{"datum","DATETIME DEFAULT CURRENT_TIMESTAMP"},
				{"typ","INT"},
				{"ereignis","TEXT"},

				{"PRIMARY KEY(id)",""} //LOG.ID
			},{ //statistiken: Cloud
				{"datum","DATETIME DEFAULT CURRENT_TIMESTAMP"},
				{"size","INT"}, //in bytes
                {"id","INT NOT NULL AUTO_INCREMENT"},

                {"PRIMARY KEY(id)",""},
			},{ //statistiken: Tage
				{"id","INT NOT NULL AUTO_INCREMENT"},
				{"datum","DATETIME DEFAULT CURRENT_TIMESTAMP"},
				{"anzahl","INT"},
				{"PRIMARY KEY(id)",""},
			},{ //statistiken: Stunden
				{"idx","INT"}, //beginnt bei 0, wird von java-logik bestimmt
				{"uhrzeit","TINYINT"},
				{"PRIMARY KEY(idx)",""},
			},{ //slider
				{"idx","INT"}, //beginnt bei 0, wird von java-logik bestimmt
				{"path","VARCHAR(1024)"},
				{"title","VARCHAR(256)"},
				{"sub","VARCHAR(256)"},
				{"direction","VARCHAR(64)"}
			},{ //static files
				{"path","VARCHAR(1024)"},
				{"mode","TINYINT"}
			},
		};
	private Connection conn = null;
	private boolean existDatabase = false, needUpdate = false;
	private Updater updater;
	
	MySQLManager(String database_name) {
		this.DATABASE_NAME = database_name;
		this.updater = new Updater(this);
	}
	
	//dummy
	public MySQLManager() { }
	
	private String name, passwort, ip, port;
	boolean connect(String name, String password, String ip, String port) {
		this.name = name;
		this.passwort = password;
		this.ip = ip;
		this.port = port;
		
		try {
		    Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
		    throw new IllegalStateException("Cannot find the driver in the classpath!", e);
		}
		
		//eventuell alte Connection schließen
		if(conn != null) {
			try {
				conn.close();
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
		try {
			String URL = "jdbc:mysql://"+ip+":"+port+"/";
			conn = DriverManager.getConnection(URL,name,password);
			lastSQLSended = System.currentTimeMillis();
			Konsole.antwort("Verbindung zu MySQL aufgebaut");
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		existDatabase = existDatabase();
		if(existDatabase == true) {
			try {
				sendQuery("USE "+DATABASE_NAME,false);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			try {
				ResultSet rs = sendQuery("SELECT "+DB[KONFIG][KONFIG_WERT][0]+" FROM "+TABLE[KONFIG]+" WHERE "+DB[KONFIG][VARIABLE][0]+"='"+KONFIG_VERSION+"'", true);
				rs.next();
				int currVersion = rs.getInt(1);
				rs.close();
				
				if(currVersion < VALUE_VERSION) {
					needUpdate = true;
				} else {
					Konsole.update("up-to-date");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			
		} else {
			Konsole.antwort("Datenbankstruktur noch nicht erstellt.");
		}
		return true;
	}
	
	//--------------- Senden der Befehle ---------------------------------------------------
	
	protected ResultSet sendQuery(String befehl, boolean resultSet) throws SQLException {
		if(System.currentTimeMillis() - lastSQLSended > MAX_INTERVAL) {
			Konsole.antwort("RECONNECT");
			connect(name,passwort,ip,port);
		}
		
		lastSQLSended = System.currentTimeMillis();
		if(updating)
			Konsole.update(befehl);
		Konsole.sql(befehl);
		Statement statement = conn.createStatement();
		if(resultSet) {
			return statement.executeQuery(befehl);
		} else {
			statement.executeUpdate(befehl);
			return null;
		}
	}
	
	//effizient für viele sql-Befehle
	//unterbindet SQL-Injection
	//parameter[zeile][position des ?]
	//wenn nur 1 zeile (1. index hat länge 1) --> nur ein sql-Befehl ausgeführt; BITTE DANN DIE UNTERE sendQueryPrepared METHODE NUTZEN!
	protected void sendQueryPrepared(String sql, Object[][] parameter, int[] type) throws SQLException {
		if(System.currentTimeMillis() - lastSQLSended > MAX_INTERVAL) {
			Konsole.antwort("RECONNECT");
			connect(name,passwort,ip,port);
		}
		
		lastSQLSended = System.currentTimeMillis();
		
		Konsole.sql(sql);
		
		PreparedStatement statement = conn.prepareStatement(sql);
		
		for(int i = 0; i < parameter.length; i++) {
			for(int p = 0; p < parameter[0].length; p++) {
				switch(type[p]) {
				case TYPE_INT_NULL:
					statement.setNull(p+1, Types.INTEGER);
					break;
				case TYPE_STRING_NULL:
					statement.setNull(p+1, Types.VARCHAR);
					break;
				case TYPE_BOOLEAN_NULL:
					statement.setNull(p+1, Types.BOOLEAN);
					break;
				case TYPE_INT:
					statement.setInt(p+1, (int)parameter[i][p]);
					break;
				case TYPE_STRING:
					statement.setString(p+1, (String)parameter[i][p]);
					break;
				case TYPE_BOOLEAN:
					statement.setBoolean(p+1, (boolean)parameter[i][p]);
					break;
				case TYPE_BYTE:
					statement.setByte(p+1, (byte)parameter[i][p]);
					break;
				default:
					Konsole.warn("Typ des "+(int)(p+1)+".Parameters der SQL-Anweisung konnte nicht identifiziert werden.");
				}
			}
			statement.executeUpdate();
		}
		statement.close();
	}
	
	//sendet 1 PreparedStatement
	//unterbindet SQL-Injection
	protected ResultSet sendQueryPrepared(String sql, Object[] parameter, int[] type, boolean resultSet) throws SQLException {
		
		if(System.currentTimeMillis() - lastSQLSended > MAX_INTERVAL) {
			Konsole.antwort("RECONNECT");
			connect(name,passwort,ip,port);
		}
		
		lastSQLSended = System.currentTimeMillis();
		
		Konsole.sql(sql);
		
		PreparedStatement statement = conn.prepareStatement(sql);
		
		for(int i = 0; i < parameter.length; i++) {
			switch(type[i]) {
			case TYPE_INT_NULL:
				statement.setNull(i+1, Types.INTEGER);
				break;
			case TYPE_STRING_NULL:
				statement.setNull(i+1, Types.VARCHAR);
				break;
			case TYPE_BOOLEAN_NULL:
				statement.setNull(i+1, Types.BOOLEAN);
				break;
			case TYPE_INT:
				statement.setInt(i+1, (Integer)parameter[i]);
				break;
			case TYPE_STRING:
				statement.setString(i+1, (String)parameter[i]);
				break;
			case TYPE_BOOLEAN:
				statement.setBoolean(i+1, (Boolean)parameter[i]);
				break;
			default:
				Konsole.warn("Typ des "+(int)(i+1)+".Parameters der SQL-Anweisung konnte nicht identifiziert werden.");
			}
		}
		
		if(resultSet)
			return statement.executeQuery();
		else {
			statement.executeUpdate();
			statement.close();
			return null;
		}
			
	}
	
	//--------------- Datenbank-Konfiguration ---------------------------------------------------
	
	//0 bis 60%
	void createDatabase(ProgressObject progress) {
		Statement statement = null;
		try {
			statement = conn.createStatement();
			statement.executeUpdate("SET foreign_key_checks = 0");
			statement.executeUpdate("CREATE DATABASE IF NOT EXISTS "+DATABASE_NAME);
			statement.executeUpdate("USE "+DATABASE_NAME);
			StringBuilder string = new StringBuilder();
			for(int a = 0; a < DB.length; a++) {
				string.setLength(0); //StringBuilder leeren
				string.append("CREATE TABLE ");
				string.append(TABLE[a]);
				string.append(" (");
				for(int b = 0; b < DB[a].length; b++) {
					if(b>0)
						string.append(',');
					string.append(DB[a][b][0]);
					string.append(' ');
					string.append(DB[a][b][1]);
				}
				string.append(")ENGINE=INNODB");
				Konsole.sql(string.toString());
				statement.executeUpdate(string.toString());
				progress.progress = (int)(60*(a/(float)DB.length));
			}
			statement.executeUpdate("SET foreign_key_checks = 1");
			
			//Konfig-Einträge
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_SPEICHERORT+"',null)",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_TIMEOUT_PASSWORT_VERGESSEN+"',"+VALUE_TIMEOUT_PASSWORT_VERGESSEN+")",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_VERSION+"',"+VALUE_VERSION+")",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_STORAGE_LEHRER+"',"+VALUE_STORAGE_LEHRER+")",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_STORAGE_SCHUELER+"',"+VALUE_STORAGE_SCHUELER+")",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_STRING_KONTAKT+"',null)",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_STRING_IMPRESSUM+"',null)",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_STRING_BOTTOM+"',null)",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_STRING_ENTWICKLER+"',null)",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_STRING_TICKER+"',null)",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_STRING_VERTRETUNG+"',null)",false);
			
			//v14
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_MERGE_AKTUELLES+"',"+VALUE_MERGE_AKTUELLES+")",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_PICTURE_SIZE+"',"+VALUE_PICTURE_SIZE+")",false);
			
			//v15
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_SHOW_SYSTEM+"',"+VALUE_SHOW_SYSTEM+")",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_SHOW_SOFTWARE+"',"+VALUE_SHOW_SOFTWARE+")",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_STRING_HISTORY+"',null)",false);
			
			//v18
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_REDIRECT_PORT+"',"+VALUE_REDIRECT_PORT+")",false);
			
			//v23
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_DEFAULT_GRUPPE_ID+"',"+VALUE_DEFAULT_GRUPPE_ID+")",false);
			
			//v25
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_STRING_VERTRETUNG_INFO+"',null)",false);
			
			//v26
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_SHOW_CLOUD+"',"+VALUE_SHOW_CLOUD+")",false);
			
			//v27
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_TOTAL_VIEWS+"',"+VALUE_TOTAL_VIEWS+")",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_STATS_STUNDEN_INDEX+"',"+VALUE_STATS_STUNDEN_INDEX+")",false);
			
			//v30
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_STRING_EU_SA+"',null)",false);
			
			//v33
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_BACKUP_DIR+"','"+VALUE_BACKUP_DIR+"')",false);
			
			//v34
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_TERMINE+"','"+VALUE_TERMINE+"')",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_KOOP+"','"+VALUE_KOOP+"')",false);
			sendQuery("INSERT INTO "+TABLE[KONFIG]+" VALUES('"+KONFIG_KOOP_URL+"','"+VALUE_KOOP_URL+"')",false);
			
			//Statistiken: Stunden
			
			string.setLength(0);
			string.append("INSERT INTO "+TABLE[STATS_STUNDEN]+" VALUES (0,-1)");
			
			for(int i = 1; i < ViewCounter.COUNTER_HOUR_AMOUNT; i++) {
				string.append(", (");
				string.append(i);
				string.append(",-1)");
			}
			
			
			try {
				sendQuery(string.toString(),false);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			Konsole.antwort("Datenbankstruktur erfolgreich erstellt");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void createEmptyDatabase() {
		try {
			sendQuery("DROP DATABASE IF EXISTS "+DATABASE_NAME, false);
			sendQuery("CREATE DATABASE IF NOT EXISTS "+DATABASE_NAME, false);
			sendQuery("USE "+DATABASE_NAME, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//--------------- Datenbank - Update ---------------------------------------------------
	
	private static boolean updating = false;
	synchronized void updateDatabase() {
		if(updating) {
			Konsole.update("Database is already updating! Ignore this updateDatabase() call.");
			return;
		}
		updating = true;
		try {
			//Version der verbundenen Datenbank erkennen
			ResultSet rs = sendQuery("SELECT "+DB[KONFIG][KONFIG_WERT][0]+" FROM "+TABLE[KONFIG]+" WHERE "+DB[KONFIG][VARIABLE][0]+"='"+KONFIG_VERSION+"'", true);
			rs.next();
			int currVersion = rs.getInt(1);
			rs.close();
			
			if(currVersion < VALUE_VERSION) {
				updater.update(currVersion);
			}
			
			needUpdate = false;
			Konsole.update("up-to-date");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		updating = false;
	}
	
	String getCreateDatabaseQuery(int tableIndex) {
		
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE TABLE ");
		builder.append(TABLE[tableIndex]);
		builder.append(" (");
		for(int b = 0; b < DB[tableIndex].length; b++) {
			if(b>0)
				builder.append(',');
			builder.append(DB[tableIndex][b][0]);
			builder.append(' ');
			builder.append(DB[tableIndex][b][1]);
		}
		builder.append(")ENGINE=INNODB");
		
		return builder.toString();
	}
	
	boolean existDatabase() {
		try {
			if(conn == null)
				return false;
			
			ResultSet resultSet = conn.getMetaData().getCatalogs();
			while (resultSet.next()) {
				if(resultSet.getString(1).equals(DATABASE_NAME)) {
					resultSet.close();
					
					//Wenn ausgewählte Datenbank trotzdem leer
					sendQuery("USE "+DATABASE_NAME,false);
					ResultSet rs = sendQuery("SHOW TABLES",true);
					if(rs.next()) {
						return true;
					} else {
						return false;
					}
				}
			}
			resultSet.close();
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean isNeedUpdate() {
		return needUpdate;
	}
	
	String getConnectionMetaData() {
		try {
			DatabaseMetaData meta = conn.getMetaData();
			return meta.getDatabaseProductVersion();
		} catch (SQLException e) {
			return e.getMessage();
		}
	}
	
	String getDatabaseLocation() {
		try {
			
			ResultSet rs = sendQuery("SELECT @@basedir", true);
			String path;
			if(rs.next()) {
				path = rs.getString(1);
			} else {
				path = "";
			}
			rs.close();
			return path;
		} catch (SQLException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	void setKonfig(String variable, String wert) {
		try {
			int typeWert;
			if(wert == null)
				typeWert = TYPE_STRING_NULL;
			else
				typeWert = TYPE_STRING;
			
			Object[] parameter = {wert, variable};
			int[] type = {typeWert, TYPE_STRING};
			sendQueryPrepared("UPDATE "+TABLE[KONFIG]+" SET "+DB[KONFIG][KONFIG_WERT][0]+" = ? WHERE "+DB[KONFIG][VARIABLE][0]+" = ?",parameter,type,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	String getKonfig(String variable) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[KONFIG][KONFIG_WERT][0]+" FROM "+TABLE[KONFIG]+" WHERE "+DB[KONFIG][VARIABLE][0]+"='"+variable+"'",true);
			rs.next();
			String string = rs.getString(1);
			rs.close();
			return string;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	void setKonfigBoolean(String variable, boolean wert) {
		setKonfig(variable, wert ? String.valueOf(1) : String.valueOf(0));
	}
	
	boolean getKonfigBoolean(String variable) {
		return Integer.parseInt(getKonfig(variable)) == 1;
	}
	
	void setKonfigInt(String variable, int wert) {
		setKonfig(variable, String.valueOf(wert));
	}
	
	int getKonfigInt(String variable) {
		return Integer.parseInt(getKonfig(variable));
	}
	
	void setKonfigLong(String variable, long wert) {
		setKonfig(variable, String.valueOf(wert));
	}
	
	long getKonfigLong(String variable) {
		return Long.parseLong(getKonfig(variable));
	}
	
	void addKlassen(int[] stufe, String[] suffix) {
		Object[][] parameter = new Object[stufe.length*suffix.length][2];
		int[] type = {TYPE_INT, TYPE_STRING};
		int id = 0;
		
		for(int x = 0; x < stufe.length; x++) {
			for(int y = 0; y < suffix.length; y++) {
				parameter[id][0] = stufe[x];
				parameter[id][1] = suffix[y];
				id++;
			}
		}
		try {
			sendQueryPrepared("INSERT INTO "+TABLE[KLASSE]+"("+DB[KLASSE][KLASSENSTUFE][0]+","+DB[KLASSE][SUFFIX][0]+") VALUES(?,?)", parameter, type);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void addKlasse(int stufe, String suffix) {
		Object[] parameter = new Object[]{stufe,suffix};
		int[] type = {TYPE_INT, TYPE_STRING};
		
		try {
			sendQueryPrepared("INSERT INTO "+TABLE[KLASSE]+"("+DB[KLASSE][KLASSENSTUFE][0]+","+DB[KLASSE][SUFFIX][0]+") VALUES(?,?)", parameter, type, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	int getNextID(int table) {
		try {
			
			ResultSet rs = sendQuery("SELECT `AUTO_INCREMENT` FROM  INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '"+DATABASE_NAME+"' AND TABLE_NAME = '"+TABLE[table]+"'", true);
			
			rs.next();
			int id = rs.getInt(1);
			
			return id;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	//--------------- Zugangscodes ---------------------------------------------------

	//codes .... String-Array von allen Codes, die hinzugefügt werden sollen
	//anzahl_lehrer ... von diesem Code sind n Lehrer
	void createZugangscodes(String[] codes, int anzahl_lehrer, boolean withAdmin) {
		
		Object[][] parameter = new Object[codes.length][3];
		int[] type = {TYPE_STRING, TYPE_INT, TYPE_BOOLEAN};
		
		if(withAdmin) {
			//Admin
			parameter[0][0] = codes[0];
			parameter[0][1] = 100;
			parameter[0][2] = false;
			//Lehrer
			for(int i = 1; i < anzahl_lehrer+1; i++) {
				parameter[i][0] = codes[i];
				parameter[i][1] = 1;
				parameter[i][2] = false;
			}
			//Schüler
			for(int i = anzahl_lehrer+1; i < parameter.length; i++) {
				parameter[i][0] = codes[i];
				parameter[i][1] = 0;
				parameter[i][2] = false;
			}
		} else {
			//Lehrer
			for(int i = 0; i < anzahl_lehrer; i++) {
				parameter[i][0] = codes[i];
				parameter[i][1] = 1;
				parameter[i][2] = false;
			}
			//Schüler
			for(int i = anzahl_lehrer; i < parameter.length; i++) {
				parameter[i][0] = codes[i];
				parameter[i][1] = 0;
				parameter[i][2] = false;
			}
		}
		
		try {
			sendQueryPrepared("INSERT INTO "+TABLE[ZUGANGSCODE]+"("+DB[ZUGANGSCODE][CODE][0]+","+DB[ZUGANGSCODE][RANG][0]+","+DB[ZUGANGSCODE][BENUTZT][0]+") VALUES(?,?,?)",parameter, type);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void createZugangscodes(String[] codes, int rang) {
		
		Object[][] parameter = new Object[codes.length][3];
		int[] type = {TYPE_STRING, TYPE_INT, TYPE_BOOLEAN};
		
		for(int i = 0; i < codes.length; i++) {
			parameter[i][0] = codes[i];
			parameter[i][1] = rang;
			parameter[i][2] = false;
		}
		
		try {
			sendQueryPrepared("INSERT INTO "+TABLE[ZUGANGSCODE]+"("+DB[ZUGANGSCODE][CODE][0]+","+DB[ZUGANGSCODE][RANG][0]+","+DB[ZUGANGSCODE][BENUTZT][0]+") VALUES(?,?,?)",parameter, type);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	int checkZugangscode(String code) {
		try {
			ResultSet rs = null;
			Object[] input = {code};
			int[] type = {TYPE_STRING};
			rs = sendQueryPrepared("SELECT "+DB[ZUGANGSCODE][RANG][0]+" FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][CODE][0]+"=? AND "+DB[ZUGANGSCODE][BENUTZT][0]+"=0",input,type,true);
			
			if(rs.next()) {
				int rang = rs.getInt(1);
				rs.close();
				return rang;
			} else {
				rs.close();
				return Nutzer.RANG_GAST_NO_LOGIN;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Nutzer.RANG_GAST_NO_LOGIN;
		}
	}
	
	void deleteZugangscode(String code) {
		try {
			Object[] input = {code};
			int[] type = {TYPE_STRING};
			sendQueryPrepared("DELETE FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][CODE][0]+"=?",input,type,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//--------------- nützlche Statistiken ---------------------------------------------------
	
	int getAnzahlDatei() {
		try {
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[DATEI], true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			return anzahl;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	int getCloudSize() {
		try {
			ResultSet rs = sendQuery("SELECT SUM("+DB[DATEI][SIZE][0]+") FROM "+TABLE[DATEI], true);
			rs.next();
			int size = rs.getInt(1);
			rs.close();
			return size;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	String[] getCloudMime(int anzahl) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[DATEI][MIME][0]+" FROM "+TABLE[DATEI], true);
			
			String[] mimes = new String[anzahl];
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				mimes[i] = rs.getString(1);
			}
			rs.close();
			return mimes;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	Map<String,Double> getCloudActivity() {
		try {
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[STATS_CLOUD], true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			rs = sendQuery("SELECT "+DB[STATS_CLOUD][STATS_DATE][0]+","+DB[STATS_CLOUD][STATS_SIZE][0]+" FROM "+TABLE[STATS_CLOUD]+" ORDER BY "+DB[STATS_CLOUD][STATS_DATE][0], true);
			
			Map<String,Double> map = new LinkedHashMap<>();
			double current = 0;
			SimpleDateFormat format = Timeformats.normed();
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				
				String currentDate = format.format(rs.getTimestamp(1));
				
				current += rs.getInt(2);
				map.put(currentDate, current);
			}
			rs.close();
			return map;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	int getAnzahlBeitrag() {
		try {
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[BEITRAG]+" WHERE "+DB[BEITRAG][GENEHMIGT][0]+"=true", true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			return anzahl;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	int getAnzahlBilderBeitrag() {
		try {
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[BEITRAG]+","+TABLE[DATEI_BEITRAG]+" WHERE "+DB[BEITRAG][GENEHMIGT][0]+"=true AND "+TABLE[BEITRAG]+"."+DB[BEITRAG][ID][0]+"="+DB[DATEI_BEITRAG][MN_BEITRAG_ID][0], true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			return anzahl;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	Map<Nutzer,String> getSpeicherverbrauch() {
		try {
			ResultSet rs = sendQuery("SELECT n."+DB[NUTZER][ID][0]+",n."+DB[NUTZER][VORNAME][0]+", n."
												+DB[NUTZER][NACHNAME][0]+",SUM(d."+DB[DATEI][SIZE][0]+") AS size_sum FROM "
												+TABLE[DATEI]+" d, "+TABLE[NUTZER]+" n WHERE d."+DB[DATEI][NUTZER_ID][0]+"=n."+DB[NUTZER][ID][0]+" GROUP BY n."+DB[NUTZER][ID][0]+" ORDER BY size_sum DESC", true);
			
			Map<Nutzer,String> map = new LinkedHashMap<>();
			while(rs.next()) {
				int id = rs.getInt(1);
				String vorname = rs.getString(2);
				String nachname = rs.getString(3);
				long size = rs.getLong(4);
				
				map.put(new Nutzer(id,vorname,nachname), Datei.convertSizeToString(size));
			}
			rs.close();
			return map;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	String[] getAllZugangscodes(int rang, boolean benutzt) {
		try {
			ResultSet rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][BENUTZT][0]+" = "+benutzt+" AND "+DB[ZUGANGSCODE][RANG][0]+"="+rang,true);
			rs.next();
			String[] codes = new String[rs.getInt(1)];
			rs.close();
			rs = sendQuery("SELECT "+DB[ZUGANGSCODE][CODE][0]+" FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][BENUTZT][0]+" = "+benutzt+" AND "+DB[ZUGANGSCODE][RANG][0]+"="+rang,true);
			for(int i = 0; i < codes.length; i++) {
				rs.next();
				codes[i] = rs.getString(1); 
			}
			rs.close();
			return codes;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	int getAnzahlZugangscodes(int rang, boolean benutzt) {
		try {
			ResultSet rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][BENUTZT][0]+" = "+benutzt+" AND "+DB[ZUGANGSCODE][RANG][0]+"="+rang,true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			return anzahl;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	String[] getAllZugangscodes() {
		try {
			ResultSet rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[ZUGANGSCODE],true);
			rs.next();
			String[] codes = new String[rs.getInt(1)];
			rs.close();
			rs = sendQuery("SELECT "+DB[ZUGANGSCODE][CODE][0]+" FROM "+TABLE[ZUGANGSCODE],true);
			for(int i = 0; i < codes.length; i++) {
				rs.next();
				codes[i] = rs.getString(1); 
			}
			rs.close();
			return codes;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	int[] getAllKlassenstufe() {
		try {
			ResultSet rs = sendQuery("SELECT COUNT(distinct "+DB[KLASSE][KLASSENSTUFE][0]+") FROM "+TABLE[KLASSE],true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			rs = sendQuery("SELECT distinct "+DB[KLASSE][KLASSENSTUFE][0]+" FROM "+TABLE[KLASSE],true);
			
			int[] klasse_stufe = new int[anzahl];
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				klasse_stufe[i] = rs.getInt(1);
			}
			
			return klasse_stufe;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	Klasse[] getAllKlassen() {
		
		try {
			ResultSet rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[KLASSE],true);
			rs.next();
			final int anzahl = rs.getInt(1);
			rs.close();
			
			rs = sendQuery("SELECT * FROM "+TABLE[KLASSE]+" ORDER BY "+DB[KLASSE][KLASSENSTUFE][0]+" ASC, "+DB[KLASSE][SUFFIX][0]+" ASC",true);
			
			Klasse[] klassen = new Klasse[anzahl];
			StringBuilder builder = new StringBuilder();
			
			for(int i = 0; i < klassen.length; i++) {
				rs.next();
				int id = rs.getInt(DB[KLASSE][ID][0]);
				int klassenstufe = rs.getInt(DB[KLASSE][KLASSENSTUFE][0]);
				String suffix = rs.getString(DB[KLASSE][SUFFIX][0]);
				builder.append(klassenstufe);
				builder.append(suffix);
				klassen[i] = new Klasse(id, klassenstufe, suffix, builder.toString());
				
				builder.setLength(0); //reset
			}
			
			rs.close();
			return klassen;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	int getAnzahlNutzer(int rang) {
		try {
			ResultSet rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][RANG][0]+"="+rang+" AND "+DB[ZUGANGSCODE][BENUTZT][0]+"=true",true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			return anzahl;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	int getAnzahlNutzer() {
		try {
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[NUTZER],true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			return anzahl;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	int[] getAllNutzerId(int rang) {
		try {
			ResultSet rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][RANG][0]+"="+rang+" AND "+DB[ZUGANGSCODE][BENUTZT][0]+"=true",true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			rs = sendQuery("SELECT "+TABLE[NUTZER]+"."+DB[NUTZER][ID][0]+" FROM "+TABLE[NUTZER]+","+TABLE[ZUGANGSCODE]+" WHERE "+
							TABLE[NUTZER]+"."+DB[NUTZER][CODE_ID][0]+"="+TABLE[ZUGANGSCODE]+"."+DB[ZUGANGSCODE][ID][0]+ //inner join
							" AND "+TABLE[ZUGANGSCODE]+"."+DB[ZUGANGSCODE][RANG][0]+"="+rang,true);
			
			int[] nutzerID = new int[anzahl];
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				nutzerID[i] = rs.getInt(1);
			}
			
			rs.close();
			return nutzerID;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	int[] getAllNutzerId(int rang, int klassenstufe) {
		try {
			//Klassenstufe --> KlasseIDs
			//--anzahl
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[KLASSE]+" WHERE "+DB[KLASSE][KLASSENSTUFE][0]+"="+klassenstufe,true);
			rs.next();
			int anzahl_klasse_id = rs.getInt(1);
			rs.close();
			
			//alle klassenIDs
			rs = sendQuery("SELECT "+DB[KLASSE][ID][0]+" FROM "+TABLE[KLASSE]+" WHERE "+DB[KLASSE][KLASSENSTUFE][0]+"="+klassenstufe,true);
			int[] klasse_id = new int[anzahl_klasse_id];
			for(int i = 0; i < klasse_id.length; i++) {
				rs.next();
				klasse_id[i] = rs.getInt(1);
			}
			rs.close();
			
			//alle nutzer der jeweiligen klassenID UND Bedingung: rang
			int[] nutzer_id = new int[getAnzahlNutzer()];
			int current_index = 0; //von nutzer_id[]
			for(int i = 0; i < klasse_id.length; i++) {
				//Anzahl
				rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[ZUGANGSCODE]+","+TABLE[NUTZER]+" WHERE "+
						TABLE[NUTZER]+"."+DB[NUTZER][CODE_ID][0]+"="+TABLE[ZUGANGSCODE]+"."+DB[ZUGANGSCODE][ID][0]+ //inner join
						" AND "+TABLE[ZUGANGSCODE]+"."+DB[ZUGANGSCODE][RANG][0]+"="+rang+
						" AND "+TABLE[NUTZER]+"."+DB[NUTZER][KLASSE_ID][0]+"="+klasse_id[i],true);
				rs.next();
				int anzahl = rs.getInt(1);
				rs.close();
				
				rs = sendQuery("SELECT "+TABLE[NUTZER]+"."+DB[NUTZER][ID][0]+" FROM "+TABLE[ZUGANGSCODE]+","+TABLE[NUTZER]+" WHERE "+
						TABLE[NUTZER]+"."+DB[NUTZER][CODE_ID][0]+"="+TABLE[ZUGANGSCODE]+"."+DB[ZUGANGSCODE][ID][0]+ //inner join
						" AND "+TABLE[ZUGANGSCODE]+"."+DB[ZUGANGSCODE][RANG][0]+"="+rang+
						" AND "+TABLE[NUTZER]+"."+DB[NUTZER][KLASSE_ID][0]+"="+klasse_id[i],true);
		
				for(int x = 0; x < anzahl; x++) {
					rs.next();
					nutzer_id[current_index] = rs.getInt(1);
					current_index++;
				}
				rs.close();
			}
			//kleineres Array
			int[] final_nutzer_id = new int[current_index];
			for(int i = 0; i < final_nutzer_id.length; i++)
				final_nutzer_id[i] = nutzer_id[i];
			return final_nutzer_id;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	Nutzer[] getAllNutzer() {
		try {
			
			ResultSet rs = null;
			int anzahl = 0;
			
			rs = sendQuery("SELECT count(*) FROM "+TABLE[NUTZER],true);
			rs.next();
			anzahl = rs.getInt(1);
			rs.close();
			rs = sendQuery("SELECT n."+DB[NUTZER][ID][0]+","
									+"n."+DB[NUTZER][VORNAME][0]+","
									+"n."+DB[NUTZER][NACHNAME][0]+","
									+"n."+DB[NUTZER][EMAIL][0]+","
									+"k."+DB[KLASSE][KLASSENSTUFE][0]+","
									+"k."+DB[KLASSE][SUFFIX][0]+" FROM "+TABLE[NUTZER]+" n LEFT JOIN "+TABLE[KLASSE]+" k "
											+"ON k."+DB[KLASSE][ID][0]+"=n."+DB[NUTZER][KLASSE_ID][0]
													+" ORDER BY k."+DB[KLASSE][KLASSENSTUFE][0]+","
													+"k."+DB[KLASSE][SUFFIX][0]+","
													+"n."+DB[NUTZER][VORNAME][0]+","
													+"n."+DB[NUTZER][NACHNAME][0],true);
			
			Nutzer[] nutzer = new Nutzer[anzahl];
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				int klassenstufe = rs.getInt(5);
				String suffix = rs.getString(6);
				String klassenname = suffix != null ? klassenstufe+suffix : null;
				
				nutzer[i] = new Nutzer(rs.getInt(1), rs.getString(2), rs.getString(3), klassenname,klassenstufe,suffix, rs.getString(4));
			}
			rs.close();
			
			return nutzer;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	Nutzer[] getAllNutzer(int rang) {
		try {
			
			ResultSet rs = null;
			int anzahl = 0;
			
			rs = sendQuery("SELECT count(*) FROM "+TABLE[NUTZER]+" n, "+TABLE[ZUGANGSCODE]+" z WHERE "
									+"z."+DB[ZUGANGSCODE][ID][0]+"=n."+DB[NUTZER][CODE_ID][0]+" AND "
									+"z."+DB[ZUGANGSCODE][RANG][0]+"="+rang,true);
			rs.next();
			anzahl = rs.getInt(1);
			rs.close();
			rs = sendQuery("SELECT "
								+"n."+DB[NUTZER][ID][0]+","
								+"n."+DB[NUTZER][VORNAME][0]+","
								+"n."+DB[NUTZER][NACHNAME][0]+","
								+"n."+DB[NUTZER][GESCHLECHT][0]
										+" FROM "+TABLE[NUTZER]+" n, "+TABLE[ZUGANGSCODE]+" z WHERE "
										+"z."+DB[ZUGANGSCODE][ID][0]+"=n."+DB[NUTZER][CODE_ID][0]+" AND "
										+"z."+DB[ZUGANGSCODE][RANG][0]+"="+rang
										+" ORDER BY n."+DB[NUTZER][NACHNAME][0],true);
			
			Nutzer[] nutzer = new Nutzer[anzahl];
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				
				nutzer[i] = new Nutzer(rs.getInt(1), rs.getByte(4), rs.getString(2), rs.getString(3));
			}
			rs.close();
			
			return nutzer;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	int[] getAllID(int table_id) {
		try {
			ResultSet rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[table_id],true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			rs = sendQuery("SELECT "+DB[table_id][ID][0]+" FROM "+TABLE[table_id],true);
			
			int[] id = new int[anzahl];
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				id[i] = rs.getInt(1);
			}
			
			return id;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	void clearAllStorageAndBeitrag() {
		try {
			sendQuery("DELETE FROM "+TABLE[BEITRAG],false);
			
			ResultSet rs = sendQuery("SELECT "+DB[NUTZER][ID][0]+" FROM "+TABLE[NUTZER], true);
			
			while(rs.next()) {
				int id = rs.getInt(1);
				
				clearStorage(id);
			}
			
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	Nutzer[] getNutzerAllowRubrik() {
		try {
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][RUBRIK_ERSTELLEN][0]+" = true", true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			rs = sendQuery("SELECT "+DB[NUTZER][ID][0]+","+DB[NUTZER][VORNAME][0]+","+DB[NUTZER][NACHNAME][0]+" FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][RUBRIK_ERSTELLEN][0]+" = true ORDER BY "+VORNAME, true);
			Nutzer[] nutzer = new Nutzer[anzahl];
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				
				int id = rs.getInt(1);
				String vorname = rs.getString(2);
				String nachname = rs.getString(3);
				
				nutzer[i] = new Nutzer(id, vorname, nachname);
			}
			rs.close();
			
			return nutzer;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//--------------- Registrierung/Authentifizierung ---------------------------------------------------
	
	private String getRandomSalt(int length, boolean upper_case_only) {
		SecureRandom random = new SecureRandom();
		char[] salt = new char[length];
		if(upper_case_only) {
			char[] zeichen = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
					'0','1','2','3','4','5','6','7','8','9'};
			for(int i = 0; i < salt.length; i++) {
				salt[i] = zeichen[random.nextInt(zeichen.length)];
			}
		} else {
			char[] zeichen = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
					'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
					'0','1','2','3','4','5','6','7','8','9'};
			for(int i = 0; i < salt.length; i++) {
				salt[i] = zeichen[random.nextInt(zeichen.length)];
			}
		}
		return String.valueOf(salt);
	}

	/**
	 * 
	 * @param code
	 * @param klasse
	 * @param vorname
	 * @param nachname
	 * @param geschlecht
	 * @param email
	 * @param passwort
	 * @return 0...OK, 1...E-Mail bereits verwendet, 2... Klasse existiert nicht
	 */
	int registerNutzer(String code,String klasse,String vorname,String nachname,int geschlecht,String email,String passwort) {
		
		email = email.toLowerCase();
		
		try {
			
			//E-Mail überprüfen
			if(existEmail(email)) {
				return 1;
			}
			
			//Zugangscode entwerten
			int code_id = editZugangscode(code,false,true);
			
			int rang = getRangByCodeID(code_id);
			
			int klasse_id = -1;
			
			if(klasse != null) {
				//6b   -> {6,b}
				//12-2 -> {12,-2}
				String[] string = klasse.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)"); //12-2 -> {12,-,2}
				for(int i = 2; i < string.length; i++) {
					string[1] += string[i]; // {-,2} -> {-2}
				}
				
				if(string.length >= 2) //2: {6,a}, 3: {12,-,2}
					klasse_id = getKlasseId(string[0],string[1]);
				else
					klasse_id = -1;
				
				//Wenn Klasse falsch --> Entwertung rückgangig machen
				if(klasse_id == -1 && (!klasse.trim().equals("") || rang == Nutzer.RANG_SCHUELER)) { //entweder falsche Klasse eingegeben oder Schüler 
					if(code_id != -1) {
						editZugangscode(code,true,false);
					}
					return 2;
				}
			} else if(rang != Nutzer.RANG_SCHUELER){
				//klasse_id == null und kein Schüler
				klasse_id = -1;
			} else {
				//klasse_id == null und Schüler --> ERROR: Ein Schüler muss einer Klasse zugeordnet werden
				if(code_id != -1) {
					editZugangscode(code,true,false);
				}
				return 2;
			}
			
			int storage;
			
			if(rang == Nutzer.RANG_SCHUELER) {
				storage = Integer.parseInt(getKonfig(KONFIG_STORAGE_SCHUELER));
			} else {
				storage = Integer.parseInt(getKonfig(KONFIG_STORAGE_LEHRER));
			}
			
			boolean projektErstellen = rang == Nutzer.RANG_SCHUELER ? false : true; //alle dürfen Projekte erstellen außer Schüler
			
			String salt = getRandomSalt(223,false);
			Object[] input = new Object[]{code_id,vorname,nachname,klasse_id,geschlecht,email,passwort+salt+PEPPER,salt,storage,projektErstellen};
			int[] type;
			if(klasse_id == -1)
				type = new int[]{TYPE_INT, TYPE_STRING, TYPE_STRING, TYPE_INT_NULL, TYPE_INT, TYPE_STRING, TYPE_STRING, TYPE_STRING, TYPE_INT, TYPE_BOOLEAN};
			else
				type = new int[]{TYPE_INT, TYPE_STRING, TYPE_STRING, TYPE_INT, TYPE_INT, TYPE_STRING, TYPE_STRING, TYPE_STRING, TYPE_INT, TYPE_BOOLEAN};
			
			
			sendQueryPrepared("INSERT INTO "+TABLE[NUTZER]+"("+DB[NUTZER][CODE_ID][0]+","+
														DB[NUTZER][VORNAME][0]+","+
														DB[NUTZER][NACHNAME][0]+","+
														DB[NUTZER][KLASSE_ID][0]+","+
														DB[NUTZER][GESCHLECHT][0]+","+
														DB[NUTZER][EMAIL][0]+","+
														DB[NUTZER][PASSWORT][0]+","+
														DB[NUTZER][STORAGE][0]+","+
														DB[NUTZER][PROJEKT_ERSTELLEN][0]+") "
																+ "VALUES(?,?,?,?,?,?,CONCAT(MD5(?),?),?,?)", input, type, false);
			return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 2;
		}
	}
	
	boolean existEmail(String email) {
		
		try {
			Object[] input_email = {email.toLowerCase()};
			int[] type_email = {TYPE_STRING};
			ResultSet rs = sendQueryPrepared("SELECT count(*) FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][EMAIL][0]+"=?",input_email,type_email,true);
			rs.next();
			if(rs.getInt(1) > 0) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
		
	}
	
	private int tempStorageLehrer, tempStorageSchueler;
	
	//temporär vorbereiten, damit registerNutzerFast nicht überlastet wird
	void prepareTempStorageTypes() {
		tempStorageLehrer = Integer.parseInt(getKonfig(KONFIG_STORAGE_LEHRER));
		tempStorageSchueler = Integer.parseInt(getKonfig(KONFIG_STORAGE_SCHUELER));
	}
	
	//Zugangscodes, die mit keinem Nutzer zugeordnet sind, werden gelöscht
	void validateZugangscode() {
		try {
			ResultSet rs = sendQuery("SELECT * FROM "+TABLE[ZUGANGSCODE], true); //alle Zugangscodes erhalten
			
			while(rs.next()) {
				int codeID = rs.getInt(DB[ZUGANGSCODE][ID][0]);
				
				//zeigt ein Nutzer auf diesen Zugangscode?
				ResultSet rs2 = sendQuery("SELECT * FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][CODE_ID][0]+"="+codeID,true);
				
				if(!rs2.next()) {
					//NEIN --> Zugangscode wird gelöscht
					rs.close();
					rs2.close();
					sendQuery("DELETE FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][ID][0]+"="+codeID,false);
					validateZugangscode(); //abbrechen und noch mal aufrufen, weil im rs einer zu viel ist
					return;
				}
				rs2.close();
			}
			
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//Registrieren ohne Überprüfung
	void registerNutzerFast(String code,int klasse_id,String vorname,String nachname,int geschlecht,String email,String passwort) {
		try {
			int code_id = editZugangscode(code,false,true);
			
			int storage;
			
			int rang = getRangByCodeID(code_id);
			if(rang == Nutzer.RANG_SCHUELER) {
				storage = tempStorageSchueler;
			} else {
				storage = tempStorageLehrer;
			}
			
			//Nutzer mit der gleichen E-Mail löschen
			int nutzer_id = getNutzerIdFromEmail(email.toLowerCase());
			if(nutzer_id != -1) {
				deleteAccount(nutzer_id);
			}
			
			boolean projektErstellen = rang == Nutzer.RANG_SCHUELER ? false : true; //alle dürfen Projekte erstellen außer Schüler
			
			String salt = getRandomSalt(223,false);
			Object[] input = new Object[]{code_id ,vorname     ,nachname    ,klasse_id,geschlecht,email.toLowerCase(),passwort+salt+PEPPER,salt, storage, projektErstellen};
			int[] type = new int[]{TYPE_INT, TYPE_STRING, TYPE_STRING, TYPE_INT, TYPE_INT , TYPE_STRING, TYPE_STRING        , TYPE_STRING, TYPE_INT, TYPE_BOOLEAN};
			if(klasse_id == -1)
				type[3] = TYPE_INT_NULL;
			sendQueryPrepared("INSERT INTO "+TABLE[NUTZER]+"("+DB[NUTZER][CODE_ID][0]+","+
														DB[NUTZER][VORNAME][0]+","+
														DB[NUTZER][NACHNAME][0]+","+
														DB[NUTZER][KLASSE_ID][0]+","+
														DB[NUTZER][GESCHLECHT][0]+","+
														DB[NUTZER][EMAIL][0]+","+
														DB[NUTZER][PASSWORT][0]+","+
														DB[NUTZER][STORAGE][0]+","+
														DB[NUTZER][PROJEKT_ERSTELLEN][0]+") "
																+ "VALUES(?,?,?,?,?,?,CONCAT(MD5(?),?),?,?)", input, type, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void registerAdmin(String vorname,String nachname,int geschlecht,String email,String passwort) {
		try {
			//Zugangscode entwerten
			sendQuery("UPDATE "+TABLE[ZUGANGSCODE]+" SET "+DB[ZUGANGSCODE][BENUTZT][0]+"=true WHERE "+DB[ZUGANGSCODE][ID][0]+"=1", false);
			
			int storage = Integer.parseInt(getKonfig(KONFIG_STORAGE_LEHRER));
			
			String salt = getRandomSalt(223,false);
			Object[] input = {vorname,nachname,geschlecht,email,passwort+salt+PEPPER,salt,storage};
			int[] type = {TYPE_STRING, TYPE_STRING, TYPE_INT, TYPE_STRING, TYPE_STRING, TYPE_STRING,TYPE_INT};
			sendQueryPrepared("INSERT INTO "+TABLE[NUTZER]+"("+DB[NUTZER][CODE_ID][0]+","+
														DB[NUTZER][VORNAME][0]+","+
														DB[NUTZER][NACHNAME][0]+","+
														DB[NUTZER][KLASSE_ID][0]+","+
														DB[NUTZER][GESCHLECHT][0]+","+
														DB[NUTZER][EMAIL][0]+","+
														DB[NUTZER][PASSWORT][0]+","+
														DB[NUTZER][STORAGE][0]+","+
														DB[NUTZER][PROJEKT_ERSTELLEN][0]+","+
														DB[NUTZER][BEITRAG_MANAGER][0]+","+
														DB[NUTZER][RUBRIK_ERSTELLEN][0]+ ") "
																+ "VALUES(1,?,?,NULL,?,?,CONCAT(MD5(?),?),?,true,true,false)", input, type, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//Rückgabewert = code_id
	int editZugangscode(String code, boolean where_benutzt, boolean set_benutzt) {
		try {
			Object[] input = {code};
			int[] type = {TYPE_STRING};
			ResultSet rs = sendQueryPrepared("SELECT "+DB[ZUGANGSCODE][ID][0]+" FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][CODE][0]+"=? AND "+DB[ZUGANGSCODE][BENUTZT][0]+"="+where_benutzt,input, type, true);
			
			if(rs.next() == true) {
				int id = rs.getInt(1);
				rs.close();
				sendQuery("UPDATE "+TABLE[ZUGANGSCODE]+" SET "+DB[ZUGANGSCODE][BENUTZT][0]+"="+set_benutzt+" WHERE "+DB[ZUGANGSCODE][ID][0]+"='"+id+"'", false);
				return id;
			} else {
				return -1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	int getKlasseId(String stufe, String suffix) {
		try {
			Object[] input = {stufe, suffix};
			int[] type = {TYPE_STRING, TYPE_STRING};
			ResultSet rs = sendQueryPrepared("SELECT "+DB[KLASSE][ID][0]+" FROM "+TABLE[KLASSE]+" WHERE "+DB[KLASSE][KLASSENSTUFE][0]+"=? AND "+DB[KLASSE][SUFFIX][0]+"=?",input, type, true);
			
			if(rs.next()) {
				int klasse_id = rs.getInt(1);
				rs.close();
				return klasse_id;
			} else {
				return -1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	boolean isGesperrt(int nutzer_id) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[NUTZER][GESPERRT][0]+" FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id,true);
			
			if(!rs.next()) { //nutzer existiert nicht
				rs.close();
				return true;
			}
			
			boolean gesperrt = rs.getBoolean(1); //false, auch wenn gesperrt == null
			
			rs.close();
			return gesperrt;
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	void setGesperrt(int nutzer_id, boolean gesperrt) {
		try {
			sendQuery("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][GESPERRT][0]+"="+gesperrt+" WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	boolean isBeitragManager(int nutzer_id) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[NUTZER][BEITRAG_MANAGER][0]+" FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id,true);
			
			if(!rs.next()) {
				rs.close();
				return false;
			}
			
			boolean beitragManager = rs.getBoolean(1);
			
			rs.close();
			return beitragManager;
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	void setBeitragManager(int nutzer_id, boolean beitragManager) {
		try {
			sendQuery("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][BEITRAG_MANAGER][0]+"="+beitragManager+" WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	boolean isProjektErstellen(int nutzer_id) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[NUTZER][PROJEKT_ERSTELLEN][0]+" FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id,true);
			
			if(!rs.next()) {
				rs.close();
				return true;
			}
			
			boolean projektErstellen = rs.getBoolean(1);
			
			rs.close();
			return projektErstellen;
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	void setProjektErstellen(int nutzer_id, boolean projektErstellen) {
		try {
			sendQuery("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][PROJEKT_ERSTELLEN][0]+"="+projektErstellen+" WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	boolean isRubrikErstellen(int nutzer_id) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[NUTZER][RUBRIK_ERSTELLEN][0]+" FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id,true);
			
			if(!rs.next()) {
				rs.close();
				return false;
			}
			
			boolean rubrikErstellen = rs.getBoolean(1);
			
			rs.close();
			return rubrikErstellen;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	void setRubrikErstellen(int nutzer_id, boolean rubrikErstellen) {
		try {
			sendQuery("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][RUBRIK_ERSTELLEN][0]+"="+rubrikErstellen+" WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id,false);
			
			if(!rubrikErstellen) {
				//Alle Rubriken löschen, die noch nicht genehmigt wurden
				sendQuery("DELETE FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][RUBRIK_LEITER][0]+"="+nutzer_id+" AND ("+DB[GRUPPE][GENEHMIGT][0]+" IS NULL OR "+DB[GRUPPE][GENEHMIGT][0]+"=false)",false);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	int login(String email, String passwort) {
		ResultSet rs;
		try {
			// SALT_STRING erhalten (anhand der E-Mail)
			Object[] input_email = {email};
			int[] type_email = {TYPE_STRING};
			rs = sendQueryPrepared("SELECT "+DB[NUTZER][PASSWORT][0]+" FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][EMAIL][0]+"=?",input_email, type_email, true);
			if(rs.next() == false) {
				return -1;
			}
			
			
			String database_passwort = rs.getString(1);
			String salt = database_passwort.substring(32);
			
			// Vergleich der Passwörter
			Object[] input = {email,passwort+salt+PEPPER,salt};
			int[] type = {TYPE_STRING, TYPE_STRING, TYPE_STRING};
			rs = sendQueryPrepared("SELECT "+DB[NUTZER][ID][0]+" FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][EMAIL][0]+"=? AND "+DB[NUTZER][PASSWORT][0]+"=CONCAT(MD5(?),?)",input, type, true);
			
			if(rs.next()) {
				int code_id = rs.getInt(1);
				rs.close();
				return code_id;
			} else {
				rs.close();
				return -1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	int getRang(int nutzer_id) {
		ResultSet rs;
		try {
			//Nutzer.id --> Zugangscode.id
			rs = sendQuery("SELECT "+DB[NUTZER][CODE_ID][0]+" FROM "+TABLE[NUTZER]+ " WHERE "+DB[NUTZER][ID][0]+" = "+nutzer_id, true);
			rs.next();
			int code_id = rs.getInt(1);
			rs.close();
			
			//Zugangscode.id --> Zugangscode.rang
			rs = sendQuery("SELECT "+DB[ZUGANGSCODE][RANG][0]+" FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][ID][0]+" = "+code_id, true);
			rs.next();
			int rang = rs.getInt(1);
			rs.close();
			
			return rang;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	int getRangByCodeID(int code_id) {
		ResultSet rs;
		try {
			//Zugangscode.id --> Zugangscode.rang
			rs = sendQuery("SELECT "+DB[ZUGANGSCODE][RANG][0]+" FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][ID][0]+" = "+code_id, true);
			rs.next();
			int rang = rs.getInt(1);
			rs.close();
			
			return rang;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	//Rückgabewert = Verifizierungscode
	//Wenn null = E-Mail existiert nicht in der Datenbank
	String addPasswortVergessen(String email) {
		try {
			//Überprüfen: existiert die E-Mail?
			Object[] input_email = {email};
			int[] type_email = {TYPE_STRING};
			ResultSet rs = sendQueryPrepared("SELECT count(*) FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][EMAIL][0]+"=?",input_email, type_email, true);
			rs.next();
			if(rs.getInt(1) == 0) {
				rs.close();
				return null;
			}
			
			//temporären Code eintragen
			String code = getRandomSalt(8, true);
			Object[] input_final = {code,email};
			int[] type_final = {TYPE_STRING, TYPE_STRING};
			sendQueryPrepared("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][CODE_PASSWORT_VERGESSEN][0]+"=MD5(?) WHERE "+DB[NUTZER][EMAIL][0]+"=?",input_final,type_final,false);
			return code;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//true, wenn E-Mail und Passwort stimmt
	boolean checkCodePasswortVergessen(String email, String code) {
		try {
			//Überprüfen: existiert die E-Mail?
			Object[] input = {email,code};
			int[] type = {TYPE_STRING, TYPE_STRING};
			ResultSet rs = sendQueryPrepared("SELECT * FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][EMAIL][0]+"=? AND "+DB[NUTZER][CODE_PASSWORT_VERGESSEN][0]+"=MD5(?)",input,type,true);
			if(rs.next())
				return true;
			else
				return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
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
	void setPasswort(int nutzerID, String email, String passwort) {
		try {
			String salt = getRandomSalt(223,false);
			Object[] input = {passwort+salt+PEPPER,salt,nutzerID};
			int[] type = {TYPE_STRING,TYPE_STRING,TYPE_INT};
			String field = DB[NUTZER][ID][0];
			
			if(email != null) {
				input[2] = email;
				type[2] = TYPE_STRING;
				field = DB[NUTZER][EMAIL][0];
			}
			sendQueryPrepared("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][PASSWORT][0]+"=CONCAT(MD5(?),?) WHERE "+field+"=?",input,type,false);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void setEmail(String oldEmail, String newEmail) {
		try {
			Object[] input = {newEmail,oldEmail};
			int[] type = {TYPE_STRING,TYPE_STRING};
			sendQueryPrepared("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][EMAIL][0]+"=? WHERE "+DB[NUTZER][EMAIL][0]+"=?",input,type,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//-1, wenn keins vorhanden
	int getNutzerIdFromEmail(String email) {
		Object[] input = {email};
		int[] type = {TYPE_STRING};
		try {
			ResultSet rs = sendQueryPrepared("SELECT "+DB[NUTZER][ID][0]+" FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][EMAIL][0]+"=?",input,type,true);
			
			int id = -1;
			if(rs.next()) {
				id = rs.getInt(1);
			}
			
			rs.close();
			
			return id;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	void deleteCodePasswortVergessen(String email) {
		Object[] input = {null,email};
		int[] type = {TYPE_STRING_NULL, TYPE_STRING};
		try {
			sendQueryPrepared("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][CODE_PASSWORT_VERGESSEN][0]+"=? WHERE "+DB[NUTZER][EMAIL][0]+"=?",input,type,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//alle
	void deleteCodePasswortVergessen() {
		Object[] input = {null};
		int[] type = {TYPE_STRING_NULL};
		try {
			sendQueryPrepared("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][CODE_PASSWORT_VERGESSEN][0]+"=?",input,type,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void deleteAccount(int nutzer_id) {
		
		//unveröffentlichte Beiträge löschen
		try {
			sendQuery("DELETE FROM "+TABLE[BEITRAG]+" WHERE "+DB[BEITRAG][GENEHMIGT][0]+" = false AND "+DB[BEITRAG][NUTZER_ID][0]+"="+nutzer_id, false);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
		
		//Speicher leeren
		clearStorage(nutzer_id);
		
		//von allen Projekten abmelden
		Projekt[] projekt = getProjektArray(nutzer_id);
		for(int i = 0; i < projekt.length; i++) {
			quitProjekt(nutzer_id, projekt[i].getProjektID()); //ggf. wird das Projekt auch gelöscht
		}
		
		try {
			
			//alle Beiträge verweisen als Autor den Admin
			sendQuery("UPDATE "+TABLE[BEITRAG]+" SET "+DB[BEITRAG][NUTZER_ID][0]+" = 1 WHERE "+DB[BEITRAG][NUTZER_ID][0]+" = "+nutzer_id, false);
			
			//Zugangscode löschen
			ResultSet rs = sendQuery("SELECT "+DB[NUTZER][CODE_ID][0]+" FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][ID][0]+" = "+nutzer_id, true);
			if(rs.next()) {
				int code_id = rs.getInt(1);
				
				sendQuery("DELETE FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][ID][0]+" = "+code_id, false); //on delete cascade --> Nutzer wird auch gelöscht
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	//--------------- Home/eingeloggt ---------------------------------------------------
	
	Object getDataTabelleNutzer(int spalte, int nutzer_id) {
		ResultSet rs;
		try {
			rs = sendQuery("SELECT "+DB[NUTZER][spalte][0]+" FROM "+TABLE[NUTZER]+ " WHERE "+DB[NUTZER][ID][0]+" = "+nutzer_id, true);
			
			Object object = null;
			if(rs.next()) {
				object = rs.getObject(1);
			}
			rs.close();
			
			return object;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	void setDataTabelleNutzer(int spalte, int nutzer_id, boolean data) {
		try {
			sendQuery("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][spalte][0]+"="+data+" WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//rufe die Original-searchNutzer-Methode auf, und sortiere das Array nach dem Kriterium "Klassenstufe"
	Nutzer[] searchNutzer(String vorname, String nachname, int rang, int klassenstufe) {
		
		Nutzer[] nutzer;
		if(vorname == null && nachname == null && rang == -1)
			nutzer = getAllNutzer();
		else
			nutzer = searchNutzer(vorname, nachname, rang);
		
		int correct = 0;
		for(int i = 0; i < nutzer.length; i++) {
			if(nutzer[i].getKlassenstufe() == klassenstufe) {
				correct++;
			}
		}
		
		Nutzer[] nutzerFiltered = new Nutzer[correct];
		
		int current = 0;
		for(int i = 0; i < nutzer.length; i++) {
			if(nutzer[i].getKlassenstufe() == klassenstufe) {
				nutzerFiltered[current] = nutzer[i];
				current++;
			}
		}
		
		return nutzerFiltered;
	}
	
	//Rang kann -1 sein, dann wird es nicht berücksichtigt
	Nutzer[] searchNutzer(String vorname, String nachname, int rang) {
		try {
			
			ResultSet rs = null;
			int anzahl = 0;
			int[] type_single = {TYPE_STRING};
			int[] type_double = {TYPE_STRING,TYPE_STRING};
			final String PRE_STRING = "SELECT "+TABLE[NUTZER]+"."+DB[NUTZER][ID][0]+","+DB[NUTZER][KLASSE_ID][0]+","+DB[NUTZER][VORNAME][0]+","+DB[NUTZER][NACHNAME][0]+","+DB[NUTZER][EMAIL][0]+" FROM ";
			String postString = null;
			if(vorname != null && nachname == null) {//nur vorname
				Object[] parameter = {"%"+vorname+"%"};
				if(rang == -1) {
					postString = TABLE[NUTZER]+" WHERE "+DB[NUTZER][VORNAME][0]+" LIKE ?";
				} else {
					postString = TABLE[NUTZER]+","+TABLE[ZUGANGSCODE]+" WHERE "
							+ TABLE[ZUGANGSCODE]+"."+DB[ZUGANGSCODE][ID][0]+"="+TABLE[NUTZER]+"."+DB[NUTZER][CODE_ID][0]+" AND "
							+ DB[ZUGANGSCODE][RANG][0]+" = "+rang+" AND "
							+ DB[NUTZER][VORNAME][0]+" LIKE ?";
				}
				
				rs = sendQueryPrepared("SELECT count(*) FROM "+postString,parameter,type_single,true);
				rs.next();
				anzahl = rs.getInt(1);
				rs.close();
				rs = sendQueryPrepared(PRE_STRING+postString,parameter,type_single,true);
			} else if(vorname == null && nachname != null) { //nur nachname
				Object[] parameter = {"%"+nachname+"%"};
				if(rang == -1) {
					postString = TABLE[NUTZER]+" WHERE "+DB[NUTZER][NACHNAME][0]+" LIKE ?";
				} else {
					postString = TABLE[NUTZER]+","+TABLE[ZUGANGSCODE]+" WHERE "
							+ TABLE[ZUGANGSCODE]+"."+DB[ZUGANGSCODE][ID][0]+"="+TABLE[NUTZER]+"."+DB[NUTZER][CODE_ID][0]+" AND "
							+ DB[ZUGANGSCODE][RANG][0]+" = "+rang+" AND "
							+ DB[NUTZER][NACHNAME][0]+" LIKE ?";
				}
				rs = sendQueryPrepared("SELECT count(*) FROM "+postString,parameter,type_single,true);
				rs.next();
				anzahl = rs.getInt(1);
				rs.close();
				rs = sendQueryPrepared(PRE_STRING+postString,parameter,type_single,true);
			} else if(vorname != null && nachname != null) { //beides
				Object[] parameter = {"%"+vorname+"%","%"+nachname+"%"};
				if(rang == -1) {
					postString = TABLE[NUTZER]+" WHERE "+DB[NUTZER][VORNAME][0]+" LIKE ? AND "+DB[NUTZER][NACHNAME][0]+" LIKE ?";
				} else {
					postString = TABLE[NUTZER]+","+TABLE[ZUGANGSCODE]+" WHERE "
							+ TABLE[ZUGANGSCODE]+"."+DB[ZUGANGSCODE][ID][0]+"="+TABLE[NUTZER]+"."+DB[NUTZER][CODE_ID][0]+" AND "
							+ DB[ZUGANGSCODE][RANG][0]+" = "+rang+" AND "
							+ DB[NUTZER][VORNAME][0]+" LIKE ? AND "
							+ DB[NUTZER][NACHNAME][0]+" LIKE ?";
				}
				rs = sendQueryPrepared("SELECT count(*) FROM "+postString,parameter,type_double,true);
				rs.next();
				anzahl = rs.getInt(1);
				rs.close();
				rs = sendQueryPrepared(PRE_STRING+postString,parameter, type_double, true);
			} else if(vorname == null && nachname == null && rang != -1) {
				
				postString = TABLE[NUTZER]+","+TABLE[ZUGANGSCODE]+" WHERE "
						+ TABLE[ZUGANGSCODE]+"."+DB[ZUGANGSCODE][ID][0]+"="+TABLE[NUTZER]+"."+DB[NUTZER][CODE_ID][0]+" AND "
						+ DB[ZUGANGSCODE][RANG][0]+" = "+rang;
				
				rs = sendQuery("SELECT count(*) FROM "+postString,true);
				rs.next();
				anzahl = rs.getInt(1);
				rs.close();
				rs = sendQuery(PRE_STRING+postString,true);
			}
			
			//Antwort der Datenbank in einen Nutzer-Array speichern
			Zwischenspeicher<Object[]> buffer = new Zwischenspeicher<>(anzahl);
			Nutzer[] nutzer = new Nutzer[anzahl];
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				
				int klasseID = rs.getInt(2);
				Object[] klasse = buffer.get(klasseID);
				if(klasse == null && klasseID > 0) {
					klasse = getKlasse(klasseID);
					if(klasse != null)
						buffer.add(klasseID, klasse);
				}
						
				int klassenstufe;
				String suffix;
				String klassenname;
				if(klasse == null) {
					klassenstufe = -1;
					suffix = "";
					klassenname = null;
				} else {
					klassenstufe = (int) klasse[0];
					suffix = (String) klasse[1];
					klassenname = klassenstufe+suffix;
				}
				
				nutzer[i] = new Nutzer(rs.getInt(1), rs.getString(3), rs.getString(4), klassenname,klassenstufe,suffix, rs.getString(5));
			}
			rs.close();
			
			
			//Sortieren nach Klassenstufe (7a-7b-7c-8a-8b-9a-...)
			//Insertion-Sort
			if(nutzer.length > 1) {
				for(int x = 1; x < nutzer.length; x++) {
					for(int y = x ; y > 0 ; y--){
						int suffix_compare = nutzer[y].getSuffix().compareTo(nutzer[y-1].getSuffix());
						int vorname_compare = nutzer[y].getVorname().compareTo(nutzer[y-1].getVorname());
						int nachname_compare = nutzer[y].getNachname().compareTo(nutzer[y-1].getNachname());
						if(nutzer[y].getKlassenstufe() < nutzer[y-1].getKlassenstufe()) { //Wenn mom. Wert kleiner als der vorheriger Wert
							//tauschen
							Nutzer nutzer_temp = nutzer[y];
							nutzer[y] = nutzer[y-1];
							nutzer[y-1] = nutzer_temp;
						} else if(nutzer[y].getKlassenstufe() == nutzer[y-1].getKlassenstufe() && suffix_compare < 0) {
							//tauschen
							Nutzer nutzer_temp = nutzer[y];
							nutzer[y] = nutzer[y-1];
							nutzer[y-1] = nutzer_temp;
						} else if(nutzer[y].getKlassenstufe() == nutzer[y-1].getKlassenstufe() && suffix_compare == 0 && vorname_compare < 0) {
							//tauschen
							Nutzer nutzer_temp = nutzer[y];
							nutzer[y] = nutzer[y-1];
							nutzer[y-1] = nutzer_temp;
						} else if(nutzer[y].getKlassenstufe() == nutzer[y-1].getKlassenstufe() && suffix_compare == 0 && vorname_compare == 0 && nachname_compare < 0) {
							//tauschen
							Nutzer nutzer_temp = nutzer[y];
							nutzer[y] = nutzer[y-1];
							nutzer[y-1] = nutzer_temp;
						} else {
		                	break;
		                }
		            }
				}
			}
			
			return nutzer;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	void setNutzerValue(int id, int spalte, String value) {
		try {
			
			Object[] input = {value, id};
			int[] type = {TYPE_STRING, TYPE_INT};
			sendQueryPrepared("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][spalte][0]+"=? WHERE "+DB[NUTZER][ID][0]+"=?",input, type, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//--------------- Schulklasse ---------------------------------------------------
	
	Nutzer[] getKlassenkameraden(int klasse_id) {
		ResultSet rs;
		try {
			//Anzahl
			rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[NUTZER]+ " WHERE "+DB[NUTZER][KLASSE_ID][0]+" = "+klasse_id, true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			//Vorname,Nachname der Tabelle Nutzer
			rs = sendQuery("SELECT "+DB[NUTZER][VORNAME][0]+","+DB[NUTZER][NACHNAME][0]+","+DB[NUTZER][ID][0]+","+DB[NUTZER][GESCHLECHT][0]+" FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][KLASSE_ID][0]+" = "+klasse_id+" ORDER BY "+DB[NUTZER][VORNAME][0]+" ASC, "+DB[NUTZER][NACHNAME][0]+" ASC", true);
			
			Nutzer[] kameraden = new Nutzer[anzahl];
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				String vorname = rs.getString(1);
				String nachname = rs.getString(2);
				int rang = getRang(rs.getInt(3));
				byte geschlecht = rs.getByte(4);
				kameraden[i] = new Nutzer(rang,vorname,nachname,geschlecht);
			}
			
			return kameraden;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	String getKlassenname(int klasse_id) {
		ResultSet rs;
		try {
			rs = sendQuery("SELECT "+DB[KLASSE][KLASSENSTUFE][0]+","+DB[KLASSE][SUFFIX][0]+" FROM "+TABLE[KLASSE]+" WHERE "+DB[KLASSE][ID][0]+" = "+klasse_id, true);
			
			StringBuilder builder = null;
			if(rs.next()) {
				builder = new StringBuilder();
				builder.append(rs.getString(1));
				builder.append(rs.getString(2));
			} else {
				return null;
			}
			
			return builder.toString();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// object[0] = Integer klassenstufe, object[1] = String suffix 
	Object[] getKlasse(int klasse_id) {
		ResultSet rs;
		try {
			rs = sendQuery("SELECT "+DB[KLASSE][KLASSENSTUFE][0]+","+DB[KLASSE][SUFFIX][0]+" FROM "+TABLE[KLASSE]+" WHERE "+DB[KLASSE][ID][0]+" = "+klasse_id, true);
			
			if(rs.next()) {
				Object[] object = {rs.getInt(1),rs.getString(2)};
				return object;
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	void translateKlassenstufe(int delta) {
		try {
			if(delta > 0) {
				sendQuery("UPDATE "+TABLE[KLASSE]+" SET "+DB[KLASSE][KLASSENSTUFE][0]+" = "+DB[KLASSE][KLASSENSTUFE][0]+" + "+delta, false);
			} else if(delta < 0) {
				sendQuery("UPDATE "+TABLE[KLASSE]+" SET "+DB[KLASSE][KLASSENSTUFE][0]+" = "+DB[KLASSE][KLASSENSTUFE][0]+" "+delta, false);
			} else {
				return;
			}
			
			//Validierung
			
			ResultSet rs = sendQuery("SELECT "+DB[KLASSE][ID][0]+","+DB[KLASSE][KLASSENSTUFE][0]+","+DB[KLASSE][SUFFIX][0]+" FROM "+TABLE[KLASSE],true);
			
			while(rs.next()) {
				int id = rs.getInt(1);
				int stufe = rs.getInt(2);
				String suffix = rs.getString(3);
				if(stufe < 10 && suffix.startsWith("-")) {
					switch(suffix) {
					case "-1":
						suffix = "a";
						break;
					case "-2":
						suffix = "b";
						break;
					case "-3":
						suffix = "c";
						break;
					case "-4":
						suffix = "d";
						break;
					case "-5":
						suffix = "e";
						break;
					}
					sendQuery("UPDATE "+TABLE[KLASSE]+" SET "+DB[KLASSE][SUFFIX][0]+" = '"+suffix+"' WHERE "+DB[KLASSE][ID][0]+"="+id, false);
				} else if(stufe >= 10 && !suffix.startsWith("-")) {
					switch(suffix) {
					case "a":
						suffix = "-1";
						break;
					case "b":
						suffix = "-2";
						break;
					case "c":
						suffix = "-3";
						break;
					case "d":
						suffix = "-4";
						break;
					case "e":
						suffix = "-5";
						break;
					}
					sendQuery("UPDATE "+TABLE[KLASSE]+" SET "+DB[KLASSE][SUFFIX][0]+" = '"+suffix+"' WHERE "+DB[KLASSE][ID][0]+"="+id, false);
				}
			}
			
			rs.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void quitKlasse(int nutzer_id, int klasse_id) {
		try {
			clearStorageKlasse(klasse_id, nutzer_id); //Speicher leeren
			sendQuery("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][KLASSE_ID][0]+" = null WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void joinKlasse(int nutzer_id, int klasse_id) {
		try {
			sendQuery("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][KLASSE_ID][0]+" = "+klasse_id+" WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//nach dem ein Nutzer gelöscht wird: Klassen löschen, zu der noch keine Schüler zugeordnet sind
	void cleanKlasse() {
		try {
			Klasse[] klasse = getAllKlassen();
			
			for(int i = 0; i < klasse.length; i++) {
				ResultSet rs = sendQuery("SELECT * FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][KLASSE_ID][0]+"="+klasse[i].getId(),true);
				if(!rs.next()) {
					sendQuery("DELETE FROM "+TABLE[KLASSE]+" WHERE "+DB[KLASSE][ID][0]+"="+klasse[i].getId(),false); //Klasse löschen
				}
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//--------------- Projekt ---------------------------------------------------
	
	private Projekt[] getProjektArray(int nutzerID) {
		Projekt[] a = getProjektArray(nutzerID, false, false);
		Projekt[] b = getProjektArray(nutzerID, true, false);
		
		Projekt[] merge = new Projekt[a.length+b.length];
		for(int i = 0; i < a.length; i++) {
			merge[i] = a[i];
		}
		for(int i = a.length; i < merge.length; i++) {
			merge[i] = b[i-a.length];
		}
		
		return merge;
	}
	
	Projekt[] getProjektArray(int nutzer_id, boolean akzeptiert, boolean lehrerchat) {
		try {
			
			//Anzahl
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[NUTZER_PROJEKT]+","+TABLE[PROJEKT]
						+" WHERE "+DB[NUTZER_PROJEKT][NUTZER_ID][0]+"="+nutzer_id
						+" AND "+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+"="+TABLE[PROJEKT]+"."+DB[PROJEKT][ID][0]
						+" AND "+TABLE[PROJEKT]+"."+DB[PROJEKT][AKZEPTIERT][0]+"="+akzeptiert
						+" AND "+TABLE[PROJEKT]+"."+DB[PROJEKT][PROJEKT_LEHRERCHAT][0]+"="+lehrerchat,true);
			rs.next();
			Projekt[] projekt = new Projekt[rs.getInt(1)];
			rs.close();
			
			//Projektdaten erhalten
			rs = sendQuery("SELECT np."+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+","
									+"p."+DB[PROJEKT][NAME][0]+","
									+"p."+DB[PROJEKT][CHAT][0]+","
									+"n."+DB[NUTZER][ID][0]+","
									+"n."+DB[NUTZER][VORNAME][0]+","
									+"n."+DB[NUTZER][NACHNAME][0]+","
									+"np."+DB[NUTZER_PROJEKT][AKZEPTIERT][0]
											+" FROM "
											+TABLE[NUTZER_PROJEKT]+" np,"
											+TABLE[PROJEKT]+" p,"
											+TABLE[NUTZER]+" n"
													+" WHERE "
														+"p."+DB[PROJEKT][LEITER_NUTZER_ID][0]+"=n."+DB[NUTZER][ID][0]+" AND " //Nutzer <-> Projektleiter
														+"np."+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+"=p."+DB[PROJEKT][ID][0]+" AND " //Projekt <-> MN_Projekt
														+"np."+DB[NUTZER_PROJEKT][NUTZER_ID][0]+"="+nutzer_id+" AND "
														+"p."+DB[PROJEKT][AKZEPTIERT][0]+"="+akzeptiert+" AND "
														+"p."+DB[PROJEKT][PROJEKT_LEHRERCHAT][0]+"="+lehrerchat
																+" ORDER BY p."+DB[PROJEKT][NAME][0],true);
			
			ResultSet curr_rs = null;
			for(int i = 0; i < projekt.length; i++) {
				rs.next();
				
				//aus der Main-Query
				int projekt_id = rs.getInt(1);
				String projekt_name = rs.getString(2);
				boolean projektChat = rs.getBoolean(3);
				int leiterID = rs.getInt(4);
				String leiterVorname = rs.getString(5);
				String leiterNachname = rs.getString(6);
				boolean selberAkzeptiert = rs.getBoolean(7);
				
				//Teilnehmer herausfinden
				//Anzahl Teilnehmer
				curr_rs = sendQuery("SELECT count(*) FROM "+TABLE[NUTZER_PROJEKT]+" WHERE "+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+"="+projekt_id+" AND "+DB[NUTZER_PROJEKT][MN_BETREUER][0]+"=false",true);
				curr_rs.next();
				String[] teilnehmer = new String[curr_rs.getInt(1)];
				curr_rs.close();
				
				//nutzer_id der Teilnehmer
				curr_rs = sendQuery("SELECT n."+DB[NUTZER][VORNAME][0]+",n."+DB[NUTZER][NACHNAME][0]+",n."+DB[NUTZER][GESCHLECHT][0]+",z."+DB[ZUGANGSCODE][RANG][0]
												+ " FROM "+TABLE[NUTZER_PROJEKT]+" p, "+TABLE[NUTZER]+" n ,"+TABLE[ZUGANGSCODE]+" z "
													+ "WHERE "
													+ "n."+DB[NUTZER][ID][0]+"=p."+DB[NUTZER_PROJEKT][NUTZER_ID][0]+" AND " //Nutzer <-> MN_Projekt
													+ "n."+DB[NUTZER][CODE_ID][0]+"=z."+DB[ZUGANGSCODE][ID][0]+" AND " //Nutzer <-> Zugangscode
													+ "p."+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+"="+projekt_id+" AND "
													+ "p."+DB[NUTZER_PROJEKT][MN_BETREUER][0]+"=false",true);
				
				for(int n = 0; n < teilnehmer.length; n++) {
					curr_rs.next();
					int rang = curr_rs.getByte(4);
					if(rang == Nutzer.RANG_SCHUELER) {
						teilnehmer[n] = curr_rs.getString(1)+" "+curr_rs.getString(2);
					} else {
						byte geschlecht = curr_rs.getByte(3);
						switch(geschlecht) {
						case Nutzer.GESCHLECHT_MANN:
							teilnehmer[n] = "Herr "+curr_rs.getString(2);
							break;
						case Nutzer.GESCHLECHT_FRAU:
							teilnehmer[n] = "Frau "+curr_rs.getString(2);
							break;
						default:
							teilnehmer[n] = curr_rs.getString(1)+" "+curr_rs.getString(2);
						}
					}
					
				}
				curr_rs.close();
				
				//Alle Namen der Teilnehmer in einen String zusammenfügen
				StringBuilder stringBuilder = new StringBuilder();
				for(int n = 0; n < teilnehmer.length; n++) {
					if(n == 0)
						stringBuilder.append(teilnehmer[n]);
					else {
						stringBuilder.append(", ");
						stringBuilder.append(teilnehmer[n]);
					}
				}
				
				projekt[i] = new Projekt(projekt_name,stringBuilder.toString(),projekt_id,new Nutzer(leiterID,leiterVorname,leiterNachname),projektChat,selberAkzeptiert);
			}
			rs.close();
			
			return projekt;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @return alle Projekte, die akzeptiert sind
	 */
	Projekt[] getAllProjektArray() {
		try {
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[PROJEKT]+" WHERE "+DB[PROJEKT][AKZEPTIERT][0]+"=true",true);
			rs.next();
			Projekt[] projekt = new Projekt[rs.getInt(1)];
			rs.close();
			
			rs = sendQuery("SELECT p."+DB[PROJEKT][NAME][0]+","
									+"p."+DB[PROJEKT][ID][0]+","
									+"p."+DB[PROJEKT][CHAT][0]+","
									+"n."+DB[NUTZER][ID][0]+","
									+"n."+DB[NUTZER][VORNAME][0]+","
									+"n."+DB[NUTZER][NACHNAME][0]
									
											+ " FROM "+TABLE[PROJEKT]+" p, "+TABLE[NUTZER]+" n"
											+ " WHERE p."+DB[PROJEKT][LEITER_NUTZER_ID][0]+"=n."+DB[NUTZER][ID][0]
											+ " AND p."+DB[PROJEKT][AKZEPTIERT][0]+"=true ORDER BY p."+DB[PROJEKT][NAME][0],true);
			
			for(int i = 0; i < projekt.length; i++) {
				rs.next();
				//Projektname
				String projektName = rs.getString(1);
				int projektID = rs.getInt(2);
				boolean chat = rs.getBoolean(3);
				
				//Projektleiter
				int leiterNutzerID = rs.getInt(4);
				String leiterVorname = rs.getString(5);
				String leiterNachname = rs.getString(6);
				
				projekt[i] = new Projekt(projektName, projektID, new Nutzer(leiterNutzerID,leiterVorname,leiterNachname),chat);
			}
			rs.close();
			
			return projekt;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param nutzerID die ID vom Nutzer, der es akzeptiert
	 * @param projektID die ID vom Projekt
	 * @return 0 ok, warte auf andere Betreuer, 1 alle Betreuer haben akzeptiert/Projekt verfügbar
	 */
	int projektAkzeptieren(int nutzerID, int projektID) {
		try {
			sendQuery("UPDATE "+TABLE[NUTZER_PROJEKT]+" SET "+DB[NUTZER_PROJEKT][AKZEPTIERT][0]+" = true WHERE "+DB[NUTZER_PROJEKT][NUTZER_ID][0]+"="+nutzerID
					+" AND "+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+"="+projektID,false);
			
			//gibt es noch Betreuer, die noch nicht akzeptiert haben?
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[NUTZER_PROJEKT]+" WHERE "+DB[NUTZER_PROJEKT][AKZEPTIERT][0]+" = false"
					+" AND "+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+"="+projektID
					+" AND "+DB[NUTZER_PROJEKT][MN_BETREUER][0]+"=true",true);
			
			rs.next();
			int amount = rs.getInt(1);
			rs.close();
			
			if(amount == 0) {
				//nein, das Projekt wird freigegeben
				sendQuery("UPDATE "+TABLE[PROJEKT]+" SET "+DB[PROJEKT][AKZEPTIERT][0]+" = true WHERE "+DB[PROJEKT][ID][0]+"="+projektID,false);
				return 1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	private int[] getProjektIDArray(int nutzer_id) {
		
		try {
			//Anzahl
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[NUTZER_PROJEKT]+" WHERE "+DB[NUTZER_PROJEKT][NUTZER_ID][0]+"="+nutzer_id,true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			//Projekt-IDs herausfinden
			rs = sendQuery("SELECT "+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+" FROM "+TABLE[NUTZER_PROJEKT]+" WHERE "+DB[NUTZER_PROJEKT][NUTZER_ID][0]+"="+nutzer_id,true);
			
			int[] projekt_id = new int[anzahl];
			for(int i = 0; i < projekt_id.length; i++) {
				rs.next();
				projekt_id[i] = rs.getInt(1);
			}
			return projekt_id;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	Nutzer[] getNutzerProjektArray(int projekt_id, boolean betreuer) {
		try {
			//Anzahl Nutzer-Projekt-Verknüpfung
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[NUTZER_PROJEKT]
								+" WHERE "+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+"="+projekt_id+" AND "+DB[NUTZER_PROJEKT][MN_BETREUER][0]+"="+betreuer,true);
			rs.next();
			Nutzer[] nutzer  = new Nutzer[rs.getInt(1)];
			rs.close();
			
			//nutzer_id herausfinden
			rs = sendQuery("SELECT "+DB[NUTZER_PROJEKT][NUTZER_ID][0]+" FROM "+TABLE[NUTZER_PROJEKT]
								+" WHERE "+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+"="+projekt_id+" AND "+DB[NUTZER_PROJEKT][MN_BETREUER][0]+"="+betreuer,true);
			
			for(int i = 0; i < nutzer.length; i++) {
				rs.next();
				int nutzer_id = rs.getInt(1);
				String[] curr_name = getName(nutzer_id);
				
				String vorname = curr_name[0];
				String nachname = curr_name[1];
				Object object = getDataTabelleNutzer(KLASSE_ID, nutzer_id);
				String klasse = "";
				if(object != null)
					klasse = getKlassenname((int)object);
				byte geschlecht = (byte) (int) getDataTabelleNutzer(GESCHLECHT,nutzer_id);
				nutzer[i] = new Nutzer(nutzer_id,vorname,nachname,klasse,geschlecht);
			}
			rs.close();
			return nutzer;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	Projekt createProjekt(int nutzer_id, String projekt_name, boolean sofortAkzeptiert, boolean lehrerchat) {
		try {
			Object[] parameter = {projekt_name};
			int[] type = {TYPE_STRING};
			sendQueryPrepared("INSERT INTO "+TABLE[PROJEKT]+"("+DB[PROJEKT][NAME][0]+","+DB[PROJEKT][LEITER_NUTZER_ID][0]+","+DB[PROJEKT][AKZEPTIERT][0]+","+DB[PROJEKT][PROJEKT_LEHRERCHAT][0]+") VALUES(?,"+nutzer_id+","+sofortAkzeptiert+","+lehrerchat+")",parameter,type,false);
			ResultSet rs = sendQuery("SELECT LAST_INSERT_ID() FROM "+TABLE[PROJEKT],true);
			rs.next();
			int projekt_id = rs.getInt(1);
			rs.close();
			
			//der Projektleiter ist kein betreuer und hat bereits akzeptiert
			sendQuery("INSERT INTO "+TABLE[NUTZER_PROJEKT]+"("+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+","+DB[NUTZER_PROJEKT][NUTZER_ID][0]+","+DB[NUTZER_PROJEKT][MN_BETREUER][0]+","+DB[NUTZER_PROJEKT][AKZEPTIERT][0]+") VALUES("+projekt_id+","+nutzer_id+",false,true)",false);
			return new Projekt(projekt_id,projekt_name);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	void renameProjekt(int projektID, String name) {
		try {
			Object[] parameter = {name, projektID};
			int[] type = {TYPE_STRING, TYPE_INT};
			sendQueryPrepared("UPDATE "+TABLE[PROJEKT]+" SET "+DB[PROJEKT][NAME][0]+"=? WHERE "+DB[PROJEKT][ID][0]+"=?",parameter,type,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	boolean joinProjekt(int nutzer_id, int projekt_id, boolean betreuer, boolean akzeptiert) {
		try {
			sendQuery("INSERT INTO "+TABLE[NUTZER_PROJEKT]+"("+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+","+DB[NUTZER_PROJEKT][NUTZER_ID][0]+","+DB[NUTZER_PROJEKT][MN_BETREUER][0]+","+DB[NUTZER_PROJEKT][AKZEPTIERT][0]+") VALUES("+projekt_id+","+nutzer_id+","+betreuer+","+akzeptiert+")",false);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//-1 error, 0 ok, 1 ok & projekt gelöscht
	int quitProjekt(int nutzer_id, int projekt_id) {
		try {
			
			//Überprüfen ob Projektleiter oder Betreuer verlässt
			ResultSet rs = sendQuery("SELECT * FROM "+TABLE[PROJEKT]+" p, "+TABLE[NUTZER_PROJEKT]+" n WHERE "
										+"p."+DB[PROJEKT][ID][0]+"=n."+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+" AND "
										+"n."+DB[NUTZER_PROJEKT][NUTZER_ID][0]+"="+nutzer_id+" AND "
										+"n."+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+"="+projekt_id+" AND ("
										+"n."+DB[NUTZER_PROJEKT][MN_BETREUER][0]+"=true OR "
										+"p."+DB[PROJEKT][LEITER_NUTZER_ID][0]+"="+nutzer_id+")",true);
			
			sendQuery("DELETE FROM "+TABLE[NUTZER_PROJEKT]+" WHERE "+DB[NUTZER_PROJEKT][MN_PROJEKT_ID][0]+"="+projekt_id+" AND "+DB[NUTZER_PROJEKT][NUTZER_ID][0]+"="+nutzer_id,false);
			
			if(rs.next()) {
				rs.close();
				clearStorageProjekt(projekt_id);
				sendQuery("DELETE FROM "+TABLE[PROJEKT]+" WHERE "+DB[PROJEKT][ID][0]+"="+projekt_id,false);
				return 1;
			} else {
				clearStorageProjekt(projekt_id,nutzer_id);
				rs.close();
				return 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	void clearStorageProjekt(int projekt_id) {
		Datei[] datei = getFileArrayProjekt(projekt_id, null);
		
		for(int i = 0; i < datei.length; i++) {
			deleteFile(datei[i].getDatei_id(),datei[i].getPfad());
		}
	}
	
	//nach dem ein Nutzer gekickt wurde
	void clearStorageProjekt(int projekt_id, int nutzer_id) {
		Datei[] datei = getFileArrayProjekt(projekt_id, null);
		
		int anzahl = 0;
		
		for(int i = 0; i < datei.length; i++) {
			if(datei[i].getEigtum_nutzer_id() == nutzer_id) {
				anzahl++;
			}
		}
		
		Datei[] datei_clean = new Datei[anzahl];
		int current = 0;
		for(int i = 0; i < datei.length; i++) {
			if(datei[i].getEigtum_nutzer_id() == nutzer_id) {
				datei_clean[current] = datei[i];
			}
		}
		
		for(int i = 0; i < datei_clean.length; i++) {
			deleteFile(datei_clean[i].getDatei_id(),datei_clean[i].getPfad());
		}
	}
	
	//nachdem Lehrer Klasse verlassen
	void clearStorageKlasse(int klasse_id, int nutzer_id) {
		
		Datei[] datei = getFileArrayKlasse(klasse_id, null);
		
		int anzahl = 0;
		
		for(int i = 0; i < datei.length; i++) {
			if(datei[i].getEigtum_nutzer_id() == nutzer_id) {
				anzahl++;
			}
		}
		
		Datei[] datei_clean = new Datei[anzahl];
		int current = 0;
		for(int i = 0; i < datei.length; i++) {
			if(datei[i].getEigtum_nutzer_id() == nutzer_id) {
				datei_clean[current] = datei[i];
			}
		}
		
		for(int i = 0; i < datei_clean.length; i++) {
			deleteFile(datei_clean[i].getDatei_id(),datei_clean[i].getPfad());
		}
	}
	
	//-1 error syntax, -2 error Kein Projektleiter, 0 ok
	int deleteProjekt(int nutzer_id, int projekt_id) {
		try {
			//Überprüfen, ob Nutzer = Projektleiter
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[PROJEKT]+" WHERE "+DB[PROJEKT][LEITER_NUTZER_ID][0]+"="+nutzer_id+" AND "+DB[PROJEKT][ID][0]+"="+projekt_id,true);
			rs.next();
			if(rs.getInt(1) == 1) {
				//Projekt löschen
				rs.close();
				clearStorageProjekt(projekt_id);
				sendQuery("DELETE FROM "+TABLE[PROJEKT]+" WHERE "+DB[PROJEKT][ID][0]+"="+projekt_id,false); //on delete kaskade
				return 0;
			} else {
				rs.close();
				return -2;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	boolean existProjekt(int projektID) {
		try {
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[PROJEKT]+" WHERE "+DB[PROJEKT][ID][0]+"="+projektID,true);
			rs.next();
			if(rs.getInt(1) >= 1) {
				rs.close();
				return true;
			} else {
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//forcieren: einfach ohne Nachfrage löschen (Admin-Mode) oder vom Betreuer oder Chatraum (Lehrer)
	void deleteProjekt(int projekt_id) {
		try {
			clearStorageProjekt(projekt_id);
			sendQuery("DELETE FROM "+TABLE[PROJEKT]+" WHERE "+DB[PROJEKT][ID][0]+"="+projekt_id,false); //on delete kaskade
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	boolean isProjektChat(int projekt_id) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[PROJEKT][CHAT][0]+" FROM "+TABLE[PROJEKT]+" WHERE "+DB[PROJEKT][ID][0]+"="+projekt_id,true);
			if(rs.next()) {
				boolean allowed = rs.getBoolean(1);
				rs.close();
				return allowed;
			} else {
				rs.close();
				return false;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	void setProjektChat(int projekt_id, boolean allowed) {
		try {
			sendQuery("UPDATE "+TABLE[PROJEKT]+" SET "+DB[PROJEKT][CHAT][0]+"="+allowed+" WHERE "+DB[PROJEKT][ID][0]+"="+projekt_id,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	Gruppe getProjektGruppe(int projektID) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[GRUPPE][ID][0]+","+DB[GRUPPE][NAME][0]+" FROM "+TABLE[GRUPPE]+" WHERE "+TABLE[GRUPPE]+"."+DB[GRUPPE][ID][0]+" = (SELECT "+DB[PROJEKT_GRUPPE][MN_GRUPPE_ID][0]+" FROM "+TABLE[PROJEKT_GRUPPE]+" WHERE "+DB[PROJEKT_GRUPPE][MN_PROJEKT_ID][0]+"="+projektID+" LIMIT 1)",true);
			
			Gruppe gruppe = null;
			if(rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				gruppe = new Gruppe(id, name);
			}
			
			return gruppe;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	Beitrag[] getProjektBeitrag(int projektID) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[PROJEKT_GRUPPE][MN_GRUPPE_ID][0]+" FROM "+TABLE[PROJEKT_GRUPPE]+" WHERE "+DB[PROJEKT_GRUPPE][MN_PROJEKT_ID][0]+"="+projektID,true);
			
			Beitrag[] beitrag = null;
			if(rs.next()) {
				int gruppeID = rs.getInt(1);
				beitrag = getBeitragArray(gruppeID, -1);
			}
			
			return beitrag;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//Alle Gruppen löschen, die mit dem Projekt verknüpft sind
	void clearProjektGruppe(int projektID) {
		try {
			sendQuery("DELETE FROM "+TABLE[PROJEKT_GRUPPE]+" WHERE "+DB[PROJEKT_GRUPPE][MN_PROJEKT_ID][0]+"="+projektID,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void addProjektGruppe(int projektID, int gruppeID) {
		try {
			sendQuery("INSERT INTO "+TABLE[PROJEKT_GRUPPE]+"("+DB[PROJEKT_GRUPPE][MN_PROJEKT_ID][0]+","+DB[PROJEKT_GRUPPE][MN_GRUPPE_ID][0]+") VALUES("+projektID+","+gruppeID+")",false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//--------------- Speicher ---------------------------------------------------
	
	boolean addFile(int nutzer_id, int klasse_projekt_rubrik_id, Datei.Mode mode, String pfad, int size_bytes, String mime_type, int ordner_id) {
		
		try {
			Object[] parameter = {nutzer_id,			0,			0,			false,			pfad    ,size_bytes ,mime_type,	  false,	ordner_id};
			int[] type =		 {TYPE_INT ,TYPE_INT_NULL  ,TYPE_INT_NULL ,TYPE_BOOLEAN,TYPE_STRING,TYPE_INT,TYPE_STRING,TYPE_BOOLEAN,TYPE_INT};
			
			if(nutzer_id == -1)
				type[0] = TYPE_INT_NULL; //z.B. bei Rubrik
			
			if(ordner_id == Datei.ORDNER_HOME)
				type[8] = TYPE_INT_NULL;
			
			switch(mode) {
			case PROJEKTSPEICHER:
				parameter[1] = klasse_projekt_rubrik_id;
				type[1] = TYPE_INT;
				break;
			case KLASSENSPEICHER:
				parameter[2] = klasse_projekt_rubrik_id;
				type[2] = TYPE_INT;
				break;
			case LEHRERCHAT:
				parameter[3] = true;
				break;
			default:
			}
			
			sendQueryPrepared("INSERT INTO "+TABLE[DATEI]+"("+DB[DATEI][NUTZER_ID][0]+","+
																DB[DATEI][PROJEKT_ID][0]+","+
																DB[DATEI][KLASSE_ID][0]+","+
																DB[DATEI][DATEI_LEHRERCHAT][0]+","+
																DB[DATEI][PFAD][0]+","+
																DB[DATEI][SIZE][0]+","+
																DB[DATEI][MIME][0]+","+
																DB[DATEI][PUBLIC][0]+","+
																DB[DATEI][ORDNER_ID][0]+") VALUES(?,?,?,?,?,?,?,?,?)", parameter, type, false);
			
			if(mode == Datei.Mode.RUBRIK) {
				
				ResultSet rs = sendQuery("SELECT LAST_INSERT_ID();",true);
				rs.next();
				int newID = rs.getInt(1);
				rs.close();
				
				//altes Bild wird durch den Garbage-Collector später gelöscht
				
				sendQuery("UPDATE "+TABLE[GRUPPE]+" SET "+DB[GRUPPE][DATEI_ID][0]+"="+newID+" WHERE "+DB[GRUPPE][ID][0]+"="+klasse_projekt_rubrik_id, false);
				updateRubrikBildRekursive(klasse_projekt_rubrik_id, newID); //rekursiv an die Kinder
			}
			
			insertCloudStats(size_bytes);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	void addFolder(int nutzer_id, int klasse_projekt_id, String name, int parent_id, Datei.Mode mode) {
		try {
			Object[] parameter = {name,nutzer_id,0,0,false,parent_id};
			int[] type;
			
			if(parent_id == -1)
				type = new int[]{TYPE_STRING,TYPE_INT,TYPE_INT_NULL,TYPE_INT_NULL,TYPE_BOOLEAN,TYPE_INT_NULL}; //parent ist home
			else
				type = new int[]{TYPE_STRING,TYPE_INT,TYPE_INT_NULL,TYPE_INT_NULL,TYPE_BOOLEAN,TYPE_INT};
			
			switch(mode) {
			case PROJEKTSPEICHER:
				parameter[2] = klasse_projekt_id;
				type[2] = TYPE_INT;
				break;
			case KLASSENSPEICHER:
				parameter[3] = klasse_projekt_id;
				type[3] = TYPE_INT;
				break;
			case LEHRERCHAT:
				parameter[4] = true;
				break;
			default:
			}
			
			sendQueryPrepared("INSERT INTO "+TABLE[ORDNER]+"("+DB[ORDNER][NAME][0]+","+
																DB[ORDNER][EIGENTUM][0]+","+
																DB[ORDNER][PROJEKT_ID][0]+","+
																DB[ORDNER][KLASSE_ID][0]+","+
																DB[ORDNER][ORDNER_LEHRERCHAT][0]+","+
																DB[ORDNER][PARENT][0]+") VALUES(?,?,?,?,?,?)", parameter, type, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void setPublic(int datei_id, boolean isPublic) {
		
		try {
			sendQuery("UPDATE "+TABLE[DATEI]+" SET "+DB[DATEI][PUBLIC][0]+" = "+isPublic+" WHERE "+DB[DATEI][ID][0]+"="+datei_id,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	boolean isPublic(int datei_id) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[DATEI][PUBLIC][0]+" FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ID][0]+"="+datei_id, true);
			
			boolean isPublic = false;
			if(rs.next()) {
				isPublic = rs.getBoolean(1);
			}
			rs.close();
			
			return isPublic;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	int getStorage(int nutzer_id) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[NUTZER][STORAGE][0]+" FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id, true);
			rs.next();
			int bytes = rs.getInt(1);
			rs.close();
			
			return bytes;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	int getStorageUsed(int nutzer_id) {
		try {
			ResultSet rs = sendQuery("SELECT SUM("+DB[DATEI][SIZE][0]+") FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][NUTZER_ID][0]+"="+nutzer_id, true);
			rs.next();
			int sum = rs.getInt(1);
			rs.close();
			return sum;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * 
	 * @param nutzer_id
	 * @param bytes
	 * @return true = OK, false = Nutzer überschreitet Limit, keine Änderung
	 */
	boolean editStorage(int nutzer_id, int bytes) {
		try {
			int currentUsed = getStorageUsed(nutzer_id);
			
			if(currentUsed > bytes)
				return false;
			
			sendQuery("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][STORAGE][0]+" = "+bytes+" WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id,false);
			
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * Ändert den Speicherlimit von allen Schülern einer Klasse
	 * Überschreitet ein Schüler den Limit, dann wird dessen Speicher geleert
	 * @param klasse_id
	 * @param bytes
	 */
	void editStorageKlasse(int klasse_id, int bytes) {
		try {
			
			//für alle Schüler einer Klasse, die den Limit überschreiten
			ResultSet rs = sendQuery("SELECT sum("+DB[DATEI][SIZE][0]+") as s, "+DB[DATEI][NUTZER_ID][0]
					+ " FROM "+TABLE[DATEI]+" d, "+TABLE[NUTZER]+" n, "+TABLE[ZUGANGSCODE]+" z"
					+ " WHERE d."+DB[DATEI][NUTZER_ID][0]+"=n."+DB[NUTZER][ID][0]+" AND n."+DB[NUTZER][CODE_ID][0]+"=z."+DB[ZUGANGSCODE][ID][0]
					+ " AND n."+DB[NUTZER][KLASSE_ID][0]+"="+klasse_id+" AND z."+DB[ZUGANGSCODE][RANG][0]+"="+Nutzer.RANG_SCHUELER
					+ " GROUP BY "+DB[DATEI][NUTZER_ID][0]+" HAVING s > "+bytes, true);
			
			while(rs.next()) {
				clearStorage(rs.getInt(2));
			}
			rs.close();
			
			sendQuery("UPDATE "+TABLE[NUTZER]+" n SET "+DB[NUTZER][STORAGE][0]+" = "+bytes+" WHERE n."+DB[NUTZER][KLASSE_ID][0]+"="+klasse_id+" AND EXISTS("
					+ "SELECT * FROM "+TABLE[ZUGANGSCODE]+" z"
					+ " WHERE n."+DB[NUTZER][CODE_ID][0]+"=z."+DB[ZUGANGSCODE][ID][0]
					+ " AND z."+DB[ZUGANGSCODE][RANG][0]+"="+Nutzer.RANG_SCHUELER+")",false);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Löscht alle Dateien, die mit dem Nutzer assoziiert sind (Privat-/Klassen-/Projekt-/Lehrerchat-speicher)
	 * @param nutzer_id
	 */
	void clearStorage(int nutzer_id) {
		Datei[] datei = getFileArrayUploaded(nutzer_id, null);
		
		for(int i = 0; i < datei.length; i++) {
			deleteFile(datei[i].getDatei_id(),datei[i].getPfad());
		}
	}
	
	//true... gelöscht, false... nicht gelöscht (z.B. Beitrag-Verknüpfung oder Error)
	//wird von clearStorage aufgerufen oder wenn man manuell im Privat-/Klassen-/Projektspeicher etwas löscht
	boolean deleteFile(int datei_id, String pfad) {
		try {
			
			// Ist Datei mit einem Beitrag verknüpft?
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[DATEI_BEITRAG]+" WHERE "+DB[DATEI_BEITRAG][MN_DATEI_ID][0]+"="+datei_id,true);
			rs.next();
			
			boolean keep = rs.getInt(1) > 0 ? true : false;
			rs.close();
			
			if(!keep) {
				//Ist diese Datei mit einer Gruppe verknüpft?
				rs = sendQuery("SELECT count(*) FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][DATEI_ID][0]+"="+datei_id,true);
				rs.next();
				keep = rs.getInt(1) > 0 ? true : false;
			}
			rs.close();
			
			if(keep) {
				//JA
				sendQuery("UPDATE "+TABLE[DATEI]+" SET "+DB[DATEI][NUTZER_ID][0]+"=null, "+DB[DATEI][KLASSE_ID][0]+"=null, "+DB[DATEI][PROJEKT_ID][0]+"=null, "+DB[DATEI][DATEI_LEHRERCHAT][0]+"=false WHERE "+DB[DATEI][ID][0]+"="+datei_id,false);
				Konsole.antwort("Verknüpfung gelöscht. Datei besteht wegen eines Beitrags weiterhin.");
				return false;
			} else {
				//NEIN
				//Datei löschen
				
				rs = sendQuery("SELECT "+DB[DATEI][SIZE][0]+" FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ID][0]+"="+datei_id,true);
				if(rs.next())
					insertCloudStats(-rs.getInt(1));
				rs.close();
				
				sendQuery("DELETE FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ID][0]+"="+datei_id,false);
				
				Files.delete(Paths.get(pfad));
				return true;
			}
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	void renameFile(Datei datei, String neu) {
		Path source = Paths.get(datei.getPfad());
		try {
			
			Path neuerPfad = source.resolveSibling(datei.getDatei_id()+"_"+neu);
			Files.move(source, neuerPfad);
			
			Object[] parameter = {neuerPfad.toString().replace('\\', '/')};
			int[] type = new int[] {TYPE_STRING};
			
			sendQueryPrepared("UPDATE "+TABLE[DATEI]+" SET "+DB[DATEI][PFAD][0]+" = ? WHERE "+DB[DATEI][ID][0]+"="+datei.getDatei_id(), parameter, type, false);
		} catch (SQLException | IOException e) {
		     e.printStackTrace();
		}
	}
	
	//Ordnerstruktur: alle Unterebenen; notwendig, um die Ordnergröße (kumulativ) zu erhalten
	Datei[] getFolderRecursive(int nutzer_klasse_projekt_id, int parent, Datei.Mode mode, boolean firstCall) {
		try {
			String suffix;
			if(parent == Datei.ORDNER_HOME) {
				switch(mode) {
				case PRIVATSPEICHER:
					suffix =  " FROM "+TABLE[ORDNER]+" WHERE "+DB[ORDNER][EIGENTUM][0]+"="+nutzer_klasse_projekt_id+" AND "+DB[ORDNER][PARENT][0]+" IS NULL AND "+DB[ORDNER][KLASSE_ID][0]+" IS NULL AND "+DB[ORDNER][PROJEKT_ID][0]+" IS NULL AND "+DB[ORDNER][ORDNER_LEHRERCHAT][0]+"=false ORDER BY "+DB[ORDNER][NAME][0];
					break;
				case KLASSENSPEICHER:
					suffix =  " FROM "+TABLE[ORDNER]+" WHERE "+DB[ORDNER][KLASSE_ID][0]+"="+nutzer_klasse_projekt_id+" AND "+DB[ORDNER][PARENT][0]+" IS NULL ORDER BY "+DB[ORDNER][NAME][0];
					break;
				case PROJEKTSPEICHER:
					suffix =  " FROM "+TABLE[ORDNER]+" WHERE "+DB[ORDNER][PROJEKT_ID][0]+"="+nutzer_klasse_projekt_id+" AND "+DB[ORDNER][PARENT][0]+" IS NULL ORDER BY "+DB[ORDNER][NAME][0];
					break;
				case LEHRERCHAT:
					suffix =  " FROM "+TABLE[ORDNER]+" WHERE "+DB[ORDNER][ORDNER_LEHRERCHAT][0]+"=true AND "+DB[ORDNER][PARENT][0]+" IS NULL ORDER BY "+DB[ORDNER][NAME][0];
					break;
				default:
					suffix = " FROM "+TABLE[ORDNER]+" WHERE "+DB[ORDNER][ID][0]+"=0"; //leere Menge anzeigen
				}
				
			} else {
				suffix =  " FROM "+TABLE[ORDNER]+" WHERE "+DB[ORDNER][PARENT][0]+"="+parent+" ORDER BY "+DB[ORDNER][NAME][0];
			}
			
			ResultSet rs = sendQuery("SELECT COUNT(*)"+suffix, true);
				
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			//alle Unterordner
			rs = sendQuery("SELECT *"+suffix, true);
			Datei[] ordner = new Datei[anzahl];
			
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				int id = rs.getInt(DB[ORDNER][ID][0]);
				String name = rs.getString(DB[ORDNER][NAME][0]);
				
				//Eigentum_Nutzer
				int eigtum_nutzer_id = rs.getInt(DB[ORDNER][EIGENTUM][0]);
				String[] eigtumTemp = getName(eigtum_nutzer_id);
				String eigtum = eigtumTemp[0]+" "+eigtumTemp[1];
				
				//Alle Dateien, die mit diesem Ordner verknüpft sind
				ResultSet rs2 = sendQuery("SELECT "+DB[DATEI][SIZE][0]+" FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ORDNER_ID][0]+"="+id,true);
				
				long bytes = 0;
				while(rs2.next()) {
					bytes += rs2.getInt(1);
				}
				
				ordner[i] = new Datei(id,name,getFolderRecursive(-1,id,mode,false),bytes,eigtum_nutzer_id,eigtum); //nutzerID ist für die Kinder nicht mehr notwendig
			}
			
			if(firstCall)
				kumulativSizes(ordner);
			return ordner;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private long kumulativSizes(Datei[] ordner) {
		
		long size = 0;
		for(int i = 0; i < ordner.length; i++) {
			long sizeCurrent = ordner[i].getSizeInt(); //alle Dateien mit diesem Ordner
			long sizeKind = kumulativSizes(ordner[i].getKinder()); //alle Dateien der Unterordner
			long merged = sizeKind+sizeCurrent;
			ordner[i].setSizeInt(merged);
			ordner[i].setSize(Datei.convertSizeToString(merged));
			
			size += merged;
		}
		
		return size;
	}
	
	void renameFolder(int ordnerID, String neu) {
		try {
			Object[] parameter = {neu, ordnerID};
			int[] types = {TYPE_STRING, TYPE_INT};
			sendQueryPrepared("UPDATE "+TABLE[ORDNER]+" SET "+DB[ORDNER][NAME][0]+" = ? WHERE "+DB[ORDNER][ID][0]+"=?",parameter,types,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void deleteFolder(int ordnerID) {
		try {
			
			//Unterordner löschen
			ResultSet rs = sendQuery("SELECT * FROM "+TABLE[ORDNER]+" WHERE "+DB[ORDNER][PARENT][0]+"="+ordnerID,true);
			
			while(rs.next()) {
				
				int ordner_id = rs.getInt(DB[ORDNER][ID][0]);
				deleteFolder(ordner_id);
			}
			
			rs.close();
			
			//Dateien löschen
			
			rs = sendQuery("SELECT "+DB[DATEI][ID][0]+","+DB[DATEI][PFAD][0]+" FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ORDNER_ID][0]+"="+ordnerID,true);
			
			while(rs.next()) {
				
				int datei_id = rs.getInt(1);
				String pfad = rs.getString(2);
				
				deleteFile(datei_id, pfad);
			}
			
			rs.close();
			
			//datei -> ordner: on delete set null
			//dateien, die vorher durch deleteDatei() "entfernt" wurden, aber weiterhin für Beiträge angezeigt werden müssen
			//erhalten als orderID = null
			sendQuery("DELETE FROM "+TABLE[ORDNER]+" WHERE "+DB[ORDNER][ID][0]+"="+ordnerID,  false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//false... Fehler aufgetreten
	boolean moveFile(int datei_id, int ordner_id) {
		try {
			
			String ordner;
			if(ordner_id == Datei.ORDNER_HOME)
				ordner = "null";
			else
				ordner = String.valueOf(ordner_id);
			
			sendQuery("UPDATE "+TABLE[DATEI]+" SET "+DB[DATEI][ORDNER_ID][0]+" = "+ordner+" WHERE "+DB[DATEI][ID][0]+"="+datei_id,false);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//false... Fehler aufgetreten
	boolean moveOrdner(int ordner_id, int parent) {
		try {
			
			String parentString;
			if(parent == Datei.ORDNER_HOME)
				parentString = "null";
			else
				parentString = String.valueOf(parent);
			
			sendQuery("UPDATE "+TABLE[ORDNER]+" SET "+DB[ORDNER][PARENT][0]+" = "+parentString+" WHERE "+DB[ORDNER][ID][0]+"="+ordner_id,false);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//gibt die Dateien EINER ORDNEREBENE zurück
	Datei[] getFileArray(int nutzer_klasse_projekt_id, int ordnerID, Datei.Mode mode) {
		try {
			
			String query;
			if(ordnerID == Datei.ORDNER_HOME) {
				switch(mode) {
				case PRIVATSPEICHER:
					query=" IS NULL AND "+DB[DATEI][NUTZER_ID][0]+"="+nutzer_klasse_projekt_id+" AND "
							+DB[DATEI][PROJEKT_ID][0]+" IS NULL AND "
							+DB[DATEI][KLASSE_ID][0]+" IS NULL AND "
							+DB[DATEI][DATEI_LEHRERCHAT][0]+"=false";
					break;
				case KLASSENSPEICHER:
					query=" IS NULL AND "+DB[DATEI][KLASSE_ID][0]+"="+nutzer_klasse_projekt_id;
					break;
				case PROJEKTSPEICHER:
					query=" IS NULL AND "+DB[DATEI][PROJEKT_ID][0]+"="+nutzer_klasse_projekt_id;
					break;
				case LEHRERCHAT:
					query=" IS NULL AND "+DB[DATEI][DATEI_LEHRERCHAT][0]+"=true";
					break;
				default:
					query = "";
				}
			} else {
				query="="+ordnerID;
			}
			ResultSet rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ORDNER_ID][0]+query, true);
				
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			rs = sendQuery("SELECT * FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ORDNER_ID][0]+query+" ORDER BY "+DB[DATEI][ID][0]+" DESC", true);
			
			return resultSetToDatei(rs, anzahl);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 
	 * @param nutzer_id
	 * @param mime optional
	 * @return Alle Dateien, die mit den Nutzer assoziiert werden (Privat-/Klassen-/Projekt-/Lehrerchat-speicher)
	 */
	Datei[] getFileArrayUploaded(int nutzer_id, String mime) {
		try {
			//ANZAHL datei_id für den nutzer_id
			ResultSet rs = null;
			
			if(mime == null)
				rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][NUTZER_ID][0]+"="+nutzer_id, true);
				
			else
				rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][NUTZER_ID][0]+"="+nutzer_id+" AND "
									+DB[DATEI][MIME][0]+" LIKE '"+mime+"%'", true);
				
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			rs = null;
			
			if(mime == null)
				rs = sendQuery("SELECT * FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][NUTZER_ID][0]+"="+nutzer_id, true);
			else
				rs = sendQuery("SELECT * FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][NUTZER_ID][0]+"="+nutzer_id+" AND "
									+DB[DATEI][MIME][0]+" LIKE '"+mime+"%'", true);
			
			return resultSetToDatei(rs, anzahl);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//mime kann null sein, dann wird es nicht berücksichtigt
	//wie oben, nur wird Klassen- und Projektspeicher nicht berücksichtigt
	Datei[] getFileArrayPrivate(int nutzer_id, String mime) {
		try {
			//ANZAHL datei_id für den nutzer_id
			ResultSet rs = null;
			
			if(mime == null)
				rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][NUTZER_ID][0]+"="+nutzer_id+" AND "
																				+DB[DATEI][PROJEKT_ID][0]+" IS NULL AND "
																				+DB[DATEI][KLASSE_ID][0]+" IS NULL AND "
																				+DB[DATEI][DATEI_LEHRERCHAT][0]+"=false", true);
				
			else
				rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][NUTZER_ID][0]+"="+nutzer_id+" AND "
																				+DB[DATEI][PROJEKT_ID][0]+" IS NULL AND "
																				+DB[DATEI][KLASSE_ID][0]+" IS NULL AND "
																				+DB[DATEI][DATEI_LEHRERCHAT][0]+"=false AND "
																				+DB[DATEI][MIME][0]+" LIKE '"+mime+"%'", true);
				
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			rs = null;
			
			if(mime == null)
				rs = sendQuery("SELECT * FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][NUTZER_ID][0]+"="+nutzer_id+" AND "
																		+DB[DATEI][PROJEKT_ID][0]+" IS NULL AND "
																		+DB[DATEI][KLASSE_ID][0]+" IS NULL AND "
																		+DB[DATEI][DATEI_LEHRERCHAT][0]+"=false ORDER BY "+DB[DATEI][ID][0]+" DESC", true);
			else
				rs = sendQuery("SELECT * FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][NUTZER_ID][0]+"="+nutzer_id+" AND "
						+DB[DATEI][PROJEKT_ID][0]+" IS NULL AND "
						+DB[DATEI][KLASSE_ID][0]+" IS NULL AND "
						+DB[DATEI][DATEI_LEHRERCHAT][0]+"=false AND "
						+DB[DATEI][MIME][0]+" LIKE '"+mime+"%' ORDER BY "+DB[DATEI][ID][0]+" DESC", true);
			
			return resultSetToDatei(rs, anzahl);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//ALLE Dateien in einer Klasse (Ordnerübergreifend)
	Datei[] getFileArrayKlasse(int klasse_id, String mime) {
		try {
			//ANZAHL datei_id für den klasse_id
			ResultSet rs = null;
			if(mime == null)
				rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][KLASSE_ID][0]+"="+klasse_id, true);
			else
				rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][KLASSE_ID][0]+"="+klasse_id+" AND "+DB[DATEI][MIME][0]+" LIKE '"+mime+"%'", true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			//datei
			rs = null;
			if(mime == null)
				rs = sendQuery("SELECT * FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][KLASSE_ID][0]+"="+klasse_id+" ORDER BY "+DB[DATEI][ID][0]+" DESC", true);
			else
				rs = sendQuery("SELECT * FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][KLASSE_ID][0]+"="+klasse_id+" AND "+DB[DATEI][MIME][0]+" LIKE '"+mime+"%' ORDER BY "+DB[DATEI][ID][0]+" DESC", true);
			return resultSetToDatei(rs, anzahl);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//ALLE Dateien in einem Projekt (Ordnerübergreifend)
	Datei[] getFileArrayProjekt(int projekt_id, String mime) {
		try {
			//ANZAHL datei_id für den projekt_id
			ResultSet rs = null;
			if(mime == null)
				rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][PROJEKT_ID][0]+"="+projekt_id, true);
			else
				rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][PROJEKT_ID][0]+"="+projekt_id+" AND "+DB[DATEI][MIME][0]+" LIKE '"+mime+"%'", true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			//datei
			rs = null;
			if(mime == null)
				rs = sendQuery("SELECT * FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][PROJEKT_ID][0]+"="+projekt_id+ " ORDER BY "+DB[DATEI][ID][0]+" DESC", true);
			else
				rs = sendQuery("SELECT * FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][PROJEKT_ID][0]+"="+projekt_id+" AND "+DB[DATEI][MIME][0]+" LIKE '"+mime+"%' ORDER BY "+DB[DATEI][ID][0]+" DESC", true);
			return resultSetToDatei(rs, anzahl);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//Alle Dateien (Bilder), die mit dem Beitrag verknüpft sind
	Datei[] getFileArrayBeitrag(int beitrag_id) {
		try {
			//MN: datei_beitrag
			ResultSet rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[DATEI_BEITRAG]+" WHERE "+DB[DATEI_BEITRAG][MN_BEITRAG_ID][0]+"="+beitrag_id, true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			//datei
			rs = sendQuery("SELECT * FROM "+TABLE[DATEI]+","+TABLE[DATEI_BEITRAG]+" WHERE "+TABLE[DATEI]+"."+DB[DATEI][ID][0]+"="+DB[DATEI_BEITRAG][MN_DATEI_ID][0]+" AND "+DB[DATEI_BEITRAG][MN_BEITRAG_ID][0]+"="+beitrag_id, true);
			return resultSetToDatei(rs, anzahl);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//alle Dateien, auf die der Nutzer Zugang hat
	//im Gegensatz zu getFileArrayUploaded: es werden auch Dateien von anderen angezeigt
	Datei[] getFileArrayAccess(int nutzer_id, String mime) {
		//Privat
		Datei[] datei_privat = getFileArrayPrivate(nutzer_id,mime);
		Datei[] datei_klasse = null;
		Object o = getDataTabelleNutzer(KLASSE_ID, nutzer_id);
		if(o == null)
			datei_klasse = new Datei[0];
		else
			datei_klasse = getFileArrayKlasse((int)o,mime);
		
		//Projekte
		int[] projekt_id = getProjektIDArray(nutzer_id);
		Datei[][] datei_projekt = new Datei[projekt_id.length][];
		for(int i = 0; i < projekt_id.length; i++) {
			datei_projekt[i] = getFileArrayProjekt(projekt_id[i],mime);
		}
		
		//FUSION-------------------------------------------------------------------------
		//Anzahl
		int anzahl = 0;
		anzahl += datei_privat.length;
		anzahl += datei_klasse.length;
		for(int i = 0; i < datei_projekt.length; i++) {
			anzahl += datei_projekt[i].length; //Projekt
		}
		
		//FUSION
		Datei[] datei = new Datei[anzahl];
		int currIndex = 0;
		for(int i = 0; i < datei_privat.length; i++) { //Privat
			datei[currIndex] = datei_privat[i];
			currIndex++;
		}
		for(int i = 0; i < datei_klasse.length; i++) { //Klasse
			datei[currIndex] = datei_klasse[i];
			currIndex++;
		}
		for(int x = 0; x < datei_projekt.length; x++) { //Projekt
			for(int y = 0; y < datei_projekt[x].length; y++) {
				datei[currIndex] = datei_projekt[x][y];
				currIndex++;
			}
		}
		return datei;
	}
	
	public Datei getFile(int dateiID) {
		try {
			ResultSet rs = sendQuery("SELECT * FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ID][0]+"="+dateiID, true);
			
			//Daten entnehmen
			if(rs.next()) {
				rs.beforeFirst(); //cursor zurückspringen
				return resultSetToDatei(rs, 1)[0];
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//durch SELECT * FROM datei WHERE [...] wird aus dem ResultSet, ein Datei-Array erstellt und dann ResultSet geschlossen
	Datei[] resultSetToDatei(ResultSet rs, int anzahl) {
		try {
			Datei[] datei = new Datei[anzahl];
			Zwischenspeicher<String> buffer = new Zwischenspeicher<>(anzahl);
			SimpleDateFormat format = Timeformats.completeNoSeconds();
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				int index = i;
				int datei_id = rs.getInt(DB[DATEI][ID][0]);
				String pfad = rs.getString(DB[DATEI][PFAD][0]);
				String time = format.format(rs.getTimestamp(DB[DATEI][DATUM][0]));
				int size = rs.getInt(DB[DATEI][SIZE][0]);
				String datei_name = pfad.substring(pfad.lastIndexOf("/")+1);
				datei_name = datei_name.substring(datei_name.indexOf("_")+1);
				String mime = rs.getString(DB[DATEI][MIME][0]);
				boolean isPublic = rs.getBoolean(DB[DATEI][PUBLIC][0]);
				
				//Eigentum_Nutzer
				int eigtum_nutzer_id = rs.getInt(DB[DATEI][NUTZER_ID][0]);
				String eigtum = buffer.get(eigtum_nutzer_id);
				if(eigtum == null) {
					String[] name = getName(eigtum_nutzer_id);
					if(name != null) {
						eigtum = name[0]+" "+name[1];
						buffer.add(eigtum_nutzer_id, eigtum);
					}
				}
				datei[i] = new Datei(index, datei_id, pfad, time, size, datei_name, mime, eigtum_nutzer_id, eigtum, isPublic);
			}
			rs.close();
			return datei;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * ResultSet der Form:
	 * ID - PFAD - DATUM - SIZE - MIME - PUBLIC - NUTZER_ID
	 * @param rs ResultSet
	 * @param vorname Vorname (optional) wird ansonsten mit einer seperaten Query erhalten
	 * @param vorname Nachname (optional) wird ansonsten mit einer seperaten Query erhalten
	 * @param format das Datumformat, standardmäßig Timeformats.completeNoSeconds()
	 * @param close wird das ResultSet nach diesem Aufruf geschlossen?
	 * @return
	 */
	private Datei resultSetToDatei(ResultSet rs, String vorname, String nachname, SimpleDateFormat format, boolean close) {
		try {
			int datei_id = rs.getInt(1);
			String pfad = rs.getString(2);
			String time = format.format(rs.getTimestamp(3));
			int size = rs.getInt(4);
			String datei_name = pfad.substring(pfad.lastIndexOf("/")+1);
			datei_name = datei_name.substring(datei_name.indexOf("_")+1);
			String mime = rs.getString(5);
			boolean isPublic = rs.getBoolean(6);
			
			//Eigentum_Nutzer
			int eigtum_nutzer_id = rs.getInt(7);
			String eigtum = null;
			
			if(vorname != null) {
				//Argument übernehmen
				eigtum = vorname+" "+nachname;
			} else {
				String[] name = getName(eigtum_nutzer_id);
				if(name != null) {
					eigtum = name[0]+" "+name[1];
				}
			}
			
			Datei datei = new Datei(-1, datei_id, pfad, time, size, datei_name, mime, eigtum_nutzer_id, eigtum, isPublic);
			if(close)
				rs.close();
			return datei;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//für jede Datei den Pfad ändern mit REPLACE()
	void updatePath(String alterPfad, String neuerPfad) {
		try {
			Object[] parameter = {alterPfad,neuerPfad};
			int[] types = {TYPE_STRING,TYPE_STRING};
			sendQueryPrepared("UPDATE "+TABLE[DATEI]+" SET "+DB[DATEI][PFAD][0]+" = REPLACE("+DB[DATEI][PFAD][0]+",?,?)", parameter, types, false);
			
			//Vertretungsplan
			sendQueryPrepared("UPDATE "+TABLE[KONFIG]+" SET "+DB[KONFIG][KONFIG_WERT][0]+" = REPLACE("+DB[KONFIG][KONFIG_WERT][0]+",?,?) WHERE "+DB[KONFIG][VARIABLE][0]+" = '"+KONFIG_STRING_VERTRETUNG+"'",parameter,types,false);
			
			//Slides
			sendQueryPrepared("UPDATE "+TABLE[SLIDER]+" SET "+DB[SLIDER][SLIDER_PATH][0]+" = REPLACE("+DB[SLIDER][SLIDER_PATH][0]+",?,?)",parameter,types,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// NutzerID --> String[] {Vorname,Nachname}
	String[] getName(int nutzer_id) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[NUTZER][VORNAME][0]+","+DB[NUTZER][NACHNAME][0]+" FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][ID][0]+"="+nutzer_id, true);
			if(rs.next()) {
				String[] s = new String[2];
				s[0] = rs.getString(1);
				s[1] = rs.getString(2);
				rs.close();
				return s;
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//wenn ein Bild automatisch verkleinert wird
	void updateFileSizeAndPath(int dateiID, int size, String path) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[DATEI][SIZE][0]+" FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ID][0]+"="+dateiID,true);
			if(rs.next()) {
				int oldSize = rs.getInt(1);
				sendQuery("UPDATE "+TABLE[DATEI]+" SET "+DB[DATEI][SIZE][0]+"="+size+","+DB[DATEI][PFAD][0]+"='"+path+"' WHERE "+DB[DATEI][ID][0]+"="+dateiID,false);
				
				insertCloudStats(size - oldSize);
			} else {
				Konsole.warn("updateFileSizeAndPath: rs.next() returned false");
			}
			rs.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	//--------------- Homepage / Gruppen ---------------------------------------------------
	
	// Haupt(gruppen), Links
	// false...ein Fehler ist aufgetreten
	boolean createGruppe(int gruppeID, String name, int priory, String url, String password) {
		try {
			Object[] parameter = {name,gruppeID,url,priory,password,0};
			int[] types = {TYPE_STRING,TYPE_INT,TYPE_STRING,TYPE_INT,TYPE_STRING,TYPE_INT_NULL};
			
			if(gruppeID == -1)
				types[1] = TYPE_INT_NULL;
			else {
				
				//es gibt ein Parent --> Rubrik-Bild ggf. erben
				ResultSet rs = sendQuery("SELECT "+DB[GRUPPE][DATEI_ID][0]+" FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][ID][0]+"="+gruppeID, true);
				if(rs.next()) {
					parameter[5] = rs.getInt(1);
					
					if((int) parameter[5] != 0)
						types[5] = TYPE_INT;
				}
				rs.close();
			}
			
			if(url == null)
				types[2] = TYPE_STRING_NULL;
			
			if(password == null)
				types[4] = TYPE_STRING_NULL;

			
			sendQueryPrepared("INSERT INTO "+TABLE[GRUPPE]+"("+DB[GRUPPE][NAME][0]+","+DB[GRUPPE][GRP_ID][0]+","+DB[GRUPPE][LINK][0]+","+DB[GRUPPE][PRIORY][0]+","+DB[GRUPPE][PASSWORT][0]+","+DB[GRUPPE][DATEI_ID][0]+") VALUES(?,?,?,?,?,?)", parameter, types, false);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	boolean isGruppePublic(int gruppeID) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[GRUPPE][GRP_ID][0]+","+DB[GRUPPE][RUBRIK_LEITER][0]+","+DB[GRUPPE][GENEHMIGT][0]+" FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][ID][0]+"="+gruppeID, true);
			
			if(rs.next()) {
				int parentID = rs.getInt(1);
				int leiterID = rs.getInt(2);
				boolean genehmigt = rs.getBoolean(3);
				rs.close();
				
				if(leiterID != 0) {
					return genehmigt; //Kopf der Rubrik gefunden
				} else if(parentID != 0) {
					return isGruppePublic(parentID); //suche weiter nach oben
				} else {
					return true; //am Ende angekommen
				}
			} else {
				return false;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	void createRubrik(int nutzerID, String name) {
		try {
			
			//gibt es bereits eine Rubrik?
			ResultSet rs = sendQuery("SELECT "+DB[GRUPPE][ID][0]+" FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][RUBRIK_LEITER][0]+"="+nutzerID, true);
			if(rs.next()) {
				rs.close();
				return;
			}
			
			Object[] parameter = {name,nutzerID};
			int[] types = {TYPE_STRING,TYPE_INT};
			
			sendQueryPrepared("INSERT INTO "+TABLE[GRUPPE]+"("+DB[GRUPPE][NAME][0]+","+DB[GRUPPE][RUBRIK_LEITER][0]+") VALUES(?,?)", parameter, types, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	Rubrik getRubrik(int nutzerID) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[GRUPPE][ID][0]+","+DB[GRUPPE][NAME][0]+","+DB[GRUPPE][DATEI_ID][0]+","+DB[GRUPPE][GENEHMIGT][0]+" FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][RUBRIK_LEITER][0]+"="+nutzerID, true);
			if(rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				int dateiID = rs.getInt(3);
				boolean genehmigt = rs.getBoolean(4);
				rs.close();
				
				String[] nameArray = getName(nutzerID);
				String nutzerName = nameArray[0]+" "+nameArray[1];
				
				Gruppe gruppe = new Gruppe(id, name, nutzerName, getGruppeArrayRubrik(nutzerID, false), null, null, -1, null);
				
				Datei bild = null;
				if(dateiID != 0) {
					rs = sendQuery("SELECT * FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ID][0]+"="+dateiID, true);
					bild = resultSetToDatei(rs, 1)[0];
				}
				
				return new Rubrik(id, name, nutzerID, nutzerName, gruppe, bild, genehmigt);
			} else {
				rs.close();
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	Datei getRubrikBild(int gruppeID) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[GRUPPE][DATEI_ID][0]+" FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][ID][0]+"="+gruppeID, true);
			rs.next();
			int dateiID = rs.getInt(1);
			rs.close();
			
			if(dateiID != 0) {
				//gefunden
				rs = sendQuery("SELECT * FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ID][0]+"="+dateiID, true);
				return resultSetToDatei(rs, 1)[0];
			} 
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	void renameRubrik(int gruppeID, String name) {
		
		try {
			
			Object[] parameter = {name,gruppeID};
			int[] types = {TYPE_STRING,TYPE_INT};
			sendQueryPrepared("UPDATE "+TABLE[GRUPPE]+" SET "+DB[GRUPPE][NAME][0]+"=? WHERE "+DB[GRUPPE][ID][0]+"=?", parameter, types, false);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void deleteRubrikBild(int gruppeID) {
		try {
			sendQuery("UPDATE "+TABLE[GRUPPE]+" SET "+DB[GRUPPE][DATEI_ID][0]+"=null WHERE "+DB[GRUPPE][ID][0]+"="+gruppeID, false);
			updateRubrikBildRekursive(gruppeID,-1); //rekursiv an die Kinder
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//nach unten rekursiv übernehmen
	//-1 == null
	private void updateRubrikBildRekursive(int parentID, int dateiID) {
		try {
			String s = dateiID == -1 ? "null" : String.valueOf(dateiID);
			sendQuery("UPDATE "+TABLE[GRUPPE]+" SET "+DB[GRUPPE][DATEI_ID][0]+"="+s+" WHERE "+DB[GRUPPE][GRP_ID][0]+"="+parentID, false);
			
			ResultSet rs = sendQuery("SELECT "+DB[GRUPPE][ID][0]+" FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][GRP_ID][0]+"="+parentID, true);
			while(rs.next()) {
				int id = rs.getInt(1);
				updateRubrikBildRekursive(id, dateiID); //neuer Stack
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Haupt(gruppen), Links, Rubriken
	void deleteGruppe(int gruppeID) {
		try {
			sendQuery("DELETE FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][ID][0]+"="+gruppeID,  false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Haupt(gruppen), Links, Rubriken
	// parentID == -1 => Parent nicht ändern
	// false...ein Fehler ist aufgetreten
	boolean editGruppe(int gruppeID, int parentID, String name, int priory, String link, String password, boolean genehmigt) {
		try {
			if(parentID != -1) {
				Object[] parameter = {name, parentID, priory, link, genehmigt, password, gruppeID};
				int[] types = {TYPE_STRING, TYPE_INT, TYPE_INT, TYPE_STRING, TYPE_BOOLEAN, TYPE_STRING, TYPE_INT};
				
				if(link == null)
					types[3] = TYPE_STRING_NULL;
				
				if(password == null)
					types[5] = TYPE_STRING_NULL;
				
				sendQueryPrepared("UPDATE "+TABLE[GRUPPE]+" SET "+DB[GRUPPE][NAME][0]+" = ?, "+DB[GRUPPE][GRP_ID][0]+" = ?, "+DB[GRUPPE][PRIORY][0]+"=?, "+DB[GRUPPE][LINK][0]+"=?, "+DB[GRUPPE][GENEHMIGT][0]+"=?, "+DB[GRUPPE][PASSWORT][0]+"=? WHERE "+DB[GRUPPE][ID][0]+"=?",parameter,types,false);
			} else {
				Object[] parameter = {name, priory, link, genehmigt, password, gruppeID};
				int[] types = {TYPE_STRING, TYPE_INT, TYPE_STRING, TYPE_BOOLEAN, TYPE_STRING, TYPE_INT};
				
				if(link == null)
					types[2] = TYPE_STRING_NULL;
				
				if(password == null)
					types[4] = TYPE_STRING_NULL;
				
				sendQueryPrepared("UPDATE "+TABLE[GRUPPE]+" SET "+DB[GRUPPE][NAME][0]+" = ?, "+DB[GRUPPE][PRIORY][0]+"=?, "+DB[GRUPPE][LINK][0]+"=?, "+DB[GRUPPE][GENEHMIGT][0]+"=?, "+DB[GRUPPE][PASSWORT][0]+"=? WHERE "+DB[GRUPPE][ID][0]+"=?",parameter,types,false);
			}
			
			updatePassword(gruppeID, password); //Passwort auf die Kinder übertragen
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Übernimmt das Passwort auf die Kinder
	 * @param parentID
	 * @param password
	 */
	private void updatePassword(int parentID, String password) {
		try {

			Object[] parameter = {password, parentID};
			int[] types = {TYPE_STRING, TYPE_INT};
			
			if(password == null)
				types[0] = TYPE_STRING_NULL;
			
			sendQueryPrepared("UPDATE "+TABLE[GRUPPE]+" SET "+DB[GRUPPE][PASSWORT][0]+"=? WHERE "+DB[GRUPPE][GRP_ID][0]+"=?",parameter,types,false);
			
			ResultSet rs = sendQuery("SELECT "+DB[GRUPPE][ID][0]+" FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][GRP_ID][0]+"="+parentID,true);
			while(rs.next()) {
				updatePassword(rs.getInt(1), password);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	Rubrik[] getRubrikArray(boolean genehmigt) {
		try {
			
			String bedingung = genehmigt ? DB[GRUPPE][GENEHMIGT][0]+"=true" : "("+DB[GRUPPE][GENEHMIGT][0]+" IS NULL OR "+DB[GRUPPE][GENEHMIGT][0]+"=false) AND "+DB[GRUPPE][RUBRIK_LEITER][0]+" IS NOT NULL";
			
			//Anzahl
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[GRUPPE]+" WHERE "+bedingung, true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			Rubrik[] rubriken = new Rubrik[anzahl];
			rs = sendQuery("SELECT "+DB[GRUPPE][ID][0]+","+DB[GRUPPE][NAME][0]+","+DB[GRUPPE][DATEI_ID][0]+","+DB[GRUPPE][RUBRIK_LEITER][0]+" FROM "+TABLE[GRUPPE]+" WHERE "+bedingung, true);
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				
				int id = rs.getInt(1);
				String name = rs.getString(2);
				int dateiID = rs.getInt(3);
				int rubrikLeiter = rs.getInt(4);
				
				String[] nameArray = getName(rubrikLeiter);
				String nutzerName = nameArray[0]+" "+nameArray[1];
				
				Gruppe gruppe = new Gruppe(id, name, nutzerName, getGruppeArrayRubrik(rubrikLeiter, false), null, null, -1, null);
				
				Datei bild = null;
				if(dateiID != 0) {
					ResultSet rs2 = sendQuery("SELECT * FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ID][0]+"="+dateiID, true);
					bild = resultSetToDatei(rs2, 1)[0];
				}
				
				rubriken[i] = new Rubrik(id, name, rubrikLeiter, nutzerName, gruppe, bild, genehmigt);
			}
			rs.close();
			return rubriken;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//Alle Gruppen und Untergruppen
	Gruppe[] getGruppeArray() {
		try {
			
			String wurzel = " FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][GRP_ID][0]+" IS NULL AND ("+DB[GRUPPE][RUBRIK_LEITER][0]+" IS NULL OR "+DB[GRUPPE][GENEHMIGT][0]+"=true)";
			
			//Anzahl
			ResultSet rs = sendQuery("SELECT count(*)"+wurzel,  true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			rs = sendQuery("SELECT "+DB[GRUPPE][ID][0]+","+DB[GRUPPE][NAME][0]+","+DB[GRUPPE][LINK][0]+","+DB[GRUPPE][PRIORY][0]+","+DB[GRUPPE][RUBRIK_LEITER][0]+","+DB[GRUPPE][PASSWORT][0]+wurzel,  true);
			Gruppe[] gruppe = new Gruppe[anzahl];
			
			for(int i = 0; i < gruppe.length; i++) {
				rs.next();
				
				int gruppe_id = rs.getInt(1); //id
				String name = rs.getString(2); //name
				String link = rs.getString(3); //Link
				int priory = rs.getInt(4); //priory
				int leiter = rs.getInt(5); //rubrikleiter
				String password = rs.getString(6); //passwort
				
				String rubrikLeiter = null;
				if(leiter != 0) {
					String[] nameArray = getName(leiter);
					rubrikLeiter = nameArray[0]+" "+nameArray[1];
				}
				
				//Untergruppen
				Gruppe[] unterGrp = getGruppeArrayRekursion(gruppe_id, rubrikLeiter, false);
				
				gruppe[i] = new Gruppe(gruppe_id, name, rubrikLeiter, unterGrp, null, link, priory, password);
				
				//parent setzen
				for(int x = 0; x < unterGrp.length; x++) {
					unterGrp[x].setParent(gruppe[i]);
				}
			}
			Gruppe.sortGruppeRekursive(gruppe);
			return gruppe;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//im Gegensatz zu getRubrik(nutzerID), erhält man hier ein Gruppe[]-Array
	Gruppe[] getGruppeArrayRubrik(int nutzerID, boolean includeHome) {
		try {
			
			ResultSet rs = sendQuery("SELECT "+DB[GRUPPE][ID][0]+","+DB[GRUPPE][NAME][0]+","+DB[GRUPPE][LINK][0]+","+DB[GRUPPE][PRIORY][0]+","+DB[GRUPPE][PASSWORT][0]+" FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][RUBRIK_LEITER][0]+"="+nutzerID,  true);
			
			if(rs.next()) {
				
				int gruppeID = rs.getInt(1); //id
				String name = rs.getString(2); //name
				String link = rs.getString(3); //Link
				int priory = rs.getInt(4); //priory
				String password = rs.getString(5); //passwort
				rs.close();
				
				//Leiter
				String[] nameArray = getName(nutzerID);
				String rubrikLeiter = nameArray[0]+" "+nameArray[1];
				
				Gruppe[] gruppe;
				if(includeHome) {
					
					gruppe = new Gruppe[1];
					
					//Untergruppen
					Gruppe[] unterGrp = getGruppeArrayRekursion(gruppeID, rubrikLeiter, false);
					
					gruppe[0] = new Gruppe(gruppeID, name, rubrikLeiter, unterGrp, null, link, priory, password);
					
					//parent setzen
					for(int x = 0; x < unterGrp.length; x++) {
						unterGrp[x].setParent(gruppe[0]);
					}
				} else {
					//Rubrik-Startseite überspringen, Untergruppen angucken
					gruppe = getGruppeArrayRekursion(gruppeID, rubrikLeiter, false);
				}
				Gruppe.sortGruppeRekursive(gruppe);
				return gruppe;
			} else {
				rs.close();
				return new Gruppe[0];
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 
	 * @param parentID Gruppe-ID, von der die Rekursion startet. Alle Gruppen-Objekte "erben" in/direkt die parentID
	 * @param rubrikLeiter Alle Kinder haben auch den Rubrikleiter, wird ggf. durch Rubrik in Rubrik durch anderen Leiter abgelöst
	 * @param onlyOneCall Es werden nur die Kinder zurückgegeben, nicht die Kinder der Kinder, usw.
	 * @return Gruppe-Array, Kinder sind in deren jeweiligen Objekte gespeichert
	 */
	Gruppe[] getGruppeArrayRekursion(int parentID, String rubrikLeiter, boolean onlyOneCall) {
		try {
			
			String kinder = " FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][GRP_ID][0]+" = "+parentID+" AND ("+DB[GRUPPE][RUBRIK_LEITER][0]+" IS NULL OR "+DB[GRUPPE][GENEHMIGT][0]+"=true)";
			
			//Anzahl
			ResultSet rs = sendQuery("SELECT count(*)"+kinder, true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			rs = sendQuery("SELECT "+DB[GRUPPE][ID][0]+","+DB[GRUPPE][NAME][0]+","+DB[GRUPPE][LINK][0]+","+DB[GRUPPE][PRIORY][0]+","+DB[GRUPPE][RUBRIK_LEITER][0]+","+DB[GRUPPE][PASSWORT][0]+kinder,  true);
			Gruppe[] gruppe = new Gruppe[anzahl];
			
			for(int i = 0; i < gruppe.length; i++) {
				rs.next();
				
				int gruppe_id = rs.getInt(1); //id
				String name = rs.getString(2); //name
				String link = rs.getString(3); //Link
				int priory = rs.getInt(4); //priory
				int leiter = rs.getInt(5); //rubrikleiter
				String password = rs.getString(6); //passwort
				
				if(leiter != 0) {
					//ein anderer führt die Rubrik (Rubrik in der Rubrik)
					String[] nameArray = getName(leiter);
					rubrikLeiter = nameArray[0]+" "+nameArray[1];
				}
				
				//Untergruppen
				Gruppe[] unterGrp = onlyOneCall ? new Gruppe[] {}: getGruppeArrayRekursion(gruppe_id, rubrikLeiter, false);
				
				gruppe[i] = new Gruppe(gruppe_id, name, rubrikLeiter, unterGrp, null, link, priory,password);
				
				//parent setzen
				for(int x = 0; x < unterGrp.length; x++) {
					unterGrp[x].setParent(gruppe[i]);
				}
			}
			return gruppe;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	String getGruppeName(int gruppeID) {
		try {
			//Anzahl
			ResultSet rs = sendQuery("SELECT "+DB[GRUPPE][NAME][0]+" FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][ID][0]+"="+gruppeID,  true);
			String name;
			if(rs.next())
				name = rs.getString(1);
			else
				name = null;
			rs.close();
			
			return name;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	int getFirstGruppeID() {
		try {
			//Anzahl
			ResultSet rs = sendQuery("SELECT "+DB[GRUPPE][ID][0]+" FROM "+TABLE[GRUPPE]+" limit 1",  true);
			if(rs.next() == false) {
				return VALUE_DEFAULT_GRUPPE_ID;
			}
			int id = rs.getInt(1);
			rs.close();
			
			return id;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	//gibt eine Gruppe(ID,Name) mit Hilfe der ID zurück
	//keine weiteren Informationen (Untergruppen, etc.)
	Gruppe getGruppeBasic(int gruppeID) {
		//Sammlung
		String name = null;
		ResultSet rs;
		try {
			rs = sendQuery("SELECT "+DB[GRUPPE][NAME][0]+" FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][ID][0]+"="+gruppeID,true);
			if(rs.next()) {
				name = rs.getString(1);
			}
			rs.close();
			
			if(name != null)
				return new Gruppe(gruppeID,name);
			else
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	String getGruppePasswort(int gruppeID) {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[GRUPPE][PASSWORT][0]+" FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][ID][0]+"="+gruppeID,true);
			String password = null;
			if(rs.next()) {
				password = rs.getString(1);
			}
			rs.close();
			
			return password;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	int getAnzahlGruppe() {
		try {
			ResultSet rs = sendQuery("SELECT COUNT(*) FROM "+TABLE[GRUPPE],true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			return anzahl;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	//--------------- Homepage / Beiträge ---------------------------------------------------
	
	void createBeitrag(int grp_id, int nutzer_id, String titel, String text, int vorschau, int layout_beitrag, int layout_vorschau, boolean showAutor) {
		
		int type_text;
		if(text == null)
			type_text = TYPE_STRING_NULL;
		else
			type_text = TYPE_STRING;
		
		try {
			if(grp_id != -1) {
				//Beitrag wird erstellt und SOFORT angezeigt
				Object[] parameter = {nutzer_id,grp_id,titel,text,vorschau,layout_beitrag,layout_vorschau,showAutor};
				int[] types = {TYPE_INT,TYPE_INT,TYPE_STRING,type_text,TYPE_INT,TYPE_INT,TYPE_INT,TYPE_BOOLEAN};
				sendQueryPrepared("INSERT INTO "+TABLE[BEITRAG]+"("+DB[BEITRAG][NUTZER_ID][0]+","+
																	DB[BEITRAG][GRP_ID][0]+","+
																	DB[BEITRAG][TITEL][0]+","+
																	DB[BEITRAG][TEXT][0]+","+
																	DB[BEITRAG][GENEHMIGT][0]+","+
																	DB[BEITRAG][VORSCHAU][0]+","+
																	DB[BEITRAG][LAYOUT_BEITRAG][0]+","+
																	DB[BEITRAG][LAYOUT_VORSCHAU][0]+","+
																	DB[BEITRAG][SHOW_AUTOR][0]+") VALUES(?,?,?,?,true,?,?,?,?)", parameter, types, false);	
			} else {
				//Beitrag wird erstellt, aber noch nicht genehmigt
				Object[] parameter = {nutzer_id,titel,text,vorschau,layout_beitrag,layout_vorschau,showAutor};
				int[] types = {TYPE_INT,TYPE_STRING,type_text,TYPE_INT,TYPE_INT,TYPE_INT,TYPE_BOOLEAN};
				sendQueryPrepared("INSERT INTO "+TABLE[BEITRAG]+"("+DB[BEITRAG][NUTZER_ID][0]+","+
																	DB[BEITRAG][TITEL][0]+","+
																	DB[BEITRAG][TEXT][0]+","+
																	DB[BEITRAG][GENEHMIGT][0]+","+
																	DB[BEITRAG][VORSCHAU][0]+","+
																	DB[BEITRAG][LAYOUT_BEITRAG][0]+","+
																	DB[BEITRAG][LAYOUT_VORSCHAU][0]+","+
																	DB[BEITRAG][SHOW_AUTOR][0]+") VALUES(?,?,?,false,?,?,?,?)", parameter, types, false);	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void deleteBeitrag(int beitrag_id) {
		try {
			sendQuery("DELETE FROM "+TABLE[BEITRAG]+" WHERE "+DB[BEITRAG][ID][0]+"="+beitrag_id,  false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void editBeitrag(int nutzer_id, int beitrag_id, String titel, String text, int vorschau, int layout_beitrag, int layout_vorschau, boolean showAutor, boolean updateDatum) {
		try {
			int type_text;
			if(text == null)
				type_text = TYPE_STRING_NULL;
			else
				type_text = TYPE_STRING;
			
			String updateDatumString = "";
			if(updateDatum)
				updateDatumString = DB[BEITRAG][DATUM][0]+"=NOW(),";
			
			Object[] parameter = {titel, text, nutzer_id, vorschau, layout_beitrag, layout_vorschau, showAutor, beitrag_id};
			int[] types = {TYPE_STRING, type_text, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_BOOLEAN, TYPE_INT};
			sendQueryPrepared("UPDATE "+TABLE[BEITRAG]+" SET "+DB[BEITRAG][TITEL][0]+" = ?,"
																	+DB[BEITRAG][TEXT][0]+"=?,"
																	+DB[BEITRAG][NUTZER_ID_UPDATE][0]+"=?,"
																	+updateDatumString
																	+DB[BEITRAG][VORSCHAU][0]+"=?,"
																	+DB[BEITRAG][LAYOUT_BEITRAG][0]+"=?,"
																	+DB[BEITRAG][LAYOUT_VORSCHAU][0]+"=?,"
																	+DB[BEITRAG][SHOW_AUTOR][0]+"=?"
																	+" WHERE "+DB[BEITRAG][ID][0]+"=?",parameter,types,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void editUnapprovedBeitrag(int beitrag_id, String titel, String text, int vorschau, int layout_beitrag, int layout_vorschau, boolean showAutor) {
		try {
			int type_text;
			if(text == null)
				type_text = TYPE_STRING_NULL;
			else
				type_text = TYPE_STRING;
			
			Object[] parameter = {titel, text, vorschau, layout_beitrag, layout_vorschau, showAutor, beitrag_id};
			int[] types = {TYPE_STRING, type_text, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_BOOLEAN, TYPE_INT};
			sendQueryPrepared("UPDATE "+TABLE[BEITRAG]+" SET "+DB[BEITRAG][TITEL][0]+" = ?,"
																	+DB[BEITRAG][TEXT][0]+"=?,"
																	+DB[BEITRAG][DATUM][0]+"=NOW(),"
																	+DB[BEITRAG][VORSCHAU][0]+"=?,"
																	+DB[BEITRAG][LAYOUT_BEITRAG][0]+"=?,"
																	+DB[BEITRAG][LAYOUT_VORSCHAU][0]+"=?,"
																	+DB[BEITRAG][SHOW_AUTOR][0]+"=?"
																	+" WHERE "+DB[BEITRAG][ID][0]+"=?",parameter,types,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//boolean update = Admin hat während der Genehmigung den Beitrag geändert
	void approveBeitrag(int nutzer_id, int beitrag_id, int gruppeID, String titel, String text, int vorschau, int layout_beitrag, int layout_vorschau, boolean showAutor, boolean update) {
		try {
			if(update) {
				//Beitrag wurde geändert
				int type_text;
				if(text == null)
					type_text = TYPE_STRING_NULL;
				else
					type_text = TYPE_STRING;
				
				Object[] parameter = {titel, text, nutzer_id, gruppeID, vorschau, layout_beitrag, layout_vorschau, showAutor, beitrag_id};
				int[] types = {TYPE_STRING, type_text, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_BOOLEAN, TYPE_INT};
				sendQueryPrepared("UPDATE "+TABLE[BEITRAG]+" SET "+DB[BEITRAG][TITEL][0]+"=?,"
																	+DB[BEITRAG][TEXT][0]+"=?,"
																	+DB[BEITRAG][NUTZER_ID_UPDATE][0]+"=?,"
																	+DB[BEITRAG][DATUM][0]+"=NOW(),"
																	+DB[BEITRAG][GENEHMIGT][0]+"=true,"
																	+DB[BEITRAG][GRP_ID][0]+"=?,"
																	+DB[BEITRAG][VORSCHAU][0]+"=?,"
																	+DB[BEITRAG][LAYOUT_BEITRAG][0]+"=?,"
																	+DB[BEITRAG][LAYOUT_VORSCHAU][0]+"=?,"
																	+DB[BEITRAG][SHOW_AUTOR][0]+"=?"
																	+" WHERE "+DB[BEITRAG][ID][0]+"=?",parameter,types,false);
			} else {
				//Beitrag blieb im Originalzustand
				sendQuery("UPDATE "+TABLE[BEITRAG]+" SET "+DB[BEITRAG][GENEHMIGT][0]+"=true, "
															+DB[BEITRAG][GRP_ID][0]+"="+gruppeID+" WHERE "+DB[BEITRAG][ID][0]+"="+beitrag_id,false);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//von BeitragOpen-Bean aufgerufen
	Beitrag getBeitrag(int beitragID, boolean genehmigt) {
		try {
			
			//Beitrag[] erstellen
			ResultSet rs = sendQuery("SELECT b."+DB[BEITRAG][ID][0]
										+",b."+DB[BEITRAG][TITEL][0]
										+",b."+DB[BEITRAG][TEXT][0]
										+",b."+DB[BEITRAG][DATUM][0]
										+",b."+DB[BEITRAG][VORSCHAU][0] //5
										+",b."+DB[BEITRAG][LAYOUT_BEITRAG][0]
										+",b."+DB[BEITRAG][LAYOUT_VORSCHAU][0]
										+",b."+DB[BEITRAG][SHOW_AUTOR][0]
										+",b."+DB[BEITRAG][GRP_ID][0]
										+",b."+DB[BEITRAG][PASSWORT][0] //10
										+",n."+DB[NUTZER][VORNAME][0]
										+",n."+DB[NUTZER][NACHNAME][0]
										+",n."+DB[NUTZER][GESCHLECHT][0]
										+",z."+DB[ZUGANGSCODE][RANG][0] //14
											+" FROM "+TABLE[BEITRAG]+" b, "+TABLE[NUTZER]+" n, "+TABLE[ZUGANGSCODE]+" z"
											+" WHERE n."+DB[NUTZER][ID][0]+"=b."+DB[BEITRAG][NUTZER_ID][0]+" AND " //Nutzer <-> Beitrag
												+"n."+DB[NUTZER][CODE_ID][0]+"=z."+DB[ZUGANGSCODE][ID][0]+" AND " //Nutzer <-> Zugangscode
												+"b."+DB[BEITRAG][ID][0]+"="+beitragID+" AND "
												+"b."+DB[BEITRAG][GENEHMIGT][0]+"="+genehmigt,true);
			Beitrag beitrag = null;
			
			if(rs.next()) {
				int beitrag_id = rs.getInt(1);
				String titel = rs.getString(2);
				String text = rs.getString(3);
				String datum = Timeformats.dateOnly().format(rs.getTimestamp(4));
				int vorschau = rs.getInt(5);
				int layoutBeitrag = rs.getInt(6);
				int layoutVorschau = rs.getInt(7);
				boolean showAutor = rs.getBoolean(8);
				
				//Gruppe
				int gruppeID = rs.getInt(9);
				Gruppe gruppe = getGruppeBasic(gruppeID);
				
				//Password?
				boolean password = rs.getString(10) != null;
				
				//Nutzer
				String vorname = rs.getString(11);
				String nachname = rs.getString(12);
				byte geschlecht = rs.getByte(13);
				int rang = rs.getInt(14);
				Nutzer nutzer = new Nutzer(rang, vorname, nachname, geschlecht);
				
				//Bilder
				Datei[] datei = getFileArrayBeitrag(beitrag_id);
				
				beitrag = new Beitrag(beitrag_id, gruppe, titel, text, nutzer, datum, datei, vorschau, layoutBeitrag, layoutVorschau, showAutor, password, true);
			}
			
			rs.close();
			return beitrag;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	void setPasswordBeitrag(int beitragID, String password) {
		try {
			Object[] parameter = {password,beitragID};
			
			if(password != null) {
				int[] types = {TYPE_STRING,TYPE_INT};
				sendQueryPrepared("UPDATE "+TABLE[BEITRAG]+" SET "+DB[BEITRAG][PASSWORT][0]+"=MD5(?) WHERE "+DB[BEITRAG][ID][0]+"=?", parameter, types, false);
			} else {
				int[] types = {TYPE_STRING_NULL,TYPE_INT};
				sendQueryPrepared("UPDATE "+TABLE[BEITRAG]+" SET "+DB[BEITRAG][PASSWORT][0]+"=? WHERE "+DB[BEITRAG][ID][0]+"=?", parameter, types, false);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	boolean checkPasswordBeitrag(int beitragID, String password) {
		try {
			Object[] parameter = {beitragID,password};
			int[] types = {TYPE_INT,TYPE_STRING};
			ResultSet rs = sendQueryPrepared("SELECT count(*) FROM "+TABLE[BEITRAG]+" WHERE "+DB[BEITRAG][ID][0]+"=? AND "+DB[BEITRAG][PASSWORT][0]+"=MD5(?)", parameter, types, true);
			rs.next();
			
			boolean ok = rs.getInt(1) > 0;
			rs.close();
			
			return ok;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//von BeitragList,EditPost,ProjektOpen aufgerufen
	// limit = -1 (no limit)
	Beitrag[] getBeitragArray(int gruppeID, int limit) {
		try {
			//Anzahl
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[BEITRAG]+" WHERE "+DB[BEITRAG][GRP_ID][0]+"="+gruppeID,true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			if(anzahl == 0)
				return new Beitrag[]{};
			
			//Sammlung
			Gruppe gruppe = getGruppeBasic(gruppeID); //Alle Beiträge verweisen auf diese Sammlung
			
			if(limit != -1 && anzahl > limit)
				anzahl = limit;
			
			//Beitrag[] erstellen
			rs = sendQuery("SELECT d."+DB[DATEI][ID][0]
										+",d."+DB[DATEI][PFAD][0]
										+",d."+DB[DATEI][DATUM][0]
										+",d."+DB[DATEI][SIZE][0]
										+",d."+DB[DATEI][MIME][0]
										+",d."+DB[DATEI][PUBLIC][0]
										+",d."+DB[DATEI][NUTZER_ID][0]
												
										+",b."+DB[BEITRAG][ID][0] // 8
										+",b."+DB[BEITRAG][TITEL][0] // 9
										+",b."+DB[BEITRAG][TEXT][0] // 10
										+",b."+DB[BEITRAG][DATUM][0] // 11
										+",b."+DB[BEITRAG][VORSCHAU][0] // 12
										+",b."+DB[BEITRAG][LAYOUT_BEITRAG][0] // 13
										+",b."+DB[BEITRAG][LAYOUT_VORSCHAU][0] // 14
										+",b."+DB[BEITRAG][SHOW_AUTOR][0] // 15
										+",b."+DB[BEITRAG][NUTZER_ID][0] // 16
										+",n."+DB[NUTZER][VORNAME][0] // 17
										+",n."+DB[NUTZER][NACHNAME][0] // 18
										+",b."+DB[BEITRAG][PASSWORT][0]+" FROM "+TABLE[BEITRAG]+" b JOIN "+TABLE[NUTZER]+" n ON n."+DB[NUTZER][ID][0]+"=b."+DB[BEITRAG][NUTZER_ID][0]
												+ " LEFT JOIN "+TABLE[DATEI_BEITRAG]+" db ON db."+DB[DATEI_BEITRAG][MN_BEITRAG_ID][0]+"=b."+DB[BEITRAG][ID][0]
												+ " LEFT JOIN "+TABLE[DATEI]+" d ON db."+DB[DATEI_BEITRAG][MN_DATEI_ID][0]+"=d."+DB[DATEI][ID][0]
												+ " WHERE b."+DB[BEITRAG][GRP_ID][0]+"="+gruppeID+" ORDER BY b."+DB[BEITRAG][DATUM][0]+" DESC",true);
			Beitrag[] beitrag = new Beitrag[anzahl];
			
			SimpleDateFormat format = Timeformats.dateOnly();
			SimpleDateFormat formatDatei = Timeformats.completeNoSeconds();
			List<Datei> list = new ArrayList<>();
			for(int x = 0; x < anzahl; x++) {
				rs.next();
				int beitrag_id = rs.getInt(8);
				String titel = rs.getString(9);
				String text = rs.getString(10);
				String datum = format.format(rs.getTimestamp(11));
				int vorschau = rs.getInt(12);
				int layoutBeitrag = rs.getInt(13);
				int layoutVorschau = rs.getInt(14);
				boolean showAutor = rs.getBoolean(15);
				
				//Nutzer
				int nutzer_id = rs.getInt(16);
				String vorname = rs.getString(17);
				String nachname = rs.getString(18);
				
				Nutzer nutzer = new Nutzer(nutzer_id, vorname, nachname);
				
				//Password?
				boolean password = rs.getString(19) != null;
				
				//Bilder
				list.clear();
				if(rs.getString(2) != null) {
					
					while(rs.getInt(8) == beitrag_id) {
						
						Datei datei = resultSetToDatei(rs, vorname, nachname, formatDatei, false);
						list.add(datei);
						
						if(!rs.next())
							break; //Ende des ResultSets
					}
					rs.previous();
				}
				
				Datei[] datei = list.toArray(new Datei[list.size()]);
				
				beitrag[x] = new Beitrag(beitrag_id, gruppe, titel, text, nutzer, datum, datei, vorschau, layoutBeitrag, layoutVorschau, showAutor, password, true);
			}
			rs.close();
			return beitrag;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//z.B. von ApprovePost, aber auch BeitragSearch, BeitragList (Merge aktiviert)
	//im Gegensatz zur oberen methode, muss grpID jedesmal ermittelt werden
	Beitrag[] getBeitragArray(boolean genehmigt) {
		try {
			//Anzahl
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[BEITRAG]+" WHERE "+DB[BEITRAG][GENEHMIGT][0]+"="+genehmigt,true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			if(anzahl == 0)
				return new Beitrag[]{};
			
			//Beitrag[] erstellen
			rs = sendQuery("SELECT b."+DB[BEITRAG][ID][0]
										+",b."+DB[BEITRAG][TITEL][0]
										+",b."+DB[BEITRAG][TEXT][0]
										+",b."+DB[BEITRAG][DATUM][0]
										+",b."+DB[BEITRAG][VORSCHAU][0] // 5
										+",b."+DB[BEITRAG][LAYOUT_BEITRAG][0]
										+",b."+DB[BEITRAG][LAYOUT_VORSCHAU][0]
										+",b."+DB[BEITRAG][SHOW_AUTOR][0]
										+",b."+DB[BEITRAG][GRP_ID][0]
										+",b."+DB[BEITRAG][PASSWORT][0] // 10
										+",n."+DB[NUTZER][VORNAME][0]
										+",n."+DB[NUTZER][NACHNAME][0]
										+",n."+DB[NUTZER][GESCHLECHT][0]
										+",z."+DB[ZUGANGSCODE][RANG][0] //14
												+" FROM "+TABLE[BEITRAG]+" b,"+TABLE[NUTZER]+" n,"+TABLE[ZUGANGSCODE]+" z "
														+ "WHERE b."+DB[BEITRAG][NUTZER_ID][0]+"=n."+DB[NUTZER][ID][0]+" AND "
														+ "n."+DB[NUTZER][CODE_ID][0]+"=z."+DB[ZUGANGSCODE][ID][0]+" AND "
														+ "b."+DB[BEITRAG][GENEHMIGT][0]+"="+genehmigt+" ORDER BY b."+DB[BEITRAG][DATUM][0]+" DESC",true);
			Beitrag[] beitrag = new Beitrag[anzahl];
			
			Zwischenspeicher<Gruppe> speicherGruppe = new Zwischenspeicher<>(getAnzahlGruppe());
			SimpleDateFormat format = Timeformats.dateOnly();
			for(int x = 0; x < beitrag.length; x++) {
				rs.next();
				int beitrag_id = rs.getInt(1);
				String titel = rs.getString(2);
				String text = rs.getString(3);
				String datum = format.format(rs.getTimestamp(4));
				int vorschau = rs.getInt(5);
				int layoutBeitrag = rs.getInt(6);
				int layoutVorschau = rs.getInt(7);
				boolean showAutor = rs.getBoolean(8);
				boolean password = rs.getString(10) != null;
				
				//Nutzer
				String vorname = rs.getString(11);
				String nachname = rs.getString(12);
				byte geschlecht = rs.getByte(13);
				int rang = rs.getInt(14);
				Nutzer nutzer = new Nutzer(rang, vorname, nachname, geschlecht);
				
				//Bilder
				Datei[] datei = getFileArrayBeitrag(beitrag_id);
				
				//Parent
				int gruppeID = rs.getInt(9);
				Gruppe gruppe = speicherGruppe.get(gruppeID);
				
				if(gruppe == null) {
					gruppe = getGruppeBasic(gruppeID);
					speicherGruppe.add(gruppeID, gruppe);
				}
				
				beitrag[x] = new Beitrag(beitrag_id, gruppe, titel, text, nutzer, datum, datei, vorschau, layoutBeitrag, layoutVorschau, showAutor,password, true);
			}
			rs.close();
			return beitrag;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	int getBeitragAnzahl(boolean genehmigt) {
		try {
			//Anzahl
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[BEITRAG]+" WHERE "+DB[BEITRAG][GENEHMIGT][0]+"="+genehmigt,true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			return anzahl;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	//wird zur noch-maligen überprüfung gebraucht
	boolean isApproved(int beitrag_id) {
		try {
			
			//Anzahl
			ResultSet rs = sendQuery("SELECT "+DB[BEITRAG][GENEHMIGT][0]+" FROM "+TABLE[BEITRAG]+" WHERE "+DB[BEITRAG][ID][0]+"="+beitrag_id,true);
			boolean hasNext = rs.next();
			
			if(!hasNext) {
				rs.close();
				return true;
			}
			
			boolean genehmigt = rs.getBoolean(1);
			rs.close();
			
			return genehmigt;
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	boolean existBeitrag(int beitrag_id) {
		try {
			//Anzahl
			ResultSet rs = sendQuery("SELECT * FROM "+TABLE[BEITRAG]+" WHERE "+DB[BEITRAG][ID][0]+"="+beitrag_id,true);
			boolean hasNext = rs.next();
			
			if(!hasNext) {
				rs.close();
				return false;
			}
			
			rs.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	Beitrag[] getUnapprovedBeitragArray(int nutzer_id) {
		try {
			//Anzahl
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[BEITRAG]+" WHERE "+DB[BEITRAG][GENEHMIGT][0]+"=false AND "+DB[BEITRAG][NUTZER_ID][0]+"="+nutzer_id,true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			if(anzahl == 0)
				return new Beitrag[]{};
			
			//Nutzer
			String[] nutzer_name = getName(nutzer_id);
			
			Object klasse_id_object = getDataTabelleNutzer(KLASSE_ID, nutzer_id);
			String klasse = null;
			if(klasse_id_object != null)
				klasse = getKlassenname((int) klasse_id_object);
			byte geschlecht = (byte) (int) getDataTabelleNutzer(GESCHLECHT, nutzer_id);
			Nutzer nutzer = new Nutzer(nutzer_id, nutzer_name[0], nutzer_name[1], klasse, geschlecht);
			
			//Beitrag[] erstellen
			rs = sendQuery("SELECT * FROM "+TABLE[BEITRAG]+" WHERE "+DB[BEITRAG][GENEHMIGT][0]+"=false AND "+DB[BEITRAG][NUTZER_ID][0]+"="+nutzer_id+" ORDER BY "+DB[BEITRAG][DATUM][0]+" DESC",true);
			Beitrag[] beitrag = new Beitrag[anzahl];
			SimpleDateFormat format = Timeformats.dateOnly();
			for(int x = 0; x < beitrag.length; x++) {
				rs.next();
				int beitrag_id = rs.getInt(DB[BEITRAG][ID][0]);
				String titel = rs.getString(DB[BEITRAG][TITEL][0]);
				String text = rs.getString(DB[BEITRAG][TEXT][0]);
				String datum = format.format(rs.getTimestamp(DB[BEITRAG][DATUM][0]));
				int vorschau = rs.getInt(DB[BEITRAG][VORSCHAU][0]);
				int layoutBeitrag = rs.getInt(DB[BEITRAG][LAYOUT_BEITRAG][0]);
				int layoutVorschau = rs.getInt(DB[BEITRAG][LAYOUT_VORSCHAU][0]);
				boolean showAutor = rs.getBoolean(DB[BEITRAG][SHOW_AUTOR][0]);
				
				//Bilder
				Datei[] datei = getFileArrayBeitrag(beitrag_id);
				
				beitrag[x] = new Beitrag(beitrag_id, null, titel, text, nutzer, datum, datei, vorschau, layoutBeitrag, layoutVorschau, showAutor,false, false);
			}
			rs.close();
			return beitrag;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//eine zu einem beliebigen Datei eine beitrag_id hinzufügen
	void connectDateiBeitrag(int datei_id, int beitrag_id) {
		try {
			//ggf. alte Verknüpfung löschen
			sendQuery("DELETE FROM "+TABLE[DATEI_BEITRAG]+" WHERE "+DB[DATEI_BEITRAG][MN_DATEI_ID][0]+"="+datei_id+" AND "+DB[DATEI_BEITRAG][MN_BEITRAG_ID][0]+"="+beitrag_id, false);
			//neue hinzufügen
			sendQuery("INSERT INTO "+TABLE[DATEI_BEITRAG]+"("+DB[DATEI_BEITRAG][MN_DATEI_ID][0]+","+DB[DATEI_BEITRAG][MN_BEITRAG_ID][0]+") VALUES("+datei_id+","+beitrag_id+")", false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void disconnectDateiBeitrag(int beitrag_id) {
		try {
			sendQuery("DELETE FROM "+TABLE[DATEI_BEITRAG]+" WHERE "+DB[DATEI_BEITRAG][MN_BEITRAG_ID][0]+"="+beitrag_id, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//Alle Dateien löschen, die keine NutzerID,KlasseID,ProjektID,Rubrik haben und mit keinem Beitrag verknüpft sind
	//wird beim bearbeiten von Beiträgen aufgerufen
	void cleanDateiBeitrag() {
		try {
			//potentielle DateiIDs finden
			
			String s = " FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][NUTZER_ID][0]+" IS NULL AND "
														+DB[DATEI][PROJEKT_ID][0]+" IS NULL AND "
														+DB[DATEI][KLASSE_ID][0]+" IS NULL";
			ResultSet rs = sendQuery("SELECT count(*)"+s,true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			
			rs = sendQuery("SELECT "+DB[DATEI][ID][0]+s,true);
			
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				int datei_id = rs.getInt(1);
				
				//Ist die Datei mit einem Beitrag verknüpft ?
				ResultSet rs_curr = sendQuery("SELECT count(*) FROM "+TABLE[DATEI_BEITRAG]+" WHERE "+DB[DATEI_BEITRAG][MN_DATEI_ID][0]+"="+datei_id,true);
				rs_curr.next();
				int beziehungen = rs_curr.getInt(1);
				rs_curr.close();
				
				//falls vorher beziehungen > 0 herrscht, dann darf man nicht mehr überprüfen
				if(beziehungen == 0) {
					//Ist diese Datei mit einer Gruppe verknüpft?
					rs_curr = sendQuery("SELECT count(*) FROM "+TABLE[GRUPPE]+" WHERE "+DB[GRUPPE][DATEI_ID][0]+"="+datei_id,true);
					rs_curr.next();
					beziehungen = rs_curr.getInt(1);
					rs_curr.close();
				}
				
				if(beziehungen == 0) {
					//Datei löschen
					rs_curr = sendQuery("SELECT "+DB[DATEI][PFAD][0]+","+DB[DATEI][SIZE][0]+" FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ID][0]+"="+datei_id,true);
					rs_curr.next();
					String pfad = rs_curr.getString(1);
					int size = rs_curr.getInt(2);
					rs_curr.close();
					sendQuery("DELETE FROM "+TABLE[DATEI]+" WHERE "+DB[DATEI][ID][0]+"="+datei_id,false);
					insertCloudStats(-size);
					Files.delete(Paths.get(pfad));
				}
			}
			rs.close();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	
	//--------------- Logs ---------------------------------------------------
	
	int getLogsAmount() {
		try {
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[LOG], true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			return anzahl;
		}  catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	Ereignis[] getLogs(boolean[] showEreignis, int limit) {
		try {
			
			int anzahlShow = 0;
			for(int i = 0; i < showEreignis.length; i++)
				if(showEreignis[i])
					anzahlShow++;
			
			StringBuilder builder = new StringBuilder();
			int current = 0;
			for(int i = 0; i < showEreignis.length; i++) {
				
				if(showEreignis[i]) {
					builder.append(DB[LOG][LOG_TYP][0]+"="+i);
					current++;
					
					if(current < anzahlShow)
						builder.append(" OR ");
				}
			}
			
			if(anzahlShow == 0)
				return new Ereignis[0];
			
			
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[LOG]+" WHERE "+builder.toString(), true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			String s = "";
			if(limit != -1 && limit < anzahl) {
				s = " LIMIT "+limit;
				anzahl = limit;
			}
			
			rs = sendQuery("SELECT * FROM "+TABLE[LOG]+" WHERE "+builder.toString()+" ORDER BY "+DB[LOG][LOG_DATUM][0]+" DESC"+s, true);
			
			Ereignis[] output = new Ereignis[anzahl];
			SimpleDateFormat format = Timeformats.complete();
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				
				String datum = format.format(rs.getTimestamp(DB[LOG][LOG_DATUM][0]));
				String ereignis = rs.getString(DB[LOG][LOG_EREIGNIS][0]);
				int typ = rs.getInt(DB[LOG][LOG_TYP][0]);
				
				output[i] = new Ereignis(datum, ereignis, typ);
			}
			
			return output;
		} catch (SQLException e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	void addLog(String ereignis, int typ) {
		try {
			Object[] input = {typ,ereignis};
			int[] type = {TYPE_INT,TYPE_STRING};
			sendQueryPrepared("INSERT INTO "+TABLE[LOG]+"("+DB[LOG][LOG_TYP][0]+","+DB[LOG][LOG_EREIGNIS][0]+") VALUES(?,?)",input,type,false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void clearLog(int days) {
		try {
			sendQuery("DELETE FROM "+TABLE[LOG]+" WHERE "+DB[LOG][LOG_DATUM][0]+" < (NOW() - INTERVAL "+days+" DAY)", false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//--------------- Statistiken ---------------------------------------------------
	
	//Aktualisert oder Fügt ein neuer Eintrag ein
	synchronized int saveStats(String dateString, int amountDay, int indexHour, int hour) {
		try {
			int id;
			ResultSet rs = sendQuery("SELECT "+DB[STATS_TAGE][ID][0]+" FROM "+TABLE[STATS_TAGE]+" WHERE "+DB[STATS_TAGE][STATS_TAGE_DATUM][0]+" LIKE '"+dateString+"%'", true);
			if(rs.next()) {
				//existiert bereits
				id = rs.getInt(1);
				rs.close();
			} else {
				rs.close();
				sendQuery("INSERT INTO "+TABLE[STATS_TAGE]+"("+DB[STATS_TAGE][STATS_TAGE_DATUM][0]+","+DB[STATS_TAGE][STATS_TAGE_ANZAHL][0]+") VALUES('"+dateString+"',"+amountDay+")", false);
				
				rs = sendQuery("SELECT LAST_INSERT_ID();",true);
				rs.next();
				id = rs.getInt(1);
				rs.close();
			}
			
			saveStats(id,amountDay,indexHour,hour);
			return id;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	//Aktualisert ein Eintrag
	void saveStats(int dateID, int amountDay, int indexHour, int hour) {
		try {
			sendQuery("UPDATE "+TABLE[STATS_TAGE]+" SET "+DB[STATS_TAGE][STATS_TAGE_ANZAHL][0]+"="+amountDay+" WHERE "+DB[STATS_TAGE][ID][0]+"="+dateID, false);
			sendQuery("UPDATE "+TABLE[STATS_STUNDEN]+" SET "+DB[STATS_STUNDEN][STATS_STUNDEN_UHRZEIT][0]+"="+hour+" WHERE "+DB[STATS_STUNDEN][STATS_STUNDEN_INDEX][0]+"="+indexHour, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	Count[] getStatsTage() {
		try {
			
			String suffix = " FROM "+TABLE[STATS_TAGE]+" WHERE "+DB[STATS_TAGE][STATS_TAGE_DATUM][0]+" >= CURDATE() - INTERVAL "+(ViewCounter.COUNTER_DAY_INTERVAL-1)+" DAY";
			ResultSet rs = sendQuery("SELECT count(*)" + suffix, true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			rs = sendQuery("SELECT "+DB[STATS_TAGE][STATS_TAGE_DATUM][0]+","+DB[STATS_TAGE][STATS_TAGE_ANZAHL][0] + suffix, true);
			
			Calendar calendar = Calendar.getInstance();
			Count[] count = new Count[ViewCounter.COUNTER_DAY_INTERVAL];
			
			if(anzahl == ViewCounter.COUNTER_DAY_INTERVAL) {
				
				// Daten 1:1 übernehmen
				
				for(int i = 0; i < count.length; i++) {
					rs.next();
					
					Timestamp stamp = rs.getTimestamp(1);
					int views = rs.getInt(2);
					
					calendar.setTimeInMillis(stamp.getTime());
					int day = calendar.get(Calendar.DAY_OF_MONTH);
					int month = calendar.get(Calendar.MONTH)+1;
					String dateString = day == 1 ? day+"."+month : String.valueOf(day);
					
					count[i] = new Count(dateString, views, day, month);
				}
				
			} else if(anzahl > 0){
				// anzahl < ViewCounter.COUNTER_DAY_INTERVAL
				// es gibt lücken (Tage mit 0 Aufrufe)
				
				
				calendar.setTimeInMillis(System.currentTimeMillis());
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.add(Calendar.DATE, -ViewCounter.COUNTER_DAY_INTERVAL+1); //abziehen, heutiger Tag zählt mit
				
				int index = 0; // Count[index]
				
				Calendar databaseCalendar = Calendar.getInstance();
				while(rs.next()) { // ResultSet durchgehen
					
					Timestamp stamp = rs.getTimestamp(1);
					int views = rs.getInt(2);
					
					databaseCalendar.setTimeInMillis(stamp.getTime());
					
					while(!calendar.equals(databaseCalendar)) {
						// "leeren" Tagen überspringen
						int day = calendar.get(Calendar.DAY_OF_MONTH);
						int month = calendar.get(Calendar.MONTH)+1;
						String dateString = day == 1 ? day+"."+month : String.valueOf(day);
						count[index] = new Count(dateString, 0, day, month);
						index++;
						calendar.add(Calendar.DATE, 1); //für nächste Iteration
					}
					
					int day = databaseCalendar.get(Calendar.DAY_OF_MONTH);
					int month = databaseCalendar.get(Calendar.MONTH)+1;
					String dateString = day == 1 ? day+"."+month : String.valueOf(day);
					
					count[index] = new Count(dateString, views, day, month);
					index++;
					calendar.add(Calendar.DATE, 1); //für nächste Iteration
				}
				
				while(index < ViewCounter.COUNTER_DAY_INTERVAL) {
					
					//falls die leeren Tage am Ende sind
					//Kalendar zählt bis heute
					int day = calendar.get(Calendar.DAY_OF_MONTH);
					int month = calendar.get(Calendar.MONTH)+1;
					String dateString = day == 1 ? day+"."+month : String.valueOf(day);
					count[index] = new Count(dateString, 0, day, month);
					
					index++;
					
					calendar.add(Calendar.DATE, 1); //für nächste Iteration
				}
				
				calendar.add(Calendar.DATE, -1); //letzte Aktion invalid, da Schleife abgebrochen
				if(calendar.get(Calendar.DAY_OF_YEAR) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
					Konsole.warn("MySQLManager.getStatsTage(): "+calendar.get(Calendar.DAY_OF_YEAR)+" != "+Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
				}
			} else {
				//leer
				calendar.setTimeInMillis(System.currentTimeMillis());
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.add(Calendar.DATE, -ViewCounter.COUNTER_DAY_INTERVAL+1); //abziehen, heutiger Tag zählt mit
				
				for(int i = 0; i < ViewCounter.COUNTER_DAY_INTERVAL; i++) {
					int day = calendar.get(Calendar.DAY_OF_MONTH);
					int month = calendar.get(Calendar.MONTH)+1;
					String dateString = day == 1 ? day+"."+month : String.valueOf(day);
					count[i] = new Count(dateString, 0, day, month);
					
					calendar.add(Calendar.DATE, 1); //für nächste Iteration
				}
			}
			
			return count;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	long[] getStatsStunden() {
		
		try {
			
			long[] stats = new long[24];
			
			for(int i = 0; i < stats.length; i++) {
				
				ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[STATS_STUNDEN]+" WHERE "+DB[STATS_STUNDEN][STATS_STUNDEN_UHRZEIT][0]+"="+i,true);
				rs.next();
				stats[i] = rs.getLong(1);
				rs.close();
			}
			
			return stats;
			
		} catch (SQLException e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	void resetStatsTage() {
		try {
			sendQuery("DELETE FROM "+TABLE[STATS_TAGE] , false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void resetStatsStunden() {
		try {
			sendQuery("UPDATE "+TABLE[STATS_STUNDEN]+" SET "+DB[STATS_STUNDEN][STATS_STUNDEN_UHRZEIT][0]+"=-1" , false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void insertCloudStats(int size) {
		try {
			sendQuery("INSERT INTO "+TABLE[STATS_CLOUD]+"("+DB[STATS_CLOUD][STATS_SIZE][0]+") VALUES("+size+")" , false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//--------------- Chat ---------------------------------------------------
	
	//damit Polling weniger Leistung beansprucht
	int getNachrichtAnzahl(int nutzerID, int projektID, boolean lehrerchat, boolean updateZeitstempel) {
		try {
			ResultSet rs = null;
			if(lehrerchat)
				rs = sendQuery("SELECT count(*) FROM "+TABLE[NACHRICHT]+" WHERE "+DB[NACHRICHT][NACHRICHT_LEHRERCHAT][0]+"=true", true);
			else
				rs = sendQuery("SELECT count(*) FROM "+TABLE[NACHRICHT]+" WHERE "+DB[NACHRICHT][PROJEKT_ID][0]+"="+projektID, true);
			
			if(updateZeitstempel) {
				//Sekunden werden beim Zeitstempel ignoriert
				sendQuery("UPDATE "+TABLE[NUTZER]+" SET "+DB[NUTZER][LEHRERCHAT_DATUM][0]+"=CONCAT(LEFT(NOW(), 16), ':00') WHERE "+DB[NUTZER][ID][0]+"="+nutzerID, false);
			}
			
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			return anzahl;
		} catch(SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	void clearNachrichten(int projektID) {
		try {
			sendQuery("DELETE FROM "+TABLE[NACHRICHT]+" WHERE "+DB[NACHRICHT][PROJEKT_ID][0]+"="+projektID, false);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	void deleteNachricht(int nachrichtID) {
		try {
			sendQuery("DELETE FROM "+TABLE[NACHRICHT]+" WHERE "+DB[NACHRICHT][ID][0]+"="+nachrichtID, false);
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	//anzahlNutzerProjekt wird für den Zwischenspeicher genutzt für bessere Effizienz
	Nachricht[] getNachricht(int projektID, boolean lehrerchat, int maxAnzahlNutzer) {
		try {
			int anzahl = getNachrichtAnzahl(-1, projektID, lehrerchat, false); //hier braucht man den Zeitstempel nicht aktualisieren
			
			String suffix = lehrerchat ? DB[NACHRICHT][NACHRICHT_LEHRERCHAT][0]+"=true" : DB[NACHRICHT][PROJEKT_ID][0]+"="+projektID;
			
			ResultSet rs = sendQuery("SELECT "+DB[NACHRICHT][ID][0]+","+DB[NACHRICHT][NUTZER_ID][0]+","+DB[NACHRICHT][STRING][0]+","+DB[NACHRICHT][DATUM][0]+" FROM "+TABLE[NACHRICHT]+" WHERE "+suffix, true);
			
			Nachricht[] nachricht = new Nachricht[anzahl];
			Zwischenspeicher<String> speicher = new Zwischenspeicher<>(maxAnzahlNutzer);
			
			SimpleDateFormat formatDate = Timeformats.dateOnly();
			SimpleDateFormat formatTime = Timeformats.timeOnly();
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				
				int id = rs.getInt(1);
				
				//Nutzer
				int nutzerID = rs.getInt(2);
				String name = speicher.get(nutzerID);
				
				
				if(name == null) {
					String[] nameArray = getName(nutzerID);
					name = nameArray[0]+" "+nameArray[1];
					speicher.add(nutzerID, name);
				}
				
				String text = rs.getString(3);
				
				Timestamp stamp = rs.getTimestamp(4);
				String date = formatDate.format(stamp);
				String time = formatTime.format(stamp);
				
				nachricht[i] = new Nachricht(id, nutzerID, name, projektID, text, date, time);
			}
			
			return nachricht;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	void addNachricht(int nutzerID, int projektID, boolean lehrerchat, String text) {
		try {
			
			Object[] input = {nutzerID,text,projektID,lehrerchat};
			int[] type = {TYPE_INT,TYPE_STRING,TYPE_INT,TYPE_BOOLEAN};
			
			if(projektID == -1)
				type[2] = TYPE_INT_NULL;
			
			sendQueryPrepared("INSERT INTO "+TABLE[NACHRICHT]+"("+DB[NACHRICHT][NUTZER_ID][0]+","+
															DB[NACHRICHT][STRING][0]+","+
															DB[NACHRICHT][PROJEKT_ID][0]+","+
															DB[NACHRICHT][NACHRICHT_LEHRERCHAT][0]+") VALUES(?,?,?,?)", input, type, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	void editNachricht(int nachrichtID, String text) {
		try {
			
			Object[] input = {text,nachrichtID};
			int[] type = {TYPE_STRING,TYPE_INT};
			
			sendQueryPrepared("UPDATE "+TABLE[NACHRICHT]+" SET "+DB[NACHRICHT][STRING][0]+"=? WHERE "+DB[NACHRICHT][ID][0]+"=?", input, type, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	Nutzer[] getLehrerChat() {
		try {
			
			ResultSet rs = null;
			int anzahl = 0;
			
			rs = sendQuery("SELECT count(*) FROM "+TABLE[NUTZER]+" n, "+TABLE[ZUGANGSCODE]+" z WHERE "
									+"z."+DB[ZUGANGSCODE][ID][0]+"=n."+DB[NUTZER][CODE_ID][0]+" AND "
									+"z."+DB[ZUGANGSCODE][RANG][0]+"="+Nutzer.RANG_LEHRER,true);
			rs.next();
			anzahl = rs.getInt(1);
			rs.close();
			rs = sendQuery("SELECT "
								+"n."+DB[NUTZER][ID][0]+","
								+"n."+DB[NUTZER][VORNAME][0]+","
								+"n."+DB[NUTZER][NACHNAME][0]+","
								+"n."+DB[NUTZER][LEHRERCHAT_DATUM][0]+", "
								+"z."+DB[ZUGANGSCODE][RANG][0]
										+" FROM "+TABLE[NUTZER]+" n, "+TABLE[ZUGANGSCODE]+" z WHERE "
										+"z."+DB[ZUGANGSCODE][ID][0]+"=n."+DB[NUTZER][CODE_ID][0]+" AND "
										+"(z."+DB[ZUGANGSCODE][RANG][0]+"="+Nutzer.RANG_LEHRER+" OR "
										+"z."+DB[ZUGANGSCODE][RANG][0]+"="+Nutzer.RANG_ADMIN+")"
										+" ORDER BY n."+DB[NUTZER][LEHRERCHAT_DATUM][0]+" DESC, n."+DB[NUTZER][VORNAME][0]+" ASC",true);
			
			Nutzer[] nutzer = new Nutzer[anzahl];
			for(int i = 0; i < anzahl; i++) {
				rs.next();
				
				Timestamp time = rs.getTimestamp(4);
				
				String lastLehrerChatTime = "";
				if(time != null) {
					LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(time.getTime()), ZoneId.systemDefault());
					LocalDateTime now = LocalDateTime.now();
					
					long minutes = ChronoUnit.MINUTES.between(date, now);
					
					if(minutes < 2) {
						lastLehrerChatTime = "$";
					} else if(minutes < 60) {
						lastLehrerChatTime = minutes+"min";
					} else {
						long hours = ChronoUnit.HOURS.between(date, now);
						
						if(hours < 24) {
							lastLehrerChatTime = hours+"h";
						} else {
							long days = ChronoUnit.DAYS.between(date, now);
							lastLehrerChatTime = days+"T";
						}
					}
					
				}
				
				String vorname = rs.getString(2);
				String nachname = rs.getString(3);
				int rang = rs.getInt(5);
				
				if(rang == Nutzer.RANG_ADMIN) {
					nachname += " (Administrator)";
				}
				
				nutzer[i] = new Nutzer(rs.getInt(1), vorname, nachname, lastLehrerChatTime);
			}
			rs.close();
			
			return nutzer;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//--------------- Slider ---------------------------------------------------
	
	public ArrayList<Slide> getSlides() {
		try {
			ResultSet rs = sendQuery("SELECT * FROM "+TABLE[SLIDER]+" ORDER BY "+DB[SLIDER][SLIDER_INDEX][0], true);
			ArrayList<Slide> list = new ArrayList<>(6);
			while(rs.next()) {
				Slide slide = new Slide(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
				list.add(slide);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void addSlide(int index, String path, String title, String sub, String direction) {
		try {
			
			Object[] input = {index,path,title,sub,direction};
			int[] type = {TYPE_INT,TYPE_STRING,TYPE_STRING,TYPE_STRING,TYPE_STRING};
			
			sendQueryPrepared("INSERT INTO "+TABLE[SLIDER]+"("+DB[SLIDER][SLIDER_INDEX][0]+","+
															DB[SLIDER][SLIDER_PATH][0]+","+
															DB[SLIDER][SLIDER_TITLE][0]+","+
															DB[SLIDER][SLIDER_SUB][0]+","+
															DB[SLIDER][SLIDER_DIRECTION][0]+") VALUES(?,?,?,?,?)", input, type, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void modifySlide(int index, String title, String sub, String direction) {
		try {
			
			Object[] input = {title,sub,direction,index};
			int[] type = {TYPE_STRING,TYPE_STRING,TYPE_STRING,TYPE_INT};
			
			sendQueryPrepared("UPDATE "+TABLE[SLIDER]+" SET "+ DB[SLIDER][SLIDER_TITLE][0]+" = ?,"+
															DB[SLIDER][SLIDER_SUB][0]+" = ?,"+
															DB[SLIDER][SLIDER_DIRECTION][0]+" = ? WHERE "+DB[SLIDER][SLIDER_INDEX][0]+" = ?", input, type, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void swapSlides(Slide slide1, Slide slide2) {
		try {
			sendQuery("UPDATE "+TABLE[SLIDER]+" SET "+DB[SLIDER][SLIDER_INDEX][0]+"=999999 WHERE "+DB[SLIDER][SLIDER_INDEX][0]+"="+slide1.getIndex(), false);
			sendQuery("UPDATE "+TABLE[SLIDER]+" SET "+DB[SLIDER][SLIDER_INDEX][0]+"="+slide1.getIndex()+" WHERE "+DB[SLIDER][SLIDER_INDEX][0]+"="+slide2.getIndex(), false);
			sendQuery("UPDATE "+TABLE[SLIDER]+" SET "+DB[SLIDER][SLIDER_INDEX][0]+"="+slide2.getIndex()+" WHERE "+DB[SLIDER][SLIDER_INDEX][0]+"=999999", false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteSlide(int index) {
		try {
			
			ResultSet rs = sendQuery("SELECT "+DB[SLIDER][SLIDER_PATH][0]+" FROM "+TABLE[SLIDER]+" WHERE "+DB[SLIDER][SLIDER_INDEX][0]+"="+index, true);
			
			if(rs.next()) {
				String path = rs.getString(1);
				Files.delete(Paths.get(path));
				sendQuery("DELETE FROM "+TABLE[SLIDER]+" WHERE "+DB[SLIDER][SLIDER_INDEX][0]+"="+index, false);
			}
			
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	
	//Bei großen Datenbankanfragen eignet sich ein "Zwischenspeicher", damit z.B. ID -> Nutzer nicht so häufig abgefragt wird
	//length sollte so klein sein wie möglich, aber muss den Extremfall einbeziehen
	static private class Zwischenspeicher<T> {
		
		private int[] key;
		private T[] object;
		private int curr_index = 0;
		
		/**
		 * 
		 * @param length wie groß darf soll der Zwischenspeicher maximal sein? IndexOutOfBounds !!!!
		 */
		@SuppressWarnings("unchecked")
		public Zwischenspeicher(int length) {
			key = new int[length];
			object = (T[]) new Object[length];
		}
		
		public void add(int key, T obj) {
			if(curr_index >= this.key.length) {
				return;
			}
			this.key[curr_index] = key;
			this.object[curr_index] = obj;
			curr_index++;
		}
		
		public T get(int key) {
			for(int i = 0; i < this.key.length; i++) {
				if(this.key[i] == key) {
					return object[i];
				}
			}
			return null;
		}

	}

}
