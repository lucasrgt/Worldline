package worldline.b173server;

/** Exact bounded official-server profile text. */
final class B173ServerProperties {
    private B173ServerProperties() {}
    static String text(long seed, int port, int viewDistance, boolean allowFlight, boolean allowNether) {
        return text(seed,port,viewDistance,allowFlight,allowNether,false,false); }
    static String text(long seed, int port, int viewDistance, boolean allowFlight, boolean allowNether, boolean spawnAnimals) {
        return text(seed,port,viewDistance,allowFlight,allowNether,spawnAnimals,false); }
    static int difficulty(boolean spawnMonsters) { return spawnMonsters ? 1 : 0; }
    static String text(long seed, int port, int viewDistance, boolean allowFlight, boolean allowNether, boolean spawnAnimals, boolean spawnMonsters) {
        return text(seed,port,viewDistance,allowFlight,allowNether,spawnAnimals,spawnMonsters,difficulty(spawnMonsters)); }
    static String text(long seed, int port, int viewDistance, boolean allowFlight, boolean allowNether, boolean spawnAnimals, boolean spawnMonsters, int difficulty) {
        return profile(seed, port, viewDistance, allowFlight, allowNether,
                spawnAnimals, spawnMonsters, difficulty, 4);
    }
    static String admission(long seed, int port, int maximumPlayers, boolean whitelist) {
        return profile(seed, port, 3, true, false, false, false, 0, maximumPlayers)
                + "white-list=" + whitelist + "\n";
    }
    private static String profile(long seed, int port, int viewDistance,
            boolean allowFlight, boolean allowNether, boolean spawnAnimals,
            boolean spawnMonsters, int difficulty, int maximumPlayers) {
        if (difficulty < 0 || difficulty > 3 || maximumPlayers < 1 || maximumPlayers > 32) {
            throw new IllegalArgumentException("invalid server profile");
        }
        return "allow-nether=" + allowNether + "\nlevel-name=world\nlevel-seed=" + seed
                + "\nmax-players=" + maximumPlayers
                + "\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=" + port
                + "\npvp=true\nspawn-animals="+spawnAnimals+"\nspawn-monsters="+spawnMonsters+"\ndifficulty="+difficulty
                + "\nview-distance=" + viewDistance + "\nallow-flight=" + allowFlight + "\n";
    }
}
