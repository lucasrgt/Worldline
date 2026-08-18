package worldline.m74.client;

import aero.modellib.Aero_FramePacer;
import net.minecraft.client.Minecraft;
import worldline.m74.*;

/** Qualifies the runtime and discards M74's partial pre-instrumentation interval. */
public final class WorldlineStageGate {
    private static boolean prepared;
    private WorldlineStageGate() {}
    public static boolean prepare(Minecraft minecraft) {
        if (prepared || !WorldlineStageTimer.censusArmed()) return false;
        if (!WorldlineCensusProbe.mode().equals("present") || minecraft.options.fpsLimit != 0
                || Aero_FramePacer.ENABLED || Aero_FramePacer.targetFps() != 0)
            throw new IllegalStateException("M77 runtime configuration drift");
        prepared = true; WorldlineStageTimer.armAligned();
        System.out.println("[WorldlineStage] armed fpsLimit=0 aeroFramePacing=false"); return true;
    }
}
