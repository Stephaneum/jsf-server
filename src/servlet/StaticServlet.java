package servlet;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mysql.Datenbank;
import objects.Datei;

public class StaticServlet extends HttpServlet {
	
	/*
	 * öffentliche Dateien (wie DynamicImageServlet)
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	
	public static final String STATIC_FOLDER_NAME = "static";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			
			request.setCharacterEncoding("UTF-8");
			
			String file = request.getPathInfo();
			String path = Datenbank.getSpeicherort();
			
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(path+"/"+STATIC_FOLDER_NAME+file));
			
			// Content
			byte[] bytes = getBytesFromStream(in);
			
			// Schreibe content in response.
			
			InputStream input = new URL("http://localhost:8080/Stephaneum/home.xhtml").openStream();
			bytes = getBytesFromStream(input);
			
			//MIME-type
			String endung = file.substring(file.lastIndexOf('.')+1, file.length()).toLowerCase();
			String mime = Datei.toMime(endung);
			
			if(mime != null) {
				response.setContentType(mime);
			}
			
			if(mime != null && !mime.equals("text/html")) {
				String fileWithoutPath = file.substring(file.lastIndexOf("/")+1);
				response.setHeader("Content-Disposition","inline; filename=\""+fileWithoutPath+"\""); //Namen der Datei
			}
			response.getOutputStream().write(bytes);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			falseURL500(response);
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
	
	private static byte[] getBytesFromStream(InputStream in) throws IOException {
		byte[] bytes = new byte[in.available()];
		in.read(bytes);
		in.close();
		return bytes;
	}

}