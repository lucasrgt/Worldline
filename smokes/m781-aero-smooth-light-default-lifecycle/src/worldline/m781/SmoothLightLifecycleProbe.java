package worldline.m781;

import java.lang.reflect.Method;

/** External Java 8 probe for startup policy and world-identity lifecycle. */
public final class SmoothLightLifecycleProbe {
    private SmoothLightLifecycleProbe() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 1, "one arm is required");
        String arm = arguments[0];
        boolean expectedEnabled = !"false".equals(arm);
        Class<?> cache = Class.forName("aero.modellib.render.Aero_SmoothLightCache");
        require(cache.getField("ENABLED").getBoolean(null) == expectedEnabled,
                "startup policy drift");

        Method configure = cache.getMethod("configure", long.class, int.class);
        Method claim = cache.getMethod("claim", Object.class, Object.class,
                int.class, int.class, int.class, int.class, long.class);
        Method cached = cache.getMethod("cached", Object.class, Object.class,
                int.class, int.class, int.class, int.class, long.class);
        Method entryCount = cache.getMethod("entryCount");
        configure.invoke(null, 50L, 8);

        Object firstWorld = new Object();
        Object secondWorld = new Object();
        Object geometry = new Object();
        float[] claimed = (float[]) claim.invoke(
                null, firstWorld, geometry, 1, 2, 3, 4, 0L);
        claimed[0] = 0.25f;
        boolean sameWorldReuse = cached.invoke(
                null, firstWorld, geometry, 1, 2, 3, 4, 1L) == claimed;
        require(sameWorldReuse && entries(entryCount) == 1, "same-world reuse drift");

        require(cached.invoke(null, secondWorld, geometry, 1, 2, 3, 4, 1L) == null,
                "new world unexpectedly reused old values");
        boolean worldSwitchClears = entries(entryCount) == 0;
        claim.invoke(null, secondWorld, geometry, 1, 2, 3, 4, 2L);
        require(cached.invoke(null, firstWorld, geometry, 1, 2, 3, 4, 2L) == null,
                "old world unexpectedly retained values");
        worldSwitchClears &= entries(entryCount) == 0;
        require(worldSwitchClears, "world-switch lifecycle drift");

        System.out.println("M781_ARM=" + arm + ";enabled=" + expectedEnabled
                + ";sameWorldReuse=" + sameWorldReuse
                + ";worldSwitchClears=" + worldSwitchClears
                + ";entries=" + entries(entryCount));
    }

    private static int entries(Method entryCount) throws Exception {
        return ((Integer) entryCount.invoke(null)).intValue();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
