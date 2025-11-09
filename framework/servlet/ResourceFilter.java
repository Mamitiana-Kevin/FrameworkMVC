package framework.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.IOException;

public class ResourceFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String resourcePath = requestURI.substring(contextPath.length());

        // For root URL
        if (resourcePath.equals("/") || resourcePath.isEmpty()) {
            request.setAttribute("originalURI", requestURI);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/framework-dispatch");
            dispatcher.forward(request, response);
            return;
        }
        
        ServletContext context = request.getServletContext();
        String fullResourcePath = context.getRealPath(resourcePath);
        File resourceFile = new File(fullResourcePath);
        
        if (resourceFile.exists() && resourceFile.isFile()) {
            // Let Tomcat serve static files (CSS, JS, images, index.html)
            chain.doFilter(request, response);
        } else {
            // Not a file → send to FrontServlet
            request.setAttribute("originalURI", requestURI);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/framework-dispatch");
            dispatcher.forward(request, response);
        }
    }
}