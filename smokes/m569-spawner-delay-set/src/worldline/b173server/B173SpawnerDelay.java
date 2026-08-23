package worldline.b173server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import worldline.api.BlockPosition;
import worldline.api.RemoteMobSpawn;

/** Smoke-local MobSpawner Delay rewrite and non-blocking Packet24 peek. */
public final class B173SpawnerDelay {
  private static final byte[] NAME = new byte[] {2, 0, 5, 'D', 'e', 'l', 'a', 'y'};

  private B173SpawnerDelay() {
  }

  public static RemoteMobSpawn peek(B173WireClient client, int type) {
    return client.channel().inbound().mobs().peek(type);
  }

  public static void delay(Path serverDirectory, BlockPosition spawner, short value) {
    if (serverDirectory == null || spawner == null || value < 0)
      throw new IllegalArgumentException("invalid spawner delay");
    Path root = serverDirectory.toAbsolutePath().normalize();
    int cx = spawner.x() >> 4, cz = spawner.z() >> 4;
    Path region =
        root.resolve("world/region/r." + (cx >> 5) + "." + (cz >> 5) + ".mcr").normalize();
    if (!region.startsWith(root) || !Files.isRegularFile(region))
      throw new IllegalStateException("spawner region absent");
    try (RandomAccessFile file = new RandomAccessFile(region.toFile(), "rw")) {
      int index = (cx & 31) + (cz & 31) * 32;
      file.seek(index * 4L);
      int loc = file.readInt(), offset = (loc >>> 8) * 4096, sectors = loc & 255;
      if (offset == 0 || sectors < 1)
        throw new IllegalStateException("spawner chunk absent");
      file.seek(offset);
      int length = file.readInt(), type = file.readUnsignedByte();
      if (length < 2 || type < 1 || type > 2)
        throw new IllegalStateException("invalid spawner chunk");
      byte[] compressed = new byte[length - 1];
      file.readFully(compressed);
      byte[] raw = inflate(compressed, type), patched = replace(raw, value),
             out = deflate(patched, type);
      int next = out.length + 1, need = (next + 4 + 4095) / 4096;
      if (need <= sectors) {
        file.seek(offset);
        file.writeInt(next);
        file.writeByte(type);
        file.write(out);
      } else {
        int start = (int) ((file.length() + 4095) / 4096);
        file.setLength((long) (start + need) * 4096L);
        file.seek((long) start * 4096L);
        file.writeInt(next);
        file.writeByte(type);
        file.write(out);
        file.seek(index * 4L);
        file.writeInt((start << 8) | need);
      }
    } catch (IOException error) {
      throw new IllegalStateException("could not rewrite spawner delay", error);
    }
  }

  private static byte[] replace(byte[] raw, short value) {
    int at = indexOf(raw), extra = -1;
    if (at < 0)
      throw new IllegalStateException("MobSpawner Delay absent");
  outer:
    for (int i = at + NAME.length + 2; i + NAME.length + 2 <= raw.length; i++) {
      for (int j = 0; j < NAME.length; j++)
        if (raw[i + j] != NAME[j])
          continue outer;
      extra = i;
      break;
    }
    if (extra >= 0)
      throw new IllegalStateException("duplicate MobSpawner Delay");
    byte[] next = raw.clone();
    next[at + NAME.length] = (byte) ((value >> 8) & 255);
    next[at + NAME.length + 1] = (byte) (value & 255);
    return next;
  }

  private static int indexOf(byte[] raw) {
  outer:
    for (int i = 0; i + NAME.length + 2 <= raw.length; i++) {
      for (int j = 0; j < NAME.length; j++)
        if (raw[i + j] != NAME[j])
          continue outer;
      return i;
    }
    return -1;
  }

  private static byte[] inflate(byte[] compressed, int type) throws IOException {
    if (type == 1) {
      try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) >= 0)
          out.write(buf, 0, n);
        return out.toByteArray();
      }
    }
    Inflater inflater = new Inflater();
    inflater.setInput(compressed);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    try {
      while (!inflater.finished()) {
        int n = inflater.inflate(buf);
        if (n == 0)
          break;
        out.write(buf, 0, n);
      }
    } catch (java.util.zip.DataFormatException error) {
      throw new IOException("spawner delay inflate failed", error);
    } finally {
      inflater.end();
    }
    return out.toByteArray();
  }

  private static byte[] deflate(byte[] raw, int type) throws IOException {
    if (type == 1) {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (GZIPOutputStream out = new GZIPOutputStream(bytes)) {
        out.write(raw);
      }
      return bytes.toByteArray();
    }
    Deflater deflater = new Deflater(Deflater.BEST_SPEED);
    deflater.setInput(raw);
    deflater.finish();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    try {
      while (!deflater.finished())
        out.write(buf, 0, deflater.deflate(buf));
    } finally {
      deflater.end();
    }
    return out.toByteArray();
  }
}
