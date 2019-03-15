package servlet;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import mysql.Datenbank;
import objects.Datei;
import objects.StaticFile;
import tools.URLManager;

public class StaticServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public static final String STATIC_FOLDER_NAME = "static";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			
			request.setCharacterEncoding("UTF-8");
			
			String filepath = request.getPathInfo().substring(1);
			String path = Datenbank.getSpeicherort();
			
			//MIME-type
			String endung = filepath.substring(filepath.lastIndexOf('.')+1, filepath.length()).toLowerCase();
			String mime = Datei.toMime(endung);
			
			BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(path+"/"+STATIC_FOLDER_NAME+"/"+filepath));
			byte[] bytes = null;
			
			if(mime != null && mime.equals("text/html")) {
				// HTML file
				
				StaticFile file = Datenbank.getStaticFile(filepath);
				if(file == null) {
					// file not in database yet (will be added when admin access static konfig page)
					// default: MODE_MIDDLE
					file = new StaticFile(filepath, StaticFile.MODE_MIDDLE);
				}
				
				response.setContentType(mime+";charset=UTF-8");
				switch(file.getMode()) {
				case StaticFile.MODE_MIDDLE:
				case StaticFile.MODE_FULL_WIDTH:
					String cookie = request.getHeader("Cookie");
					String url = file.getMode() == StaticFile.MODE_MIDDLE ? URLManager.getMainURL(request)+"/static_container_middle.xhtml" : URLManager.getMainURL(request)+"/static_container_full.xhtml";
					HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
					conn.addRequestProperty("Cookie", cookie);
					
		            String template = getStringFromStream(conn.getInputStream());
		            String content = getStringFromStream(fileInput);
		            Document doc = Jsoup.parse(content);
		            String head = doc.select("head").first().html();
		            String body = doc.select("body").first().html();
		            String title;
		            
		            // get title
		            Element element = doc.select("title").first();
		            if(element != null)
		            	title = element.html();
		            else
		            	title = "Beitrag";
		            
		            template = template.replace("$replaceBody", body);
		            template = template.replace("$replaceHead", head);
		            template = template.replace("$replaceTitle", title);
		            
		            bytes = template.getBytes(Charset.forName("UTF-8"));
					break;
				case StaticFile.MODE_FULL_SCREEN:
					bytes = getBytesFromStream(fileInput);
					break;
				}
			} else {
				// other files
				bytes = getBytesFromStream(fileInput);
				response.setContentType(mime);
			}
			
			response.addHeader("Cache-Control", "private");
			response.addHeader("Content-Length", String.valueOf(bytes.length));
			response.getOutputStream().write(bytes);
			response.flushBuffer();
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
	
	private static String getStringFromStream(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(
                new InputStreamReader(in));

		StringBuilder builder = new StringBuilder();
		String inputLine;
		while ((inputLine = br.readLine()) != null) {
			builder.append(inputLine);
			builder.append("\n");
		}
		br.close();
		return builder.toString();
	}

}