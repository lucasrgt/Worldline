import java.util.HashSet;
import java.util.Set;

record M788Visual(String label, M788Artifact baseline, M788Artifact candidate,
                  long changedPixels, int maximumDelta, Set<Long> changedLocations) {
    static M788Visual compare(String label, M788Artifact baseline,
                              M788Artifact candidate) throws Exception {
        SmokeSupport.require(baseline.width == candidate.width && baseline.height == candidate.height
            && baseline.captures == candidate.captures, "M788 framebuffer shape diverged");
        long changed = 0L;
        int maximum = 0;
        Set<Long> locations = new HashSet<>();
        long framePixels = (long) baseline.width * baseline.height;
        for (int checkpoint = 0; checkpoint < baseline.captures; checkpoint++) {
            byte[] left = baseline.pixels(checkpoint), right = candidate.pixels(checkpoint);
            for (int pixel = 0; pixel < left.length; pixel += 4) {
                boolean differs = false;
                for (int channel = 0; channel < 4; channel++) {
                    int delta = Math.abs((left[pixel + channel] & 255)
                        - (right[pixel + channel] & 255));
                    maximum = Math.max(maximum, delta);
                    differs |= delta != 0;
                }
                if (differs) {
                    changed++;
                    locations.add(checkpoint * framePixels + pixel / 4);
                }
            }
        }
        return new M788Visual(label, baseline, candidate, changed, maximum, locations);
    }

    String summary() {
        return "visual." + label + ",changedPixels=" + changedPixels
            + ",maxDelta=" + maximumDelta;
    }
}
