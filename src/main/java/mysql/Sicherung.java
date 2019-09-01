package mysql;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import org.apache.commons.io.FileUtils;

import managedBean.Konfig;
import managedBean.MySQLConnection;
import managedBean.Statistiken;
import managedBean.TopMenu;
import sitzung.ViewCounter;
import tools.Countdown;
import tools.FileTools;
import tools.Timeformats;
import tools.Zipper;

public class Sicherung {
	
	//Timer-Konstanten
	final static private int BACKUP_HOUR = 4, BACKUP_MINUTE = 30;
	final static private DayOfWeek BACKUP_DAY = DayOfWeek.SATURDAY;
	final static private int MIN_BACKUP_INTERVAL = 30*60*1000; //30min
	final static private int CHECK_INTERVAL = 60*1000; //1min
	
	private static boolean timerRunning = false;
	private static boolean backupRunning = false;
	private static Boolean result = null; // null...noch kein Ergebnis, false...Error, true...Success
	private static StringBuilder log = new StringBuilder();
	
	public static Boolean getResult() {
		return result;
	}
	
	public static String getLog() {
		return log.toString();
	}
	
	public static void startBackupTimer() {

		if(true) {
			Konsole.backup("Backup-Timer ist deaktiviert, wird demnächst aus dem JSF Projekt entfernt.");
			return;
		}

		if(timerRunning)
			return;
		
		timerRunning = true;
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Konsole.backup("Backup-Timer gestartet.");
				
				long lastBackup = 0;
				while(true) {
					LocalDateTime now = LocalDateTime.now();
					if(now.getDayOfWeek().equals(BACKUP_DAY) &&
							now.getHour() == BACKUP_HOUR &&
							(now.getMinute() == BACKUP_MINUTE || now.getMinute() == BACKUP_MINUTE+1) &&
							System.currentTimeMillis() - lastBackup >= MIN_BACKUP_INTERVAL) {
						
						
						startBackup(true);
						lastBackup = System.currentTimeMillis();
					}
					
					Countdown.sleep(CHECK_INTERVAL);
				}
			}
		}).start();
	}
	
	public static String getNextBackup() {
		LocalDateTime dateTime = LocalDateTime.now();
		
		boolean sameDay = false;
		if(dateTime.getHour() < BACKUP_HOUR)
			sameDay = true;
		else if(dateTime.getHour() == BACKUP_HOUR && dateTime.getMinute() < BACKUP_MINUTE)
			sameDay = true;
		
		LocalDateTime nextMonday;
		if(sameDay)
			nextMonday = dateTime.with(TemporalAdjusters.nextOrSame(BACKUP_DAY));
		else
			nextMonday = dateTime.with(TemporalAdjusters.next(BACKUP_DAY));
	    
		return Timeformats.dateFull.format(nextMonday)+", um "+BACKUP_HOUR+":"+BACKUP_MINUTE+" Uhr";
	}
	
	public static void startBackup(final boolean fromTimer) {
		if(backupRunning)
			return;
		
		backupRunning = true;
		result = null;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				if(fromTimer)
					Konsole.backup("Automatisches Backup wird durchgeführt.");
				else
					Konsole.backup("Manuelles Backup wird durchgeführt.");
				
				log.setLength(0);
				log.append("Backup wird gestartet.\n");
				result = backup(); // eigentliches Backup
				if(result) {
					log.append("Backup ist fertig! Sie werden in Kürze weitergeleitet.\n");
					Konsole.backup("Backup erfolgreich erstellt.");
				} else {
					log.append("Backup fehlgeschlagen.");
					Konsole.backup("Backup fehlgeschlagen.");
				}
				
				backupRunning = false;
			}
		}).start();
	}
	
	private static boolean backup() {
		
		Countdown.sleep(2000);
		
		final String tempPath = Datenbank.getBackupDir()+"/tmp";
		
		// Ordner backup/tmp erstellen
		log.append("[1/5] Temporäre Ordner werden erstellt.\n");
		File file = new File(tempPath+"/dateien");
        if (!file.exists())
            if (!file.mkdirs())
            	return false;
        
        Countdown.sleep(2000);
        
        log.append("[2/5] Datenbank wird gesichert.\n");
        String windowsMySQLPath = null;
        String dumpPath = tempPath+"/datenbank.sql";
        if(Konfig.isWindowsOS()) {
        	windowsMySQLPath = Datenbank.getDatabaseLocation();
        	dumpPath = dumpPath.replace("/", "\\");
        }
        String result = backupDump(windowsMySQLPath,MySQLConnection.USER, MySQLConnection.PASSWORD, MySQLConnection.DATABASE, dumpPath);
        if(result != null) {
        	log.append(result+"\n");
        	return false;
        }
        
        Countdown.sleep(2000);
        
        log.append("[3/5] Dateien werden kopiert.\n");
        try {
			FileUtils.copyDirectory(new File(Datenbank.getSpeicherort()), file);
		} catch (IOException e) {
			e.printStackTrace();
			log.append(e.getMessage()+"\n");
			return false;
		}
        
        Countdown.sleep(2000);
        
        log.append("[4/5] Zip-Archiv wird erstellt.\n");
        
        String path = Datenbank.getBackupDir();
		String date = LocalDateTime.now().format(Timeformats.backup);
        String destination = path + "/backup_" + date + ".zip";
        
        try {
			Zipper.zip(tempPath, destination);
		} catch (IOException e) {
			e.printStackTrace();
			log.append(e.getMessage()+"\n");
			return false;
		}
        
        log.append("[5/5] Temporäre Dateien werden gelöscht.\n");
        FileTools.deleteFolder(new File(tempPath), true);
        
        Countdown.sleep(2000);
        return true;
	}
	
	public static void startRestore(final String path, final String backupDir, final String speicherort) {
		if(backupRunning)
			return;
		
		backupRunning = true;
		result = null;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				Konsole.backup("Wiederherstellung wird durchgeführt.");
				
				log.setLength(0);
				log.append("Wiederherstellung wird gestartet.\n");
				result = restore(path, backupDir, speicherort); // eigentliche Wiederherstellung
				if(result) {
					log.append("Wiederherstellung ist fertig! Sie werden in Kürze weitergeleitet.\n");
					Konsole.backup("Wiederherstellung erfolgreich.");
				} else {
					log.append("Wiederherstellung fehlgeschlagen.");
					Konsole.backup("Wiederherstellung fehlgeschlagen.");
				}
				
				backupRunning = false;
			}
		}).start();
	}
	
	private static boolean restore(String zipFile, String backupDir, String speicherort) {
		
		Countdown.sleep(2000);
		
		final String tempPath = backupDir+"/tmp";
		
		// Ordner backup/tmp erstellen
		log.append("[1/7] Temporäre Ordner werden erstellt.\n");
		File file = new File(tempPath+"/dateien");
        if (!file.exists())
            if (!file.mkdirs())
            	return false;
        
        Countdown.sleep(1000);
        
        log.append("[2/7] Zip-Archiv wird extrahiert.\n");
        
        try {
			Zipper.unzip(zipFile, tempPath);
		} catch (IOException e) {
			e.printStackTrace();
			log.append(e.getMessage()+"\n");
			return false;
		}
        
        Countdown.sleep(1000);
        
        log.append("[3/7] Alter Speicher wird gelöscht.\n");
        FileTools.deleteFolder(new File(speicherort), false);
        
        Countdown.sleep(1000);
        
        log.append("[4/7] Neue Dateien werden kopiert.\n");
        try {
			FileUtils.copyDirectory(file, new File(speicherort));
		} catch (IOException e) {
			e.printStackTrace();
			log.append(e.getMessage()+"\n");
			return false;
		}
        
        Countdown.sleep(1000);
        
        log.append("[5/7] Datenbank wird geladen und ggf. aktualisiert.\n");
        String windowsMySQLPath = null;
        String dumpPath = tempPath+"/datenbank.sql";
        if(Konfig.isWindowsOS()) {
        	windowsMySQLPath = Datenbank.getDatabaseLocation();
        	dumpPath = dumpPath.replace("/", "\\");
        }
        
        String result = restoreDump(windowsMySQLPath, MySQLConnection.USER, MySQLConnection.PASSWORD, MySQLConnection.DATABASE, dumpPath);
        if(result != null) {
        	log.append(result+"\n");
        	return false;
        }
        
        Countdown.sleep(1000);
        
        log.append("[6/7] Speicherort und Backup-Ordner werden gesetzt.\n");
        log.append("        -> Speicherort = "+Datenbank.getSpeicherort()+"\n");
        log.append("                -> Cloud\n");
        log.append("                -> Vertretungsplan\n");
        log.append("                -> Diashow\n");
        Datenbank.setSpeicherort(speicherort);
        
        log.append("        -> Backup-Ordner = "+Datenbank.getBackupDir()+"\n");
        Datenbank.setBackupDir(backupDir);
        
        Countdown.sleep(1000);
        
        log.append("[7/7] Temporäre Dateien werden gelöscht.\n");
        FileTools.deleteFolder(new File(tempPath), true);
        
        Countdown.sleep(1000);
        return true;
	}
	
	private static String backupDump(String windowsMySQLPath, String username, String password, String database, String destination) {
		
	    try {
	        File file = new File(destination);
	        file.getParentFile().mkdirs();
	        
	        String[] cmd;
	        if (windowsMySQLPath != null) {
	        	// on windows, dump file path cannot have spaces
	        	cmd = new String[]{"cmd.exe","/c", "\""+windowsMySQLPath+"bin\\mysqldump.exe\" -u\""+username+"\" -p\""+password+"\" \""+database+"\" > "+destination};
	        } else {
	        	cmd = new String[]{"/bin/bash","-c","mysqldump -u\""+username+"\" -p\""+password+"\" \""+database+"\" > \""+destination+"\""};
	        }
	        
	        Process runtimeProcess = Runtime.getRuntime().exec(cmd);
	        int processComplete = runtimeProcess.waitFor();

	        if (processComplete == 0) {
	        	return null;
	        } else {
	        	return "Error-Code: "+processComplete;
	        }

	    } catch (IOException | InterruptedException ex) {
	        return ex.getMessage();
	    }
	}
	
	private static String restoreDump(String windowsMySQLPath, String username, String password, String database, String source) {
		
        try {
        	
        	Datenbank.createEmptyDatabase(); //Datenbank leeren (ggf. leeres Datenbank erstellen)
        	
        	String[] cmd;
	        if (windowsMySQLPath != null) {
	        	// on windows, dump file path cannot have spaces
	        	cmd = new String[]{"cmd.exe","/c", "\""+windowsMySQLPath+"bin\\mysql.exe\" -u\""+username+"\" -p\""+password+"\" \""+database+"\" < "+source};
	        } else {
	        	cmd = new String[]{"/bin/bash","-c","mysql -u\""+username+"\" -p\""+password+"\" \""+database+"\" < \""+source+"\""};
	        }
            Process runtimeProcess = Runtime.getRuntime().exec(cmd);
            int processComplete = runtimeProcess.waitFor();

            if (processComplete == 0) {
            	
            	Datenbank.updateDatabase(); //enthält Datenbank.syncVariables
            	ViewCounter.forceSync();
            	Statistiken.initData();
            	TopMenu.triggerChanged();
            	return null;
            } else {
            	return "Error-Code: "+processComplete;
            }

        } catch (IOException | InterruptedException ex) {
        	return ex.getMessage();
        }
    }

}
