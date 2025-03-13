package org.example.Utility;

import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {
    // Read the request body as a string
    public static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            return body.toString();
        }
    }

    // Parse form data from request body
    public static Map<String, String> parseFormData(String formData) {
        Map<String, String> result = new HashMap<>();

        if (formData == null || formData.isEmpty()) {
            return result;
        }

        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                try {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    result.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Error decoding form data: " + e.getMessage());
                }
            }
        }

        return result;
    }

    // Parse query parameters from URL
    public static Map<String, String> parseQueryParams(String query) {
        Map<String, String> result = new HashMap<>();

        if (query == null || query.isEmpty()) {
            return result;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                try {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    result.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Error decoding query params: " + e.getMessage());
                }
            }
        }

        return result;
    }

    // Send an HTML response
    public static void sendHtmlResponse(HttpExchange exchange, String html) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    // Send a JSON response
    public static void sendJsonResponse(HttpExchange exchange, String json) throws IOException {
        sendJsonResponse(exchange, json, 200);
    }

    // Send a JSON response with a specific status code
    public static void sendJsonResponse(HttpExchange exchange, String json, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    // Send a redirect response
    public static void sendRedirectResponse(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(303, -1);
    }

    // Send a "not found" response
    public static void sendNotFoundResponse(HttpExchange exchange) throws IOException {
        String notFoundHtml = "<html><body><h1>404 Not Found</h1><p>The requested resource was not found.</p></body></html>";
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = notFoundHtml.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(404, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    // Send a "method not allowed" response
    public static void sendMethodNotAllowedResponse(HttpExchange exchange) throws IOException {
        String methodNotAllowedHtml = "<html><body><h1>405 Method Not Allowed</h1><p>The method specified in the request is not allowed for the resource identified by the request URI.</p></body></html>";
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = methodNotAllowedHtml.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(405, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}