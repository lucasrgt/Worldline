package dev.worldline.gradle;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

/** Writes an explicit user profile from Gradle properties; never guesses paths. */
public abstract class WorldlineConfigureTask extends DefaultTask {
    @TaskAction public void configureProfile() {
        Object client = getProject().findProperty("worldlineClientJar");
        if (client == null) throw new GradleException(
                "use -PworldlineClientJar=PATH and optional -PworldlineServerJar=PATH");
        String profile = String.valueOf(getProject().findProperty("worldlineProfile"));
        if ("null".equals(profile)) profile = "b173-local";
        require(profile.matches("[A-Za-z0-9_.-]+"), "invalid profile name");
        Object server = getProject().findProperty("worldlineServerJar");
        Object retro = getProject().findProperty("worldlineRetroMcpRoot");
        Path target = Paths.get(System.getProperty("user.home"), ".worldline", "config.toml");
        try {
            Files.createDirectories(target.getParent());
            java.util.List<String> lines = new java.util.ArrayList<>(Arrays.asList(
                    "[profiles." + profile + "]", "clientJar = \"" + portable(client) + "\""));
            if (server != null) lines.add("serverJar = \"" + portable(server) + "\"");
            if (retro != null) lines.add("retroMcp = \"" + portable(retro) + "\"");
            String existing = Files.isRegularFile(target) ? Files.readString(target, StandardCharsets.UTF_8) : "";
            if (existing.contains("[profiles." + profile + "]"))
                throw new GradleException("refusing to overwrite existing profile " + profile);
            if (!existing.isEmpty() && !existing.endsWith("\n")) existing += "\n";
            String addition = String.join("\n", lines) + "\n";
            Files.writeString(target, existing + (existing.isEmpty() ? "" : "\n") + addition,
                    StandardCharsets.UTF_8);
        } catch (Exception error) { throw new GradleException("cannot write " + target, error); }
        getLogger().lifecycle("Worldline profile written: {}", target);
    }
    private static String portable(Object value) {
        return value == null ? "" : Paths.get(value.toString()).toAbsolutePath().normalize()
                .toString().replace('\\', '/').replace("\"", "");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new GradleException(message);
    }
}
