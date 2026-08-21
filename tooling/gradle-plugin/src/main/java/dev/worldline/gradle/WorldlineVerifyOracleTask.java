package dev.worldline.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/** Verifies configured oracle identity without starting a game process. */
public abstract class WorldlineVerifyOracleTask extends DefaultTask {
    @TaskAction public void verify() {
        WorldlineExtension extension = getProject().getExtensions().getByType(WorldlineExtension.class);
        OraclePaths paths = OraclePaths.resolve(getProject(), extension);
        OracleVerifier.verify(paths.client, OraclePaths.CLIENT_BYTES, OraclePaths.CLIENT_SHA256);
        if (paths.serverPresent()) OracleVerifier.verify(
                paths.server, OraclePaths.SERVER_BYTES, OraclePaths.SERVER_SHA256);
        getLogger().lifecycle("WORLDLINE_ORACLE=PASS client={} server={}", paths.client,
                paths.serverPresent() ? paths.server : "absent");
    }
}
