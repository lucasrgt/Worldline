package worldline.smoke.saveworldgensetb173;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/** Strict McRegion location, timestamp, sector, compression, and NBT-root validator. */
final class RegionFrame {
  private RegionFrame() {}

  static Summary inspect(Path directory, int minX, int maxX, int minZ, int maxZ) throws Exception {
    Path root = directory.toAbsolutePath().normalize();
    int frames = 0, gzip = 0, zlib = 0;
    for (int cx = minX; cx <= maxX; cx++) {
      for (int cz = minZ; cz <= maxZ; cz++) {
        Path file = root.resolve("r." + Math.floorDiv(cx, 32) + "." + Math.floorDiv(cz, 32) + ".mcr")
            .normalize();
        require(file.startsWith(root) && Files.isRegularFile(file), "McRegion file absent");
        int type = inspect(file, cx, cz);
        frames++;
        if (type == 1) gzip++;
        else zlib++;
      }
    }
    return new Summary(frames, gzip, zlib);
  }

  private static int inspect(Path file, int cx, int cz) throws Exception {
    try (RandomAccessFile input = new RandomAccessFile(file.toFile(), "r")) {
      require(input.length() >= 12288L && input.length() % 4096L == 0L,
          "invalid McRegion sector geometry");
      int index = Math.floorMod(cx, 32) + Math.floorMod(cz, 32) * 32;
      input.seek(index * 4L);
      int location = input.readInt(), offset = location >>> 8, sectors = location & 255;
      input.seek(4096L + index * 4L);
      int timestamp = input.readInt();
      require(offset >= 2 && sectors >= 1 && timestamp > 0, "invalid McRegion header entry");
      long start = (long) offset * 4096L;
      require(start + (long) sectors * 4096L <= input.length(), "McRegion frame escapes file");
      input.seek(start);
      int length = input.readInt(), type = input.readUnsignedByte();
      require(length > 1 && length + 4 <= sectors * 4096 && (type == 1 || type == 2),
          "invalid McRegion compressed frame");
      byte[] compressed = new byte[length - 1];
      input.readFully(compressed);
      try (DataInputStream nbt = new DataInputStream(type == 1
          ? new GZIPInputStream(new ByteArrayInputStream(compressed))
          : new InflaterInputStream(new ByteArrayInputStream(compressed)))) {
        require(nbt.readUnsignedByte() == 10 && nbt.readUnsignedShort() <= 128,
            "McRegion payload lacks an NBT compound root");
      }
      return type;
    }
  }

  static final class Summary {
    private final int frames, gzip, zlib;
    Summary(int count, int gz, int zl) {
      frames = count;
      gzip = gz;
      zlib = zl;
    }
    int frames() { return frames; }
    int zlib() { return zlib; }
    @Override public boolean equals(Object other) {
      if (!(other instanceof Summary)) return false;
      Summary value = (Summary) other;
      return frames == value.frames && gzip == value.gzip && zlib == value.zlib;
    }
    @Override public int hashCode() { return frames * 31 * 31 + gzip * 31 + zlib; }
  }

  private static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }
}
