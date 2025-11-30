package com.example.talkingCanvas.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Database configuration for production environments
 * Handles DATABASE_URL from platforms like Render, Railway, Heroku
 * which provide URLs in format: postgresql://user:password@host:port/database
 */
@Configuration
@Profile("prod")
public class DatabaseConfig {

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource dataSource() {
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set");
        }

        try {
            // Parse the DATABASE_URL
            URI dbUri = new URI(databaseUrl);

            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];

            // Get port, default to 5432 if not specified
            int port = dbUri.getPort();
            if (port == -1) {
                port = 5432; // PostgreSQL default port
            }

            // Construct JDBC URL from the postgresql:// format
            String jdbcUrl = "jdbc:postgresql://" +
                    dbUri.getHost() +
                    ":" + port +
                    dbUri.getPath();

            // Add SSL parameters for Render PostgreSQL
            jdbcUrl += "?sslmode=require";

            // Build the datasource
            HikariDataSource dataSource = DataSourceBuilder
                    .create()
                    .type(HikariDataSource.class)
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();

            // Configure HikariCP connection pool
            dataSource.setMaximumPoolSize(10);
            dataSource.setMinimumIdle(2);
            dataSource.setConnectionTimeout(30000);
            dataSource.setIdleTimeout(600000);
            dataSource.setMaxLifetime(1800000);

            return dataSource;

        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid DATABASE_URL format: " + databaseUrl, e);
        }
    }
}
