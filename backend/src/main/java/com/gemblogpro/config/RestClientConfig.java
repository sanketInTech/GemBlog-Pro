package com.gemblogpro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Provides a single shared {@link RestTemplate} bean used by
 * {@code ImageKitService} and {@code GeminiContentService} to call the
 * ImageKit Upload API and the Gemini REST API respectively.
 * <p>
 * The architecture document explicitly allows either an official Java SDK
 * or a {@code RestTemplate}/{@code WebClient} REST call for both
 * integrations. A REST client was chosen here over pulling in the ImageKit
 * and Google Gen AI Java SDKs, since {@code spring-boot-starter-web}
 * already provides everything needed - no new Maven dependency, no
 * third-party SDK version to track, and no risk of an unverifiable SDK
 * coordinate breaking the build.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
