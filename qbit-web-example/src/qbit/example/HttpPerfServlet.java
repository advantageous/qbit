package qbit.example;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Richard on 11/12/14.
 */

@WebServlet("/perf/")
public class HttpPerfServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, IOException {


            response.setStatus(200);
            response.addHeader("Content-Type", "application/json");
            response.getWriter().append("\"ok\"");


    }
}
