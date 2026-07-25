package com.gemblogpro.util;

/**
 * Builds the final, transformation-optimized ImageKit URL stored on a
 * {@code Blog}.
 * <p>
 * Replaces the manual template-literal construction in
 * {@code blogController.js}'s {@code addBlog}:
 * <pre>
 *   const transformationString = "tr=q-auto,f-webp,w-1280";
 *   const optimizedImageURL = `${process.env.IMAGEKIT_URL_ENDPOINT}/${response.filePath}?${transformationString}`;
 * </pre>
 * Preserved exactly as written, including the literal {@code "/"} joining
 * the endpoint and {@code filePath} even though ImageKit's {@code filePath}
 * already starts with its own leading {@code "/"} (producing a double
 * slash in the final URL). This is harmless - ImageKit's CDN normalizes it
 * - and matches the original app's output byte-for-byte rather than
 * "fixing" something that was never reported as broken.
 */
public final class ImageUrlBuilder {

    private static final String TRANSFORMATION = "tr=q-auto,f-webp,w-1280";

    private ImageUrlBuilder() {
    }

    public static String buildOptimizedUrl(String urlEndpoint, String filePath) {
        return urlEndpoint + "/" + filePath + "?" + TRANSFORMATION;
    }
}
