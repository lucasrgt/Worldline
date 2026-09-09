package worldline.testkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.GamePosition;
import worldline.api.GameUi;
import worldline.api.GameUiNode;
import worldline.api.UiMinecraftRuntime;

/** Reusable acceptance for block fixtures, player setup and native inventory interaction. */
public final class WorldControlFixture {
    private WorldControlFixture() { }
    public static WorldControlEvidence exercise(UiMinecraftRuntime runtime) {
        List<String> rows = new ArrayList<String>(); long time = runtime.world().time();
        BlockPosition position = new BlockPosition(8, 120, 8); BlockState air = new BlockState(0, 0);
        require(runtime.world().block(position).equals(air), "fixture block is not fresh air");
        rows.add("write=" + runtime.world().setBlock(position, new BlockState(35, 7)));
        BlockState block = runtime.world().block(position);
        rows.add("block=" + block.legacyId() + ":" + block.metadata());
        rows.add("repeat=" + runtime.world().setBlock(position, new BlockState(35, 7)));
        rows.add("remove=" + runtime.world().setBlock(position, air));
        rows.add("air=" + runtime.world().block(position).legacyId());
        GamePosition target = new GamePosition(8.5, 122, 8.5); runtime.player().teleport(target);
        require(runtime.player().position().equals(target), "teleport acknowledgement differs");
        rows.add("pose=8.5,122.0,8.5");
        runtime.player().selectHotbarSlot(8); rows.add("selected=" + runtime.player().selectedHotbarSlot());
        runtime.player().selectHotbarSlot(0); rows.add("selected=" + runtime.player().selectedHotbarSlot());
        reject(() -> runtime.player().selectHotbarSlot(9)); reject(() -> runtime.player().selectHotbarSlot(-1));
        reject(() -> runtime.world().block(new BlockPosition(0, 128, 0)));
        reject(() -> runtime.player().teleport(new GamePosition(0, -1, 0)));
        require(runtime.player().items().total() == 0, "inventory is not fresh");
        runtime.player().give(1, 3); rows.add("given=" + runtime.player().items().count(1));
        GameUi ui = runtime.ui(); ui.openInventory(); rows.add("screen=" + ui.screen());
        rows.add("slots=" + ui.getByRole(GameUiNode.SLOT).all().size());
        GameUiNode source = ui.slot(36); rows.add("source=" + source.itemId() + ":" + source.count());
        ui.click(source); rows.add("picked=" + ui.slot(36).count());
        rows.add("cursor-inclusive=" + runtime.player().items().count(1)); reject(() -> ui.click(source));
        ui.click(ui.slot(37)); rows.add("destination=" + ui.slot(37).itemId() + ":" + ui.slot(37).count());
        ui.close(); rows.add("closed=" + ui.screen().isEmpty());
        rows.add("retained=" + runtime.player().items().count(1));
        require(runtime.world().time() == time, "control command advanced game time"); rows.add("command-ticks=0");
        runtime.tick(); rows.add("native-ticks=" + (runtime.world().time() - time));
        return verify(new WorldControlEvidence(rows));
    }
    public static WorldControlEvidence verify(WorldControlEvidence evidence) {
        if (evidence == null || !evidence.observations().equals(Arrays.asList(
                "write=true", "block=35:7", "repeat=false",
                "remove=true", "air=0", "pose=8.5,122.0,8.5", "selected=8", "selected=0", "given=3", "screen=inventory",
                "slots=45", "source=1:3", "picked=0", "cursor-inclusive=3", "destination=1:3", "closed=true",
                "retained=3", "command-ticks=0", "native-ticks=1")))
            throw new IllegalStateException("world control evidence differs: "
                    + (evidence == null ? "null" : evidence.canonical()));
        return evidence;
    }
    private static void reject(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new IllegalStateException("invalid control request was accepted");
    }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }
}
