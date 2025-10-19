package framework.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class FrontServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {

        String contextPath = req.getContextPath();  
        String uri = req.getRequestURI();           
        String relativePath = uri.substring(contextPath.length()); 

        if (relativePath.isEmpty()) {
            relativePath = "/";
        }

        System.out.println("FrontServlet reçoit : " + relativePath);

        resp.setContentType("text/html");
        resp.getWriter().write("<h1>Vous avez demandé : " + relativePath + "</h1>");
    }
}
