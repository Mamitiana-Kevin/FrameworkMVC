package framework.servlet;

import framework.core.AnnotationReader;
import framework.utils.ModelAndView;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FrontServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        try {
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

            // ==================== INJECTION DES PARAMÈTRES ====================
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] args = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> type = paramTypes[i];

                if (type.equals(HttpServletRequest.class)) {
                    args[i] = req;

                } else if (type.equals(HttpServletResponse.class)) {
                    args[i] = resp;

                } else if (Map.class.isAssignableFrom(type)) {   
                    Map<String, Object> formData = new HashMap<>();
                    Map<String, String[]> parameterMap = req.getParameterMap();

                    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                        String key = entry.getKey();
                        String[] values = entry.getValue();

                        if (values.length == 1) {
                            formData.put(key, values[0]);
                        } else {
                            formData.put(key, values); // plusieurs valeurs (checkbox, select multiple...)
                        }
                    }
                    args[i] = formData;

                } else if (type.equals(String.class)) {
                    String paramName = method.getParameters()[i].getName();
                    args[i] = req.getParameter(paramName);

                } else {
                    args[i] = null;
                }
            }
            // =================================================================

            Object result = method.invoke(handler.instance, args);

            // ==================== TRAITEMENT DU RÉSULTAT ====================
            if (result instanceof ModelAndView) {
                ModelAndView mv = (ModelAndView) result;
                String jspPath = mv.getView();

                if (!jspPath.startsWith("/")) {
                    jspPath = "/views/" + jspPath;
                }
                if (!jspPath.endsWith(".jsp")) {
                    jspPath += ".jsp";
                }

                mv.getModel().forEach(req::setAttribute);

                RequestDispatcher rd = req.getRequestDispatcher(jspPath);
                rd.forward(req, resp);

            } else if (result instanceof String str && !str.trim().isEmpty()) {
                resp.setContentType("text/html");
                resp.getWriter().write("<h2>" + str + "</h2>");

            } else {
                String controller = handler.instance.getClass().getSimpleName();
                String methodName = handler.method.getName();
                resp.getWriter().write("controller " + controller + " method " + methodName);
            }

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("<h1>500 - Error: " + e.getMessage() + "</h1>");
            e.printStackTrace();
        }
    }
}