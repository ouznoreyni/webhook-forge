package sn.noreyni.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;


@QuarkusMain
public class Application implements QuarkusApplication {

    private static final Logger logger = Logger.getLogger(Application.class);

    static {
        // Load .env before anything else - even before logging is fully configured
        loadEnvironmentVariables();
    }

    /**
     * Load environment variables from .env file before application startup
     */
    private static void loadEnvironmentVariables() {
        try {
            // Using System.out here since logger might not be initialized yet
            System.out.println("Loading .env file before application startup...");

            Dotenv dotenv = Dotenv.configure()
                    .filename(".env")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            int loadedCount = 0;
            for (var entry : dotenv.entries()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // Only set if not already defined (allows overriding via system properties)
                if (System.getProperty(key) == null) {
                    System.setProperty(key, value);
                    loadedCount++;
                }
            }

            System.out.println("Successfully loaded " + loadedCount + " environment variables from .env file");

        } catch (Exception e) {
            System.err.println("Warning: Could not load .env file: " + e.getMessage());
            // Don't fail startup - just log the warning
        }
    }

    @Override
    public int run(String... args) throws Exception {
        logger.info("Starting Webhook Service Application...");

        // Log some startup information
        //logger.info("Java version: {}", System.getProperty("java.version"));
        //logger.info("Operating system: {}", System.getProperty("os.name"));

        // Keep the application running
        Quarkus.waitForExit();

        logger.info("Webhook Service Application shutting down...");
        return 0;
    }

    /**
     * Main method - entry point for the application
     */
    public static void main(String... args) {
        Quarkus.run(Application.class, args);
    }
}