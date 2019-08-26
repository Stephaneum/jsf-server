package servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mysql.Datenbank;
import sitzung.Sitzung;

public class VertretungServlet extends HttpServlet {
	
	/*
	 * gibt den Vertretungsplan zurück, falls nicht hochgeladen: eine Fehlermeldung
	 * MAIN_URL/vertretung.pdf
	 */
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			
			String path = Datenbank.getVertretung();
			
			if(path == null) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				PrintWriter writer = response.getWriter();
				writer.println("<html>");
				writer.println("<head><title>Ein Fehler ist aufgetreten</title></title>");
				writer.println("<body>");
				writer.println("<br><br>");
				writer.println("<h1 align=\"center\">Bis jetzt wurde kein Vertretungsplan hochgeladen.</h1>");
				writer.println("<br><br>");
				writer.println("<h2 align=\"center\">Error 500</h2>");
				writer.println("</body></html>");
				writer.flush();
				writer.close();
				return;
			}
			
			//ViewCounter
			Sitzung.countIfNoSession(request);
			
			//last-modified
			File file = new File(path);
			long lastModified = (file.lastModified() / 1000 ) * 1000; //last-modified speichert keine Millisekunden
			long lastModifiedBrowser = request.getDateHeader("If-Modified-Since");
			if(lastModified <= lastModifiedBrowser) {
				response.addDateHeader("Last-Modified", lastModified);
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			
			// Content
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
			byte[] bytes = new byte[in.available()];
			in.read(bytes);
			in.close();
			
			// Schreibe Content in response
			response.addHeader("Cache-Control", "no-cache");
			response.addDateHeader("Last-Modified", lastModified);
			response.setContentType("application/pdf");
			response.getOutputStream().write(bytes);
		} catch (IOException e) {
			Konsole.error(e.getMessage());
		}
	}

}