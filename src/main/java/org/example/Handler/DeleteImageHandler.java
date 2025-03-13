package org.example.Handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.util.*;
import org.example.S3Integration.S3Client;
import org.example.Utility.HttpUtils;
import org.json.JSONObject;

// Handler for deleting images
public class DeleteImageHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            // Parse the request body
            String requestBody = HttpUtils.readRequestBody(exchange);
            Map<String, String> formData = HttpUtils.parseFormData(requestBody);

            String fileName = formData.get("fileName");

            if (fileName != null && !fileName.isEmpty()) {
                // Delete the image from S3
                S3Client.deleteImage(fileName);

                // Respond with success
                JSONObject response = new JSONObject();
                response.put("success", true);
                response.put("message", "Image deleted successfully");

                HttpUtils.sendJsonResponse(exchange, response.toString());
            } else {
                // Missing required parameters
                JSONObject response = new JSONObject();
                response.put("success", false);
                response.put("message", "Missing fileName");

                HttpUtils.sendJsonResponse(exchange, response.toString(), 400);
            }
        } else {
            HttpUtils.sendMethodNotAllowedResponse(exchange);
        }
    }
}

