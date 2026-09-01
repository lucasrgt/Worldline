import java.util.HashSet;
import java.util.Set;

record M790Visual(String label, M790Artifact baseline, M790Artifact candidate,
                  long changedPixels, int maximumDelta, Set<Long> changedLocations) {
    static M790Visual compare(String label, M790Artifact baseline,
                              M790Artifact candidate) throws Exception {
        SmokeSupport.require(baseline.width == candidate.width && baseline.height == candidate.height
            && baseline.captures == candidate.captures, "M790 framebuffer shape diverged");
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
        return new M790Visual(label, baseline, candidate, changed, maximum, locations);
    }

    String summary() {
        return "visual." + label + ",changedPixels=" + changedPixels
            + ",maxDelta=" + maximumDelta;
    }
}
