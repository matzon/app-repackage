package dk.matzon.repackage.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "repackage", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PackageMojo extends AbstractMojo {
    public static final String MAVEN_PLUGIN_WORK_DIRECTORY = "maven-plugin-application-repackage";
    final Logger logger = LoggerFactory.getLogger(PackageMojo.class);

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession session;

    @Component
    private BuildPluginManager pluginManager;

    @Override
    public void execute() {
        logger.info("Executing mojo");

        executeSpringBootRepackage();
        executeAssembly();

        logger.info("Finished mojo");
    }

    private void executeAssembly() {
        try {
            // copy files before executing
            copyAssemblyResources();

            logger.info("Executing maven-assembly-plugin");
            executeMojo(
                    plugin(
                            groupId("org.apache.maven.plugins"),
                            artifactId("maven-assembly-plugin")
                    ),
                    goal("single"),
                    configuration(
                            element("appendAssemblyId", "false"),
                            element("descriptors",
                                    element("descriptor", "${project.build.directory}/" + MAVEN_PLUGIN_WORK_DIRECTORY + "/application-assembly.xml"))
                    ),
                    executionEnvironment(project, session, pluginManager)
            );
        } catch (MojoExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    private void copyAssemblyResources() {
        // copy assembly resources to a plugin-specific directory in the target folder
        try {
            Path directory = Files.createDirectories(Paths.get(project.getBuild().getDirectory(), MAVEN_PLUGIN_WORK_DIRECTORY));
            copyFile(directory, "Procfile");
            copyFile(directory, "run.sh");
            copyFile(directory, "application-assembly.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyFile(Path directory, String file) throws IOException {
        InputStream resourceToCopy = PackageMojo.class.getResourceAsStream("/assembly/" + file);
        if (resourceToCopy == null) {
            throw new RuntimeException("Unable to locate assembly resource: " + file);
        }
        Path target = Path.of(directory.toString(), file);
        Files.copy(resourceToCopy, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private void executeSpringBootRepackage() {

        try {
            logger.info("Executing spring-boot-maven-plugin, build-info");
            executeMojo(
                    plugin(
                            groupId("org.springframework.boot"),
                            artifactId("spring-boot-maven-plugin")
                    ),
                    goal("build-info"),
                    configuration(),
                    executionEnvironment(project, session, pluginManager)
            );
        } catch (MojoExecutionException e) {
            throw new RuntimeException(e);
        }

        try {
            logger.info("Executing spring-boot-maven-plugin, repackage");
            executeMojo(
                    plugin(
                            groupId("org.springframework.boot"),
                            artifactId("spring-boot-maven-plugin")
                    ),
                    goal("repackage"),
                    configuration(element("mainClass", "dk.matzon.repackage.application.Application")),
                    executionEnvironment(project, session, pluginManager)
            );
        } catch (MojoExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
