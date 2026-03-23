package framework.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FileUploadUtils {

    /**
     * Récupère tous les fichiers uploadés.
     * Clé   = name de l'input (ex: "photo")
     * Valeur = contenu du fichier en byte[]
     */
    public static Map<String, byte[]> getUploadedFiles(HttpServletRequest request) {
        Map<String, byte[]> files = new HashMap<>();

        try {
            Collection<Part> parts = request.getParts();

            for (Part part : parts) {
                // On ignore les champs texte
                if (part.getSubmittedFileName() == null || part.getSubmittedFileName().isEmpty()) {
                    continue;
                }

                String fieldName = part.getName(); // name="photo"
                byte[] content = toByteArray(part);
                files.put(fieldName, content);
            }

        } catch (Exception e) {
            System.err.println("[FileUpload] Erreur : " + e.getMessage());
        }

        return files;
    }

    private static byte[] toByteArray(Part part) throws Exception {
        try (InputStream is = part.getInputStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] data = new byte[8192];
            int n;
            while ((n = is.read(data)) != -1) {
                buffer.write(data, 0, n);
            }
            return buffer.toByteArray();
        }
    }
}