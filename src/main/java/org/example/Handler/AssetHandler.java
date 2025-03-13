package org.example.Handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.Utility.HttpUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// Handler for serving static assets
public class AssetHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            String path = exchange.getRequestURI().getPath();

            // Remove the /assets prefix
            String assetPath = path.substring("/assets".length());
            if (assetPath.isEmpty()) {
                assetPath = "/";
            }

            // Determine content type based on file extension
            String contentType = "text/plain";
            if (assetPath.endsWith(".css")) {
                contentType = "text/css";
            } else if (assetPath.endsWith(".js")) {
                contentType = "text/javascript";
            } else if (assetPath.endsWith(".html")) {
                contentType = "text/html";
            } else if (assetPath.endsWith(".jpg") || assetPath.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (assetPath.endsWith(".png")) {
                contentType = "image/png";
            } else if (assetPath.endsWith(".svg")) {
                contentType = "image/svg+xml";
            }

            try {
                // Load the asset from the classpath
                InputStream inputStream = getClass().getResourceAsStream("/assets" + assetPath);
                if (inputStream == null) {
                    HttpUtils.sendNotFoundResponse(exchange);
                    return;
                }

                // Read the asset
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Send the response
                exchange.getResponseHeaders().set("Content-Type", contentType);
                byte[] responseBytes = outputStream.toByteArray();
                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
            } catch (Exception e) {
                System.err.println("Error serving asset: " + e.getMessage());
                HttpUtils.sendNotFoundResponse(exchange);
            }
        } else {
            HttpUtils.sendMethodNotAllowedResponse(exchange);
        }
    }
}
