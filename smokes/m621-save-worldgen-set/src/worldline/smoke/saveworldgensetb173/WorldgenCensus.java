package worldline.smoke.saveworldgensetb173;

import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;

/** Stable geology, surface-family, cave-air, and ore-component census. */
final class WorldgenCensus {
  private final String geology;
  private final int chunks, surfaceFamilies, caveAir, oreBlocks, oreVeins;

  private WorldgenCensus(String hash, int count, int families, int air, int ores, int veins) {
    geology = hash;
    chunks = count;
    surfaceFamilies = families;
    caveAir = air;
    oreBlocks = ores;
    oreVeins = veins;
  }

  static WorldgenCensus measure(RemoteWorldView world, int minX, int maxX, int minZ, int maxZ)
      throws Exception {
    if (world == null) throw new IllegalArgumentException("remote world is absent");
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    Set<String> surfaces = new HashSet<String>();
    int chunks = 0, air = 0, ores = 0, veins = 0;
    for (int cx = minX; cx <= maxX; cx++) {
      for (int cz = minZ; cz <= maxZ; cz++) {
        RemoteChunkSnapshot chunk = world.chunkAt(cx, cz);
        chunks++;
        updateInt(digest, cx);
        updateInt(digest, cz);
        boolean[] ore = new boolean[32768];
        for (int x = 0; x < 16; x++) {
          for (int z = 0; z < 16; z++) {
            int top = top(chunk, x, z);
            surfaces.add(family(chunk.blockAt(x, top, z).legacyId()) + ':' + top);
            for (int y = 0; y < 128; y++) {
              int id = chunk.blockAt(x, y, z).legacyId();
              digest.update((byte) (geology(id) ? id : 0));
              if (y >= 5 && y + 5 < top && id == 0) air++;
              if (ore(id)) {
                ore[index(x, y, z)] = true;
                ores++;
              }
            }
          }
        }
        veins += components(ore);
      }
    }
    return new WorldgenCensus(hex(digest.digest()), chunks, surfaces.size(), air, ores, veins);
  }

  int surfaceFamilies() { return surfaceFamilies; }
  int caveAir() { return caveAir; }
  int oreBlocks() { return oreBlocks; }
  int oreVeins() { return oreVeins; }

  boolean replayEquals(WorldgenCensus value) {
    return value != null && geology.equals(value.geology) && chunks == value.chunks
        && oreBlocks == value.oreBlocks && oreVeins == value.oreVeins;
  }

  String describe() {
    return "chunks=" + chunks + ",geology=" + geology + ",surfaceFamilies=" + surfaceFamilies
        + ",caveAir=" + caveAir + ",oreBlocks=" + oreBlocks + ",oreVeins=" + oreVeins;
  }

  @Override public boolean equals(Object other) {
    if (!(other instanceof WorldgenCensus)) return false;
    WorldgenCensus value = (WorldgenCensus) other;
    return geology.equals(value.geology) && chunks == value.chunks
        && surfaceFamilies == value.surfaceFamilies && caveAir == value.caveAir
        && oreBlocks == value.oreBlocks && oreVeins == value.oreVeins;
  }

  @Override public int hashCode() { return geology.hashCode(); }

  private static int components(boolean[] ore) {
    boolean[] seen = new boolean[ore.length];
    int[] queue = new int[ore.length];
    int count = 0;
    for (int start = 0; start < ore.length; start++) {
      if (!ore[start] || seen[start]) continue;
      count++;
      int read = 0, write = 0;
      queue[write++] = start;
      seen[start] = true;
      while (read < write) {
        int at = queue[read++], y = at & 127, cell = at >> 7, z = cell & 15, x = cell >> 4;
        write = add(ore, seen, queue, write, x - 1, y, z);
        write = add(ore, seen, queue, write, x + 1, y, z);
        write = add(ore, seen, queue, write, x, y - 1, z);
        write = add(ore, seen, queue, write, x, y + 1, z);
        write = add(ore, seen, queue, write, x, y, z - 1);
        write = add(ore, seen, queue, write, x, y, z + 1);
      }
    }
    return count;
  }

  private static int add(boolean[] ore, boolean[] seen, int[] queue, int write, int x, int y, int z) {
    if (x < 0 || x > 15 || y < 0 || y > 127 || z < 0 || z > 15) return write;
    int at = index(x, y, z);
    if (ore[at] && !seen[at]) {
      seen[at] = true;
      queue[write++] = at;
    }
    return write;
  }

  private static int top(RemoteChunkSnapshot chunk, int x, int z) {
    for (int y = 127; y >= 0; y--)
      if (surface(chunk.blockAt(x, y, z).legacyId())) return y;
    throw new IllegalStateException("empty worldgen column");
  }

  private static boolean surface(int id) {
    return id != 0 && id != 6 && id != 17 && id != 18 && id != 31 && id != 32
        && id != 37 && id != 38 && id != 39 && id != 40;
  }

  private static String family(int id) {
    if (id == 8 || id == 9) return "water";
    if (id == 12 || id == 13 || id == 24) return "sand";
    if (id == 78 || id == 79 || id == 80) return "snow";
    if (id == 2 || id == 3) return "grass";
    return "other";
  }

  private static boolean ore(int id) {
    return id == 14 || id == 15 || id == 16 || id == 21 || id == 56 || id == 73 || id == 74;
  }

  private static boolean geology(int id) { return id == 1 || id == 7 || ore(id); }
  private static int index(int x, int y, int z) { return (x * 16 + z) * 128 + y; }
  private static void updateInt(MessageDigest digest, int value) {
    digest.update((byte) (value >>> 24));
    digest.update((byte) (value >>> 16));
    digest.update((byte) (value >>> 8));
    digest.update((byte) value);
  }
  private static String hex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte item : bytes) result.append(String.format("%02x", item & 255));
    return result.toString();
  }
}
