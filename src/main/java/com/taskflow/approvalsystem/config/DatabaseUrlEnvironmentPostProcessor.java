package com.taskflow.approvalsystem.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts a DATABASE_URL in the format postgres://user:pass@host:port/db
 * into Spring datasource properties (spring.datasource.url, username, password)
 * so that platforms like Render that provide DATABASE_URL without the jdbc: prefix work out-of-the-box.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String DATABASE_URL = "DATABASE_URL";
    private static final String SPRING_DATASOURCE_URL = "spring.datasource.url";
    private static final String SPRING_DATASOURCE_USERNAME = "spring.datasource.username";
    private static final String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String dbUrl = environment.getProperty(DATABASE_URL);
        if (dbUrl == null || dbUrl.isBlank()) {
            return;
        }
        if (dbUrl.startsWith("jdbc")) {
            // Already a JDBC URL, map it directly if target property not set
            Map<String, Object> props = new HashMap<>();
            if (environment.getProperty(SPRING_DATASOURCE_URL) == null) {
                props.put(SPRING_DATASOURCE_URL, dbUrl);
            }
            environment.getPropertySources().addFirst(new MapPropertySource("converted-database-url", props));
            return;
        }

        try {
            URI uri = new URI(dbUrl);
            String scheme = uri.getScheme();
            String userInfo = uri.getUserInfo();
            String host = uri.getHost();
            int port = uri.getPort();
            String path = uri.getPath(); // includes leading '/'

            if (scheme == null || host == null) {
                return;
            }

            String jdbcScheme = "postgresql";
            if (scheme.equalsIgnoreCase("postgres") || scheme.equalsIgnoreCase("postgresql")) {
                jdbcScheme = "postgresql";
            } else if (scheme.equalsIgnoreCase("mysql")) {
                jdbcScheme = "mysql";
            } else {
                // unknown scheme - default to scheme as-is
                jdbcScheme = scheme;
            }

            StringBuilder jdbcUrl = new StringBuilder();
            jdbcUrl.append("jdbc:").append(jdbcScheme).append("://").append(host);
            if (port != -1) {
                jdbcUrl.append(":").append(port);
            }
            if (path != null && !path.isBlank()) {
                jdbcUrl.append(path);
            }

            String username = null;
            String password = null;
            if (userInfo != null) {
                String[] parts = userInfo.split(":", 2);
                username = parts[0];
                if (parts.length > 1) {
                    password = parts[1];
                }
            }

            Map<String, Object> props = new HashMap<>();
            // only set properties if they are not already provided
            if (environment.getProperty(SPRING_DATASOURCE_URL) == null) {
                props.put(SPRING_DATASOURCE_URL, jdbcUrl.toString());
            }
            if (username != null && environment.getProperty(SPRING_DATASOURCE_USERNAME) == null) {
                props.put(SPRING_DATASOURCE_USERNAME, username);
            }
            if (password != null && environment.getProperty(SPRING_DATASOURCE_PASSWORD) == null) {
                props.put(SPRING_DATASOURCE_PASSWORD, password);
            }

            if (!props.isEmpty()) {
                environment.getPropertySources().addFirst(new MapPropertySource("converted-database-url", props));
            }

        } catch (URISyntaxException e) {
            // ignore invalid DATABASE_URL
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

