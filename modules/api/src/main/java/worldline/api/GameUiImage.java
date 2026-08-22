package worldline.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/** Immutable bounded ARGB capture suitable for exact visual regression. */
public final class GameUiImage {
    public static final int MAX_PIXELS = 2_500_000;
    private final int width, height;
    private final int[] argb;

    public GameUiImage(int width, int height, int[] argb) {
        if (width <= 0 || height <= 0 || argb == null) throw new IllegalArgumentException("UI image shape");
        int pixels = Math.multiplyExact(width, height);
        if (pixels > MAX_PIXELS || argb.length != pixels) throw new IllegalArgumentException("UI image size");
        this.width = width; this.height = height; this.argb = argb.clone();
    }

    public int width() { return width; }
    public int height() { return height; }
    public int pixels() { return argb.length; }

    public int argb(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) throw new IllegalArgumentException("pixel coordinate");
        return argb[y * width + x];
    }

    public int[] argb() { return argb.clone(); }

    public String sha256() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            update(digest, width); update(digest, height);
            for (int pixel : argb) update(digest, pixel);
            StringBuilder value = new StringBuilder();
            for (byte item : digest.digest()) value.append(String.format("%02x", item & 0xff));
            return value.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 unavailable", error);
        }
    }

    public String snapshotValue() {
        return "ui-image[" + width + "x" + height + " sha256=" + sha256() + "]";
    }

    /** Binary PPM artifact. Alpha is intentionally excluded from this preview. */
    public byte[] ppm() {
        byte[] header = ("P6\n" + width + " " + height + "\n255\n").getBytes(StandardCharsets.US_ASCII);
        byte[] value = Arrays.copyOf(header, header.length + argb.length * 3);
        int target = header.length;
        for (int pixel : argb) {
            value[target++] = (byte) (pixel >>> 16);
            value[target++] = (byte) (pixel >>> 8);
            value[target++] = (byte) pixel;
        }
        return value;
    }

    public GameUiImageDiff difference(GameUiImage expected) {
        return GameUiImageDiff.between(expected, this);
    }

    @Override public boolean equals(Object other) {
        return other instanceof GameUiImage && width == ((GameUiImage) other).width
                && height == ((GameUiImage) other).height && Arrays.equals(argb, ((GameUiImage) other).argb);
    }

    @Override public int hashCode() { return 31 * (31 * width + height) + Arrays.hashCode(argb); }

    @Override public String toString() { return snapshotValue(); }

    private static void update(MessageDigest digest, int value) {
        digest.update((byte) (value >>> 24)); digest.update((byte) (value >>> 16));
        digest.update((byte) (value >>> 8)); digest.update((byte) value);
    }
}
