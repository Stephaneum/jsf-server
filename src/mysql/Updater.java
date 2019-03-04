package mysql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import objects.Beitrag;
import objects.Datei;
import sitzung.ViewCounter;

public class Updater extends MySQLManager {

	// die Vererbung ist nur notwendig, weil wir auf die Variablen zugreifen wollen (ohne immer MySQLManager.xy eingeben zu müssen)
	
	private MySQLManager mysql;
	
	Updater(MySQLManager mysql) {
		super();
		this.mysql = mysql;
	}
	
	//leite die Befehle an die MySQLManager-Klasse weiter
	
	@Override
	protected ResultSet sendQuery(String befehl, boolean resultSet) throws SQLException {
		return mysql.sendQuery(befehl, resultSet);
	}
	
	@Override
	protected void sendQueryPrepared(String sql, Object[][] parameter, int[] type) throws SQLException {
		mysql.sendQueryPrepared(sql, parameter, type);
	}
	
	@Override
	protected ResultSet sendQueryPrepared(String sql, Object[] parameter, int[] type, boolean resultSet) throws SQLException {
		return mysql.sendQueryPrepared(sql, parameter, type, resultSet);
	}
	
	void update(int currVersion) throws SQLException {
		
		Konsole.antwort("Datenbank wird geupdatet!");
		ResultSet rs = null;
		switch(currVersion) {
		case 1:
			Konsole.update("1 auf 2");
			sendQuery("DELETE FROM grp",false);
			sendQuery("ALTER TABLE datei DROP FOREIGN KEY datei_ibfk_5",false); //datei_ibfk_5 = beitrag_id
			sendQuery("ALTER TABLE datei DROP COLUMN beitrag_id",false);
			sendQuery("CREATE TABLE datei_beitrag(datei_id INT, beitrag_id INT, FOREIGN KEY (datei_id) REFERENCES datei(id) ON DELETE CASCADE, FOREIGN KEY (beitrag_id) REFERENCES beitrag(id) ON DELETE CASCADE)",false);
			setKonfig(KONFIG_VERSION, String.valueOf(2));
			//1 nach 2
			//ZEILEN UMBENENNNEN: UPDATE *TABELLE* SET *SPALTE* = *WERT* WHERE ...
			//SPALTE UMBENENNNEN: ALTER TABLE `table_name` CHANGE `old_name` `new_name` VARCHAR(255) NOT NULL;
			//MIT  NEUE SPALTEN: create table *NEUE TABELLE*(*NEUE SPALTEN*) select *ALTE SPALTE MIT ALTEN NAMEN* as *ALTE SPALTE MIT NEUEM NAMEN* from *TABELLE*
			//OHNE NEUE SPALTEN: create table *NEUE TABELLE* select *ALTE SPALTE MIT ALTEN NAMEN* as *ALTE SPALTE MIT NEUEM NAMEN* from *TABELLE*
		case 2:
			Konsole.update("2 auf 3");
			sendQuery("DELETE FROM grp",false);
			sendQuery("SET foreign_key_checks = 0",false); //DROP TABLE kann nur damit funktionieren, wegen Fremdschlüssel
			sendQuery("DROP TABLE beitrag",false);
			sendQuery("SET foreign_key_checks = 1",false); //vorherige Einstellung wiederherstellen
			sendQuery("CREATE TABLE beitrag (id INT NOT NULL AUTO_INCREMENT,nutzer_id INT,untergrp_id INT,titel VARCHAR(128),text TEXT,datum DATETIME DEFAULT CURRENT_TIMESTAMP,nutzer_id_update INT,genehmigt TINYINT(1),"
					+ "PRIMARY KEY(id) ,FOREIGN KEY (nutzer_id) REFERENCES nutzer(id) ON DELETE CASCADE ,FOREIGN KEY (untergrp_id) REFERENCES untergrp(id) ON DELETE CASCADE ,FOREIGN KEY (nutzer_id_update) REFERENCES nutzer(id) ON DELETE CASCADE )ENGINE=INNODB",false);
			setKonfig(KONFIG_VERSION, String.valueOf(3));
		case 3:
			Konsole.update("3 auf 4");
			sendQuery("ALTER TABLE nutzer ADD COLUMN storage INT AFTER code_passwort_vergessen",false);
			sendQuery("UPDATE nutzer SET storage = "+VALUE_STORAGE_SCHUELER,false);
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_STORAGE_LEHRER+"',"+VALUE_STORAGE_LEHRER+")",false);
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_STORAGE_SCHUELER+"',"+VALUE_STORAGE_SCHUELER+")",false);
			setKonfig(KONFIG_VERSION, String.valueOf(4));
		case 4:
			Konsole.update("4 auf 5");
			sendQuery("ALTER TABLE untergrp ADD COLUMN link TEXT AFTER grp_id",false);
			setKonfig(KONFIG_VERSION, String.valueOf(5));
		case 5:
			Konsole.update("5 auf 6");
			sendQuery("ALTER TABLE beitrag ADD COLUMN vorschau INT AFTER genehmigt",false);
			sendQuery("ALTER TABLE beitrag ADD COLUMN show_autor TINYINT(1) AFTER vorschau",false);
			sendQuery("UPDATE beitrag SET vorschau = "+Beitrag.STANDARD_VORSCHAU_LENGTH,false);
			sendQuery("UPDATE beitrag SET show_autor = true",false);
			setKonfig(KONFIG_VERSION, String.valueOf(6));
		case 6:
			Konsole.update("6 auf 7");
			sendQuery("ALTER TABLE grp ADD grp_id INT",false);
			sendQuery("ALTER TABLE grp ADD CONSTRAINT fk_grp_id FOREIGN KEY (grp_id) REFERENCES grp(id) ON DELETE CASCADE",false);
			sendQuery("ALTER TABLE grp ADD COLUMN hatSammlung TINYINT(1)",false);
			sendQuery("ALTER TABLE datei ADD COLUMN public TINYINT(1) AFTER mime_type",false);
			sendQuery("UPDATE datei SET public = false",false);
			
			//hatSammlung aktualisieren
			rs = sendQuery("SELECT id FROM grp",true);
			while(rs.next()) {
				int id = rs.getInt(DB[GRUPPE][ID][0]);
				
				ResultSet rs2 = sendQuery("SELECT * FROM untergrp where grp_id = "+id,true);
				if(rs2.next()) {
					sendQuery("UPDATE grp SET hatSammlung = true WHERE id = "+id,false);
				} else {
					sendQuery("UPDATE grp SET hatSammlung = false WHERE id = "+id,false);
				}
				rs2.close();
			}
			rs.close();
			
			setKonfig(KONFIG_VERSION, String.valueOf(7));
		case 7:
			Konsole.update("7 auf 8");
			sendQuery("ALTER TABLE konfig MODIFY wert TEXT",false);
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_STRING_KONTAKT+"',null)",false);
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_STRING_IMPRESSUM+"',null)",false);
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_STRING_BOTTOM+"',null)",false);
			sendQuery("ALTER TABLE nutzer ADD COLUMN gesperrt TINYINT(1) AFTER storage",false);
			repairFileNames();
			setKonfig(KONFIG_VERSION, String.valueOf(8));
		case 8:
			Konsole.update("8 auf 9");
			
			sendQuery("CREATE TABLE log(datum DATETIME DEFAULT CURRENT_TIMESTAMP, typ INT, ereignis TEXT)",false);
			sendQuery("CREATE TABLE stats(uhrzeit INT, anzahl BIGINT)",false);
			
			for(int i = 0; i < 24*2; i++) {
				sendQuery("INSERT INTO stats VALUES("+i+",0)",false);
			}
			
			validateDeleteZugangscodes();
			setKonfig(KONFIG_VERSION, String.valueOf(9));
		case 9:
			Konsole.update("9 auf 10");
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_STRING_ENTWICKLER+"',null)",false);
			setKonfig(KONFIG_VERSION, String.valueOf(10));
		case 10:
			Konsole.update("10 auf 11");
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_STRING_TICKER+"',null)",false);
			sendQuery("DELETE FROM stats WHERE uhrzeit >= 24",false);
			
			for(int i = 24; i < 24+30; i++) {
				sendQuery("INSERT INTO stats VALUES("+i+",0)",false);
			}
			setKonfig(KONFIG_VERSION, String.valueOf(11));
		case 11:
			Konsole.update("11 auf 12");
			
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_STRING_VERTRETUNG+"',null)",false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(12));
		case 12:
			Konsole.update("12 auf 13");
			
			sendQuery("CREATE TABLE ordner (id INT NOT NULL AUTO_INCREMENT,name TEXT,eigentum INT,parent INT,PRIMARY KEY(id) ,FOREIGN KEY (eigentum) REFERENCES nutzer(id) ON DELETE CASCADE ,FOREIGN KEY (parent) REFERENCES ordner(id) ON DELETE CASCADE )ENGINE=INNODB",false);
			
			sendQuery("ALTER TABLE datei ADD ordner_id INT",false);
			sendQuery("ALTER TABLE datei ADD CONSTRAINT fk_ordner_id FOREIGN KEY (ordner_id) REFERENCES ordner(id) ON DELETE SET NULL",false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(13));
		case 13:
			Konsole.update("13 auf 14");
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_MERGE_AKTUELLES+"',"+VALUE_MERGE_AKTUELLES+")",false);
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_PICTURE_SIZE+"',"+VALUE_PICTURE_SIZE+")",false);
			setKonfig(KONFIG_VERSION, String.valueOf(14));
		case 14:
			Konsole.update("14 auf 15");
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_SHOW_SYSTEM+"',"+VALUE_SHOW_SYSTEM+")",false);
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_SHOW_SOFTWARE+"',"+VALUE_SHOW_SOFTWARE+")",false);
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_STRING_HISTORY+"',null)",false);
			setKonfig(KONFIG_VERSION, String.valueOf(15));
		case 15:
			Konsole.update("15 auf 16");
			
			sendQuery("ALTER TABLE nutzer ADD COLUMN beitrag_manager TINYINT(1) AFTER gesperrt",false);
			sendQuery("ALTER TABLE nutzer ADD COLUMN projekt_erstellen TINYINT(1) AFTER beitrag_manager",false);
			sendQuery("UPDATE nutzer SET projekt_erstellen = true",false);
			
			sendQuery("ALTER TABLE beitrag ADD COLUMN passwort CHAR(32) AFTER nutzer_id_update",false);
			setKonfig(KONFIG_VERSION, String.valueOf(16));
		case 16:
			Konsole.update("16 auf 17");
			
			sendQuery("ALTER TABLE grp ADD COLUMN priory INT",false);
			sendQuery("ALTER TABLE untergrp ADD COLUMN priory INT",false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(17));
		case 17:
			Konsole.update("17 auf 18");
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_REDIRECT_PORT+"',"+VALUE_REDIRECT_PORT+")",false);
			setKonfig(KONFIG_VERSION, String.valueOf(18));
		case 18:
			Konsole.update("18 auf 19");
			sendQuery("CREATE TABLE home_images (id INT NOT NULL AUTO_INCREMENT, datei_id INT,PRIMARY KEY(id) ,FOREIGN KEY (datei_id) REFERENCES datei(id) ON DELETE CASCADE)ENGINE=INNODB",false);
			setKonfig(KONFIG_VERSION, String.valueOf(19));
		case 19:
			//unnötige Tabellen entfernen
			Konsole.update("19 auf 20");
			sendQuery("SET foreign_key_checks = 0",false); //DROP TABLE kann nur damit funktionieren, wegen Fremdschlüssel
			
			sendQuery("DROP TABLE IF EXISTS home_images",false);
			sendQuery("DROP TABLE IF EXISTS note",false);
			sendQuery("DROP TABLE IF EXISTS schulstunde",false);
			sendQuery("DROP TABLE IF EXISTS fach",false);
			sendQuery("DROP TABLE IF EXISTS kurs",false);
			sendQuery("DROP TABLE IF EXISTS nutzer_kurs",false);
			
			sendQuery("ALTER TABLE datei DROP FOREIGN KEY datei_ibfk_4",false); //datei_ibfk_4 = kurs_id
			sendQuery("ALTER TABLE datei DROP COLUMN kurs_id",false);
			sendQuery("ALTER TABLE datei CHANGE pfad pfad varchar(1024) AFTER nutzer_id",false); //pfad nach nutzer_id verschieben
			
			sendQuery("SET foreign_key_checks = 1",false); //vorherige Einstellung wiederherstellen
			setKonfig(KONFIG_VERSION, String.valueOf(20));
		case 20:
			Konsole.update("20 auf 21");
			
			sendQuery("ALTER TABLE ordner ADD COLUMN projekt_id INT AFTER eigentum",false);
			sendQuery("ALTER TABLE ordner ADD CONSTRAINT fk_projekt_id FOREIGN KEY (projekt_id) REFERENCES projekt(id) ON DELETE CASCADE",false);
			
			sendQuery("ALTER TABLE ordner ADD COLUMN klasse_id INT AFTER projekt_id",false);
			sendQuery("ALTER TABLE ordner ADD CONSTRAINT fk_klasse_id FOREIGN KEY (klasse_id) REFERENCES klasse(id) ON DELETE CASCADE",false);
			
			sendQuery("ALTER TABLE projekt ADD COLUMN homepage TINYINT(1) DEFAULT 0",false);
			sendQuery("UPDATE projekt SET homepage=false",false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(21));
		case 21:
			Konsole.update("21 auf 22");
			
			sendQuery("ALTER TABLE projekt MODIFY COLUMN homepage TINYINT(1) DEFAULT 1",false);
			sendQuery("UPDATE projekt SET homepage=true",false);
			
			sendQuery("ALTER TABLE beitrag ADD COLUMN layout_beitrag INT DEFAULT 0",false);
			sendQuery("ALTER TABLE beitrag ADD COLUMN layout_vorschau INT DEFAULT 0",false);
			sendQuery("UPDATE beitrag SET layout_beitrag=0",false);
			sendQuery("UPDATE beitrag SET layout_vorschau=0",false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(22));
		case 22:
			Konsole.update("22 auf 23");
			
			sendQuery("SET foreign_key_checks = 0",false); //DROP TABLE kann nur damit funktionieren, wegen Fremdschlüssel
			
			//Spalten von grp modifizieren
			
			sendQuery("ALTER TABLE grp DROP COLUMN hatSammlung",false);
			
			sendQuery("ALTER TABLE grp ADD COLUMN link TEXT",false);
			sendQuery("ALTER TABLE grp ADD COLUMN rubrik_leiter INT",false);
			sendQuery("ALTER TABLE grp ADD CONSTRAINT fk_grp_nutzer FOREIGN KEY (rubrik_leiter) REFERENCES nutzer(id) ON DELETE SET NULL",false);
			sendQuery("ALTER TABLE grp ADD COLUMN datei_id INT",false);
			sendQuery("ALTER TABLE grp ADD CONSTRAINT fk_grp_datei FOREIGN KEY (datei_id) REFERENCES datei(id) ON DELETE SET NULL",false);
			sendQuery("ALTER TABLE grp ADD COLUMN passwort CHAR(32)",false);
			sendQuery("ALTER TABLE grp ADD COLUMN genehmigt TINYINT(1)",false);
			
			//Spalten von beitrag modifizieren
			sendQuery("ALTER TABLE beitrag ADD COLUMN grp_id INT AFTER untergrp_id",false);
			sendQuery("ALTER TABLE beitrag ADD CONSTRAINT fk_beitrag_grp FOREIGN KEY (grp_id) REFERENCES grp(id) ON DELETE CASCADE",false);
			
			rs = sendQuery("SELECT * FROM untergrp",true);
			
			while(rs.next()) {
				int oldID = rs.getInt(1);
				String name = rs.getString(2);
				int grpID = rs.getInt(3);
				String link = rs.getString(4);
				int priory = rs.getInt(5);
				
				if(link != null)
					link = "'"+link+"'";
				
				//in grp einfügen
				sendQuery("INSERT INTO grp(name,grp_id,link,priory) VALUES('"+name+"',"+grpID+","+link+","+priory+")",false);
				
				ResultSet rs2 = sendQuery("SELECT LAST_INSERT_ID();",true);
				rs2.next();
				
				int id = rs2.getInt(1);
				rs2.close();
				
				//in beitrag aktualisieren
				sendQuery("UPDATE beitrag SET grp_id = "+id+" WHERE untergrp_id = "+oldID,false);
			}
			rs.close();
			
			//Beitrag: untergrp_id löschen
			//constraint herausfinden
			rs = sendQuery("SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE"
					+ " WHERE TABLE_NAME = 'beitrag' AND COLUMN_NAME = 'untergrp_id' AND REFERENCED_TABLE_NAME = 'untergrp' AND REFERENCED_COLUMN_NAME = 'id'",true);
			rs.next();
			String constraint = rs.getString(1);
			rs.close();
			
			sendQuery("ALTER TABLE beitrag DROP FOREIGN KEY "+constraint,false);
			sendQuery("ALTER TABLE beitrag DROP COLUMN untergrp_id",false);
			
			sendQuery("DROP TABLE IF EXISTS untergrp",false); //untergrp löschen
			sendQuery("RENAME TABLE grp TO gruppe",false); //grp in gruppe umbenennen
			
			sendQuery("SET foreign_key_checks = 1",false); //vorherige Einstellung wiederherstellen
			
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_DEFAULT_GRUPPE_ID+"',"+VALUE_DEFAULT_GRUPPE_ID+")",false); //neuer Konfig-Eintrag
			sendQuery("ALTER TABLE nutzer ADD COLUMN rubrik_erstellen TINYINT(1)",false); //neue Spalte in nutzer
			
			setKonfig(KONFIG_VERSION, String.valueOf(23));
		case 23:
			Konsole.update("23 auf 24");
			
			sendQuery("CREATE TABLE nachricht (id INT NOT NULL AUTO_INCREMENT,nutzer_id INT,string TEXT,projekt_id INT,nutzer_empfang_id INT,datum DATETIME DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(id) ,FOREIGN KEY (nutzer_id) REFERENCES nutzer(id) ON DELETE CASCADE ,FOREIGN KEY (projekt_id) REFERENCES projekt(id) ON DELETE CASCADE ,FOREIGN KEY (nutzer_empfang_id) REFERENCES nutzer(id) ON DELETE CASCADE )ENGINE=INNODB",false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(24));
		case 24:
			Konsole.update("24 auf 25");
			
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_STRING_VERTRETUNG_INFO+"',null)",false);
			sendQuery("UPDATE konfig SET wert = null WHERE variable = 'str_vertretung'",false); //Vertretungsplan resetten
			sendQuery("ALTER TABLE nutzer ADD COLUMN vertretungsplan TINYINT(1)",false); //neue Spalte in nutzer
			sendQuery("ALTER TABLE projekt CHANGE homepage chat TINYINT(1)", false); //Projekt: homepage in chat umbenennen
			
			setKonfig(KONFIG_VERSION, String.valueOf(25));
		case 25:
			Konsole.update("25 auf 26");
			
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_SHOW_CLOUD+"',"+VALUE_SHOW_CLOUD+")",false);
			sendQuery("CREATE TABLE stats_cloud(datum DATETIME DEFAULT CURRENT_TIMESTAMP, size INT)",false);
			
			//sync
			sendQuery("INSERT INTO stats_cloud(datum,size) SELECT datum, size FROM datei",false);
			
			//Bei Version 24 wurde ausversehen DEFAULT 1 vergessen
			sendQuery("ALTER TABLE projekt MODIFY COLUMN chat TINYINT(1) DEFAULT 1",false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(26));
		case 26:
			Konsole.update("26 auf 27");
			
			rs = sendQuery("SELECT SUM(anzahl) FROM stats WHERE uhrzeit < 24",true);
			rs.next();
			long totalViews = rs.getLong(1);
			rs.close();
			
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_TOTAL_VIEWS+"',"+totalViews+")",false);
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_STATS_STUNDEN_INDEX+"',"+VALUE_STATS_STUNDEN_INDEX+")",false);
			
			sendQuery("CREATE TABLE projekt_gruppe (projekt_id INT,gruppe_id INT,FOREIGN KEY (projekt_id) REFERENCES projekt(id) ON DELETE CASCADE ,FOREIGN KEY (gruppe_id) REFERENCES gruppe(id) ON DELETE CASCADE )ENGINE=INNODB",false);
			sendQuery("CREATE TABLE stats_tage (id INT NOT NULL AUTO_INCREMENT,datum DATETIME DEFAULT CURRENT_TIMESTAMP,anzahl INT,PRIMARY KEY(id) )ENGINE=INNODB",false);
			sendQuery("CREATE TABLE stats_stunden (idx INT,uhrzeit TINYINT,PRIMARY KEY(idx) )ENGINE=INNODB",false);
			
			convertStats(totalViews); //Tage- und Stundenstatistiken übertragen
			
			//Schüler das Recht "Projekt erstellen" aberkennen
			sendQuery("UPDATE nutzer n, zugangscode z SET n.projekt_erstellen = false WHERE n.code_id = z.id AND z.rang = 0", false);
			
			//Tabelle stats löschen
			sendQuery("DROP TABLE stats",false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(27));
		case 27:
			Konsole.update("27 auf 28");
			
			sendQuery("ALTER TABLE nutzer_projekt ADD COLUMN betreuer TINYINT(1)",false);
			sendQuery("UPDATE nutzer_projekt SET betreuer = false", false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(28));
		case 28:
			Konsole.update("28 auf 29");
			
			sendQuery("ALTER TABLE nutzer_projekt ADD COLUMN akzeptiert TINYINT(1)",false);
			sendQuery("UPDATE nutzer_projekt SET akzeptiert = true", false);
			
			sendQuery("ALTER TABLE projekt ADD COLUMN akzeptiert TINYINT(1) AFTER leiter_nutzer_id",false);
			sendQuery("UPDATE projekt SET akzeptiert = true", false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(29));
		case 29:
			Konsole.update("29 auf 30");
			sendQuery("INSERT INTO konfig VALUES('"+KONFIG_STRING_EU_SA+"',null)",false);
			setKonfig(KONFIG_VERSION, String.valueOf(30));
		case 30:
			Konsole.update("30 auf 31");
			
			//Nachricht.nutzer_empfang_id
			sendQuery("ALTER TABLE nachricht DROP FOREIGN KEY nachricht_ibfk_3",false); //nachricht_ibfk_3 = nutzer_empfang_id
			sendQuery("ALTER TABLE nachricht DROP COLUMN nutzer_empfang_id",false);
			
			//Nachricht.lehrerchat
			sendQuery("ALTER TABLE nachricht ADD COLUMN lehrerchat TINYINT(1) AFTER projekt_id",false);
			sendQuery("UPDATE nachricht SET lehrerchat = false", false);
			
			//Datei.lehrerchat
			sendQuery("ALTER TABLE datei ADD COLUMN lehrerchat TINYINT(1) AFTER public",false);
			sendQuery("UPDATE datei SET lehrerchat = false", false);
			
			//Ordner.lehrerchat
			sendQuery("ALTER TABLE ordner ADD COLUMN lehrerchat TINYINT(1) AFTER klasse_id",false);
			sendQuery("UPDATE ordner SET lehrerchat = false", false);
			
			//Nutzer.lehrerchat_datum
			sendQuery("ALTER TABLE nutzer ADD COLUMN lehrerchat_datum DATETIME",false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(31));
		case 31:
			Konsole.update("31 auf 32");
			
			sendQuery("ALTER TABLE projekt ADD COLUMN lehrerchat TINYINT(1) DEFAULT 0",false);
			sendQuery("UPDATE projekt SET lehrerchat = false", false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(32));
		case 32:
			Konsole.update("32 auf 33");
			
			sendQuery("INSERT INTO konfig VALUES('backup_dir','/opt/backup')",false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(33));
		case 33:
			Konsole.update("33 auf 34");
			
			sendQuery("INSERT INTO konfig VALUES('str_termine','beitrag.xhtml?id=266')",false);
			sendQuery("INSERT INTO konfig VALUES('str_koop','Dänemark;Finnland;Frankreich;Italien;Niederlande;Norwegen;Polen')",false);
			sendQuery("INSERT INTO konfig VALUES('str_koop_url','http://www.czechbc.de/contao/index.php/kooperationspartner.html')",false);
			sendQuery("CREATE TABLE slider (idx INT,path VARCHAR(1024),title VARCHAR(256),sub VARCHAR(256),direction VARCHAR(64))ENGINE=INNODB",false);
			
			setKonfig(KONFIG_VERSION, String.valueOf(34));
		}
	}
	
	//update 7 --> 8
	//manche Dateien haben die falsche ID im Namen
	private void repairFileNames() {
		
		try {
			ResultSet rs = sendQuery("SELECT count(*) FROM "+TABLE[DATEI],true);
			rs.next();
			int anzahl = rs.getInt(1);
			rs.close();
			
			rs = sendQuery("SELECT * FROM "+TABLE[DATEI],true);
			
			Datei[] datei = resultSetToDatei(rs, anzahl);
			
			String mainPfad = Datenbank.getSpeicherort();
			
			for(int i = 0; i < datei.length; i++) {
				
				int id = datei[i].getDatei_id();
				String pfad = datei[i].getPfad();
				String name = datei[i].getDatei_name();
				
				Path source = Paths.get(pfad);
				
				try {
					Files.move(source, source.resolveSibling(id+"_"+name));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				sendQuery("UPDATE "+TABLE[DATEI]+" SET "+DB[DATEI][PFAD][0]+" = \""+mainPfad+"/"+id+"_"+name+"\" WHERE "+DB[DATEI][ID][0]+"="+id,false);
			}
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	//update 8 --> 9
	//Wenn Dummy-Nutzer erstellt werden, werden manche Nutzer gelöscht (duplikate E-Mail), die Zugangscodes aber nicht
	private void validateDeleteZugangscodes() {
		try {
			ResultSet rs = sendQuery("SELECT "+DB[ZUGANGSCODE][ID][0]+" FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][BENUTZT][0]+"=true",true);
			
			List<Integer> codeIds = new ArrayList<Integer>(100);
			
			while(rs.next()) {
				codeIds.add(rs.getInt(1));
			}
			
			rs.close();
			
			for(int i = 0; i < codeIds.size(); i++) {
				//existiert Nutzer mit diesem codeID ?
				
				rs = sendQuery("SELECT * FROM "+TABLE[NUTZER]+" WHERE "+DB[NUTZER][CODE_ID][0]+"="+codeIds.get(i),true);
				
				if(!rs.next()) {
					//nutzer existiert nicht
					sendQuery("DELETE FROM "+TABLE[ZUGANGSCODE]+" WHERE "+DB[ZUGANGSCODE][ID][0]+"="+codeIds.get(i),false);
				}
				
				rs.close();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//update 26 --> 27
	private void convertStats(long totalViews) throws SQLException {
		
		//Tage synchronisieren
		ResultSet rs = sendQuery("SELECT anzahl FROM stats WHERE uhrzeit >= 24", true); //24-53
		int[] viewsDay = new int[30];
		for(int i = 0; i < viewsDay.length; i++) {
			rs.next();
			viewsDay[i] = rs.getInt(1);
		}
		rs.close();
		
		//String-Array
		String[] dateString = new String[viewsDay.length];
		
		int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		int month = Calendar.getInstance().get(Calendar.MONTH);
		int year = Calendar.getInstance().get(Calendar.YEAR);
		
		for(int i = 0; i < viewsDay.length; i++) {
			String sMonth = month >= 9 ? String.valueOf(month+1) : "0"+String.valueOf(month+1); //API zählt Monate ab 0
			String sDay = day >= 10 ? String.valueOf(day) : "0"+String.valueOf(day);
			dateString[i] = year+"-"+sMonth+"-"+sDay;
			
			day--;
			if(day == 0) {
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.MONTH, month);
				calendar.set(Calendar.DAY_OF_MONTH, 1);  
				calendar.add(Calendar.DATE, -1);
				day = calendar.get(Calendar.DAY_OF_MONTH); //letzter Tag des vorherigen Monats
				month--;
				if(month == -1) {
					month = 11;
					year--;
				}
			}
		}
		
		//Ergebnisse für ein PreparedStatement in ein Object[][] verpacken
		Object[][] parameter = new Object[viewsDay.length][2];
		int[] type = {TYPE_STRING, TYPE_INT};
		
		for(int i = 0; i < viewsDay.length; i++) {
			parameter[i][0] = dateString[viewsDay.length-1-i]; //umkehreren: von alt nach neu
			parameter[i][1] = viewsDay[viewsDay.length-1-i]; //umkehreren: von alt nach neu
		}
			
		sendQueryPrepared("INSERT INTO stats_tage(datum,anzahl) VALUES(?,?)",parameter, type);
		
		//Stunden synchronisieren
		rs = sendQuery("SELECT anzahl FROM stats WHERE uhrzeit < 24", true); //0-23
		long[] viewsHour = new long[24];
		for(int i = 0; i < viewsHour.length; i++) {
			rs.next();
			viewsHour[i] = rs.getLong(1);
		}
		rs.close();
		
		byte[] viewsHourFinal = new byte[ViewCounter.COUNTER_HOUR_AMOUNT];
		
		if(totalViews > ViewCounter.COUNTER_HOUR_AMOUNT) {
			//mit Downsampling, die Anzahl ist stets < ViewCounter.COUNTER_HOUR_AMOUNT, wegen Ganzzahl-Abrundung
			for(int i = 0; i < 24; i++) {
				viewsHour[i] = (long) ((viewsHour[i] / (double) totalViews) * ViewCounter.COUNTER_HOUR_AMOUNT);
			}
		}
		
		int currentIndex = 0;
		for(int i = 0; i < viewsHour.length; i++) {
			int views = (int) viewsHour[i]; //Aufrufe einer Stunde [0...23]
			for(int x = 0; x < views; x++) {
				viewsHourFinal[currentIndex] = (byte) i;
				currentIndex++;
			}
		}
		
		//ggf. Auffüllen
		byte fill = (byte) (totalViews <= ViewCounter.COUNTER_HOUR_AMOUNT ? -1 : 23);
		for(int i = currentIndex; i < viewsHourFinal.length; i++) {
			viewsHourFinal[i] = fill;
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("INSERT INTO "+TABLE[STATS_STUNDEN]+" VALUES (");
		builder.append(0);
		builder.append(',');
		builder.append(viewsHourFinal[0]);
		builder.append(')');
		
		for(int i = 1; i < ViewCounter.COUNTER_HOUR_AMOUNT; i++) {
			builder.append(", (");
			builder.append(i);
			builder.append(',');
			builder.append(viewsHourFinal[i]);
			builder.append(')');
		}
		
		sendQuery(builder.toString(),false);
	}

}
