package com.example.talkingCanvas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @org.springframework.beans.factory.annotation.Value("${cors.allowed-origins}")
    private String allowedOrigins;

    // CORS is handled by SecurityConfig and CorsConfig
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // logic removed to avoid conflicts
    }
}
