package worldline.b173server;

import worldline.api.RemoteItemStack;
import worldline.test.TestRuntimeRequest;
import worldline.testkit.BlockLifecyclePlan;

/** Validated neutral lifecycle slot options translated to an official player loadout. */
final class B173LifecycleLoadout {
    final int placementHotbar, breakHotbar;
    final RemoteItemStack placement, tool;

    private B173LifecycleLoadout(int placementHotbar, RemoteItemStack placement,
            int breakHotbar, RemoteItemStack tool) {
        this.placementHotbar = placementHotbar; this.placement = placement;
        this.breakHotbar = breakHotbar; this.tool = tool;
    }

    static B173LifecycleLoadout from(TestRuntimeRequest request) {
        if (request.testPath() == null) throw new IllegalArgumentException(
                "lifecycle provider requires a TestKit test path");
        Slot placement = parse(request.runtimeOption(BlockLifecyclePlan.PLACEMENT_SLOT_OPTION),
                "placement");
        Slot tool = parse(request.runtimeOption(BlockLifecyclePlan.BREAK_SLOT_OPTION), "break");
        if (placement.hotbar == tool.hotbar) throw new IllegalArgumentException(
                "lifecycle placement and break slots overlap");
        return new B173LifecycleLoadout(placement.hotbar, placement.item,
                tool.hotbar, tool.item);
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
}
