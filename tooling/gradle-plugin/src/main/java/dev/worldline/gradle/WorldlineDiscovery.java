package dev.worldline.gradle;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

/** Conservative product discovery for an isolated tests/worldline build. */
final class WorldlineDiscovery {
    private WorldlineDiscovery() {}
    static void apply(Project project, WorldlineExtension extension) {
        Path repository = repository(project.getProjectDir().toPath());
        if (extension.getProductClasspath().isEmpty()) {
            addDirectory(extension, repository.resolve("build/classes/java/main"));
            addDirectory(extension, repository.resolve("build/resources/main"));
            addDirectory(extension, repository.resolve(".betaenergistics/build/product/classes"));
            addDirectory(extension, repository.resolve(".butter/build/classes/main"));
        }
        if (extension.getModFiles().isEmpty()) {
            List<Path> jars = new ArrayList<>();
            collect(jars, repository.resolve("build/libs"));
            collect(jars, repository.resolve(".betaenergistics/build/product"));
            collect(jars, repository.resolve(".butter/build/libs"));
            if (jars.size() == 1) extension.getModFiles().from(jars.get(0).toFile());
            else if (jars.size() > 1) throw new GradleException("multiple mod JARs detected; set worldline.modFiles explicitly: " + jars);
        }
    }
    private static Path repository(Path project) {
        Path absolute = project.toAbsolutePath().normalize();
        Path parent = absolute.getParent();
        if (absolute.endsWith(Path.of("tests", "worldline")) && parent != null && parent.getParent() != null)
            return parent.getParent();
        return absolute;
    }
    private static void addDirectory(WorldlineExtension extension, Path path) {
        if (Files.isDirectory(path)) extension.getProductClasspath().from(path.toFile());
    }
    private static void collect(List<Path> result, Path directory) {
        if (!Files.isDirectory(directory)) return;
        try (Stream<Path> paths = Files.list(directory)) {
            paths.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted().forEach(result::add);
        } catch (java.io.IOException error) { throw new GradleException("cannot inspect " + directory, error); }
    }
}
