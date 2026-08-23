package worldline.benchmark;

import worldline.b173.B173Mod;
import worldline.b173.B173ModContext;

/** Fails closed by scheduling an action for a tick that already ran. */
public final class ScheduleRejectMod implements B173Mod {
  @Override
  public void onLoad(B173ModContext context) {
    context.at(context.clientTick(), () -> {});
  }

  @Override
  public void onTick(B173ModContext context) {
  }
}
