package net.minecraft.src;

/** Minimal ModLoader lifecycle probe used only by the controlled qualification. */
public final class mod_WorldlineProfilerProbe extends BaseMod {
    private int ticks;

    public mod_WorldlineProfilerProbe() {
        System.out.println("WORLDLINE_LEGACY_LOADER_BOOT=modloader version=" + ModLoader.VERSION);
        ModLoader.SetInGUIHook(this, true, false);
    }

    public String Version() { return "worldline-profiler-qualification-v1"; }

    public boolean OnTickInGUI(net.minecraft.client.Minecraft client, GuiScreen screen) {
        if (++ticks < 20) return true;
        worldline.modloader.profiler.ModLoaderProfilerHooks.finish("qualification");
        System.out.println("WORLDLINE_LEGACY_LOADER_SHUTDOWN=modloader");
        client.shutdown();
        return false;
    }
}
