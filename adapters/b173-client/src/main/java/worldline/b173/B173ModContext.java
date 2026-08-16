package worldline.b173;

/** Narrow capabilities granted to a controlled b1.7.3 mod tick. */
public interface B173ModContext {
    int clientTick();

    int blockAt(int x, int y, int z);

    boolean setBlock(int x, int y, int z, int blockId);
}
