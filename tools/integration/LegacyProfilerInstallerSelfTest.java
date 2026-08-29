import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Synthetic install, idempotence, loader, and drift checks. */
final class LegacyProfilerInstallerSelfTest {
    private LegacyProfilerInstallerSelfTest() { }

    static void execute(Path repository) throws Exception {
        Path workspace = Files.createTempDirectory("worldline-legacy-profiler-");
        try {
            Path source = workspace.resolve("minecraft/src");
            fixture(source.resolve(LegacyProfilerSourceTransform.MINECRAFT), minecraft());
            fixture(source.resolve(LegacyProfilerSourceTransform.ENTITY_RENDERER), entityRenderer());
            fixture(source.resolve(LegacyProfilerSourceTransform.RENDER_GLOBAL), renderGlobal());
            fixture(source.resolve(LegacyProfilerSourceTransform.WORLD_RENDERER), worldRenderer());
            LegacyProfilerInstaller.Result ready = LegacyProfilerInstaller.execute(
                    repository, workspace, "modloader", false);
            require(!ready.installed() && !ready.changed(), "check mutated pristine workspace");
            LegacyProfilerInstaller.Result installed = LegacyProfilerInstaller.execute(
                    repository, workspace, "modloader", true);
            require(installed.installed() && installed.changed()
                    && Files.isRegularFile(workspace.resolve(
                            ".worldline-profiler/backup-v1/net/minecraft/client/Minecraft.java"))
                    && Files.isRegularFile(source.resolve(
                            "worldline/profiling/ClientProfilerRuntime.java")),
                    "legacy profiler installation drifted");
            LegacyProfilerInstaller.Result repeated = LegacyProfilerInstaller.execute(
                    repository, workspace, "modloader", true);
            require(repeated.installed() && !repeated.changed(), "installer is not idempotent");
            failure(() -> LegacyProfilerInstaller.execute(repository, workspace, "forge", false));
            Path renderer = source.resolve(LegacyProfilerSourceTransform.ENTITY_RENDERER);
            Files.writeString(renderer, Files.readString(renderer) + "// drift\n", StandardCharsets.UTF_8);
            failure(() -> LegacyProfilerInstaller.execute(repository, workspace, "modloader", false));
            System.out.println("LegacyProfilerInstaller self-test passed");
        } finally { SafeTreeDelete.delete(workspace); }
    }

    private static String minecraft() {
        return "public class Minecraft {\n"
                + "\tpublic void shutdownMinecraftApplet() {\n\t}\n"
                + "\tpublic void run() {\n"
                + "\t\t\t\t\tif(!Keyboard.isKeyDown(Keyboard.KEY_F7)) {\n"
                + "\t\t\t\t\t\tDisplay.update();\n\t\t\t\t\t}\n"
                + "\t\t\t\t\tif(Keyboard.isKeyDown(Keyboard.KEY_F7)) {\n"
                + "\t\t\t\t\t\tDisplay.update();\n\t\t\t\t\t}\n\t}\n"
                + "\tprivate void resize(int var1, int var2) {\n\t}\n"
                + "\tpublic void runTick() {\n\t}\n}\n";
    }
    private static String entityRenderer() {
        return "class EntityRenderer {\n"
                + "\tpublic void updateCameraAndRender(float var1) {\n\t}\n"
                + "\tpublic void renderWorld(float var1, long var2) {\n\t}\n}\n";
    }
    private static String renderGlobal() {
        return "class RenderGlobal { java.util.List worldRenderersToUpdate;\n"
                + "\tpublic boolean updateRenderers(EntityLiving var1, boolean var2) {\n"
                + "\t\treturn true;\n\t}\n}\n";
    }
    private static String worldRenderer() {
        return "class WorldRenderer {\n\tpublic void updateRenderer() {\n\t}\n}\n";
    }
    private static void fixture(Path path, String value) throws Exception {
        Files.createDirectories(path.getParent()); Files.writeString(path, value, StandardCharsets.UTF_8);
    }
    private static void failure(Checked action) {
        try { action.run(); throw new AssertionError("invalid legacy install was accepted"); }
        catch (Exception expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private interface Checked { void run() throws Exception; }
}
