package dk.matzon.repackage.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

@Mojo(name = "tenant-information", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class TenantMojo extends AbstractMojo {
    private static final String TENANT_FILE_LOCATION = "META-INF";
    private static final String TENANT_FILE_NAME = "tenant-info.properties";

    final Logger logger = LoggerFactory.getLogger(TenantMojo.class);

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "tenant", defaultValue = "generic")
    private String tenant;

    @Override
    public void execute() {
        logger.info("Executing mojo");

        createTenantInformation();

        logger.info("Finished mojo");
    }

    private void createTenantInformation() {
        // write tenant information
        Properties properties = new Properties();
        properties.setProperty("tenant", tenant);

        logger.info("Created properties with tenant: {}", tenant);

        // create folder for tenant file - in project output directory
        Path tenantFolderLocation = Paths.get(project.getBuild().getOutputDirectory(), TENANT_FILE_LOCATION);
        try {
            Files.createDirectories(tenantFolderLocation);
        } catch (IOException e) {
            logger.warn("Unable to create directory '{}' for tenant file: {}", tenantFolderLocation, e.getMessage());
            return;
        }

        Path tenantFile = Paths.get(tenantFolderLocation.toString(), TENANT_FILE_NAME);
        try (BufferedWriter writer = Files.newBufferedWriter(tenantFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            properties.store(writer, null);
        } catch (IOException f) {
            logger.warn("Unable to write tenant file '{}'", tenantFile, f);
        }
    }
}
