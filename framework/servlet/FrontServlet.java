package framework.servlet;

import framework.core.AnnotationReader;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.Method;

public class FrontServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            // Scan the test controllers package
            AnnotationReader.scanPackage("test.controllers");
        } catch (Exception e) {
            throw new ServletException("Failed to scan controllers", e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String urlPath = req.getRequestURI().substring(req.getContextPath().length());
        String httpMethod = req.getMethod();

        System.out.println("Request: " + httpMethod + " " + urlPath);

        AnnotationReader.MethodHandler handler = AnnotationReader.getHandler(urlPath, httpMethod);

        if (handler == null) {
            resp.setStatus(404);
            resp.getWriter().write("<h1>404 - Not Found: " + urlPath + "</h1>");
            return;
        }

        try {
            // Invoke the controller method (assume it returns String for simplicity)
            Method method = handler.method;
            method.setAccessible(true);
            Object result = method.invoke(handler.instance, req, resp);

            if (result instanceof String) {
                resp.getWriter().write((String) result);
            }
        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("<h1>500 - Error: " + e.getMessage() + "</h1>");
            e.printStackTrace();
        }
    }
}