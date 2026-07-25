package com.gemblogpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 * <p>
 * Replaces {@code server.js}: where the Express app manually called
 * {@code connectDB()} and {@code app.listen(PORT, ...)}, Spring Boot's
 * auto-configuration wires up the MySQL {@code DataSource}, Hibernate
 * {@code EntityManagerFactory}, and the embedded servlet container from the
 * properties declared in {@code application.yml}.
 */
@SpringBootApplication
public class GemBlogProApplication {

    public static void main(String[] args) {
        SpringApplication.run(GemBlogProApplication.class, args);
    }

}
