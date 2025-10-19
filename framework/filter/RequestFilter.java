package framework.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class RequestFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String relativePath = uri.substring(contextPath.length());

        if (relativePath.isEmpty()) relativePath = "/";

        // Chemin réel sur le disque
        String realPath = req.getServletContext().getRealPath(relativePath);

        // Si le fichier existe et n’est pas un dossier → laisser Tomcat le servir
        if (realPath != null) {
            File f = new File(realPath);
            if (f.exists() && f.isFile()) {
                chain.doFilter(request, response); // sert le fichier statique
                return;
            }
        }

        // Sinon, passe au FrontServlet pour traitement
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
