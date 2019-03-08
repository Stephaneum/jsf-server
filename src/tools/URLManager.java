package tools;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

public class URLManager {
	
	final static public String SUCHE = "suche",
			DUMMY = "admin_dummy", KONFIG = "admin_konfig", STATIC = "admin_static", ADMIN_RUBRIK = "admin_rubriken", LOGS = "admin_logs", NUTZER = "admin_nutzer", ZUGANGSCODE = "admin_zugangscode",
			BACKUP = "admin_backup",
			STATISTIKEN = "statistiken", IMPRESSUM = "impressum", KONTAKT = "kontakt", SITEMAP = "sitemap", GESCHICHTE = "geschichte", VERTRETUNG = "vertretungsplan.pdf", TERMINE = "termine",
			SQL = "sql", EINRICHTUNG = "einrichtung", UPDATE = "update", HOME = "home", HOME_BEITRAG = "beitrag", KLASSE = "klasse", LOGIN = "login", LOGOUT = "logout",
			KONFIG_VERTRETUNG = "konfig_vertretung", LEHRER_CHAT = "lehrerchat",
			RUBRIK = "nutzer_rubrik", BEITRAG = "nutzer_beitrag", DATEIEN = "nutzer_dateien", ACCOUNT = "nutzer_account",
			PROJEKT = "projekt_all", PROJEKT_OPEN = "projekt_open",
			LOG_SICHERUNG = "log_sicherung";
	
	//mobile
	final static public String M_HOME = "pm:home", M_BEITRAG = "pm:beitrag";
	
	
	/* URL des aktuellen Contexts
	 * 
	 * z.B.	http://www.stephaneum.de
	 * 		http://stephaneum700.de:8080/beta
	 * 
	 * request optional
	 */
	static public String getMainURL(HttpServletRequest request) {
		
		if(request == null) {
			// erhalte eine neue Instanz von JSF, falls diese nicht als Parameter vorgegeben wurde
			request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		}
		
		int portInt = request.getServerPort();
		String serverName = request.getServerName();
		String port = null;
		if(portInt != 443 && portInt != 80)
			port = String.valueOf(portInt);
		String protocol = request.getScheme();
		String contextPath = request.getContextPath();
		
		if(port != null)
			return protocol+"://"+serverName+":"+port+contextPath;
		else
			return protocol+"://"+serverName+contextPath;
	}
	
	static public int getPort() {
		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		return request.getServerPort();
	}
	
	static public String translateURL(int portInt) {
		
		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		
		String serverName = request.getServerName();
		String port = null;
		if(portInt != 443 && portInt != 80)
			port = String.valueOf(portInt);
		String protocol = request.getScheme();
		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		String query = request.getQueryString();
		if(query == null)
			query = "";
		else if(!query.trim().equals(""))
			query = "?"+query;
		
		if(port != null)
			return protocol+"://"+serverName+":"+port+contextPath+servletPath+query;
		else
			return protocol+"://"+serverName+contextPath+servletPath+query;
	}
	
	
	final static public boolean CONVERT_TO_URL = false, CONVERT_FROM_URL = true;
	final static private String[][] encoding = {
			{" ", "%20"},
			{"ä", "%C3%A4"},
			{"Ä", "%C3%84"},
			{"ö", "%C3%B6"},
			{"Ö", "%C3%96"},
			{"ü", "%C3%BC"},
			{"Ü", "%C3%9C"},
			{"!", "%21"},
			{"?", "%3F"},
			{"/", "%2F"},
			{":", "%3A"},
			{"&", "%26"},
			{"=", "%3D"},
			{"é", "%C3%A9"},
			{"É", "%C3%89"},
	};
	
	static public String convertUTF8(String s, boolean direction) {
		
		int indexFrom = direction == CONVERT_TO_URL ? 0 : 1;
		int indexTo = direction == CONVERT_TO_URL ? 1 : 0;
		
		for(int i = 0; i < encoding.length; i++) {
			s = s.replace(encoding[i][indexFrom], encoding[i][indexTo]);
		}
		return s;
	}

}
