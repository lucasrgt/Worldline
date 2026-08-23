package worldline.stationapi.runtime;

/** Cross-mixin login and remote-chunk readiness for the M620 StationAPI driver. */
public final class StationApiDriverProbe {
    private static boolean hello, play, chunk;
    private StationApiDriverProbe() {}
    public static void hello() { hello = true; }
    public static void play() { play = true; }
    public static void chunk() { chunk = true; }
    public static boolean ready() { return hello && play && chunk; }
}
