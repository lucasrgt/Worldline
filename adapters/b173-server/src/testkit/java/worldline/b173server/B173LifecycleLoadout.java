package worldline.b173server;

import worldline.api.RemoteItemStack;
import worldline.api.BlockFace;
import worldline.api.BlockState;
import worldline.test.TestRuntimeRequest;
import worldline.testapi.BlockLifecyclePlan;

/** Validated neutral lifecycle slot options translated to an official player loadout. */
final class B173LifecycleLoadout {
    final int placementHotbar, breakHotbar, supportHotbar, neighborHotbar;
    final RemoteItemStack placement, tool, support, neighborItem;
    final BlockState overhead;
    final BlockFace neighborFace;
    final BlockState neighborState;

    private B173LifecycleLoadout(int placementHotbar, RemoteItemStack placement,
            int breakHotbar, RemoteItemStack tool, int supportHotbar, RemoteItemStack support,
            BlockState overhead, int neighborHotbar, RemoteItemStack neighborItem,
            BlockFace neighborFace, BlockState neighborState) {
        this.placementHotbar = placementHotbar; this.placement = placement;
        this.breakHotbar = breakHotbar; this.tool = tool;
        this.supportHotbar = supportHotbar; this.support = support;
        this.overhead = overhead;
        this.neighborHotbar = neighborHotbar; this.neighborItem = neighborItem;
        this.neighborFace = neighborFace; this.neighborState = neighborState;
    }

    static B173LifecycleLoadout from(TestRuntimeRequest request) {
        if (request.testPath() == null) throw new IllegalArgumentException(
                "lifecycle provider requires a TestKit test path");
        Slot placement = parse(request.runtimeOption(BlockLifecyclePlan.PLACEMENT_SLOT_OPTION),
                "placement");
        Slot tool = parse(request.runtimeOption(BlockLifecyclePlan.BREAK_SLOT_OPTION), "break");
        RemoteItemStack support = support(request.runtimeOption(
                BlockLifecyclePlan.SUPPORT_STATE_OPTION));
        BlockState overhead = optionalState(request.runtimeOption(
                BlockLifecyclePlan.OVERHEAD_STATE_OPTION));
        Neighbor neighbor = neighbor(request);
        if (placement.hotbar == tool.hotbar || placement.hotbar == 0 || tool.hotbar == 0) {
            throw new IllegalArgumentException("lifecycle provisioned slots overlap");
        }
        if (neighbor != null && (neighbor.slot.hotbar == 0
                || neighbor.slot.hotbar == placement.hotbar
                || neighbor.slot.hotbar == tool.hotbar)) {
            throw new IllegalArgumentException("lifecycle neighbor slot overlaps");
        }
        int supportHotbar = support.legacyId() == 1 && support.damage() == 0
                ? 0 : available(placement.hotbar, tool.hotbar,
                        neighbor == null ? -1 : neighbor.slot.hotbar);
        return new B173LifecycleLoadout(placement.hotbar, placement.item,
                tool.hotbar, tool.item, supportHotbar, support, overhead,
                neighbor == null ? -1 : neighbor.slot.hotbar,
                neighbor == null ? null : neighbor.slot.item,
                neighbor == null ? null : neighbor.face,
                neighbor == null ? null : neighbor.state);
    }

    private static Neighbor neighbor(TestRuntimeRequest request) {
        String stateValue = request.runtimeOption(BlockLifecyclePlan.NEIGHBOR_STATE_OPTION);
        String faceValue = request.runtimeOption(BlockLifecyclePlan.NEIGHBOR_FACE_OPTION);
        String slotValue = request.runtimeOption(BlockLifecyclePlan.NEIGHBOR_SLOT_OPTION);
        if ("none".equals(stateValue) && "none".equals(faceValue)
                && "none".equals(slotValue)) return null;
        if (stateValue == null || faceValue == null || slotValue == null
                || "none".equals(stateValue) || "none".equals(faceValue)
                || "none".equals(slotValue)) throw invalid("neighbor");
        try {
            RemoteItemStack state = support(stateValue);
            return new Neighbor(BlockFace.valueOf(faceValue),
                    new BlockState(state.legacyId(), state.damage()),
                    parse(slotValue, "neighbor"));
        } catch (IllegalArgumentException error) { throw invalid("neighbor"); }
    }

    private static BlockState optionalState(String value) {
        if (value == null) throw new IllegalArgumentException(
                "lifecycle provider lacks overhead state option");
        if (value.equals("none")) return null;
        RemoteItemStack parsed = support(value);
        return new BlockState(parsed.legacyId(), parsed.damage());
    }

    private static RemoteItemStack support(String value) {
        if (value == null) throw new IllegalArgumentException(
                "lifecycle provider lacks support state option");
        String[] fields = value.split(":", -1);
        try {
            if (fields.length != 2) throw invalid("support");
            int id = Integer.parseInt(fields[0]), metadata = Integer.parseInt(fields[1]);
            if (id < 1 || id > 255 || metadata < 0 || metadata > 15) throw invalid("support");
            return new RemoteItemStack(id, 1, metadata);
        } catch (NumberFormatException error) { throw invalid("support"); }
    }

    private static int available(int placement, int tool, int neighbor) {
        for (int hotbar = 3; hotbar <= 8; hotbar++) {
            if (hotbar != placement && hotbar != tool && hotbar != neighbor) return hotbar;
        }
        throw new IllegalArgumentException("lifecycle has no support hotbar slot");
    }

    private static Slot parse(String value, String role) {
        if (value == null) throw new IllegalArgumentException(
                "lifecycle provider lacks " + role + " slot option");
        String[] fields = value.split(":", -1);
        if (fields.length != 5) throw invalid(role);
        try {
            int hotbar = Integer.parseInt(fields[0]);
            int inventory = Integer.parseInt(fields[1]);
            int id = Integer.parseInt(fields[2]);
            int count = Integer.parseInt(fields[3]);
            int damage = Integer.parseInt(fields[4]);
            if (hotbar < 0 || hotbar > 8 || inventory != hotbar + 36) throw invalid(role);
            return new Slot(hotbar, new RemoteItemStack(id, count, damage));
        } catch (NumberFormatException error) { throw invalid(role); }
        catch (IllegalArgumentException error) { throw invalid(role); }
    }

    private static IllegalArgumentException invalid(String role) {
        return new IllegalArgumentException("invalid lifecycle " + role + " slot option");
    }

    private static final class Slot {
        final int hotbar; final RemoteItemStack item;
        Slot(int hotbar, RemoteItemStack item) { this.hotbar = hotbar; this.item = item; }
    }

    private static final class Neighbor {
        final BlockFace face; final BlockState state; final Slot slot;
        Neighbor(BlockFace face, BlockState state, Slot slot) {
            this.face = face; this.state = state; this.slot = slot;
        }
    }
}
