import java.lang.reflect.Field;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import worldline.trace.CanonicalStateTrace;
import worldline.trace.CanonicalTrace;

/** Executes one controlled tick directly against the official client JAR. */
public final class WorldlineClientOracle {
    private static final long SEED = 17320110707L;
    private static final long RNG_SEED = 2026071501L;
    private static final int X = 8;
    private static final int Z = 8;
    private static final String STATE_TRACE = "WORLDLINE_STATE_TRACE=";
    private static final String STATE_SIGNATURE = "WORLDLINE_STATE_SIGNATURE=";

    private WorldlineClientOracle() {}

    public static void main(String[] arguments) {
        System.setProperty("java.awt.headless", "true");
        require(!Display.isCreated(), "LWJGL display was created before oracle boot");
        OracleClientBoundaries.Client client = new OracleClientBoundaries.Client();
        client.k = new gr("Worldline", "offline");
        client.z = new kv();
        client.z.y = 0;
        client.I = OracleClientBoundaries.allocateWithoutConstructor(
                OracleClientBoundaries.Statistics.class);
        client.p = new OracleClientBoundaries.Textures(client.z);
        client.v = new uq(client);
        client.c = new ob(client);
        client.t = new px(client);
        client.g = OracleClientBoundaries.allocateWithoutConstructor(n.class);
        require(!Display.isCreated(), "oracle headless boot created an LWJGL display");

        fd world = new fd(new OracleClientWorld(SEED, "worldline-client-cycle"),
                "worldline-client-cycle", SEED);
        for (int chunkX = -2; chunkX <= 2; chunkX++) {
            for (int chunkZ = -2; chunkZ <= 2; chunkZ++) {
                world.c(chunkX, chunkZ);
            }
        }
        dc player = new dc(client, world, client.k, 0);
        player.a = new lr(client.z);
        player.c(8.5D, 66.0D, 8.5D, 0.0F, 0.0F);
        client.f = world;
        client.h = player;
        client.i = player;
        client.j = new dn(world, client.p);
        world.r.setSeed(RNG_SEED);
        require(world.a(X, 64, Z) == uu.u.bn, "oracle fixture stone missing");
        OfficialMetadataRecipes.verify();
        System.out.println("WORLDLINE_METADATA_RECIPES=families-8,recipes-25");

        CanonicalTrace trace = new CanonicalTrace(SEED);
        snapshot(trace, "loaded", client);
        CanonicalStateTrace states = stateTrace();
        state(states, "loaded", client);
        setTicksRan(client, ticksRan(client) + 1);
        ((net.minecraft.client.Minecraft) client).k();
        snapshot(trace, "tick1", client);
        state(states, "tick1", client);
        require(ticksRan(client) == 1, "oracle tick counter was not advanced exactly once");
        require(world.t() == 1L, "official world tick was not reached");
        require(!Display.isCreated(), "official controlled tick created an LWJGL display");
        for (int index = 2; index <= 16; index++) {
            if (index == 2) {
                Keyboard.worldlinePush(4, true, (char) 0);
                Keyboard.worldlinePush(4, false, (char) 0);
            }
            setTicksRan(client, ticksRan(client) + 1);
            ((net.minecraft.client.Minecraft) client).k();
            state(states, "tick" + index, client);
        }
        require(ticksRan(client) == 16 && world.t() == 16L,
                "official tick(N) did not advance to 16");
        client.J = false;
        System.out.println("WORLDLINE_CLIENT_ROOT=net.minecraft.client.Minecraft.k");
        System.out.println("WORLDLINE_CLIENT_HEADLESS=true");
        System.out.println("WORLDLINE_CLIENT_SOURCE="
                + net.minecraft.client.Minecraft.class.getProtectionDomain()
                        .getCodeSource().getLocation());
        trace.emitTo(System.out);
        System.out.println(STATE_TRACE + states.value());
        System.out.println(STATE_SIGNATURE + states.signature());
        System.out.println("WORLDLINE_PHYSICS_TRACE=v2|" + OfficialPhysicsProbe.trace(world, player)
                .substring(3) + "|compass=" + OfficialCompassProbe.trace(client, world, player));
    }

    private static CanonicalStateTrace stateTrace() {
        return new CanonicalStateTrace(SEED, "clientTick", "worldTime", "rngSeed", "entities",
                "cloudTick", "guiTick", "rendererTick", "playerX", "playerY",
                "playerZ", "health", "slot", "block64", "block65");
    }

    private static void state(CanonicalStateTrace trace, String label,
            net.minecraft.client.Minecraft client) {
        trace.record(label, ticksRan(client), client.f.t(), RNG_SEED, client.f.b.size(),
                intField(n.class, client.g, "x"), intField(uq.class, client.v, "h"),
                intField(px.class, client.t, "l"), Double.doubleToLongBits(client.h.aM),
                Double.doubleToLongBits(client.h.aN), Double.doubleToLongBits(client.h.aO),
                client.h.Y, client.h.c.c, client.f.a(X, 64, Z), client.f.a(X, 65, Z));
    }

    private static int intField(Class<?> owner, Object target, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field.getInt(target);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("cannot read official state field " + name, error);
        }
    }

    private static void snapshot(CanonicalTrace trace, String label,
            net.minecraft.client.Minecraft client) {
        trace.record(label, client.f.t(), client.f.b.size(), ticksRan(client),
                client.f.a(X, 64, Z), client.f.a(X, 65, Z));
    }

    private static int ticksRan(net.minecraft.client.Minecraft client) {
        try {
            Field field = net.minecraft.client.Minecraft.class.getDeclaredField("V");
            field.setAccessible(true);
            return field.getInt(client);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("cannot read official client tick counter", error);
        }
    }

    private static void setTicksRan(net.minecraft.client.Minecraft client, int value) {
        try {
            Field field = net.minecraft.client.Minecraft.class.getDeclaredField("V");
            field.setAccessible(true);
            field.setInt(client, value);
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("cannot control official client tick counter", error);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
