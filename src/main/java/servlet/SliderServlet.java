package servlet;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mysql.Datenbank;

@WebServlet("/slider/*")
public class SliderServlet extends HttpServlet {
	
	/*
	 * damit die Grafiken auf der Homepage angezeigt werden können
	 */
	
	private static final long serialVersionUID = 1L;
	private static final long EXPIRATION = 60*60*24*30; // 30 Tage

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			
			request.setCharacterEncoding("UTF-8");
			
			// Parameter
			String file = request.getParameter("file");
			
			if(file == null || !file.startsWith("slider_")) {
				return;
			}
			
			String path = Datenbank.getSpeicherort();
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(path +"/"+ file));
			
			// Content
			byte[] bytes = new byte[in.available()];
			in.read(bytes);
			in.close();
			
			response.setHeader("Cache-Control", "max-age="+EXPIRATION);
			response.setHeader("Content-Length", String.valueOf(bytes.length));
			
			// Schreibe Content in response
			response.getOutputStream().write(bytes);
		} catch (IOException e) {
			Konsole.error(e.getMessage());
		}
	}

}