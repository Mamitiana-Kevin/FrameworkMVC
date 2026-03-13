package framework.servlet;

import framework.core.AnnotationReader;
import framework.utils.ModelAndView;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import framework.annotation.Json;
import framework.utils.JsonUtil;

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
            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Class<?> type = parameters[i].getType();

                if (type.equals(HttpServletRequest.class)) {
                    args[i] = req;

                } else if (type.equals(HttpServletResponse.class)) {
                    args[i] = resp;

                } else if (Map.class.isAssignableFrom(type)) {
                    // Sprint 8 - Map
                    Map<String, Object> formData = new HashMap<>();
                    for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
                        String[] values = entry.getValue();
                        formData.put(entry.getKey(), values.length == 1 ? values[0] : values);
                    }
                    args[i] = formData;

                } else if (type.equals(String.class)) {
                    args[i] = req.getParameter(parameters[i].getName());

                } else {
                    // ========== SPRINT 8 bis : Binding d'objet ==========
                    args[i] = bindObject(type, req, "");
                }
            }
            // ================================================================

            Object result = method.invoke(handler.instance, args);

            // ==================== TRAITEMENT DU RÉSULTAT ====================
            if (method.isAnnotationPresent(Json.class) && !(result instanceof ModelAndView)) {
                writeJsonResponse(resp, result);
                return;
            }
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
                req.getRequestDispatcher(jspPath).forward(req, resp);

            } else if (result instanceof String str && !str.trim().isEmpty()) {
                resp.setContentType("text/html");
                resp.getWriter().write("<h2>" + str + "</h2>");

            } else {
                String controller = handler.instance.getClass().getSimpleName();
                String methodName = method.getName();
                resp.getWriter().write("controller " + controller + " method " + methodName);
            }

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("<h1>500 - Error: " + e.getMessage() + "</h1>");
            e.printStackTrace();
        }
    }

    private void writeJsonResponse(HttpServletResponse resp, Object result) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        Object payload;

        if (result instanceof Collection<?> collection) {
            Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put("count", collection.size());
            wrapper.put("data", collection);
            payload = wrapper;

        } else if (result != null && result.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(result);
            Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put("count", length);
            wrapper.put("data", result);
            payload = wrapper;

        } else {
            Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put("status", "success");
            wrapper.put("code", 200);
            wrapper.put("data", result);
            wrapper.put("message", null);
            payload = wrapper;
        }

        resp.setStatus(200);
        resp.getWriter().write(JsonUtil.toJson(payload));
    }

    /**
     * Crée une instance de l'objet et remplit ses champs à partir du formulaire.
     * Supporte les objets imbriqués (ex: adresse.ville)
     */
    private Object bindObject(Class<?> clazz, HttpServletRequest req, String prefix) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            String paramName = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;

            Class<?> fieldType = field.getType();

            // Cas 1 : Type simple (String, int, double, boolean...)
            if (isSimpleType(fieldType)) {
                String value = req.getParameter(paramName);
                if (value != null && !value.isEmpty()) {
                    field.set(instance, convert(value, fieldType));
                }
            }
            // Cas 2 : Objet imbriqué (ex: Adresse)
            else if (!fieldType.isPrimitive() && !fieldType.getName().startsWith("java.")) {
                Object nested = bindObject(fieldType, req, paramName);
                field.set(instance, nested);
            }
            // (Liste plus tard si besoin)
        }

        return instance;
    }

    private boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == double.class || type == Double.class
                || type == boolean.class || type == Boolean.class
                || type == float.class || type == Float.class;
    }

    private Object convert(String value, Class<?> targetType) {
        if (targetType == String.class) return value;
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
        if (targetType == float.class || targetType == Float.class) return Float.parseFloat(value);
        return value;
    }
}