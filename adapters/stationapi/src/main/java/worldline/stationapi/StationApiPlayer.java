package worldline.stationapi;

import worldline.api.GamePlayer;
import worldline.api.GamePosition;
import worldline.api.ItemCensus;

/** Player state and acknowledged native local-world operations. */
final class StationApiPlayer implements GamePlayer {
    private final StationApiRuntime runtime;
    StationApiPlayer(StationApiRuntime runtime) { this.runtime = runtime; }
    @Override public int id() { return state().entityId; }
    @Override public String type() { return "minecraft:player"; }
    @Override public GamePosition position() { return state().position; }
    @Override public boolean alive() { return state().health > 0; }
    @Override public void teleport(GamePosition position) {
        if (position == null) throw new NullPointerException("position");
        if (Math.abs(position.x()) >= 30000000 || position.y() < 0 || position.y() >= 128
                || Math.abs(position.z()) >= 30000000)
            throw new IllegalArgumentException("position outside Beta world");
        runtime.mutate("TELEPORT " + position.x() + " " + position.y() + " " + position.z());
    }
    @Override public String username() { return state().username; }
    @Override public int health() { return state().health; }
    @Override public int selectedHotbarSlot() { return state().selected; }
    @Override public void selectHotbarSlot(int slot) {
        if (slot < 0 || slot > 8) throw new IllegalArgumentException("hotbar slot must be 0..8");
        runtime.mutate("HOTBAR " + slot);
    }
    @Override public void give(int itemId, int count) {
        if (itemId < 0 || count < 1 || count > 2304) throw new IllegalArgumentException("invalid give request");
        runtime.mutate("GIVE " + itemId + " " + count);
    }
    @Override public ItemCensus items() {
        String[] values = runtime.control("ITEMS").split("[|]", -1);
        if (!values[0].equals("items")) throw new IllegalStateException("invalid inventory reply");
        ItemCensus result = ItemCensus.empty(); int previous = -1;
        for (int index = 1; index < values.length; index++) {
            String[] row = values[index].split(",", -1);
            if (row.length != 2) throw new IllegalStateException("invalid inventory row");
            int id = Integer.parseInt(row[0]), count = Integer.parseInt(row[1]);
            if (id <= previous || count <= 0) throw new IllegalStateException("invalid inventory census order");
            result = result.plus(id, count); previous = id;
        }
        return result;
    }
    private StationApiSnapshot state() { return runtime.snapshot(); }
}
