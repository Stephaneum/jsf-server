package sitzung;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import objects.Nutzer;

public class Sitzung implements Serializable{
	private static final long serialVersionUID = 1L;
	
	static final String KEY_NUTZER = "nutzer", KEY_COUNTER = "counter";
	
	public static void setNutzer(Nutzer nutzer) {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(KEY_NUTZER,nutzer);
		Konsole.info("Neues Nutzer-Objekt in der aktuellen Sitzung erstellt ("+nutzer.getVorname()+" "+nutzer.getNachname()+")");
	}
	
	public static void deleteNutzer() {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(KEY_NUTZER);
		Konsole.info("Nutzer-Objekt in der Sitzung gelöscht");
	}
	
	//von ManagedBean aufgerufen
	public static Nutzer getNutzer() {
		Map<String,Object> map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		return (Nutzer)map.get(KEY_NUTZER) ;
	}
	
	//von Servlet aufgerufen
	public static Nutzer getNutzerServlet(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if(session != null) {
			Object object = session.getAttribute(KEY_NUTZER);
			if(object != null) {
				return (Nutzer) object;
			}
		}
		
		return null;
	}
	
	public static boolean isLoggedIn() {
		Object object = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(KEY_NUTZER);
		if(object == null) {
			return false;
		} else {
			Nutzer nutzer = (Nutzer) object;
			if(nutzer.getRang() == Nutzer.RANG_GAST_NO_LOGIN) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	//neuer Gast
	public static Nutzer createNutzerGuest() {
		
		Nutzer nutzer = new Nutzer();
		
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		
		context.getSessionMap().put(KEY_NUTZER,nutzer);
		
		Konsole.info("Neuer Gast");
		
		//ViewCounter
		Object object = context.getSessionMap().get(KEY_COUNTER);
		
		if(object == null) {
			
			context.getSessionMap().put(KEY_COUNTER,new Object());
			ViewCounter.count((HttpServletRequest)context.getRequest(), false); //Zählen
		}
		
		return nutzer;
	}
	
	//vom Vertretungsplan aufgerufen
	public static void countIfNoSession(HttpServletRequest request) {
		if(getNutzerServlet(request) == null) 
			ViewCounter.count(request, false);
	}
	
	//request optional
	public static String getIP(HttpServletRequest request) {
		
		if(request == null)
			request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String ipAddress = request.getHeader( "X-FORWARDED-FOR" );
		if ( ipAddress == null ) {
		    ipAddress = request.getRemoteAddr();
		}
		
		return ipAddress;
	}
	
	public static boolean isAdmin() {
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer != null && nutzer.getRang() == Nutzer.RANG_ADMIN) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isLehrer() {
		Nutzer nutzer = Sitzung.getNutzer();
		if(nutzer != null && nutzer.getRang() == Nutzer.RANG_LEHRER) {
			return true;
		} else {
			return false;
		}
	}

}
