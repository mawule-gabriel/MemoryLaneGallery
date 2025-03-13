package org.example.ImageModel;

import java.util.Date;

public class S3Image {
    private final String fileName;
    private final String url;
    private final Date lastModified;

    public S3Image(String fileName, String url, Date lastModified) {
        this.fileName = fileName;
        this.url = url;
        this.lastModified = lastModified;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public Date getLastModified() {
        return lastModified;
    }

    // Convert object to JSON string for API responses
    public String toJson() {
        return String.format("{\"fileName\":\"%s\",\"url\":\"%s\",\"lastModified\":\"%s\"}",
                fileName, url, lastModified.toString());
    }
}
