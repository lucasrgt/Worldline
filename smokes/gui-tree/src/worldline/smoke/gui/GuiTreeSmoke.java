package worldline.smoke.gui;

import java.nio.file.Paths;
import java.util.List;
import worldline.api.GameUi;
import worldline.api.GameUiNode;
import worldline.api.GameUiSpec;
import worldline.api.WorldSource;
import worldline.b173.B173Runtime;
import worldline.b173.B173Runtimes;
import worldline.trace.CanonicalStateTrace;

/** Exercises the inventory UI tree only through neutral public contracts. */
public final class GuiTreeSmoke {
  private static final long SEED = 17320110707L;

  private GuiTreeSmoke() {
  }

  public static void main(String[] arguments) {
    B173Runtime runtime = B173Runtimes.create(SEED);
    runtime.bootHeadless();
    try {
      runtime.loadWorld(WorldSource.at(Paths.get("memory", "gui-tree")));
      GameUi ui = runtime.ui();
      CanonicalStateTrace trace = trace();
      require(ui.screen().isEmpty() && ui.nodes().isEmpty(), "UI tree was not empty");
      record
      (trace, "closed", ui);
      ui.openInventory();
      runtime.tick();
      require(GameUiNode.INVENTORY.equals(ui.screen()), "inventory screen missing");
      List<GameUiNode> nodes = ui.nodes();
      require(nodes.size() == 46
              && ui.node(GameUiNode.SCREEN, GameUiNode.INVENTORY).equals(nodes.get(0)),
          "inventory tree root failed");
      GameUiNode slot = ui.slot(0);
      require(slot.empty() && slot.index() == 0 && ui.node(GameUiNode.SLOT, "0").equals(slot),
          "slot selector failed");
      ui.click(slot);
      record(trace, "inventory", ui);
      ui.close();
      runtime.tick();
      require(ui.screen().isEmpty() && ui.nodes().isEmpty(), "semantic close failed");
      record(trace, "inventory_closed", ui);
      runtime.gui().openWorkbench();
      runtime.tick();
      GameUiSpec spec = GameUiSpec.workbench();
      require(GameUiNode.WORKBENCH.equals(ui.screen()) && spec.matchesStructure(ui.nodes()),
          "authored workbench spec did not match the runtime tree");
      record(trace, "workbench", ui);
      ui.close();
      runtime.tick();
      require(ui.screen().isEmpty() && ui.nodes().isEmpty(), "workbench close failed");
      record(trace, "workbench_closed", ui);
      System.out.println("WORLDLINE_GUI_SOURCE=" + minecraftClassSource());
      System.out.println("WORLDLINE_GUI_TRACE=" + trace.value());
      System.out.println("WORLDLINE_GUI_SIGNATURE=" + trace.signature());
      System.out.println("WORLDLINE_GUI_API=screen,slot,click,spec");
    } finally {
      runtime.close();
    }
  }

  private static CanonicalStateTrace trace() {
    return new CanonicalStateTrace(SEED, "screen", "nodes", "slot0", "count0",
        "slotLast", "countLast");
  }

  private static void record(CanonicalStateTrace trace, String label, GameUi ui) {
    boolean open = !ui.screen().isEmpty();
    int kind = GameUiNode.INVENTORY.equals(ui.screen()) ? 1
        : GameUiNode.WORKBENCH.equals(ui.screen()) ? 2 : 0;
    GameUiNode first = open ? ui.slot(0) : new GameUiNode(GameUiNode.SLOT, "0", 0, -1, 0);
    GameUiNode last = open ? ui.slot(ui.nodes().size() - 2)
        : new GameUiNode(GameUiNode.SLOT, "last", 0, -1, 0);
    trace.record(label, kind, ui.nodes().size(), first.itemId(), first.count(),
        last.itemId(), last.count());
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
