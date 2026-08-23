import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import worldline.trace.CanonicalStateTrace;

/** Independent official-JAR oracle for vanilla inventory pointer behavior and geometry. */
public final class GuiActionsOracle {
  private static final long SEED = 17320110707L;

  private GuiActionsOracle() {
  }

  public static void main(String[] arguments) {
    System.setProperty("java.awt.headless", "true");
    OracleClientBoundaries.Client client = client();
    fd world = new fd(new OracleClientWorld(SEED, "gui-actions"), "gui-actions", SEED);
    for (int x = -2; x <= 2; x++)
      for (int z = -2; z <= 2; z++)
        world.c(x, z);
    dc player = new dc(client, world, client.k, 0);
    player.a = new uo();
    player.c.a[0] = new iz(1, 4, 0);
    player.c(8.5D, 66.0D, 8.5D, 0.0F, 0.0F);
    client.f = world;
    client.h = player;
    client.i = player;
    client.j = new dn(world, client.p);
    world.r.setSeed(SEED);
    tap(18);
    tick(client);
    require(client.r instanceof ue, "oracle inventory screen missing");
    id screen = (id) client.r;
    CanonicalStateTrace trace = trace();
    record
    (trace, "opened", screen);
    click(screen, client, 36, 0);
    click(screen, client, 37, 0);
    tick(client);
    record
    (trace, "dragged", screen);
    click(screen, client, 37, 1);
    tick(client);
    click(screen, client, 38, 1);
    tick(client);
    record
    (trace, "split", screen);
    require(stack(screen, 36) == null && stack(screen, 37).a == 2 && stack(screen, 38).a == 1,
        "oracle inventory actions failed");
    client.J = false;
    require(!Display.isCreated(), "oracle created a display");
    System.out.println("WORLDLINE_GUI_ACTION_SOURCE="
        + net.minecraft.client.Minecraft.class.getProtectionDomain().getCodeSource().getLocation());
    System.out.println("WORLDLINE_GUI_ACTION_TRACE=" + trace.value());
    System.out.println("WORLDLINE_GUI_ACTION_SIGNATURE=" + trace.signature());
    System.out.println("WORLDLINE_GUI_ACTION_API=geometry,drag,secondary-click");
  }

  private static OracleClientBoundaries.Client client() {
    OracleClientBoundaries.Client client = new OracleClientBoundaries.Client();
    client.d = 854;
    client.e = 480;
    client.k = new gr("Worldline", "offline");
    client.z = new kv();
    client.z.y = 0;
    client.I = OracleClientBoundaries.allocateWithoutConstructor(Statistics.class);
    client.p = new OracleClientBoundaries.Textures(client.z);
    client.v = new uq(client);
    client.c = new ob(client);
    client.t = new px(client);
    client.g = OracleClientBoundaries.allocateWithoutConstructor(n.class);
    return client;
  }

  private static CanonicalStateTrace trace() {
    return new CanonicalStateTrace(SEED, "viewport_w", "viewport_h", "slot_x", "slot_y", "slot_w",
        "slot_h", "item36", "count36", "item37", "count37", "item38", "count38");
  }

  private static void record(CanonicalStateTrace trace, String label, id screen) {
    gp slot = (gp) screen.j.e.get(36);
    int x = (screen.c - screen.a) / 2 + slot.b, y = (screen.d - screen.i) / 2 + slot.c;
    iz s36 = stack(screen, 36), s37 = stack(screen, 37), s38 = stack(screen, 38);
    trace.record(label, screen.c, screen.d, x, y, 16, 16, item(s36), count(s36), item(s37),
        count(s37), item(s38), count(s38));
  }

  private static void click(
      id screen, net.minecraft.client.Minecraft client, int index, int button) {
    gp slot = (gp) screen.j.e.get(index);
    int x = (screen.c - screen.a) / 2 + slot.b + 8, y = (screen.d - screen.i) / 2 + slot.c + 8;
    int rawX = x * client.d / screen.c, rawY = (screen.d - y - 1) * client.e / screen.d;
    Mouse.worldlinePush(button, true, 0, rawX, rawY);
    Mouse.worldlinePush(button, false, 0, rawX, rawY);
  }

  private static iz stack(id screen, int index) {
    return ((gp) screen.j.e.get(index)).a();
  }
  private static int item(iz stack) {
    return stack == null ? -1 : stack.c;
  }
  private static int count(iz stack) {
    return stack == null ? 0 : stack.a;
  }
  private static void tap(int key) {
    Keyboard.worldlinePush(key, true, (char) 0);
    Keyboard.worldlinePush(key, false, (char) 0);
  }
  private static void tick(net.minecraft.client.Minecraft client) {
    try {
      java.lang.reflect.Field field = net.minecraft.client.Minecraft.class.getDeclaredField("V");
      field.setAccessible(true);
      field.setInt(client, field.getInt(client) + 1);
      client.k();
    } catch (ReflectiveOperationException error) {
      throw new IllegalStateException(error);
    }
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
  static final class Statistics extends xi {
    private Statistics() {
      super(null, (java.io.File) null);
    }
    @Override
    public void d() {
    }
    @Override
    public void c() {
    }
    @Override
    public void b() {
    }
    @Override
    public boolean a(ny achievement) {
      return true;
    }
    @Override
    public void a(vr stat, int amount) {
    }
  }
}
