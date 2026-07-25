package com.gemblogpro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Replaces {@code config/imageKit.js}:
 * <pre>
 *   const imagekit = new ImageKit({
 *     publicKey: process.env.IMAGEKIT_PUBLIC_KEY,
 *     privateKey: process.env.IMAGEKIT_PRIVATE_KEY,
 *     urlEndpoint: process.env.IMAGEKIT_URL_ENDPOINT,
 *   });
 * </pre>
 * Since {@code ImageKitService} talks to ImageKit's REST API directly via
 * the shared {@code RestTemplate} (see {@link RestClientConfig}) rather
 * than the ImageKit Java SDK, this class's role is to be the equivalent
 * "client configuration" bean: it holds the three credentials/settings the
 * original SDK instance was constructed with, sourced from
 * {@code application.yml}, and hands them to {@code ImageKitService}.
 */
@Configuration
public class ImageKitConfig {

    @Value("${imagekit.public-key}")
    private String publicKey;

    @Value("${imagekit.private-key}")
    private String privateKey;

    @Value("${imagekit.url-endpoint}")
    private String urlEndpoint;

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getUrlEndpoint() {
        return urlEndpoint;
    }
}
