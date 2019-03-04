package managedBean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import mysql.Datenbank;
import tools.URLManager;

@ViewScoped
@ManagedBean
public class MySQLConnection {
	
	public static boolean SKIP = true; //sofort mit dem festgelegten MySQL-Account verbinden
	
	/* 
	 * Name:           root
	 * Passwort:       foradmin
	 * IP:             localhost
	 * Port:           3306
	 * Datenbank-Name: s4 oder beta
	 */
	
	final static public String USER = "root", PASSWORD = "foradmin", DATABASE = Konfig.RESTORE_VERSION ? "restore" : "s4", IP = "localhost", PORT = "3306";
	
	private boolean databaseConnected = false, error = false;
	private String name, password, ip, port, database_name; //h:form
	
	//Konstruktor (wird als erstes aufgerufen)
	public MySQLConnection() {
		databaseConnected = Datenbank.isConnected();
	}
	
	//Funktion wird aufgerufen von <f:viewAction action="#{mySQLConnection.weiterleiten}" />
	public String weiterleiten() {
		if(SKIP == true) {
			Datenbank.connect(USER, PASSWORD, IP, PORT, DATABASE);
			
			if(Datenbank.needUpdate())
				return URLManager.UPDATE;
			else if(!Datenbank.existDatabase())
				return URLManager.EINRICHTUNG;
			else
				return URLManager.HOME;
		}
		if(databaseConnected == true) {
			return URLManager.HOME; //Weiterleiten, wenn bereits verbunden
		} else {
			return null;
		}
	}
	
	public boolean getRender() {
		return (databaseConnected == false); //wenn noch NICHT mit MySQL verbunden, dann zeige h:form an
	}
	
	public boolean getError() {
		return error;
	}
	
	//Button
	public String connect() {
		boolean richtig = Datenbank.connect(name, password, ip, port, database_name);
		if(richtig == true) {
			return URLManager.HOME+"?faces-redirect=true";
		} else {
			error = true;
			return null;
		}
	}
	
	//Ab hier beginnen die GET/SET - Funktionen für h:form
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getPort() {
		return port;
	}
	
	public void setPort(String port) {
		this.port = port;
	}
	
	public String getDatabase_name() {
		return database_name;
	}
	
	public void setDatabase_name(String database_name) {
		this.database_name = database_name;
	}

}
