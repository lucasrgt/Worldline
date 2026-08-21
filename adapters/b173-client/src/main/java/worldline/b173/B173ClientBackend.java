package worldline.b173;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.EffectRenderer;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityRenderer;
import net.minecraft.src.GameSettings;
import net.minecraft.src.GuiIngame;
import net.minecraft.src.MovementInputFromOptions;
import net.minecraft.src.PlayerController;
import net.minecraft.src.RenderGlobal;
import net.minecraft.src.Session;
import net.minecraft.src.World;
import org.lwjgl.opengl.Display;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import worldline.api.GamePlayer;
import worldline.api.GameUi;
import worldline.api.GameWorld;
import worldline.api.WorldSource;
import worldline.kernel.GameBackend;

/** Drives the exact mapped Minecraft.runTick root without creating a display. */
final class B173ClientBackend implements GameBackend, B173ModContext {
    private static final int X = 8;
    private static final int Z = 8;
    private final long seed;
    private final B173VirtualClock clock;
    private final B173VirtualFileSystem files;
    private final B173Scheduler scheduler;
    private final B173ThreadControl threads = new B173ThreadControl();
    private B173Boundaries.Client client;
    private B173World worldApi;
    private B173Player playerApi;
    private B173Gui gui;
    private long rngSeed;
    private final B173ModHooks hooks = new B173ModHooks(this);

    B173ClientBackend(long seed, B173VirtualClock clock, B173VirtualFileSystem files,
            B173Scheduler scheduler) {
        this.seed = seed; this.clock = clock; this.files = files; this.scheduler = scheduler; rngSeed = seed;
    }

    @Override
    public void bootHeadless() {
        System.setProperty("java.awt.headless", "true");
        require(!Display.isCreated(), "LWJGL display was created before boot");
        B173ClockHooks.install(clock);
        Keyboard.worldlineReset();
        Mouse.worldlineReset();
        client = new B173Boundaries.Client();
        threads.capture();
        client.displayWidth = 854;
        client.displayHeight = 480;
        client.session = new Session("Worldline", "offline");
        client.gameSettings = new GameSettings();
        client.gameSettings.difficulty = 0;
        client.statFileWriter = B173Boundaries.allocateWithoutConstructor(B173Boundaries.Statistics.class);
        client.renderEngine = new B173Boundaries.Textures(client.gameSettings);
        client.ingameGUI = new GuiIngame(client);
        client.playerController = new PlayerController(client);
        client.entityRenderer = new EntityRenderer(client);
        client.renderGlobal = B173Boundaries.allocateWithoutConstructor(RenderGlobal.class);
        assertHeadless();
    }

    @Override
    public void loadWorld(WorldSource source) {
        String name = source.path().getFileName().toString();
        World world = new World(new B173MemoryWorld(seed, name, files), name, seed);
        for (int chunkX = -2; chunkX <= 2; chunkX++)
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) world.getChunkFromChunkCoords(chunkX, chunkZ);
        EntityPlayerSP player = new EntityPlayerSP(client, world, client.session, 0);
        player.movementInput = new MovementInputFromOptions(client.gameSettings);
        player.setLocationAndAngles(8.5D, 66.0D, 8.5D, 0.0F, 0.0F);
        client.theWorld = world;
        client.thePlayer = player;
        client.renderViewEntity = player;
        client.effectRenderer = new EffectRenderer(world, client.renderEngine);
        playerApi = new B173Player(this, player);
        worldApi = new B173World(this, playerApi);
        gui = new B173Gui(this);
        reseed(seed);
        require(world.getBlockId(X, 64, Z) == Block.stone.blockID, "fixture stone missing");
    }

    @Override
    public void tick() {
        scheduler.advance();
        clock.advance(50L);
        setTicksRan(ticksRan() + 1);
        hooks.beforeTick(ticksRan());
        hooks.onTick(this);
        ((Minecraft) requireClient()).runTick();
    }

    @Override
    public void close() {
        if (client == null) return;
        client.running = false;
        threads.stop();
        try { hooks.dispose(); } finally {
            client.theWorld = null;
            client.thePlayer = null;
            worldApi = null; playerApi = null; gui = null; client = null;
            B173ClockHooks.clear(clock);
        }
    }

    B173Observation observe() {
        return B173Observation.capture(requireClient(), rngSeed);
    }

    void assertHeadless() { require(!Display.isCreated(), "controlled client created a display"); }

    String minecraftClassSource() {
        return Minecraft.class.getProtectionDomain().getCodeSource().getLocation().toString();
    }

    void key(int key, boolean pressed, char character) {
        Keyboard.worldlinePush(key, pressed, character);
    }

    void mouse(int button, boolean pressed, int wheel, int x, int y) {
        Mouse.worldlinePush(button, pressed, wheel, x, y);
    }

    void reseed(long value) {
        if (client == null || client.theWorld == null) return;
        client.theWorld.rand.setSeed(value);
        Random playerRandom = (Random) B173Reflect.get(Entity.class, "rand", client.thePlayer);
        playerRandom.setSeed(value ^ 0x5deece66dL);
        rngSeed = value;
    }

    boolean timerThreadAlive() { return threads.isAlive(); }

    B173Boundaries.Client client() { return requireClient(); }

    void install(B173Mod value) {
        require(worldApi != null, "mod install requires a loaded world");
        hooks.install(value);
    }

    public int clientTick() { return ticksRan(); }

    public int blockAt(int x, int y, int z) { return requireClient().theWorld.getBlockId(x, y, z); }

    public boolean setBlock(int x, int y, int z, int blockId) {
        return requireClient().theWorld.setBlockWithNotify(x, y, z, blockId);
    }

    public void at(int tick, Runnable action) { hooks.at(tick, action); }

    /** Wall-clock nanoseconds spent in mod callbacks during the last tick. */
    long lastModNanos() { return hooks.lastModNanos(); }

    @Override public GameWorld world() { return worldApi; }
    @Override public GamePlayer player() { return playerApi; }
    @Override public GameUi ui() { require(gui != null, "client is not booted"); return gui; }

    private int ticksRan() { return B173Reflect.getInt(Minecraft.class, "ticksRan", requireClient()); }
    private void setTicksRan(int value) { B173Reflect.setInt(Minecraft.class, "ticksRan", requireClient(), value); }
    private B173Boundaries.Client requireClient() {
        require(client != null, "client is not booted"); return client;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

}
