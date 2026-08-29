import java.util.LinkedHashMap;
import java.util.Map;

/** Exact mapped-source wrappers for the Beta 1.7.3 legacy profiler driver. */
final class LegacyProfilerSourceTransform {
    static final String MINECRAFT = "net/minecraft/client/Minecraft.java";
    static final String ENTITY_RENDERER = "net/minecraft/src/EntityRenderer.java";
    static final String RENDER_GLOBAL = "net/minecraft/src/RenderGlobal.java";
    static final String WORLD_RENDERER = "net/minecraft/src/WorldRenderer.java";
    static final String[] FILES = {MINECRAFT, ENTITY_RENDERER, RENDER_GLOBAL, WORLD_RENDERER};
    private static final String HOOK = "worldline.modloader.profiler.ModLoaderProfilerHooks";

    private LegacyProfilerSourceTransform() { }

    static Map<String, String> transform(Map<String, String> sources) {
        require(!instrumented(sources), "legacy profiler sources are already instrumented");
        Map<String, String> result = new LinkedHashMap<>();
        result.put(MINECRAFT, minecraft(required(sources, MINECRAFT)));
        result.put(ENTITY_RENDERER, entityRenderer(required(sources, ENTITY_RENDERER)));
        result.put(RENDER_GLOBAL, renderGlobal(required(sources, RENDER_GLOBAL)));
        result.put(WORLD_RENDERER, worldRenderer(required(sources, WORLD_RENDERER)));
        validateInstalled(result); return result;
    }

    static boolean instrumented(Map<String, String> sources) {
        int count = 0;
        for (String file : FILES) if (required(sources, file).contains(HOOK)) count++;
        require(count == 0 || count == FILES.length,
                "partial legacy profiler instrumentation detected");
        return count == FILES.length;
    }

    static void validateInstalled(Map<String, String> sources) {
        String minecraft = required(sources, MINECRAFT);
        require(minecraft.contains("private void worldline$runTick()")
                && minecraft.contains("private void worldline$displayUpdate()")
                && minecraft.contains("finish(\"client-shutdown\")"),
                "Minecraft profiler wrappers drifted");
        String renderer = required(sources, ENTITY_RENDERER);
        require(renderer.contains("private void worldline$updateCameraAndRender(float var1)")
                && renderer.contains("private void worldline$renderWorld(float var1, long var2)"),
                "EntityRenderer profiler wrappers drifted");
        require(required(sources, RENDER_GLOBAL).contains(
                "private boolean worldline$updateRenderers(EntityLiving var1, boolean var2)"),
                "RenderGlobal profiler wrapper drifted");
        require(required(sources, WORLD_RENDERER).contains(
                "private void worldline$updateRenderer()"),
                "WorldRenderer profiler wrapper drifted");
    }

    private static String minecraft(String source) {
        String n = newline(source);
        source = replace(source, lines(n, "\tpublic void shutdownMinecraftApplet() {\n"),
                lines(n, "\tpublic void shutdownMinecraftApplet() {\n"
                        + "\t\t" + HOOK + ".finish(\"client-shutdown\");\n"), "shutdown");
        source = replace(source, lines(n, "\tpublic void runTick() {\n"), lines(n,
                "\tpublic void runTick() {\n"
                + "\t\tlong worldlineStarted = " + HOOK + ".tickBegin();\n"
                + "\t\ttry {\n\t\t\tthis.worldline$runTick();\n\t\t} finally {\n"
                + "\t\t\t" + HOOK + ".tickEnd(worldlineStarted);\n\t\t}\n\t}\n\n"
                + "\tprivate void worldline$runTick() {\n"), "runTick");
        source = replaceCount(source, lines(n, "\t\t\t\t\t\tDisplay.update();\n"),
                lines(n, "\t\t\t\t\t\tthis.worldline$displayUpdate();\n"), 2, "display");
        return replace(source, lines(n, "\tprivate void resize(int var1, int var2) {\n"), lines(n,
                "\tprivate void worldline$displayUpdate() {\n"
                + "\t\tlong worldlineStarted = " + HOOK + ".displayBegin();\n"
                + "\t\ttry {\n\t\t\tDisplay.update();\n\t\t} finally {\n"
                + "\t\t\t" + HOOK + ".displayEnd(worldlineStarted);\n\t\t}\n\t}\n\n"
                + "\tprivate void resize(int var1, int var2) {\n"), "display-helper");
    }

    private static String entityRenderer(String source) {
        String n = newline(source);
        source = replace(source, lines(n, "\tpublic void updateCameraAndRender(float var1) {\n"),
                lines(n, "\tpublic void updateCameraAndRender(float var1) {\n"
                + "\t\t" + HOOK + ".frameBegin();\n\t\ttry {\n"
                + "\t\t\tthis.worldline$updateCameraAndRender(var1);\n\t\t} finally {\n"
                + "\t\t\t" + HOOK + ".frameEnd();\n\t\t}\n\t}\n\n"
                + "\tprivate void worldline$updateCameraAndRender(float var1) {\n"), "frame");
        return replace(source, lines(n, "\tpublic void renderWorld(float var1, long var2) {\n"),
                lines(n, "\tpublic void renderWorld(float var1, long var2) {\n"
                + "\t\tlong worldlineStarted = " + HOOK + ".worldBegin();\n\t\ttry {\n"
                + "\t\t\tthis.worldline$renderWorld(var1, var2);\n\t\t} finally {\n"
                + "\t\t\t" + HOOK + ".worldEnd(worldlineStarted);\n\t\t}\n\t}\n\n"
                + "\tprivate void worldline$renderWorld(float var1, long var2) {\n"), "world");
    }

    private static String renderGlobal(String source) {
        String n = newline(source);
        return replace(source, lines(n,
                "\tpublic boolean updateRenderers(EntityLiving var1, boolean var2) {\n"), lines(n,
                "\tpublic boolean updateRenderers(EntityLiving var1, boolean var2) {\n"
                + "\t\tlong worldlineStarted = " + HOOK
                + ".compileBegin(this.worldRenderersToUpdate.size());\n\t\ttry {\n"
                + "\t\t\treturn this.worldline$updateRenderers(var1, var2);\n\t\t} finally {\n"
                + "\t\t\t" + HOOK + ".compileEnd(worldlineStarted);\n\t\t}\n\t}\n\n"
                + "\tprivate boolean worldline$updateRenderers(EntityLiving var1, boolean var2) {\n"),
                "compile");
    }

    private static String worldRenderer(String source) {
        String n = newline(source);
        return replace(source, lines(n, "\tpublic void updateRenderer() {\n"), lines(n,
                "\tpublic void updateRenderer() {\n"
                + "\t\tlong worldlineStarted = " + HOOK + ".rebuildBegin();\n\t\ttry {\n"
                + "\t\t\tthis.worldline$updateRenderer();\n\t\t} finally {\n"
                + "\t\t\t" + HOOK + ".rebuildEnd(worldlineStarted);\n\t\t}\n\t}\n\n"
                + "\tprivate void worldline$updateRenderer() {\n"), "rebuild");
    }

    private static String replace(String source, String anchor, String replacement, String label) {
        int first = source.indexOf(anchor);
        require(first >= 0 && source.indexOf(anchor, first + anchor.length()) < 0,
                "expected exactly one " + label + " anchor");
        return source.substring(0, first) + replacement + source.substring(first + anchor.length());
    }
    private static String replaceCount(String source, String anchor, String replacement,
            int expected, String label) {
        int count = 0, offset = 0;
        while ((offset = source.indexOf(anchor, offset)) >= 0) { count++; offset += anchor.length(); }
        require(count == expected, "expected " + expected + " " + label + " anchors");
        return source.replace(anchor, replacement);
    }
    private static String newline(String source) { return source.contains("\r\n") ? "\r\n" : "\n"; }
    private static String lines(String newline, String value) { return value.replace("\n", newline); }
    private static String required(Map<String, String> sources, String name) {
        String value = sources.get(name); require(value != null, "missing mapped source " + name); return value;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
