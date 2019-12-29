package servlet;

import java.io.*;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sitzung.ViewCounter;

@WebServlet("/access-log")
public class AccessServlet extends HttpServlet {

    /*
     * show request log
     */

    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        try {

            StringBuilder builder = new StringBuilder();
            Map<String, String> log = ViewCounter.getLog();
            builder.append('[');
            boolean first = true;
            for (Map.Entry<String, String> entry : log.entrySet()) {
                if(!first)
                    builder.append(',');
                builder.append('{');
                builder.append("\"time\":\"");
                builder.append(entry.getKey());
                builder.append("\",\"request\":\"");
                builder.append(entry.getValue());
                builder.append("\"}");

                first = false;
            }
            builder.append(']');

            PrintWriter out = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print(builder.toString());
            out.flush();
        } catch (IOException e) {
            Konsole.error(e.getMessage());
        }
    }

}