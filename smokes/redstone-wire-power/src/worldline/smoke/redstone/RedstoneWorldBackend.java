package worldline.smoke.redstone;

import net.minecraft.src.Block;
import net.minecraft.src.World;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;
import worldline.trace.CanonicalTrace;

/** Bridges the product runtime port to one torch-and-dust vanilla fixture. */
final class RedstoneWorldBackend implements GameBackend {
  private static final int TORCH_X = 8;
  private static final int WIRE_X = 9;
  private static final int OBSERVE_X = 10;
  private static final int Y = 65;
  private static final int Z = 8;

  private final long seed;
  private World world;

  RedstoneWorldBackend(long seed) {
    this.seed = seed;
  }

  @Override
  public void bootHeadless() {
    System.setProperty("java.awt.headless", "true");
  }

  @Override
  public void loadWorld(WorldSource source) {
    String worldName = source.path().getFileName().toString();
    world = new World(new MemorySaveHandler(seed, worldName), worldName, seed, null);
    for (int chunkX = -2; chunkX <= 2; chunkX++) {
      for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
        world.getChunkFromChunkCoords(chunkX, chunkZ);
      }
    }
    require(world.getBlockId(TORCH_X, 64, Z) == Block.stone.blockID, "fixture stone missing");
    require(world.getBlockId(TORCH_X, Y, Z) == 0, "torch cell is not air");
    require(world.getBlockId(WIRE_X, Y, Z) == 0, "wire cell is not air");
  }

  @Override
  public void tick() {
    requireWorld().tick();
  }

  @Override
  public void close() {
    world = null;
  }

  void placeCircuit() {
    World current = requireWorld();
    require(
        current.setBlockAndMetadataWithNotify(TORCH_X, Y, Z, Block.torchRedstoneActive.blockID, 5),
        "torch placement failed");
    require(current.setBlockWithNotify(WIRE_X, Y, Z, Block.redstoneWire.blockID),
        "wire placement failed");
  }

  void snapshot(CanonicalTrace trace, String label) {
    World current = requireWorld();
    int powered = current.isBlockIndirectlyGettingPowered(OBSERVE_X, Y, Z) ? 1 : 0;
    int provides = Block.torchRedstoneActive.canProvidePower() ? 1 : 0;
    trace.record(label, current.getWorldTime(), current.loadedEntityList.size(),
        current.getBlockId(TORCH_X, Y, Z), current.getBlockId(WIRE_X, Y, Z),
        current.getBlockMetadata(WIRE_X, Y, Z), powered, provides);
  }

  void assertFinalState() {
    World current = requireWorld();
    require(
        current.getBlockId(TORCH_X, Y, Z) == Block.torchRedstoneActive.blockID, "torch missing");
    require(current.getBlockId(WIRE_X, Y, Z) == Block.redstoneWire.blockID, "wire missing");
    require(current.getBlockMetadata(WIRE_X, Y, Z) > 0, "wire has no power");
    require(current.isBlockIndirectlyGettingPowered(OBSERVE_X, Y, Z), "observer is unpowered");
    require(Block.torchRedstoneActive.canProvidePower(), "torch does not provide power");
    require(current.getBlockId(TORCH_X, 64, Z) == Block.stone.blockID, "fixture stone changed");
  }

  private World requireWorld() {
    if (world == null) {
      throw new IllegalStateException("vanilla world is not loaded");
    }
    return world;
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
