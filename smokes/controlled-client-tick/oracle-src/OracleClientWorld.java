import java.io.File;
import java.util.List;

/** Official-name in-memory world and chunk boundary for the client oracle. */
@SuppressWarnings("rawtypes")
final class OracleClientWorld implements wt, bf {
  private static final int HEIGHT = 128;
  private static final int STONE_TOP = 64;
  private final ei info;

  OracleClientWorld(long seed, String name) {
    info = new ei(seed, name);
    info.a(8, 65, 8);
  }

  public ei c() {
    return info;
  }

  public void b() {
  }

  public bf a(xa provider) {
    return this;
  }

  public void a(ei worldInfo, List players) {
  }

  public void a(ei worldInfo) {
  }

  public File a(String name) {
    return null;
  }

  public lm a(fd world, int chunkX, int chunkZ) {
    byte[] blocks = new byte[16 * HEIGHT * 16];
    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        for (int y = 0; y <= STONE_TOP; y++) {
          blocks[x << 11 | z << 7 | y] = (byte) (y == 0 ? uu.A.bn : uu.u.bn);
        }
      }
    }
    lm chunk = new lm(world, blocks, chunkX, chunkZ);
    chunk.n = true;
    chunk.p = true;
    chunk.c();
    return chunk;
  }

  public void a(fd world, lm chunk) {
  }

  public void b(fd world, lm chunk) {
  }

  public void a() {
  }
}
