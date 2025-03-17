package org.example.S3Integration;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.example.ImageModel.S3Image;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class S3Client {
    private static AmazonS3 s3Client;
    private static String bucketName;
    private static final int DEFAULT_IMAGES_PER_PAGE = 12;

    public static void initialize() {
        bucketName = "memorybucket10";

        // Default region - should be configurable in production
        Regions region = Regions.US_EAST_1;

        // Create S3 client using default credentials provider chain
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion(region)
                .build();

        // Create bucket if it doesn't exist
        if (!s3Client.doesBucketExistV2(bucketName)) {
            s3Client.createBucket(bucketName);
        }
    }

    public static String uploadImage(String base64Image, String fileName) {
        try {
            // Extract the actual base64 data
            String[] parts = base64Image.split(",");
            String imageData = (parts.length > 1) ? parts[1] : parts[0];

            // Decode base64 to binary
            byte[] imageBytes = Base64.getDecoder().decode(imageData);
            InputStream inputStream = new ByteArrayInputStream(imageBytes);

            // Set metadata including content type
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);

            // Try to determine content type from base64 prefix
            if (base64Image.contains("image/jpeg")) {
                metadata.setContentType("image/jpeg");
                if (!fileName.toLowerCase().endsWith(".jpg") && !fileName.toLowerCase().endsWith(".jpeg")) {
                    fileName += ".jpg";
                }
            } else if (base64Image.contains("image/png")) {
                metadata.setContentType("image/png");
                if (!fileName.toLowerCase().endsWith(".png")) {
                    fileName += ".png";
                }
            } else if (base64Image.contains("image/gif")) {
                metadata.setContentType("image/gif");
                if (!fileName.toLowerCase().endsWith(".gif")) {
                    fileName += ".gif";
                }
            } else {
                // Default to JPEG if content type not found
                metadata.setContentType("image/jpeg");
                if (!fileName.toLowerCase().endsWith(".jpg") && !fileName.toLowerCase().endsWith(".jpeg")) {
                    fileName += ".jpg";
                }
            }

            // Upload the file without ACL settings
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream, metadata);
            // ACL setting has been removed

            s3Client.putObject(putObjectRequest);

            // Return the URL to the uploaded file
            return s3Client.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            System.err.println("Error uploading image to S3: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteImage(String fileName) {
        try {
            s3Client.deleteObject(bucketName, fileName);
        } catch (Exception e) {
            System.err.println("Error deleting image from S3: " + e.getMessage());
        }
    }

    // Original method for backward compatibility
    public static List<S3Image> listImages(int page) {
        return listImages(page, DEFAULT_IMAGES_PER_PAGE);
    }

    //method with limit parameter
    public static List<S3Image> listImages(int page, int limit) {
        try {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucketName)
                    .withMaxKeys(1000);

            ListObjectsV2Result result = s3Client.listObjectsV2(request);
            List<S3ObjectSummary> objects = result.getObjectSummaries();

            // Filter only image files
            List<S3ObjectSummary> imageObjects = new ArrayList<>();
            for (S3ObjectSummary object : objects) {
                if (isImageFile(object.getKey())) {
                    imageObjects.add(object);
                }
            }

            List<S3Image> images = new ArrayList<>();

            // Calculate pagination
            int startIndex = (page - 1) * limit;
            int endIndex = Math.min(startIndex + limit, imageObjects.size());

            // Validate page bounds
            if (startIndex >= imageObjects.size()) {
                return images;
            }

            // Get images for the requested page
            for (int i = startIndex; i < endIndex; i++) {
                S3ObjectSummary object = imageObjects.get(i);
                String url = s3Client.getUrl(bucketName, object.getKey()).toString();
                images.add(new S3Image(object.getKey(), url, object.getLastModified()));
            }

            return images;
        } catch (Exception e) {
            System.err.println("Error listing images from S3: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Original method for backward compatibility
    public static int getTotalPages() {
        return getTotalPages(DEFAULT_IMAGES_PER_PAGE);
    }

    // New method with limit parameter
    public static int getTotalPages(int limit) {
        try {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucketName);

            ListObjectsV2Result result = s3Client.listObjectsV2(request);
            int totalImages = 0;

            // Count only image files
            for (S3ObjectSummary object : result.getObjectSummaries()) {
                if (isImageFile(object.getKey())) {
                    totalImages++;
                }
            }

            return (int) Math.ceil((double) totalImages / limit);
        } catch (Exception e) {
            System.err.println("Error getting total pages: " + e.getMessage());
            return 0;
        }
    }

    private static boolean isImageFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpg") ||
                lowerCaseName.endsWith(".jpeg") ||
                lowerCaseName.endsWith(".png") ||
                lowerCaseName.endsWith(".gif") ||
                lowerCaseName.endsWith(".bmp") ||
                lowerCaseName.endsWith(".webp");
    }
}