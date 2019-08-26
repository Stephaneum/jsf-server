package managedBean;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import mysql.Datenbank;
import objects.Ereignis;

@ViewScoped
@ManagedBean
public class LogBean {
	
	final private List<String> TYPES = Arrays.asList(Ereignis.TYPES);
	private String[] selectedTypes;
	private boolean[] showEreignis;
	private String logs;
	private int days = 30;
	private int logAmount = 0;
	private boolean enableTypes = false;
	
	private int onlyShowLogAmountInt; //-1 -> alle
	private String onlyShowLogAmount; // {"200","1000","all"}
	
	@PostConstruct
	private void init() {
		onlyShowLogAmountInt = 200;
		onlyShowLogAmount = "200";
		showEreignis = new boolean[Ereignis.ANZAHL_TYPES];
		for(int i = 0; i < showEreignis.length; i++)
			showEreignis[i] = true;
		
		reload();
	}
	
	public void reload() {
		
		logAmount = Datenbank.getLogsAmount();
		
		Ereignis[] events = Datenbank.getLogs(showEreignis,onlyShowLogAmountInt);
		
		StringBuilder builder = new StringBuilder();
		
		//Ereignis[] --> String mittels StringBuilder
		for(int i = 0; i < events.length; i++) {
			builder.append(events[i].getDatum());
			builder.append(events[i].getTypString());
			builder.append(' ');
			builder.append(events[i].getText());
			builder.append("\n");
		}
		
		logs = builder.toString();
		
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Logdaten geladen","Anzahl: "+events.length) );
	}
	
	public String getLogs() {
		if(logs == null)
			init();
		return logs;
	}
	
	public void setLogs(String logs) {
		this.logs = logs;
	}
	
	public void clearLogs() {
		Datenbank.clearLog(days);
		init();
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Logdaten gelöscht","Logdaten gelöscht, die älter als "+days+" Tage sind.") );
	}
	
	public int getDays() {
		return days;
	}
	
	public void setDays(int days) {
		this.days = days;
	}
	
	public int getLogAmount() {
		return logAmount;
	}
	
	public List<String> getTypes() {
		return TYPES;
	}
	
	public String[] getSelectedTypes() {
		return selectedTypes;
	}
	
	public void setSelectedTypes(String[] selectedTypes) {
		this.selectedTypes = selectedTypes;
		
		for(int n = 0; n < TYPES.size(); n++) {
			
			showEreignis[n] = false;
			
			for(int i = 0; i < selectedTypes.length; i++) {
				if(selectedTypes[i] != null) {
					if(selectedTypes[i].equals(TYPES.get(n)) ) {
						showEreignis[n] = true;
						break;
					}
				}
			}
		}
	}
	
	public void enable() {
		enableTypes = true;
	}
	
	public boolean isEnableTypes() {
		return enableTypes;
	}
	
	public String getOnlyShowLogAmount() {
		return onlyShowLogAmount;
	}
	
	public void setOnlyShowLogAmount(String onlyShowLogAmount) {
		this.onlyShowLogAmount = onlyShowLogAmount;
	}
	
	public void updateShowLogAmount() {
		if(onlyShowLogAmount == null || onlyShowLogAmount.equals("")) {
			return;
		}
		if(onlyShowLogAmount.equals("all")) {
			onlyShowLogAmountInt = -1;
			reload();
		} else {
			try {
				onlyShowLogAmountInt = Integer.parseInt(onlyShowLogAmount);
				reload();
			} catch (NumberFormatException e) {
				onlyShowLogAmount = onlyShowLogAmountInt == -1 ? "all" : String.valueOf(onlyShowLogAmountInt);
			}
		}
	}
	
}
