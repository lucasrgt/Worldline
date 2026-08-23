package worldline.benchmark;

import worldline.b173.B173Mod;
import worldline.b173.B173ModContext;

/** Second independently selected benchmark mod that places gold. */
public final class SecondProbeMod implements B173Mod {
  @Override
  public void onTick(B173ModContext context) {
    if (context.blockAt(8, 65, 8) == 0)
      context.setBlock(8, 65, 8, 41);
  }
}
