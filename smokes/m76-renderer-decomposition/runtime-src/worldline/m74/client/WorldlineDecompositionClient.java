package worldline.m74.client;

import aero.modellib.Aero_FramePacer;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.modificationstation.stationapi.api.client.event.block.entity.BlockEntityRendererRegisterEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import worldline.m74.*;
import java.lang.invoke.MethodHandles;
import java.util.Map;

/** Captures and applies the exact renderer-registration treatment. */
public final class WorldlineDecompositionClient {
  private static final String TREATMENT = readTreatment();
  private static Map<Class<? extends BlockEntity>, BlockEntityRenderer> renderers;
  private static boolean setup;
  static {
    EntrypointManager.registerLookup(MethodHandles.lookup());
  }
  @EventListener
  private static void registered(BlockEntityRendererRegisterEvent event) {
    if (renderers != null && renderers != event.renderers)
      throw new IllegalStateException("M76 renderer map drift");
    renderers = event.renderers;
  }
  public static void setup(Minecraft minecraft) {
    if (setup || !WorldlineDecompositionGate.prepare())
      return;
    String treatment = treatment();
    if (minecraft.options.fpsLimit != 0 || Aero_FramePacer.ENABLED
        || Aero_FramePacer.targetFps() != 0)
      throw new IllegalStateException("M76 frame limiter drift");
    if (renderers == null
        || !(renderers.get(WorldlineCensusBlockEntity.class) instanceof WorldlineCensusRenderer))
      throw new IllegalStateException("M76 renderer registration absent");
    boolean removed = treatment.equals("no-dispatch");
    if (removed
        && !(renderers.remove(WorldlineCensusBlockEntity.class) instanceof WorldlineCensusRenderer))
      throw new IllegalStateException("M76 renderer removal failed");
    setup = true;
    System.out.println("[WorldlineDecomposition] treatment=" + treatment
        + " rendererRemoved=" + removed + " fpsLimit=0 aeroFramePacing=false");
  }
  public static String treatment() {
    return TREATMENT;
  }
  private static String readTreatment() {
    String value = System.getProperty("worldline.decomposition.treatment", "");
    if (!(value.equals("no-dispatch") || value.equals("dispatch-only") || value.equals("aero16")))
      throw new IllegalStateException("invalid M76 treatment");
    return value;
  }
}
