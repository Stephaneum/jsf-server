package servlet;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mysql.Datenbank;
import objects.Datei;
import objects.Nutzer;
import sitzung.Sitzung;
import tools.Action;
import tools.Countdown;
import tools.Zugangscodes;

@WebServlet("/preview/*")
public class PreviewServlet extends HttpServlet {
	
	/*
	 * gibt die Datei zurück, jedoch nur, wenn man eingeloggt ist und die Bedingungen erfüllt sind
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	
	//falls die Datei an Microsoft gesendet wird
	private static final int DURATION = 60*1000; // 1 Minute
	private static List<Datei> timeLimited = new ArrayList<Datei>();
	public static final String KEY = Zugangscodes.generateZugangscode(15); //ein key zur Laufzeit, für den timeLimited-Fall

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			// Parameter
			
			request.setCharacterEncoding("UTF-8");
			String file = request.getParameter("file"); //im Spezialfall: nur Datei-ID
			String key = request.getParameter("key");
			
			if(file == null || key == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				falseURL400(response);
				Konsole.info("/preview/ -- PARAMETER MISSING");
				return;
			}
			
			//ist diese Datei öffentlich?
			
			String filename = null;
			String path = null;
			
			boolean accessDenied = false;
			if(key.equals(KEY)) {
				
				/* Spezialfall: timeLimited
				 * 
				 * Normalerweise läuft die Authentifizierung: URL-Key == Key in der Session (Nutzer.getKey())
				 * 
				 * Microsoft hat aber keine Sitzung, deswegen muss Microsoft in der URL einen statischen Key nutzen
				 * Um die Sicherheit zu gewährleisten, müssten die Dateien vorher in die ArrayList und
				 * bleiben dort maximal eine Minute
				 */
				
				
				int id = -1;
				
				try {
					id = Integer.parseInt(file);
				} catch (NumberFormatException e) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					falseURL400(response);
					Konsole.info("/preview/ -- PARAMETER MISSING");
					return;
				}
				
				
				//Datei in Array enthalten?
				boolean found = false;
				for(Datei datei : timeLimited) {
					if(datei.getDatei_id() == id) {
						found = true;
						filename = datei.getDatei_name();
						file = datei.getDatei_name_mit_id();
						path = datei.getPfad();
						break;
					}
				}
				
				if(!found) {
					accessDenied = true;
				}
				
			} else {
				Nutzer nutzer = Sitzung.getNutzerServlet(request);
				
				if(nutzer == null || !nutzer.getKey().equals(key)) {
					accessDenied = true;
				} else {
					String[] id_string = file.split("_",2);
					
					if(id_string.length == 0) {
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						falseURL400(response);
						Konsole.info("/public/?file="+file+" -- UNKNOWN ID");
						return;
					} else {
						filename = id_string[1];
						
						path = Datenbank.getSpeicherort();
						path = path +"/"+ file;
					}
				}
			}
			
			if(accessDenied) {
				Konsole.info("/public/?file="+file+" -- ACCESS DENIED");
				
				response.setContentType("text/html");
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				
				PrintWriter writer = response.getWriter();
				writer.println("<html>");
				writer.println("<head><title>Zugriff verweigert</title></title>");
				writer.println("<body>");
				writer.println("<br><br>");
				writer.println("<h1 align=\"center\">Zugriff verweigert.</h1>");
				writer.println("<br><br>");
				writer.println("<br><br>");
				writer.println("<h2 align=\"center\">Error 403</h2>");
				writer.println("</body></html>");
				writer.flush();
				writer.close();
				return;
			}
			
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
			
			// Content
			byte[] bytes = new byte[in.available()];
			in.read(bytes);
			in.close();
			
			//MIME-type
			String endung = file.substring(file.lastIndexOf('.')+1, file.length()).toLowerCase();
			String mime = Datei.toMime(endung);
			
			if(mime != null) {
				response.setContentType(mime);
				Konsole.info("public: "+file+" mime:"+mime);
			}
			response.setHeader("Content-Disposition","inline; filename=\""+filename+"\""); //Namen der Datei
			response.getOutputStream().write(bytes);
		} catch(FileNotFoundException e) {
			
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			falseURL500(response);
			
			
		} catch (IOException e) {
			
			try {
				
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				
				PrintWriter writer = response.getWriter();
				
				writer.println("<html>");
				writer.println("<head><title>Ein Fehler ist aufgetreten</title></title>");
				writer.println("<body>");
				writer.println("<br><br>");
				writer.println("<h1 align=\"center\">Ein technischer Fehler ist aufgetreten :(</h1>");
				writer.println("<br><br>");
				writer.println("<br><br>");
				writer.println("<h2 align=\"center\">Error 500</h2>");
				writer.println("<br><br>");
				writer.println("<p align=\"center\">"+e.getMessage()+"</p>");
				writer.println("</body></html>");
				writer.flush();
				writer.close();
			} catch (IOException e1) { }
			
		}
	}
	
	private void falseURL400(HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			writer.println("<html>");
			writer.println("<head><title>Ein Fehler ist aufgetreten</title></title>");
			writer.println("<body>");
			writer.println("<br><br>");
			writer.println("<h1 align=\"center\">Ein Fehler ist aufgetreten.</h1>");
			writer.println("<br><br>");
			writer.println("<br><br>");
			writer.println("<h2 align=\"center\">Error 400</h2>");
			writer.println("</body></html>");
			writer.flush();
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void falseURL500(HttpServletResponse response) {
		try {
			PrintWriter writer = response.getWriter();
			writer.println("<html>");
			writer.println("<head><title>Ein Fehler ist aufgetreten</title></title>");
			writer.println("<body>");
			writer.println("<br><br>");
			writer.println("<h1 align=\"center\">Ein Fehler ist aufgetreten.</h1>");
			writer.println("<br><br>");
			writer.println("<br><br>");
			writer.println("<h2 align=\"center\">Error 500</h2>");
			writer.println("</body></html>");
			writer.flush();
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Füge eine Datei in die ArrayList hinzu.
	 * mit dem statischen Schlüssel KEY, kann auf diese Datei maximal eine Minute lang zugegriffen werden
	 * @param datei
	 */
	public static void addDatei(Datei datei) {
		timeLimited.add(datei);
		Countdown.start(DURATION, new Action() {
			
			@Override
			public void startAction() {
				timeLimited.remove(datei);
			}
		}, datei);
	}

}