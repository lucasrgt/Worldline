package worldline.api;

/** Exact pixel difference summary for two equally sized captures. */
public final class GameUiImageDiff {
    private final int width, height, changedPixels, maximumChannelDelta;
    private final long totalChannelDelta;

    private GameUiImageDiff(int width, int height, int changedPixels,
            int maximumChannelDelta, long totalChannelDelta) {
        this.width = width; this.height = height; this.changedPixels = changedPixels;
        this.maximumChannelDelta = maximumChannelDelta; this.totalChannelDelta = totalChannelDelta;
    }

    static GameUiImageDiff between(GameUiImage expected, GameUiImage actual) {
        if (expected == null || actual == null) throw new NullPointerException("UI image");
        if (expected.width() != actual.width() || expected.height() != actual.height()) {
            throw new IllegalArgumentException("UI image dimensions differ");
        }
        int changed = 0, maximum = 0; long total = 0;
        for (int y = 0; y < expected.height(); y++) for (int x = 0; x < expected.width(); x++) {
            int left = expected.argb(x, y), right = actual.argb(x, y);
            if (left != right) changed++;
            for (int shift = 0; shift <= 24; shift += 8) {
                int delta = Math.abs(((left >>> shift) & 0xff) - ((right >>> shift) & 0xff));
                total += delta; maximum = Math.max(maximum, delta);
            }
        }
        return new GameUiImageDiff(expected.width(), expected.height(), changed, maximum, total);
    }

    public int changedPixels() { return changedPixels; }
    public int maximumChannelDelta() { return maximumChannelDelta; }
    public long totalChannelDelta() { return totalChannelDelta; }
    public boolean exact() { return changedPixels == 0; }

    public boolean within(int allowedPixels, int allowedChannelDelta) {
        if (allowedPixels < 0 || allowedChannelDelta < 0 || allowedChannelDelta > 255) {
            throw new IllegalArgumentException("visual tolerance");
        }
        return changedPixels <= allowedPixels && maximumChannelDelta <= allowedChannelDelta;
    }

    @Override public String toString() {
        return "GameUiImageDiff[" + width + "x" + height + " changed=" + changedPixels
                + " maxDelta=" + maximumChannelDelta + " totalDelta=" + totalChannelDelta + "]";
    }
}
