package worldline.b173;

/** Narrow game-neutral capabilities granted to a controlled mod tick. */
public interface B173ModContext {
    int clientTick();

    int blockAt(int x, int y, int z);

    boolean setBlock(int x, int y, int z, int blockId);
}
