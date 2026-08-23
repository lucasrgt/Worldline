package aero.modellib.test.worldline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/** Test-only cross-mixin readiness and post-ready frame/log counters. */
public final class WorldlineMultiplayerProbe {
  private static int chunks, frames, baselineLines;
  private static boolean hello, playReady, ready;
  private WorldlineMultiplayerProbe() {
  }
  public static void hello() {
    if (!hello)
      System.out.println("[WorldlineMultiplayer] packet1");
    hello = true;
  }
  public static void playReady() {
    if (!hello)
      throw new IllegalStateException("play before login hello");
    if (!playReady)
      System.out.println("[WorldlineMultiplayer] packet13");
    playReady = true;
  }
  public static void chunk() {
    if (chunks++ == 0)
      System.out.println("[WorldlineMultiplayer] packet51");
  }
  public static int chunks() {
    return chunks;
  }
  public static boolean networkReady() {
    return hello && playReady && chunks > 0;
  }
  public static boolean isReady() {
    return ready;
  }
  public static void ready() {
    if (ready)
      throw new IllegalStateException("multiplayer probe already ready");
    baselineLines = lines();
    ready = true;
  }
  public static int baselineLines() {
    return baselineLines;
  }
  public static void frame() {
    if (ready)
      frames++;
  }
  public static int frames() {
    return frames;
  }
  public static int aeroLinesAfterReady() {
    return lines() - baselineLines;
  }
  private static int lines() {
    File file = new File(System.getProperty("aero.spikelog.file", "aero-frame-spikes.log"));
    if (!file.isFile())
      return 0;
    int count = 0;
    try (BufferedReader input = new BufferedReader(new FileReader(file))) {
      while (input.readLine() != null)
        count++;
      return count;
    } catch (Exception error) {
      throw new IllegalStateException("could not count Aero log", error);
    }
  }
}
