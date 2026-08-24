package worldline.b173server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import worldline.api.PersistentMultiplayerServerRuntime;
import worldline.api.ServerPlayerState;
import worldline.api.ServerLifecycle;
import worldline.api.ServerState;

/** Process adapter for the unmodified official Beta 1.7.3 dedicated server. */
public final class B173DedicatedServer implements PersistentMultiplayerServerRuntime {
    private final Path officialJar, directory;
    private final int port, viewDistance;
    private final boolean allowFlight, allowNether, spawnAnimals, spawnMonsters; private final long seed; private final Duration timeout; private final int difficulty;
    private final B173ServerProcess process;
    private ServerLifecycle lifecycle = ServerLifecycle.NEW;
    private int saves;

    public B173DedicatedServer(Path officialJar, Path directory, int port, long seed, Duration timeout) {
        this(officialJar, directory, port, seed, timeout, 3, false); }

    public B173DedicatedServer(Path officialJar, Path directory, int port, long seed,
            Duration timeout, int viewDistance, boolean allowFlight) {
        this(officialJar, directory, port, seed, timeout, viewDistance, allowFlight, false); }

    public B173DedicatedServer(Path officialJar, Path directory, int port, long seed,
            Duration timeout, int viewDistance, boolean allowFlight, boolean allowNether) {
        this(officialJar,directory,port,seed,timeout,viewDistance,allowFlight,allowNether,false,false); }
    public static B173DedicatedServer animals(Path jar,Path directory,int port,long seed,Duration timeout,int viewDistance,boolean allowFlight){return new B173DedicatedServer(jar,directory,port,seed,timeout,viewDistance,allowFlight,false,true,false);}
    public static B173DedicatedServer monsters(Path jar,Path directory,int port,long seed,Duration timeout,int viewDistance,boolean allowFlight){return new B173DedicatedServer(jar,directory,port,seed,timeout,viewDistance,allowFlight,false,false,true);}
    public static B173DedicatedServer netherMonsters(Path jar,Path directory,int port,long seed,Duration timeout){return new B173DedicatedServer(jar,directory,port,seed,timeout,3,true,true,false,true);} public static B173DedicatedServer difficulty(Path jar,Path directory,int port,long seed,Duration timeout,int difficulty){return new B173DedicatedServer(jar,directory,port,seed,timeout,3,true,false,false,true,difficulty);}
    private B173DedicatedServer(Path officialJar, Path directory, int port, long seed, Duration timeout, int viewDistance, boolean allowFlight, boolean allowNether, boolean spawnAnimals, boolean spawnMonsters) { this(officialJar,directory,port,seed,timeout,viewDistance,allowFlight,allowNether,spawnAnimals,spawnMonsters,B173ServerProperties.difficulty(spawnMonsters)); }
    private B173DedicatedServer(Path officialJar, Path directory, int port, long seed, Duration timeout, int viewDistance, boolean allowFlight, boolean allowNether, boolean spawnAnimals, boolean spawnMonsters, int difficulty) {
        if (!Files.isRegularFile(officialJar)) throw new IllegalArgumentException("server JAR is absent");
        if (port < 1 || port > 65535) throw new IllegalArgumentException("invalid port");
        if (viewDistance < 3 || viewDistance > 15) throw new IllegalArgumentException("invalid view distance");
        this.officialJar = officialJar.toAbsolutePath().normalize();
        this.directory = directory.toAbsolutePath().normalize();
        this.port = port;
        this.seed = seed;
        this.timeout = timeout;
        this.viewDistance = viewDistance;
        this.allowFlight = allowFlight;
        this.allowNether = allowNether;
        this.spawnAnimals = spawnAnimals; this.spawnMonsters = spawnMonsters; this.difficulty = difficulty;
        this.process = new B173ServerProcess(this.officialJar, this.directory, timeout);
    }

    @Override
    public void boot() {
        require(lifecycle == ServerLifecycle.NEW, "server was already started");
        process.boot(properties());
        lifecycle = ServerLifecycle.RUNNING;
    }

    @Override
    public void setTime(long worldTime) {
        if (worldTime < 0L) throw new IllegalArgumentException("negative world time");
        send("time set " + worldTime, "CONSOLE: Set time to " + worldTime);
    }

    @Override
    public void save() {
        send("save-all", "Save complete.");
        saves++;
    }

    public void operator(String username) {
        requireUsername(username, "operator");
        send("op " + username, "Opping " + username);
    }

    public void deoperator(String username) {
        requireUsername(username, "deoperator");
        send("deop " + username, "De-opping " + username);
    }

    public void kick(String username) {
        requireUsername(username, "kick");
        send("kick " + username, "Kicking " + username);
    }

    public void ban(String username) {
        requireUsername(username, "ban");
        send("ban " + username, "Banning " + username);
    }

    public void pardon(String username) {
        requireUsername(username, "pardon");
        send("pardon " + username, "Pardoning " + username);
    }

    public String awaitPlayerCommand(String username, String command, boolean allowed) {
        requireUsername(username, "command actor");
        if (command == null || !command.matches("[a-z0-9 ]{1,64}"))
            throw new IllegalArgumentException("invalid observed player command");
        String disposition = allowed ? " issued server command: " : " tried command: ";
        return process.await(username + disposition + command);
    }

    public String awaitReadTimeout(String username) {
        requireUsername(username, "timeout actor");
        process.await("java.net.SocketTimeoutException: Read timed out");
        return process.await(username + " lost connection: disconnect.genericReason");
    }

    @Override
    public ServerState state() {
        Path level = directory.resolve("world/level.dat");
        long time = Files.isRegularFile(level) ? B173LevelDat.worldTime(level) : ServerState.UNKNOWN_TIME;
        return new ServerState(lifecycle, port, false, time, saves);
    }

    @Override
    public List<String> players() {
        require(lifecycle == ServerLifecycle.RUNNING, "server is not running");
        return process.players();
    }

    @Override
    public ServerPlayerState player(String username) {
        requireUsername(username, "player");
        Path path = directory.resolve("world/players").resolve(username + ".dat").normalize();
        require(path.startsWith(directory) && Files.isRegularFile(path), "persisted player is absent");
        return B173PlayerDat.read(path, username);
    }

    @Override
    public void close() {
        if (lifecycle != ServerLifecycle.RUNNING) return;
        process.stop();
        lifecycle = ServerLifecycle.STOPPED;
    }

    private void send(String command, String marker) {
        require(lifecycle == ServerLifecycle.RUNNING, "server is not running");
        process.send(command, marker);
    }

    private String properties() {
        return B173ServerProperties.text(seed, port, viewDistance, allowFlight, allowNether, spawnAnimals, spawnMonsters, difficulty);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static void requireUsername(String username, String operation) {
        if (username == null || !username.matches("[A-Za-z0-9_]{1,16}"))
            throw new IllegalArgumentException("invalid " + operation + " username");
    }
}
