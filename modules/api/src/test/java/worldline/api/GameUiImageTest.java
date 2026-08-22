package worldline.api;

public final class GameUiImageTest {
    private GameUiImageTest() {}

    public static void main(String[] arguments) {
        int[] pixels = {0xff000000, 0xffffffff, 0xffff0000, 0xff00ff00};
        GameUiImage image = new GameUiImage(2, 2, pixels);
        pixels[0] = 0;
        require(image.argb(0, 0) == 0xff000000 && image.argb().length == 4, "image immutability");
        require(image.snapshotValue().equals("ui-image[2x2 sha256=" + image.sha256() + "]"),
                "snapshot identity");
        byte[] ppm = image.ppm();
        require(ppm[0] == 'P' && ppm[1] == '6' && ppm.length == 23, "PPM encoding");
        require(image.difference(image).exact(), "exact diff");
        GameUiImage changed = new GameUiImage(2, 2,
                new int[] {0xff000001, 0xffffffff, 0xffff0000, 0xff00ff00});
        GameUiImageDiff diff = changed.difference(image);
        require(diff.changedPixels() == 1 && diff.maximumChannelDelta() == 1
                && diff.totalChannelDelta() == 1 && diff.within(1, 1), "pixel diff");
        failure(() -> new GameUiImage(0, 1, new int[0]));
        failure(() -> image.argb(2, 0));
        failure(() -> image.difference(new GameUiImage(1, 1, new int[] {0})));
        System.out.println("GameUiImageTest passed");
    }

    private static void failure(Runnable action) {
        try { action.run(); throw new AssertionError("invalid UI image accepted"); }
        catch (IllegalArgumentException expected) { }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
