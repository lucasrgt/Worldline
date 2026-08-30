package worldline.modloader.testkit;

import worldline.api.GamePlayer;
import worldline.api.GamePosition;
import worldline.api.ItemCensus;

/** Read-only local player identity and pose from a controlled legacy client. */
final class LegacyPlayer implements GamePlayer {
    private final LegacyRuntime runtime;
    LegacyPlayer(LegacyRuntime runtime) { this.runtime = runtime; }
    @Override public int id() { return state().entity; }
    @Override public String type() { return "minecraft:player"; }
    @Override public GamePosition position() { return state().position; }
    @Override public boolean alive() { return state().health > 0; }
    @Override public void teleport(GamePosition position) {
        throw new UnsupportedOperationException("legacy teleport is not qualified by M767");
    }
    @Override public String username() { return state().username; }
    @Override public int health() { return state().health; }
    @Override public int selectedHotbarSlot() { return state().selected; }
    @Override public void selectHotbarSlot(int slot) {
        throw new UnsupportedOperationException("legacy hotbar writes are not qualified by M767");
    }
    @Override public ItemCensus items() { return ItemCensus.empty(); }
    private LegacySnapshot state() { return runtime.snapshot(); }
}
