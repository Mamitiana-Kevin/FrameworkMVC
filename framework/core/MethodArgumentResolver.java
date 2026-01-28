package framework.core;

import framework.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MethodArgumentResolver {

    /**
     * Résout les arguments d'une méthode contrôleur en fonction de la requête HTTP.
     * Supporte :
     * - Injection de HttpServletRequest et HttpServletResponse
     * - Annotation @RequestParam
     * - Binding par nom de paramètre (fallback)
     */
    public static Object[] resolveArguments(Method method, HttpServletRequest request, HttpServletResponse response) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> type = param.getType();

            // 1. Injection des objets système (Request/Response)
            if (type.equals(HttpServletRequest.class)) {
                args[i] = request;
                continue;
            }
            if (type.equals(HttpServletResponse.class)) {
                args[i] = response;
                continue;
            }

            // 2. Gestion de @RequestParam ou Binding par nom
            String paramName = param.getName();
            String value = null;
            boolean required = false;
            String defaultValue = null;

            if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = param.getAnnotation(RequestParam.class);
                paramName = requestParam.value();
                required = requestParam.required();
                defaultValue = requestParam.defaultValue();
                
                // Si defaultValue est une chaîne vide, on considère qu'il n'y en a pas
                if (defaultValue.equals("")) {
                    defaultValue = null;
                }
            }

            value = request.getParameter(paramName);

            // Gestion de la valeur par défaut si absent
            if (value == null && defaultValue != null) {
                value = defaultValue;
            }

            // Validation : paramètre requis manquant
            if (value == null && required) {
                throw new IllegalArgumentException("Missing required parameter: " + paramName);
            }
            
            // DEBUG LOG
            System.out.println("[MethodArgumentResolver] Param: " + paramName + " (" + type.getSimpleName() + ") => Value: " + value);

            args[i] = convert(value, type);
        }

        return args;
    }

    private static Object convert(String value, Class<?> targetType) {
        if (value == null) {
            // Valeurs par défaut pour les types primitifs pour éviter NullPointerException
            if (targetType == int.class) return 0;
            if (targetType == double.class) return 0.0;
            if (targetType == boolean.class) return false;
            return null;
        }

        try {
            if (targetType == String.class) {
                return value;
            } else if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(value);
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(value);
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(value);
            }
        } catch (NumberFormatException e) {
            System.err.println("Erreur de conversion pour le paramètre : " + value + " vers " + targetType.getName());
            // Retourner la valeur par défaut en cas d'erreur de format
            if (targetType == int.class) return 0;
            if (targetType == double.class) return 0.0;
            return null;
        }

        return null; // Type non supporté ou valeur nulle
    }
}
