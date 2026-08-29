package worldline.b173server;

import worldline.api.RemoteItemStack;
import worldline.test.TestRuntimeRequest;
import worldline.testkit.BlockRandomTickSpreadPlan;

/** Validated species and break-tool slots for the mushroom spread arena. */
final class B173MushroomRandomTickLoadout {
    final int placementHotbar, breakHotbar;
    final RemoteItemStack placement, tool;

    private B173MushroomRandomTickLoadout(Slot placement, Slot tool) {
        this.placementHotbar = placement.hotbar; this.placement = placement.item;
        this.breakHotbar = tool.hotbar; this.tool = tool.item;
    }
    static B173MushroomRandomTickLoadout from(TestRuntimeRequest request) {
        if (request.testPath() == null) throw new IllegalArgumentException(
                "mushroom spread provider requires a TestKit path");
        Slot placement = parse(request.runtimeOption(
                BlockRandomTickSpreadPlan.PLACEMENT_SLOT_OPTION), "placement");
        Slot tool = parse(request.runtimeOption(
                BlockRandomTickSpreadPlan.BREAK_SLOT_OPTION), "break");
        if (placement.hotbar != 5 || tool.hotbar != 8 || placement.item.count() < 30
                || placement.item.legacyId() != 39 && placement.item.legacyId() != 40) {
            throw new IllegalArgumentException("invalid mushroom spread loadout");
        }
        return new B173MushroomRandomTickLoadout(placement, tool);
    }
    private static Slot parse(String value, String role) {
        if (value == null) throw new IllegalArgumentException("missing spread " + role + " slot");
        String[] fields = value.split(":", -1);
        if (fields.length != 5) throw invalid(role);
        try {
            int hotbar = Integer.parseInt(fields[0]);
            int inventory = Integer.parseInt(fields[1]);
            RemoteItemStack item = new RemoteItemStack(Integer.parseInt(fields[2]),
                    Integer.parseInt(fields[3]), Integer.parseInt(fields[4]));
            if (hotbar < 0 || hotbar > 8 || inventory != hotbar + 36) throw invalid(role);
            return new Slot(hotbar, item);
        } catch (NumberFormatException error) { throw invalid(role); }
        catch (IllegalArgumentException error) { throw invalid(role); }
    }
    private static IllegalArgumentException invalid(String role) {
        return new IllegalArgumentException("invalid spread " + role + " slot option");
    }
    private static final class Slot {
        final int hotbar; final RemoteItemStack item;
        Slot(int hotbar, RemoteItemStack item) { this.hotbar = hotbar; this.item = item; }
    }
}
