package worldline.m794;

import java.time.Instant;

/** Startup-only clock calibration for approximate JFR sample attribution. */
final class ProductionClock {
  static final long MONOTONIC, UTC, UNCERTAINTY;
  static {
    for (int warm = 0; warm < 16; warm++) Instant.now();
    long best = Long.MAX_VALUE, mono = 0L, utc = 0L;
    for (int sample = 0; sample < 16; sample++) {
      long before = System.nanoTime();
      Instant instant = Instant.now();
      long after = System.nanoTime();
      if (after - before < best) {
        best = after - before;
        mono = before + best / 2;
        utc = instant.getEpochSecond() * 1000000000L + instant.getNano();
      }
    }
    MONOTONIC = mono;
    UTC = utc;
    UNCERTAINTY = (best + 1L) / 2L;
  }

  private ProductionClock() {}
}
