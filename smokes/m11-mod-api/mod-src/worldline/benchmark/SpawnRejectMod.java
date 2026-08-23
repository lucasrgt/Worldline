package worldline.benchmark;

import worldline.api.GamePosition;
import worldline.b173.B173Mod;
import worldline.b173.B173ModContext;

/** Fails closed by spawning an unregistered semantic type. */
public final class SpawnRejectMod implements B173Mod {
  @Override
  public void onLoad(B173ModContext context) {
    context.world().spawn("minecraft:dragon", new GamePosition(8.5D, 65.0D, 8.5D));
  }

  @Override
  public void onTick(B173ModContext context) {
  }
}
