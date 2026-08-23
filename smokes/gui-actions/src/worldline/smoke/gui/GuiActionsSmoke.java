package worldline.smoke.gui;

import java.nio.file.Paths;
import worldline.api.GameUi;
import worldline.api.GameUiBounds;
import worldline.api.GameUiContract;
import worldline.api.UiMinecraftRuntime;
import worldline.api.WorldSource;
import worldline.b173.B173Gui;
import worldline.b173.B173Runtimes;
import worldline.trace.CanonicalStateTrace;

/** Exercises vanilla layout, secondary click, and drag through neutral UI contracts. */
public final class GuiActionsSmoke {
  private static final long SEED = 17320110707L;

  private GuiActionsSmoke() {
  }

  public static void main(String[] arguments) {
    UiMinecraftRuntime runtime = B173Runtimes.create(SEED);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "gui-actions")));
      B173Gui fixture = (B173Gui) runtime.ui();
      fixture.putMain(0, 1, 4);
      GameUi ui = runtime.ui();
      ui.openInventory();
      runtime.tick();
      GameUiContract.validate(ui);
      CanonicalStateTrace trace = trace();
      record
      (trace, "opened", ui);
      ui.getSlot(36).dragTo(ui.getSlot(37));
      runtime.tick();
      record
      (trace, "dragged", ui);
      ui.getSlot(37).rightClick();
      runtime.tick();
      ui.getSlot(38).rightClick();
      runtime.tick();
      record
      (trace, "split", ui);
      require(ui.getSlot(36).single().empty(), "drag source was not emptied");
      ui.getSlot(37).shouldHaveItem(1, 2);
      ui.getSlot(38).shouldHaveItem(1, 1);
      System.out.println("WORLDLINE_GUI_ACTION_SOURCE=" + minecraftClassSource());
      System.out.println("WORLDLINE_GUI_ACTION_TRACE=" + trace.value());
      System.out.println("WORLDLINE_GUI_ACTION_SIGNATURE=" + trace.signature());
      System.out.println("WORLDLINE_GUI_ACTION_API=geometry,drag,secondary-click");
    } finally {
      runtime.close();
    }
  }

  private static CanonicalStateTrace trace() {
    return new CanonicalStateTrace(SEED, "viewport_w", "viewport_h", "slot_x", "slot_y", "slot_w",
        "slot_h", "item36", "count36", "item37", "count37", "item38", "count38");
  }

  private static void record(CanonicalStateTrace trace, String label, GameUi ui) {
    GameUiBounds viewport = ui.viewport(), slot = ui.getSlot(36).bounds();
    trace.record(label, viewport.width(), viewport.height(), slot.x(), slot.y(), slot.width(),
        slot.height(), ui.slot(36).itemId(), ui.slot(36).count(), ui.slot(37).itemId(),
        ui.slot(37).count(), ui.slot(38).itemId(), ui.slot(38).count());
  }

  private static String minecraftClassSource() {
    try {
      return Class.forName("net.minecraft.client.Minecraft")
          .getProtectionDomain()
          .getCodeSource()
          .getLocation()
          .toString();
    } catch (ClassNotFoundException error) {
      throw new IllegalStateException(error);
    }
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
