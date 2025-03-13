package org.example;

import com.sun.net.httpserver.HttpServer;
import org.example.Handler.*;
import org.example.S3Integration.S3Client;

import java.net.InetSocketAddress;

public class ImageGalleryApp {

    // Configuration properties with default values
    private static final int DEFAULT_PORT = 5000;

    public static void main(String[] args) throws Exception {
        // Use port 5000 as default
        String portEnv = System.getenv("PORT");
        int serverPort = (portEnv != null && !portEnv.isEmpty()) ? Integer.parseInt(portEnv) : DEFAULT_PORT;

        // Initialize S3 client
        S3Client.initialize();

        // Create HTTP server listening on the specified port
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);

        // Register handlers for different routes
        server.createContext("/", new HomePageHandler());
        server.createContext("/upload", new UploadHandler());
        server.createContext("/images", new ImagesApiHandler());
        server.createContext("/delete", new DeleteImageHandler());
        server.createContext("/assets", new AssetHandler());

        // Start the server
        server.setExecutor(null);
        server.start();

        System.out.println("Image Gallery server started at http://localhost:" + serverPort + "/");
    }
}