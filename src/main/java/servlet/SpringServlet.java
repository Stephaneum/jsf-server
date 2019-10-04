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

@WebServlet("/spring")
public class SpringServlet extends HttpServlet {
	
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
					System.out.println("Received "+event);
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
							System.err.println("ERROR: unknown event type");
					}
				}
			} else {
				System.err.println("ERROR: jwt error");
			}
		}
	}

}