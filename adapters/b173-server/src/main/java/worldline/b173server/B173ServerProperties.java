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
        if (difficulty < 0 || difficulty > 3) throw new IllegalArgumentException("invalid difficulty");
        return "allow-nether=" + allowNether + "\nlevel-name=world\nlevel-seed=" + seed
                + "\nmax-players=4\nonline-mode=false\nserver-ip=127.0.0.1\nserver-port=" + port
                + "\npvp=true\nspawn-animals="+spawnAnimals+"\nspawn-monsters="+spawnMonsters+"\ndifficulty="+difficulty
                + "\nview-distance=" + viewDistance + "\nallow-flight=" + allowFlight + "\n";
    }
}
