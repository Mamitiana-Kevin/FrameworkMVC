package framework.core;

import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.*;

public class AnnotationReader {

    private static final Map<String, MethodHandler> urlToHandler = new HashMap<>();

    public static class MethodHandler {
        public final Object instance;
        public final Method method;
        public final String httpMethod; // "GET" or "POST"

        public MethodHandler(Object instance, Method method, String httpMethod) {
            this.instance = instance;
            this.method = method;
            this.httpMethod = httpMethod;
        }
    }

    /**
     * Scan a package for @Controller classes and register @GetMapping / @PostMapping
     */
    public static void scanPackage(String packageName) throws Exception {
        // Use ClassLoader to find classes in the package
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        java.net.URL resource = classLoader.getResource(path);

        if (resource == null) {
            System.out.println("Package not found: " + packageName);
            return;
        }

        java.io.File dir = new java.io.File(resource.toURI());
        if (!dir.exists()) return;

        for (java.io.File file : dir.listFiles(f -> f.getName().endsWith(".class"))) {
            String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Controller.class)) {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                registerMappings(instance, clazz);
            }
        }
    }

    private static void registerMappings(Object instance, Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping ann = method.getAnnotation(GetMapping.class);
                String url = ann.value();
                urlToHandler.put(url + ":GET", new MethodHandler(instance, method, "GET"));
                System.out.println("[REGISTERED] GET " + url + " -> " + method.getName());
            }

            if (method.isAnnotationPresent(PostMapping.class)) {
                PostMapping ann = method.getAnnotation(PostMapping.class);
                String url = ann.value();
                urlToHandler.put(url + ":POST", new MethodHandler(instance, method, "POST"));
                System.out.println("[REGISTERED] POST " + url + " -> " + method.getName());
            }
        }
    }

    public static MethodHandler getHandler(String url, String httpMethod) {
        return urlToHandler.get(url + ":" + httpMethod);
    }
}