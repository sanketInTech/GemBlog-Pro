package com.gemblogpro.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gemblogpro.config.ImageKitConfig;
import com.gemblogpro.exception.ExternalServiceException;
import com.gemblogpro.util.ImageUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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

    private final RestTemplate restTemplate;
    private final ImageKitConfig imageKitConfig;

    public ImageKitService(RestTemplate restTemplate, ImageKitConfig imageKitConfig) {
        this.restTemplate = restTemplate;
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

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBasicAuth(imageKitConfig.getPrivateKey(), "");

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", base64File);
            body.add("fileName", imageFile.getOriginalFilename());
            body.add("folder", BLOG_FOLDER);

            log.debug("Uploading image '{}' ({} bytes) to ImageKit folder {}",
                    imageFile.getOriginalFilename(), imageFile.getSize(), BLOG_FOLDER);

            ResponseEntity<ImageKitUploadResponse> response = restTemplate.postForEntity(
                    UPLOAD_ENDPOINT, new HttpEntity<>(body, headers), ImageKitUploadResponse.class);

            ImageKitUploadResponse uploadResponse = response.getBody();
            if (uploadResponse == null || uploadResponse.getFilePath() == null) {
                log.error("ImageKit upload returned an unexpected response body: {}", uploadResponse);
                throw new ExternalServiceException("ImageKit upload returned an unexpected response");
            }

            String url = ImageUrlBuilder.buildOptimizedUrl(imageKitConfig.getUrlEndpoint(), uploadResponse.getFilePath());
            log.info("Image uploaded to ImageKit: {}", uploadResponse.getFilePath());
            return url;

        } catch (IOException ex) {
            log.error("Unable to read the uploaded image file", ex);
            throw new ExternalServiceException("Unable to read the uploaded image file: " + ex.getMessage());
        } catch (RestClientException ex) {
            log.error("ImageKit upload call failed", ex);
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
