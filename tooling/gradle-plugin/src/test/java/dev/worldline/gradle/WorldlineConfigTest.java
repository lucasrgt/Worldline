package dev.worldline.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

final class WorldlineConfigTest {
    @Test void parsesProfilesAndProjectValues() {
        WorldlineConfig config = WorldlineConfig.parse(List.of(
                "runtime = \"b1.7.3\"", "[profiles.local]", "clientJar = \"oracle.jar\""));
        assertEquals("b1.7.3", config.value("runtime"));
        assertEquals("oracle.jar", config.value("profiles.local.clientJar"));
    }
    @Test void rejectsDuplicateValues() {
        assertThrows(IllegalArgumentException.class,
                () -> WorldlineConfig.parse(List.of("runtime = \"a\"", "runtime = \"b\"")));
    }
}
