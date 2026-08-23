package worldline.smoke.m11;

import java.nio.file.Path;
import java.nio.file.Paths;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.GameEntity;
import worldline.api.GameWorld;
import worldline.api.WorldSource;
import worldline.b173.B173Mod;
import worldline.b173.B173Observation;
import worldline.b173.B173Runtime;
import worldline.b173.B173Runtimes;
import worldline.mods.LoadedMod;
import worldline.mods.ModLoader;
import worldline.trace.CanonicalStateTrace;

/** Executes the v2 mod surface against the controlled client. */
public final class M11ModApiSmoke {
  private static final long SEED = 17320110707L;
  private static final String RUNTIME = "b1.7.3", API = "1";

  private M11ModApiSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    require(arguments.length == 2, "expected mode and mod JAR");
    Path jar = Paths.get(arguments[1]);
    if ("run".equals(arguments[0]))
      run(jar);
    else if ("reject-schedule".equals(arguments[0]))
      reject(jar, "SCHEDULE");
    else if ("reject-spawn".equals(arguments[0]))
      reject(jar, "SPAWN");
    else
      throw new IllegalArgumentException("invalid M11 smoke mode");
  }

  private static void run(Path jar) throws Exception {
    try (LoadedMod<B173Mod> loaded = ModLoader.load(jar, RUNTIME, API, B173Mod.class)) {
      B173Runtime runtime = B173Runtimes.create(SEED);
      runtime.bootHeadless();
      try {
        runtime.loadWorld(WorldSource.at(Paths.get("memory", "m11-mod-api")));
        runtime.installMod(loaded.instance());
        for (int index = 0; index < 4; index++)
          runtime.tick();
        GameWorld world = runtime.world();
        require(
            world.block(new BlockPosition(8, 65, 8)).legacyId() == 20, "onLoad setBlock missing");
        require(world.block(new BlockPosition(9, 65, 9)).legacyId() == 20,
            "scheduled action did not fire at tick 3");
        GameEntity pig = find(world, "minecraft:pig");
        require(pig != null && pig.alive(), "spawned pig is missing or dead");
        require(runtime.player().items().count(265) == 5, "give did not add five iron");
        require(world.remove(pig), "remove rejected a live entity");
        require(!pig.alive(), "removed entity stayed alive");
        require(find(world, "minecraft:pig") == null, "removed pig still active");
        require(!world.remove(pig), "double remove reported progress");
        require(world.setBlock(new BlockPosition(10, 65, 10), new BlockState(54, 0)),
            "chest placement failed");
        require(
            world.itemsAt(new BlockPosition(10, 65, 10)).total() == 0, "container census failed");
        B173Observation state = runtime.observe();
        CanonicalStateTrace trace =
            new CanonicalStateTrace(SEED, "tick", "block65", "block9659", "pigs", "iron");
        trace.record("final", state.clientTick(), state.blockColumn()[1],
            world.block(new BlockPosition(9, 65, 9)).legacyId(), countAlive(world, "minecraft:pig"),
            runtime.player().items().count(265));
        System.out.println("WORLDLINE_M11_MOD=PASS");
        System.out.println("WORLDLINE_M11_TRACE=" + trace.value());
        System.out.println("WORLDLINE_M11_SIGNATURE=" + trace.signature());
      } finally {
        runtime.close();
        require(DisposeMarker.marked, "onDispose was not called");
      }
    }
  }

  private static void reject(Path jar, String label) throws Exception {
    try (LoadedMod<B173Mod> loaded = ModLoader.load(jar, RUNTIME, API, B173Mod.class)) {
      B173Runtime runtime = B173Runtimes.create(SEED);
      runtime.bootHeadless();
      try {
        runtime.loadWorld(WorldSource.at(Paths.get("memory", "m11-mod-api")));
        runtime.installMod(loaded.instance());
        throw new IllegalStateException("rejecting mod was installed");
      } catch (IllegalArgumentException expected) {
        System.out.println("WORLDLINE_M11_REJECT=" + label);
      } finally {
        runtime.close();
      }
    }
  }

  private static GameEntity find(GameWorld world, String type) {
    for (GameEntity entity : world.entities()) {
      if (type.equals(entity.type()) && entity.alive())
        return entity;
    }
    return null;
  }

  private static int countAlive(GameWorld world, String type) {
    return find(world, type) == null ? 0 : 1;
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
