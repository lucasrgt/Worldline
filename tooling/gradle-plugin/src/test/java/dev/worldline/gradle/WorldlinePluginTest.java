package dev.worldline.gradle;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

final class WorldlinePluginTest {
    @Test void registersTheAdoptionSurface() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("dev.worldline.test");
        for (String task : new String[] {"worldlineTest", "worldlineTestList", "worldlineTestWatch",
                "worldlineTestInspect", "worldlineTestMinimize", "worldlineTestUpdateSnapshots",
                "worldlineDoctor", "worldlineVerifyOracle", "worldlineAcquireRuntime",
                "worldlineConfigure", "worldlineMappings"})
            assertNotNull(project.getTasks().findByName(task), task);
        assertNotNull(project.getExtensions().findByType(WorldlineExtension.class));
    }
}
