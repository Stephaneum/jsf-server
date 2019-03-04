package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tools.URLManager;

public class RobotsServlet extends HttpServlet {
	
	/*
	 * gibt eine robots.txt zurück
	 * MAIN_URL/robots.txt
	 */
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			
			String mainURL = URLManager.getMainURL(request);
			
			response.setContentType("text/plain");
			
			PrintWriter writer = response.getWriter();
			
			writer.println("User-Agent: *");
			writer.println("Allow: /");
			writer.println("");
			writer.println("Sitemap: "+mainURL+"/sitemap.xml");
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			Konsole.error(e.getMessage());
		}
	}

}