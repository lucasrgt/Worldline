package dev.worldline.gradle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class WorldlinePluginFunctionalTest {
    @TempDir Path project;
    @Test void configuresAnIsolatedConsumerBuild() throws Exception {
        Files.writeString(project.resolve("settings.gradle.kts"), "rootProject.name = \"consumer\"\n");
        Files.writeString(project.resolve("build.gradle.kts"),
                "plugins { id(\"io.github.lucasrgt.worldline.test\") }\n"
                + "worldline { noRuntime.set(true) }\n");
        Files.createDirectories(project.resolve("src/test/java"));
        BuildResult result = GradleRunner.create().withProjectDir(project.toFile())
                .withPluginClasspath().withArguments("tasks", "--all", "--stacktrace").build();
        assertTrue(result.getOutput().contains("worldlineTest"));
        assertTrue(result.getOutput().contains("worldlineDoctor"));
    }
}
