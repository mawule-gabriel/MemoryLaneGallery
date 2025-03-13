package org.example.Handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.ImageModel.S3Image;
import org.example.S3Integration.S3Client;
import org.example.Utility.HttpUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// Handler for API to get images
public class ImagesApiHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            // Parse query parameters
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = HttpUtils.parseQueryParams(query);

            // Get page parameter
            int page = 1;
            if (params.containsKey("page")) {
                try {
                    page = Integer.parseInt(params.get("page"));
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    // Use default page 1 if invalid
                }
            }

            // Get limit parameter
            int limit = 3; // Default to 3 images per page
            if (params.containsKey("limit")) {
                try {
                    limit = Integer.parseInt(params.get("limit"));
                    if (limit < 1) limit = 1;
                    if (limit > 50) limit = 50; // Set a reasonable maximum
                } catch (NumberFormatException e) {
                    // Use default limit if invalid
                }
            }

            // Get images for the requested page with the specified limit
            List<S3Image> images = S3Client.listImages(page, limit);

            // Prepare the JSON response
            JSONObject response = new JSONObject();
            response.put("page", page);
            response.put("totalPages", S3Client.getTotalPages(limit));
            response.put("limit", limit);

            JSONArray imagesArray = new JSONArray();
            for (S3Image image : images) {
                JSONObject imageJson = new JSONObject();
                imageJson.put("fileName", image.getFileName());
                imageJson.put("url", image.getUrl());
                imageJson.put("lastModified", image.getLastModified().toString());
                imagesArray.put(imageJson);
            }

            response.put("images", imagesArray);

            HttpUtils.sendJsonResponse(exchange, response.toString());
        } else {
            HttpUtils.sendMethodNotAllowedResponse(exchange);
        }
    }
}