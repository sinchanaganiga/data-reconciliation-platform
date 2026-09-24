package com.sinchana.reconciliation.config;

import java.util.Arrays;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String configuredOrigins;

    public WebConfig(@Value("${app.cors.allowed-origins:}") String configuredOrigins) {
        this.configuredOrigins = configuredOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = Stream.concat(
                        Stream.of(
                                "http://localhost:5173",
                                "https://data-reconciliation-platform--sinchanaganiga.replit.app"
                        ),
                        Arrays.stream(configuredOrigins.split(","))
                                .map(String::trim)
                                .filter(origin -> !origin.isBlank()))
                .toArray(String[]::new);

        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "OPTIONS");
    }
}