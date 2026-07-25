package com.gemblogpro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gemblogpro.config.ImageKitConfig;
import com.gemblogpro.exception.ExternalServiceException;
import com.gemblogpro.util.ImageUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.io.IOException;
import java.util.Base64;

/**
 * Replaces the ImageKit upload block inside {@code blogController.js}'s
 * {@code addBlog}:
 * <pre>
 *   const base64File = imageFile.buffer.toString("base64");
 *   const response = await imagekit.files.upload({
 *     file: base64File, fileName: imageFile.originalname, folder: "/blog",
 *   });
 *   const optimizedImageURL = `${IMAGEKIT_URL_ENDPOINT}/${response.filePath}?tr=q-auto,f-webp,w-1280`;
 * </pre>
 * Uploads to ImageKit's REST Upload API directly (Basic Auth with the
 * private key as username, matching every official ImageKit SDK's
 * authentication scheme under the hood) rather than the Node SDK, per the
 * architecture document's explicit REST-client allowance.
 */
@Service
public class ImageKitService {

    private static final Logger log = LoggerFactory.getLogger(ImageKitService.class);

    private static final String UPLOAD_ENDPOINT = "https://upload.imagekit.io/api/v1/files/upload";
    private static final String BLOG_FOLDER = "/blog";

    private final ImageKitConfig imageKitConfig;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ImageKitService(ImageKitConfig imageKitConfig) {
        this.imageKitConfig = imageKitConfig;
    }


    /**
     * Uploads a blog thumbnail image to the {@code /blog} folder and
     * returns the final, transformation-optimized URL to store on the
     * {@code Blog} entity.
     */
    public String uploadBlogImage(MultipartFile imageFile) {
        try {
            String base64File = Base64.getEncoder().encodeToString(imageFile.getBytes());

            String boundary = "Boundary-" + UUID.randomUUID();

            String body =
                    "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"file\"\r\n\r\n" +
                            base64File + "\r\n" +

                            "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"fileName\"\r\n\r\n" +
                            imageFile.getOriginalFilename() + "\r\n" +

                            "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"folder\"\r\n\r\n" +
                            BLOG_FOLDER + "\r\n" +

                            "--" + boundary + "--\r\n";
            System.out.println("ImageKit endpoint: " + imageKitConfig.getUrlEndpoint());

            String key = imageKitConfig.getPrivateKey();
            System.out.println("Private key length: " + (key == null ? 0 : key.length()));
            System.out.println("Private key prefix: " +
                    (key == null ? "null" : key.substring(0, Math.min(12, key.length()))));

            String auth = Base64.getEncoder().encodeToString(
                    (imageKitConfig.getPrivateKey() + ":")
                            .getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(UPLOAD_ENDPOINT))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());
            System.out.println("Body: " + response.body());

            if (response.statusCode() != 200) {
                throw new ExternalServiceException("ImageKit upload failed: " + response.body());
            }

            ObjectMapper mapper = new ObjectMapper();
            ImageKitUploadResponse uploadResponse =
                    mapper.readValue(response.body(), ImageKitUploadResponse.class);

            if (uploadResponse.getFilePath() == null) {
                throw new ExternalServiceException("ImageKit did not return a file path.");
            }

            String imageUrl = ImageUrlBuilder.buildOptimizedUrl(
                    imageKitConfig.getUrlEndpoint(),
                    uploadResponse.getFilePath());

            return imageUrl;

        } catch (IOException | InterruptedException ex){
            log.error("Image upload failed", ex);
            throw new ExternalServiceException("Image upload failed: " + ex.getMessage());
        }
    }

    /** Minimal projection of ImageKit's upload response - only {@code filePath} is needed. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ImageKitUploadResponse {

        private String filePath;

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }
}
