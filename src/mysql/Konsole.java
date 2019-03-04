package mysql;

import java.time.LocalDateTime;

import tools.Timeformats;

public class Konsole {
	
	final static boolean SHOW_SQL = false,
						 SHOW_INFO = false,
						 SHOW_ERROR = true,
						 SHOW_UPDATE = true,
						 SHOW_BACKUP = true;
	
	static void sql(String sql) {
		if(SHOW_SQL)
			System.out.println("mysql:      [SQL] "+sql);
	}
	
	static void update(String sql) {
		if(SHOW_UPDATE)
			System.out.println("mysql:      [UPDATE] "+sql);
	}
	
	static void backup(String info) {
		if(SHOW_BACKUP) {
			String date = Timeformats.complete.format(LocalDateTime.now());
			System.out.println("backup: ["+date+"] "+info);
		}
	}
	
	static void warn(String warnung) {
		if(SHOW_ERROR)
			System.err.println("mysql:      [WARNUNG] "+warnung);
	}
	
	static void method(String methode) {
		if(SHOW_INFO)
			System.out.println("mysql:   [METHODE] "+methode);
	}
	
	static void antwort(String antwort) {
		if(SHOW_INFO)
			System.out.println("mysql:      [ANTWORT] "+antwort);
	}
	
	static void noConnection() {
		if(SHOW_ERROR)
			System.err.println("mysql:      [WARNUNG] Noch keine Verbindung zur MySQL-Datenbank!");
	}
	
	static void noSession() {
		if(SHOW_ERROR)
			System.err.println("mysql:      [WARNUNG] Der Client besitzt noch kein Objekt in der aktuellen Sitzung!");
	}

}
