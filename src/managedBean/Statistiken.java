package managedBean;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.Datei;
import objects.Nutzer;
import sitzung.Count;
import sitzung.UserAgentDetection;
import sitzung.ViewCounter;
import tools.URLManager;

//eager bedeutet, dass diese Klasse beim Programmstart sofort instanziert wird
@ManagedBean(name="statistiken", eager = true)
@ApplicationScoped
public class Statistiken {
	
	final static private long startTime;
	final static private String startTimeReadable;
	
	final static private int UPDATE_INTERVAL = 10*60*1000; //jede 10 Minuten
	static private Runtime runtime = Runtime.getRuntime();
	static private long lastUpdatedBars = 0;
	
	static private String aufrufeLabels, aufrufeData;
	static private String aufrufeMonatLabels, aufrufeMonatData;
	static private String browserLabels, browserData;
	static private String osLabels, osData;
	static private String zugriffVerlauf;
	
	static private String cloudMimeLabel, cloudMimeData;
	static private String cloudActivityLabel, cloudActivityData, cloudActivityFormat;
	
	static private int anzahlBeitrag, anzahlBilderBeitrag, anzahlLehrer, anzahlSchueler, cloudDateien;
	static private String cloudSize;
	
	static {
		startTime = System.currentTimeMillis();
		Instant instance = Instant.ofEpochMilli(startTime);
		ZonedDateTime zonedInstance = ZonedDateTime.ofInstant(instance,ZoneId.systemDefault());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.MMMM u").withLocale(Locale.GERMAN);;
		startTimeReadable = zonedInstance.format(formatter);
	}
	
	public String getTime() {
		long millis = System.currentTimeMillis() - startTime;
		
		int seconds = (int) (millis / 1000) % 60 ;
		int minutes = (int) ((millis / (1000*60)) % 60);
		int hours   = (int) ((millis / (1000*60*60)) % 24);
		int days   = (int) (millis / (1000*60*60*24));
		
		return days+" Tage "+hours+" Stunden "+minutes+" Minuten "+seconds+" Sekunden";
	}
	
	public String getTimeReadable() {
		return startTimeReadable;
	}
	
	public String getViewsReadable() {
		return ViewCounter.getCounterReadable();
	}
	
	public String getViewsMonthReadable() {
		return ViewCounter.getCounter30Readable();
	}
	
	public long getViewsToday() {
		return ViewCounter.getCounterToday();
	}
	
	public static void initData() {
		
		Konsole.method("Statistiken.initData()");
		
		lastUpdatedBars = System.currentTimeMillis();
		
		StringBuilder builderLabel = new StringBuilder();
		StringBuilder builderData = new StringBuilder();
		
		long[] dataHour = ViewCounter.getCounterHour();
        
        if(dataHour != null) {
        	builderLabel.append(0);
    		builderData.append(dataHour[0]);
        	for(int i = 1; i < 24; i++) {
        		builderLabel.append(","+i);
        		builderData.append(","+dataHour[i]);
 	        }
        	aufrufeLabels = builderLabel.toString();
        	aufrufeData = builderData.toString();
        } else {
        	aufrufeLabels = null;
        	aufrufeData = null;
        }
		
        Count[] dataDay = ViewCounter.getCounterDay();
        
        if(dataDay != null) {
        	builderLabel.setLength(0); //reset
        	builderData.setLength(0); //reset
        	builderLabel.append("'"+dataDay[0]+"'");
    		builderData.append(dataDay[0].getViews());
        	for(int i = 1; i < dataDay.length; i++) {
        		builderLabel.append(",'"+dataDay[i]+"'");
        		builderData.append(","+dataDay[i].getViews());
	        }
        	aufrufeMonatLabels = builderLabel.toString();
        	aufrufeMonatData = builderData.toString();
        } else {
        	aufrufeMonatLabels = null;
        	aufrufeMonatData = null;
        }
		
		long[] dataBrowser = ViewCounter.getCounterBrowser();
		
		if(dataBrowser != null) {
			builderLabel.setLength(0); //reset
        	builderData.setLength(0); //reset
        	// push first to the last position
			for(int i = 1; i < dataBrowser.length; i++) {
				builderLabel.append("'"+UserAgentDetection.BROWSERS[i]+"',");
        		builderData.append(dataBrowser[i]+",");
			}
			builderLabel.append("'"+UserAgentDetection.BROWSERS[0]+"'");
    		builderData.append(dataBrowser[0]);
			browserLabels = builderLabel.toString();
			browserData = builderData.toString();
		} else {
			browserLabels = null;
			browserData = null;
		}
		
		long[] dataOS = ViewCounter.getCounterOS();
		
		if(dataOS != null) {
			builderLabel.setLength(0); //reset
        	builderData.setLength(0); //reset
        	// push first to the last position
			for(int i = 1; i < dataOS.length; i++) {
				builderLabel.append("'"+UserAgentDetection.OS[i]+"',");
        		builderData.append(dataOS[i]+",");
			}
			builderLabel.append("'"+UserAgentDetection.OS[0]+"'");
    		builderData.append(dataOS[0]);
			osLabels = builderLabel.toString();
			osData = builderData.toString();
		} else {
			osLabels = null;
			osData = null;
		}
		
		String[] verlauf = ViewCounter.getZugriffVerlauf();
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < verlauf.length; i++) {
			builder.append(verlauf[i]);
			builder.append('\n');
		}
		
		zugriffVerlauf = builder.toString();
		
		//Allgemein
		anzahlBeitrag = Datenbank.getAnzahlBeitrag();
		anzahlBilderBeitrag = Datenbank.getAnzahlBilderBeitrag();
		anzahlSchueler = Datenbank.getAnzahlNutzer(Nutzer.RANG_SCHUELER);
		anzahlLehrer = Datenbank.getAnzahlNutzer(Nutzer.RANG_LEHRER);
		
		cloudDateien = Datenbank.getAnzahlDatei();
		cloudSize = Datenbank.getCloudSize();
		
		long[] gruppen = new long[6];
		String[] mimes = Datenbank.getCloudMime(cloudDateien);
		
		for(int i = 0; i < mimes.length; i++) {
			if(Datei.isImage(mimes[i]))
				gruppen[0]++;
			else if(Datei.isPdf(mimes[i]))
				gruppen[1]++;
			else if(Datei.isOffice(mimes[i]))
				gruppen[2]++;
			else if(Datei.isVideo(mimes[i]))
				gruppen[3]++;
			else if(Datei.isAudio(mimes[i]))
				gruppen[4]++;
			else
				gruppen[5]++;
		}
		
    	builderData.setLength(0); //reset
		builderData.append(gruppen[0]);
		for(int i = 1; i < gruppen.length; i++) {
    		builderData.append(","+gruppen[i]);
		}
		cloudMimeLabel = "'Bilder','PDF','Office','Videos','Musik','andere'";
		cloudMimeData = builderData.toString();

		Map<String,Double> map = Datenbank.getCloudActivity();
		
		//max herausfinden
		double max = 0;
		for ( String key : map.keySet() ) {
			double current = map.get(key);
		    if(current > max)
		    	max = current;
		}
		
		//bytes in lesbare Einheiten umwandeln
		
		String einheit;
		if(Datei.convertToBytes(10, Datei.SIZE_GB) < max) {
			//GB-Skala
			einheit = "GB";
			for ( String key : map.keySet() ) {
				double current = map.get(key);
				map.put(key, Datei.convertByteTo(current, Datei.SIZE_GB));
			}
		} else if(Datei.convertToBytes(10, Datei.SIZE_MB) < max) {
			//MB-Skala
			einheit = "MB";
			for ( String key : map.keySet() ) {
				double current = map.get(key);
				map.put(key, Datei.convertByteTo(current, Datei.SIZE_MB));
			}
		} else {
			//KB-Skala
			einheit = "KB";
			for ( String key : map.keySet() ) {
				double current = map.get(key);
				map.put(key, Datei.convertByteTo(current, Datei.SIZE_KB));
			}
		}
		
		builderLabel.setLength(0); //reset
    	builderData.setLength(0); //reset
		
		boolean first = false;
		for ( String key : map.keySet() ) {
			if(first) {
				builderLabel.append("'"+key+"'");
	    		builderData.append(map.get(key).intValue());
			}
			
			builderLabel.append(",'"+key+"'");
    		builderData.append(","+map.get(key).intValue());
		}
		cloudActivityLabel = builderLabel.toString();
		cloudActivityData = builderData.toString();
		cloudActivityFormat = einheit;
	}
	
	public String getZugriffVerlauf() {
		if(zugriffVerlauf == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return zugriffVerlauf;
	}
	
	public String getAufrufeLabels() {
		if(aufrufeLabels == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return aufrufeLabels;
	}
	
	public String getAufrufeData() {
		if(aufrufeData == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return aufrufeData;
	}
	
	public String getAufrufeMonatLabels() {
		if(aufrufeMonatLabels == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return aufrufeMonatLabels;
	}
	
	public String getAufrufeMonatData() {
		if(aufrufeMonatLabels == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return aufrufeMonatData;
	}
	
	public String getBrowserLabels() {
		if(browserLabels == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return browserLabels;
	}
	
	public String getBrowserData() {
		if(browserData == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return browserData;
	}
	
	public String getOsLabels() {
		if(osLabels == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return osLabels;
	}
	
	public String getOsData() {
		if(osData == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return osData;
	}
	
	public int getAnzahlBeitrag() {
		if(anzahlBeitrag == 0 || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return anzahlBeitrag;
	}
	
	public String getCloudMimeLabel() {
		if(cloudMimeLabel == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return cloudMimeLabel;
	}
	
	public String getCloudMimeData() {
		if(cloudMimeData == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return cloudMimeData;
	}
	
	public String getCloudActivityLabel() {
		if(cloudActivityLabel == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return cloudActivityLabel;
	}
	
	public String getCloudActivityData() {
		if(cloudActivityData == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return cloudActivityData;
	}
	
	public String getCloudActivityFormat() {
		if(cloudActivityFormat == null || System.currentTimeMillis() - lastUpdatedBars > UPDATE_INTERVAL)
			initData();
		return cloudActivityFormat;
	}
	
	//nicht mehr verwendet
	public int getAnzahlBilderBeitrag() {
		return anzahlBilderBeitrag;
	}
	
	public int getAlter() {
		return Year.now().getValue()-1325;
	}
	
	public int getAnzahlLehrer() {
		return anzahlLehrer;
	}
	
	public int getAnzahlSchueler() {
		return anzahlSchueler;
	}
	
	public int getCloudDateien() {
		return cloudDateien;
	}
	
	public String getCloudSize() {
		return cloudSize;
	}
	
	// not used anymore
	public String reset30() {
		ViewCounter.reset30();
		initData();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Zurückgesetzt","Aufrufe der letzten 30 Tagen zurückgesetzt.") );
		return URLManager.STATISTIKEN;
	}
	
	// not used anymore
	public String reset24() {
		ViewCounter.reset24();
		initData();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Zurückgesetzt","Aufrufe nach der Uhrzeit zurückgesetzt.") );
		return URLManager.STATISTIKEN;
	}
	
	public String getEntwicklung() {
		return Datenbank.getEntwicklerInfo();
	}
	
	public boolean isShowTechnologien() {
		return Datenbank.isShowSoftware();
	}
	
	public boolean isShowSysteme() {
		return Datenbank.isShowSystem();
	}
	
	public boolean isShowCloud() {
		return Datenbank.isShowCloud();
	}
	
	public void setShowSysteme(boolean show) {
		Datenbank.setShowSystem(show);
		
		if(show)
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Öffentlich","Statistiken über Systeme sind öffentlich.") );
		else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Privat","Statistiken über Systeme sind nur für Sie sichtbar.") );
	}
	
	public void setShowTechnologien(boolean show) {
		Datenbank.setShowSoftware(show);
		
		if(show)
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Öffentlich","Informationen über die Software sind öffentlich.") );
		else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Privat","Informationen über die Software sind nur für Sie sichtbar.") );
	}
	
	public void setShowCloud(boolean show) {
		Datenbank.setShowCloud(show);
		
		if(show)
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Öffentlich","Statistiken über die Cloud sind öffentlich.") );
		else
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Privat","Statistiken über die Cloud sind nur für Sie sichtbar.") );
	}
	
	public String getUsedROM() {
		return Datenbank.getVerwendeterSpeicherplatz();
	}

	public String getMaxROM() {
		return Datenbank.getSpeicherplatz();
	}
	
	public String getUsedRAM() {
		return Datenbank.getUsedRAM(runtime);
	}
	
	public String getMaxRAM() {
		return Datenbank.getMaxRAM(runtime);
	}
	
}

