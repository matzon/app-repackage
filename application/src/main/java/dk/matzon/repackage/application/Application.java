package dk.matzon.repackage.application;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

@SpringBootApplication
public class Application {

    public static final String BUILD_INFORMATION_RESOURCE_IDENTIFIER = "/META-INF/build-info.properties";
    public static final String TENANT_INFORMATION_RESOURCE_IDENTIFIER = "/META-INF/tenant-info.properties";

    public static void main(String[] args) {
        new SpringApplication(Application.class).run(args);
    }


    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> {
            Properties properties = loadInformation(BUILD_INFORMATION_RESOURCE_IDENTIFIER, TENANT_INFORMATION_RESOURCE_IDENTIFIER);
            String buildTime = properties.getProperty("build.time", "-");
            String tenant = properties.getProperty("tenant", "-");
            System.out.printf("Application Running for tenant: %s%nbuild at: %s%n", tenant, buildTime);
        };
    }

    private Properties loadInformation(String... resourceFiles) {
        Properties information = new Properties();

        for (String resourceFile : resourceFiles) {
            information.putAll(loadInformationFile(resourceFile));
        }

        return information;
    }

    private Properties loadInformationFile(String resourceFile) {
        Properties properties = new Properties();

        URL resource = Application.class.getResource(resourceFile);
        if (resource != null) {
            try (InputStream inputStream = resource.openConnection().getInputStream()) {
                properties.load(inputStream);
            } catch (IOException e) {
                /* ignored */
            }
        }

        return properties;
    }
}
