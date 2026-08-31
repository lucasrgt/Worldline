package worldline.m780;

/** Scoped deterministic brightness source used only inside the smooth renderer. */
public final class SyntheticLight {
    private static int phase;

    private SyntheticLight() {}

    public static void phase(int value) { phase = value; }

    public static float brightness(int x, int y, int z) {
        SmoothLightProbe.lightSample();
        int cell = Math.floorMod(x * 13 + y * 3 + z * 7, 16);
        float gradient = cell / 15.0F;
        return phase == 0 ? 0.30F + gradient * 0.70F : 0.95F - gradient * 0.55F;
    }
}
