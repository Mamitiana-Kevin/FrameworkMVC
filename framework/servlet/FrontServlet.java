package framework.servlet;

import framework.core.AnnotationReader;
import framework.utils.ModelAndView;
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

            // Smart parameter injection
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> type = paramTypes[i];
                if (type.equals(HttpServletRequest.class)) args[i] = req;
                else if (type.equals(HttpServletResponse.class)) args[i] = resp;
                else args[i] = null;
            }

            Object result = method.invoke(handler.instance, args);

            // HANDLE RETURN TYPE
            if (result instanceof ModelAndView) {
                ModelAndView mv = (ModelAndView) result;
                String jspPath = mv.getView();

                // Auto prefix with /views/ if not absolute
                if (!jspPath.startsWith("/")) {
                    jspPath = "/views/" + jspPath;
                }
                if (!jspPath.endsWith(".jsp")) {
                    jspPath += ".jsp";
                }

                // Add model to request
                mv.getModel().forEach(req::setAttribute);

                // Forward to JSP
                RequestDispatcher rd = req.getRequestDispatcher(jspPath);
                rd.forward(req, resp);

            } else if (result instanceof String str && !str.trim().isEmpty()) {
                resp.setContentType("text/html");
                resp.getWriter().write("<h2>" + str + "</h2>");

            } else {
                String controller = handler.instance.getClass().getSimpleName();
                String methodName = handler.method.getName();
                resp.getWriter().write(
                    "controller " + controller +
                    " method " + methodName 
                );
            }

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("<h3>500 - Error: " + e.getMessage() + "</h3>");
            e.printStackTrace();
        }
    }
}