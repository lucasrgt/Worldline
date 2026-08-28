package worldline.b173server;

import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.test.TestRuntimeRequest;

/** Optional non-stone support item for physical-envelope arenas. */
final class B173PhysicalSupport {
    static final BlockState STONE = new BlockState(1, 0);
    final int hotbar;
    final RemoteItemStack item;
    final BlockState state;

    private B173PhysicalSupport(int hotbar, RemoteItemStack item, BlockState state) {
        this.hotbar = hotbar;
        this.item = item;
        this.state = state;
    }

    static B173PhysicalSupport from(TestRuntimeRequest request, String option, int count) {
        BlockState state = parse(request.runtimeOption(option));
        boolean stone = state.equals(STONE);
        int hotbar = stone ? 0 : 3;
        RemoteItemStack item = new RemoteItemStack(state.legacyId(), count, state.metadata());
        return new B173PhysicalSupport(hotbar, item, state);
    }

    private static BlockState parse(String value) {
        if (value == null || value.isEmpty()) {
            return STONE;
        }
        String[] fields = value.split(":", -1);
        if (fields.length != 2) {
            throw invalid();
        }
        try {
            int id = Integer.parseInt(fields[0]);
            int metadata = Integer.parseInt(fields[1]);
            if (id < 1 || id > 255 || metadata < 0 || metadata > 15) {
                throw invalid();
            }
            return new BlockState(id, metadata);
        } catch (NumberFormatException error) {
            throw invalid();
        }
    }

    private static IllegalArgumentException invalid() {
        return new IllegalArgumentException("invalid physical support state option");
    }
}
