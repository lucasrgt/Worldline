package worldline.b173server;

/** Exact bounded official-server profile text. */
final class B173ServerProperties {
    private B173ServerProperties() {}
    static String text(long seed, int port, int viewDistance, boolean allowFlight, boolean allowNether) {
        return "allow-nether=" + allowNether + "\nlevel-name=world\nlevel-seed=" + seed
                + "\nmax-players=4\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=" + port
                + "\npvp=true\nspawn-animals=false\nspawn-monsters=false\nview-distance=" + viewDistance
                + "\nallow-flight=" + allowFlight + "\n";
    }
}
