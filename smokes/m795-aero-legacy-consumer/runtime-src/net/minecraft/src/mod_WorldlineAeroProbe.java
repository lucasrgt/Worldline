package net.minecraft.src;

import net.minecraft.client.Minecraft;
import worldline.m795.LegacyAeroScene;

/** Real loader consumer: renders Aero through native Tessellator and persists native NBT. */
public final class mod_WorldlineAeroProbe extends BaseMod {
  private boolean complete;
  public mod_WorldlineAeroProbe() {
    ModLoader.SetInGUIHook(this, true, false);
    System.out.println("WORLDLINE_AERO_LEGACY_BOOT=" + ModLoader.VERSION);
  }
  public String Version() {
    return "worldline-aero-consumer-v1";
  }
  public boolean OnTickInGUI(Minecraft client, GuiScreen screen) {
    if (complete)
      return false;
    complete = true;
    try {
      if ("forge".equals(System.getProperty("worldline.aero.legacy.loader"))) {
        Class<?> hooks = Class.forName("forge.ForgeHooks");
        if (hooks.getField("revisionVersion").getInt(null) != 6)
          throw new IllegalStateException("unexpected active Forge revision");
        System.out.println("WORLDLINE_AERO_FORGE_ACTIVE=1.0.6");
      }
      LegacyAeroScene.execute();
      System.out.println("WORLDLINE_AERO_LEGACY_PASS=draw,list-cache,dispose,animation,native-nbt,exact-rgba");
    } catch (Exception error) {
      throw new IllegalStateException("Aero native consumer failed", error);
    }
    client.shutdown();
    return false;
  }
}
