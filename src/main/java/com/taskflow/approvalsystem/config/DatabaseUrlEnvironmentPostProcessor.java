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
 * Converts common platform-provided DB URLs (DATABASE_URL, DB_URL, JDBC_DATABASE_URL)
 * into Spring Boot properties (spring.datasource.url, username, password) early in startup.
 * Also prints a small sanitized message to stdout so deploy logs show what was detected.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String SPRING_DATASOURCE_URL = "spring.datasource.url";
    private static final String SPRING_DATASOURCE_USERNAME = "spring.datasource.username";
    private static final String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Try common environment variables from platforms: DATABASE_URL, DB_URL, JDBC_DATABASE_URL
        String dbUrl = firstNonBlank(
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("DB_URL"),
                environment.getProperty("JDBC_DATABASE_URL"),
                environment.getProperty(SPRING_DATASOURCE_URL)
        );

        // If there's already a fully-qualified JDBC URL configured, nothing to do.
        String existing = environment.getProperty(SPRING_DATASOURCE_URL);
        if (existing != null && existing.startsWith("jdbc")) {
            System.out.println("[DB-POST] spring.datasource.url already set and starts with jdbc; skipping conversion.");
            return;
        }

        if (dbUrl == null || dbUrl.isBlank()) {
            return;
        }

        if (dbUrl.startsWith("jdbc")) {
            // Already a JDBC URL — set it explicitly
            System.out.println("[DB-POST] Detected JDBC URL from environment; setting spring.datasource.url");
            Map<String, Object> props = new HashMap<>();
            props.put(SPRING_DATASOURCE_URL, dbUrl);
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
            // override existing non-jdbc url or set it
            props.put(SPRING_DATASOURCE_URL, jdbcUrl.toString());
            // set username/password only if not already present
            if ((environment.getProperty(SPRING_DATASOURCE_USERNAME) == null || environment.getProperty(SPRING_DATASOURCE_USERNAME).isBlank()) && username != null) {
                props.put(SPRING_DATASOURCE_USERNAME, username);
            }
            if ((environment.getProperty(SPRING_DATASOURCE_PASSWORD) == null || environment.getProperty(SPRING_DATASOURCE_PASSWORD).isBlank()) && password != null) {
                props.put(SPRING_DATASOURCE_PASSWORD, password);
            }

            // Log a sanitized message so deploy logs show what was detected (no password)
            try {
                String sanitized = dbUrl;
                if (sanitized != null && sanitized.contains("@")) {
                    int at = sanitized.indexOf('@');
                    sanitized = "[REDACTED_CREDENTIALS]@" + sanitized.substring(at + 1);
                }
                System.out.println("[DB-POST] Converted DB URL '" + sanitized + "' to JDBC URL '" + jdbcUrl + "' and will set spring.datasource.* properties (username set? " + (username!=null) + ")");
            } catch (Exception ignore) {
                // swallow any logging issues
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

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }
}

