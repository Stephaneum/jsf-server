package servlet;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mysql.Datenbank;
import objects.Datei;

public class PublicFileServlet extends HttpServlet {
	
	/*
	 * öffentliche Dateien (wie DynamicImageServlet)
	 * 
	 */
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			
			// Parameter
			request.setCharacterEncoding("UTF-8");
			String file = request.getParameter("file");
			
			if(file == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				falseURL400(response);
				Konsole.info("/public/ -- ID MISSING");
				return;
			}
			
			//ist diese Datei öffentlich?
			String[] id_string = file.split("_",2);
			
			if(id_string.length < 2) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				falseURL400(response);
				Konsole.info("/public/?file="+file+" -- UNKNOWN ID");
				return;
			}
			
			int id = -1;
			try {
				id = Integer.parseInt(id_string[0]);
			} catch (NumberFormatException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				falseURL400(response);
				Konsole.info("/public/?file="+file+" -- UNKNOWN ID");
				return;
			}
			
			if(!Datenbank.isPublic(id) ) {
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
				writer.println("<h1 align=\"center\">a) Diese Datei ist nicht (mehr) öffentlich.</h1>");
				writer.println("<br>");
				writer.println("<h1 align=\"center\">b) Diese Datei wurde bereits gelöscht.</h1>");
				writer.println("<br><br>");
				writer.println("<h2 align=\"center\">Error 403</h2>");
				writer.println("</body></html>");
				writer.flush();
				writer.close();
				return;
			}
			
			String path = Datenbank.getSpeicherort();
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(path +"/"+ file));
			
			// Content
			byte[] bytes = new byte[in.available()];
			in.read(bytes);
			in.close();
			
			// Schreibe content in response.
			
			//MIME-type
			String endung = file.substring(file.lastIndexOf('.')+1, file.length()).toLowerCase();
			String mime = Datei.toMime(endung);
			
			if(mime != null) {
				response.setContentType(mime);
				Konsole.info("public: "+file+" mime:"+mime);
			}
			response.setHeader("Content-Disposition","inline; filename=\""+id_string[1]+"\""); //Namen der Datei
			response.setHeader("Content-Length", String.valueOf(bytes.length));
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
				writer.println("<h2 align=\"center\">Error 500</h2>");
				writer.println("<br><br>");
				writer.println("<p align=\"center\">"+e.getMessage()+"</p>");
				writer.println("</body></html>");
				writer.flush();
				writer.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
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
			writer.println("<h1 align=\"center\">Die URL ist falsch.</h1>");
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
			writer.println("<h1 align=\"center\">Diese Datei wurde nicht gefunden.</h1>");
			writer.println("<br><br>");
			writer.println("<h1 align=\"center\">Die URL könnte falsch sein oder der Server wurde falsch konfiguriert.</h1>");
			writer.println("<br><br>");
			writer.println("<h2 align=\"center\">Error 500</h2>");
			writer.println("</body></html>");
			writer.flush();
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}