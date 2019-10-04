package servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mysql.Datenbank;
import objects.Datei;

@WebServlet("/images/*")
public class ImageServlet extends HttpServlet {
	
	/*
	 * damit die Grafiken auf der Homepage angezeigt werden können
	 */
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			
			request.setCharacterEncoding("UTF-8");
			
			// Parameter
			String parameter = request.getParameter("id");
			
			if(parameter == null) {
				return;
			}
			
			String[] id_string = parameter.split("_",2);
			
			if(id_string.length < 2) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			
			String file = id_string[1];
			
			String endung = file.substring(file.lastIndexOf('.')+1, file.length()).toLowerCase();
			String mime = Datei.toMime(endung);
			
			if(!Datei.isImage(mime)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return; //kein Bild
			}
			
			String path = Datenbank.getSpeicherort();
			
			//HTTP 304
			//last-modified
			File fileObject = new File(path +"/"+ parameter);
			long lastModified = (fileObject.lastModified() / 1000 ) * 1000; //last-modified speichert keine Millisekunden
			long lastModifiedBrowser = request.getDateHeader("If-Modified-Since");
			if(lastModified <= lastModifiedBrowser) {
				response.setDateHeader("Last-Modified", lastModified);
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(path +"/"+ parameter));
			
			// Content
			byte[] bytes = new byte[in.available()];
			in.read(bytes);
			in.close();
			
			response.setDateHeader("Last-Modified", lastModified);
			response.setHeader("Content-Length", String.valueOf(bytes.length));
			
			// Schreibe Content in response
			response.getOutputStream().write(bytes);
		} catch (IOException e) {
			Konsole.error(e.getMessage());
		}
	}

}