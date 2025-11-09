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

        String originalURI = (String) req.getAttribute("originalURI");
        String urlPath = originalURI != null ?
                originalURI.substring(req.getContextPath().length()) :
                req.getRequestURI().substring(req.getContextPath().length());

        String httpMethod = req.getMethod();
        System.out.println("Request: " + httpMethod + " " + urlPath);

        AnnotationReader.MethodHandler handler = AnnotationReader.getHandler(urlPath, httpMethod);

        if (handler == null) {
            resp.setStatus(404);
            resp.getWriter().write("<h1>404 - tsy hita lty ehhh!!!: " + urlPath + "</h1>");
            return;
        }

        try {
            Method method = handler.method;
            method.setAccessible(true);

            // SMART PARAMETER INJECTION
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] args = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> type = paramTypes[i];
                if (type.equals(HttpServletRequest.class)) {
                    args[i] = req;
                } else if (type.equals(HttpServletResponse.class)) {
                    args[i] = resp;
                } else {
                    args[i] = null; // for future: @RequestParam, etc.
                }
            }

            Object result = method.invoke(handler.instance, args);

            // AUTO RESPONSE
            String responseBody;
            if (result instanceof String && !((String) result).trim().isEmpty()) {
                responseBody = (String) result;
            } else {
                String controllerName = handler.instance.getClass().getSimpleName();
                String methodName = handler.method.getName();
                responseBody = "controller: " + controllerName + "==> method: " + methodName;
            }

            resp.setContentType("text/html");
            resp.getWriter().write("<h2>" + responseBody + "</h2>");

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("<h1>500 - Error: " + e.getMessage() + "</h1>");
            e.printStackTrace();
        }
    }
}