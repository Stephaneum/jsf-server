package sitzung;

import java.io.Serializable;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import mysql.Datenbank;
import tools.Timeformats;

public class ViewCounter implements Serializable{
	private static final long serialVersionUID = 2951761713611371255L;
	
	public static final int COUNTER_HOUR_AMOUNT = 20000;
	public static final int COUNTER_DAY_INTERVAL = 30;
	
	static private boolean synchronizedWithMySQL = false;
	static private long counter; //Gesamtaufrufzahl
	static private String counterReadable;
	
	static private String lastIP;
	
	//Stats: Tage
	static private long counter30;
	static private String counter30Readable;
	static private Count[] counterDay;
	static private int dayID;
	
	//Stats: Stunden
	static private long[] counterHour;
	static private int hourIndex;
	
	//userAgent-Detection
	static private int lastDayInMonth;
	static private long[] counterBrowser = new long[6];
	static private long[] counterOS = new long[9];
	
	//Zugriffverlauf
	static private String[] zugriffVerlauf = new String[100];

	private static final Map<String, String> log = new HashMap<>();
	
	public static void forceSync() {
		synchronizedWithMySQL = false;
		count(null,true);
	}
	
	public synchronized static void count(HttpServletRequest request, boolean syncOnly) {
		
		if(!synchronizedWithMySQL) {
			
			//reset
			dayID = -1;
			lastDayInMonth = -1;
			
			boolean ok = sync();
			
			if(!ok)
				return;
			
			synchronizedWithMySQL = true;
			Konsole.counter("Besucher-Zähler mit Datenbank synchronisiert.");
		}
		
		if(syncOnly)
			return;
			
		String currentIP = Sitzung.getIP(request);
		
		if(lastIP == null || !currentIP.equals(lastIP)) {

			int currentHour = getCurrentHour();
			int currentDay = getDayOfMonth();
			
			//Systeme: userAgent
			if(request == null)
				request = ((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest());
			
			String agent = request.getHeader("user-agent");
			if(agent != null)
				agent = agent.toLowerCase();
			
			//Zugriffverlauf
			//Verschieben
			for(int i = zugriffVerlauf.length-1; i >= 1; i--) {
				zugriffVerlauf[i] = zugriffVerlauf[i-1];
			}

			LocalDateTime now = LocalDateTime.now();
			if(agent == null || UserAgentDetection.isBot(agent)) {
				String content = "[IP / " + currentIP + "] [ ~ BOT ~ ] --- " + agent;
				zugriffVerlauf[0] = now.format(Timeformats.complete)+ " " + content;
				log.put(now.format(Timeformats.iso), content);
				return;
			} else {
				int browser = UserAgentDetection.getBrowser(agent);
				int os = UserAgentDetection.getOS(agent);
				counterBrowser[browser]++;
				counterOS[os]++;
				String content = "[IP / " + currentIP + "] ["+UserAgentDetection.OS[os]+" / "+UserAgentDetection.BROWSERS[browser]+"] --- " + agent;
				zugriffVerlauf[0] = now.format(Timeformats.complete)+ " " + content;
				log.put(now.format(Timeformats.iso), content);
			}
				
			//jetzt kommen die Stats: Tage, Stunden
			
			counter++;
			counterHour[currentHour]++;
			counter30++;
			Datenbank.setStatsTotal(counter);
			counterReadable = toReadable(counter);
			counter30Readable = toReadable(counter30);
			
			//Index verschieben
			hourIndex++;
			if(hourIndex == COUNTER_HOUR_AMOUNT)
				hourIndex = 0;
			Datenbank.setStatsStundenIndex(hourIndex);
			
			if(dayID == -1) {
				//erster Aufruf
				String dateString = Timeformats.normed().format(Calendar.getInstance().getTime());
				
				dayID = Datenbank.saveStats(dateString,counterDay[counterDay.length-1].getViews()+1,hourIndex,currentHour);
				counterDay[counterDay.length-1].setViews(counterDay[counterDay.length-1].getViews()+1);
				Konsole.counter("erster Aufruf seit Serverstart: "+dateString);
			} else if(lastDayInMonth == currentDay) {
				//gleicher Tag
				Datenbank.saveStats(dayID,counterDay[counterDay.length-1].getViews()+1,hourIndex,currentHour);
				counterDay[counterDay.length-1].setViews(counterDay[counterDay.length-1].getViews()+1);
			} else {
				//neuer Tag
				String dateString = Timeformats.normed().format(Calendar.getInstance().getTime());
				
				dayID = Datenbank.saveStats(dateString,1,hourIndex,currentHour);
				sync();
			}
			
			//Monatabhängig: Systeme
			if(lastDayInMonth > currentDay) {
				//neuer Monat
				//userAgent resetten
				for(int i = 0; i < counterBrowser.length; i++) {
					counterBrowser[i] = 0;
				}
				for(int i = 0; i < counterOS.length; i++) {
					counterOS[i] = 0;
				}
				
				//keine MySQL-Sync (nur Laufzeit)
			}
			
			lastDayInMonth = currentDay;
			
			lastIP = currentIP;
		}
	}
	
	private static boolean sync() {
		
		long[] tempCounterHour = Datenbank.getStatsStunden();
		
		if(tempCounterHour == null) {
			return false; //noch nicht mit MySQL verbunden.
		}
		
		//ansonsten: weiter synchronisieren
		
		//24 Stunden
		counterHour = tempCounterHour;
		hourIndex = Datenbank.getStatsStundenIndex();
		
		//letzten 30 Tage
		
		counterDay = Datenbank.getStatsTage();
		
		counter30 = 0;
		for(int i = 0; i < counterDay.length; i++)
			counter30 += counterDay[i].getViews();
		
		counter = Datenbank.getStatsTotal();
		counterReadable = toReadable(counter);
		counter30Readable = toReadable(counter30);
		return true;
	}
	
	private static String toReadable(long number) {
		NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
	    return numberFormat.format(number);
	}
	
	public static void reset30() {
		Datenbank.resetStatsTage();
		sync();
	}
	
	public static void reset24() {
		Datenbank.resetStatsStunden();
		sync();
	}
	
	private static int getCurrentHour() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		return cal.get(Calendar.HOUR_OF_DAY);
	}
	
	private static int getDayOfMonth() {
		return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	}
	
	public static long getCounter() {
		return counter;
	}
	
	public static String getCounterReadable() {
		return counterReadable;
	}
	
	public static long[] getCounterHour() {
		return counterHour;
	}
	
	public static long getCounterMonth() {
		return counter30;
	}
	
	public static String getCounter30Readable() {
		return counter30Readable;
	}
	
	public static long getCounterToday() {
		if(counterDay[counterDay.length-1] != null)
			return counterDay[counterDay.length-1].getViews();
		else
			return 0;
	}
	
	public static Count[] getCounterDay() {
		return counterDay;
	}
	
	public static long[] getCounterBrowser() {
		return counterBrowser;
	}
	
	public static long[] getCounterOS() {
		return counterOS;
	}
	
	public static String[] getZugriffVerlauf() {
		return zugriffVerlauf;
	}

	public static Map<String, String> getLog() {
		return log;
	}
}
