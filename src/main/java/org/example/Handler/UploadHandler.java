package org.example.Handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.S3Integration.S3Client;
import org.example.Utility.HttpUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

// Handler for uploading images
public class UploadHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            // Parse the request body
            String requestBody = HttpUtils.readRequestBody(exchange);
            Map<String, String> formData = HttpUtils.parseFormData(requestBody);

            String base64Image = formData.get("image");
            String fileName = formData.get("fileName");

            if (base64Image != null && !base64Image.isEmpty() && fileName != null && !fileName.isEmpty()) {
                // Upload the image to S3
                String imageUrl = S3Client.uploadImage(base64Image, fileName);

                if (imageUrl != null) {
                    // Respond with success
                    JSONObject response = new JSONObject();
                    response.put("success", true);
                    response.put("url", imageUrl);
                    response.put("message", "Image uploaded successfully");

                    HttpUtils.sendJsonResponse(exchange, response.toString());
                } else {
                    // Respond with error
                    JSONObject response = new JSONObject();
                    response.put("success", false);
                    response.put("message", "Failed to upload image");

                    HttpUtils.sendJsonResponse(exchange, response.toString(), 500);
                }
            } else {
                // Missing required parameters
                JSONObject response = new JSONObject();
                response.put("success", false);
                response.put("message", "Missing image or fileName");

                HttpUtils.sendJsonResponse(exchange, response.toString(), 400);
            }
        } else {
            HttpUtils.sendMethodNotAllowedResponse(exchange);
        }
    }
}
