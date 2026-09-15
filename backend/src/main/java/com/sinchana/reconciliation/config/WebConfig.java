package com.sinchana.reconciliation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.stream.Stream;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String configuredOrigins;

    public WebConfig(@Value("${app.cors.allowed-origins:}") String configuredOrigins) {
        this.configuredOrigins = configuredOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = Stream.concat(
                        Stream.of("http://localhost:5173"),
                        Arrays.stream(configuredOrigins.split(","))
                                .map(String::trim)
                                .filter(origin -> !origin.isBlank()))
                .toArray(String[]::new);

        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "OPTIONS");
    }
}
