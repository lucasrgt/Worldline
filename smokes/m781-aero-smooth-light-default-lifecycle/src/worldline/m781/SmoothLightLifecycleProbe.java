package worldline.m781;

import aero.modellib.render.Aero_SmoothLightCache;

/** External Java 8 probe for startup policy and world-identity lifecycle. */
public final class SmoothLightLifecycleProbe {
    private SmoothLightLifecycleProbe() {}

    public static void main(String[] arguments) {
        require(arguments.length == 1, "one arm is required");
        String arm = arguments[0];
        boolean expectedEnabled = !"false".equals(arm);
        require(Aero_SmoothLightCache.ENABLED == expectedEnabled, "startup policy drift");

        Aero_SmoothLightCache.configure(50L, 8);
        Object firstWorld = new Object();
        Object secondWorld = new Object();
        Object geometry = new Object();
        float[] claimed = Aero_SmoothLightCache.claim(
                firstWorld, geometry, 1, 2, 3, 4, 0L);
        claimed[0] = 0.25f;
        boolean sameWorldReuse = Aero_SmoothLightCache.cached(
                firstWorld, geometry, 1, 2, 3, 4, 1L) == claimed;
        require(sameWorldReuse && Aero_SmoothLightCache.entryCount() == 1,
                "same-world reuse drift");

        require(Aero_SmoothLightCache.cached(
                secondWorld, geometry, 1, 2, 3, 4, 1L) == null,
                "new world unexpectedly reused old values");
        boolean worldSwitchClears = Aero_SmoothLightCache.entryCount() == 0;
        Aero_SmoothLightCache.claim(secondWorld, geometry, 1, 2, 3, 4, 2L);
        require(Aero_SmoothLightCache.cached(
                firstWorld, geometry, 1, 2, 3, 4, 2L) == null,
                "old world unexpectedly retained values");
        worldSwitchClears &= Aero_SmoothLightCache.entryCount() == 0;
        require(worldSwitchClears, "world-switch lifecycle drift");

        System.out.println("M781_ARM=" + arm + ";enabled=" + expectedEnabled
                + ";sameWorldReuse=" + sameWorldReuse
                + ";worldSwitchClears=" + worldSwitchClears
                + ";entries=" + Aero_SmoothLightCache.entryCount());
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
