package org.example.Handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.ImageModel.S3Image;
import org.example.S3Integration.S3Client;
import org.example.Template.TemplateEngine;
import org.example.Utility.HttpUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// Home page handler - serves the main dashboard
public class HomePageHandler implements HttpHandler {
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

            // Get total pages
            int totalPages = S3Client.getTotalPages();

            // Get images for the current page
            List<S3Image> images = S3Client.listImages(page);

            // Render HTML response
            TemplateEngine.Context context = new TemplateEngine.Context();
            context.put("images", images);
            context.put("currentPage", page);
            context.put("totalPages", totalPages);

            String html = TemplateEngine.render("templates/home.html", context);
            HttpUtils.sendHtmlResponse(exchange, html);
        } else {
            HttpUtils.sendMethodNotAllowedResponse(exchange);
        }
    }
}
