/** Phase-matched full-frame comparison across opposite-order clients. */
record M792VisualPair(int phase, long changedPixels, int maxDelta) {
  static M792VisualPair compare(int phase, M792Artifact off, M792Artifact on) throws Exception {
    SmokeSupport.require(off.width == on.width && off.height == on.height, "M792 framebuffer dimensions diverged");
    long changed = 0L;
    int maximum = 0;
    for (int checkpoint = 0; checkpoint < off.captures; checkpoint++) {
      byte[] baseline = off.pixels(checkpoint);
      byte[] candidate = on.pixels(checkpoint);
      SmokeSupport.require(baseline.length == candidate.length, "M792 frame size diverged");
      for (int pixel = 0; pixel < baseline.length; pixel += 4) {
        boolean differs = false;
        for (int channel = 0; channel < 4; channel++) {
          int delta = Math.abs((baseline[pixel + channel] & 255) - (candidate[pixel + channel] & 255));
          maximum = Math.max(maximum, delta);
          differs |= delta != 0;
        }
        if (differs)
          changed++;
      }
    }
    return new M792VisualPair(phase, changed, maximum);
  }

  String summary() {
    return "visual.phase." + phase + ",changedPixels=" + changedPixels + ",maxDelta=" + maxDelta;
  }
}
