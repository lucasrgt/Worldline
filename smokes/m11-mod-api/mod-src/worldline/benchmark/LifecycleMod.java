package worldline.benchmark;

import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.GameEntity;
import worldline.api.GamePosition;
import worldline.b173.B173Mod;
import worldline.b173.B173ModContext;

/** Exercises the v2 mod surface: lifecycle, domain handles, and scheduling. */
public final class LifecycleMod implements B173Mod {
  private GameEntity spawned;

  @Override
  public void onLoad(B173ModContext context) {
    context.world().setBlock(new BlockPosition(8, 65, 8), new BlockState(20, 0));
    context.player().give(265, 5);
    spawned = context.world().spawn("minecraft:pig", new GamePosition(8.5D, 65.0D, 12.5D));
    if (spawned == null || !"minecraft:pig".equals(spawned.type())) {
      throw new IllegalStateException("spawn returned the wrong entity");
    }
    context.at(
        3, () -> context.world().setBlock(new BlockPosition(9, 65, 9), new BlockState(20, 0)));
  }

  @Override
  public void onTick(B173ModContext context) {
  }

  @Override
  public void onDispose() {
    worldline.smoke.m11.DisposeMarker.mark();
  }
}
