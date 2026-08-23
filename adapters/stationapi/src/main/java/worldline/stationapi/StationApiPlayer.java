package worldline.stationapi;

import worldline.api.GamePlayer;
import worldline.api.GamePosition;
import worldline.api.ItemCensus;

/** Read-only remote player identity and pose from the controlled StationAPI tick boundary. */
final class StationApiPlayer implements GamePlayer {
    private final StationApiRuntime runtime;
    StationApiPlayer(StationApiRuntime runtime) { this.runtime = runtime; }
    @Override public int id() { return state().entityId; }
    @Override public String type() { return "minecraft:player"; }
    @Override public GamePosition position() { return state().position; }
    @Override public boolean alive() { return state().health > 0; }
    @Override public void teleport(GamePosition position) {
        throw new UnsupportedOperationException("StationAPI teleport is not qualified by M620");
    }
    @Override public String username() { return state().username; }
    @Override public int health() { return state().health; }
    @Override public int selectedHotbarSlot() { return state().selected; }
    @Override public void selectHotbarSlot(int slot) {
        throw new UnsupportedOperationException("StationAPI hotbar writes are not qualified by M620");
    }
    @Override public ItemCensus items() { return ItemCensus.empty(); }
    private StationApiSnapshot state() { return runtime.snapshot(); }
}
