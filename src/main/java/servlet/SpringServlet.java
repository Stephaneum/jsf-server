package servlet;

import io.jsonwebtoken.Claims;
import managedBean.TopMenu;
import mysql.Datenbank;
import sitzung.ViewCounter;
import tools.Jwt;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

@WebServlet("/event")
public class SpringServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(SpringServlet.class.getName());
	
	/*
	 * reacts to spring events
	 */
	
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		String parameter = request.getParameter("event");
		if(parameter != null) {
			Claims data = Jwt.getData(parameter);
			if(data != null) {
				String event = (String) data.get("event");
				if(event != null) {
					logger.severe("Received "+event);
					switch(event) {
						case "SYNC_ALL":
							Datenbank.syncAll();
							ViewCounter.forceSync();
							TopMenu.triggerChanged();
							break;
						case "SYNC_MENU":
							TopMenu.triggerChanged();
							break;
						case "SYNC_SPECIAL_TEXT":
							Datenbank.syncSpecialText();
							break;
						case "SYNC_VARIABLES":
							Datenbank.syncVariables();
							break;
						case "SYNC_PLAN":
							Datenbank.syncPlan();
							break;
						default:
							logger.severe("ERROR: unknown event type");
					}
				}
			} else {
				logger.severe("ERROR: jwt error");
			}
		}
	}

}