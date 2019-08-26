package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mysql.Datenbank;
import objects.Gruppe;
import tools.URLManager;

public class SitemapServlet extends HttpServlet {
	
	/*
	 * gibt eine sitemap.xml zurück
	 * MAIN_URL/sitemap.xml
	 */
	
	private static final long serialVersionUID = 1L;
	
	final private static String[] URL = {
			URLManager.HOME,
			URLManager.VERTRETUNG,
			URLManager.TERMINE,
			URLManager.LOGIN,
			URLManager.STATISTIKEN,
			URLManager.KONTAKT,
			URLManager.IMPRESSUM,
			URLManager.SITEMAP
	};

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			
			String mainURL = URLManager.getMainURL(request);
			
			response.setContentType("text/xml;charset=UTF-8");
			PrintWriter writer = response.getWriter();
			writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
			
			//Alle URLs im Array
			for(int i = 0; i < URL.length; i++) {
				writer.append("\n<url><loc>");
				writer.append(mainURL);
				writer.append('/');
				writer.append(URL[i]);
				if(URL[i] != URLManager.VERTRETUNG)
					writer.append(".xhtml");
				writer.append("</loc></url>");
			}
			
			//Alle Gruppen
			Gruppe[] gruppen = Datenbank.getGruppeArray();
			if(gruppen != null) {
				for(int i = 0; i < gruppen.length; i++) {
					if(gruppen[i].getLink() == null)
						writeGruppe(writer, mainURL, gruppen[i]);
				}
			}
			
			writer.append("</urlset>");
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			Konsole.error(e.getMessage());
		}
	}
	
	private void writeGruppe(PrintWriter writer, String mainURL, Gruppe parent) {
		writer.append("\n<url><loc>");
		writer.append(mainURL);
		writer.append('/');
		writer.append(URLManager.HOME);
		writer.append(".xhtml?id="+parent.getGruppe_id());
		writer.append("</loc></url>");
		
		Gruppe[] childs = parent.getUnterGruppe();
		if(childs != null) {
			for(int i = 0; i < childs.length; i++) {
				if(childs[i].getLink() == null)
					writeGruppe(writer, mainURL, childs[i]);
			}
		}
	}

}