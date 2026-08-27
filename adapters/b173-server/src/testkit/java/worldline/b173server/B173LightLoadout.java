package worldline.b173server;

import worldline.api.RemoteItemStack;
import worldline.test.TestRuntimeRequest;
import worldline.testkit.BlockLightPlan;

/** Validated neutral light slot option translated to an official loadout. */
final class B173LightLoadout {
    final int hotbar;
    final RemoteItemStack item;

    private B173LightLoadout(int hotbar, RemoteItemStack item) {
        this.hotbar = hotbar; this.item = item;
    }

    static B173LightLoadout from(TestRuntimeRequest request) {
        if (request.testPath() == null) throw new IllegalArgumentException(
                "light provider requires a TestKit test path");
        String value = request.runtimeOption(BlockLightPlan.PLACEMENT_SLOT_OPTION);
        if (value == null) throw invalid(); String[] fields = value.split(":", -1);
        if (fields.length != 5) throw invalid();
        try {
            int hotbar = Integer.parseInt(fields[0]);
            int inventory = Integer.parseInt(fields[1]);
            int id = Integer.parseInt(fields[2]);
            int count = Integer.parseInt(fields[3]);
            int damage = Integer.parseInt(fields[4]);
            if (hotbar < 1 || hotbar > 8 || inventory != hotbar + 36) throw invalid();
            return new B173LightLoadout(hotbar, new RemoteItemStack(id, count, damage));
        } catch (NumberFormatException error) { throw invalid(); }
        catch (IllegalArgumentException error) { throw invalid(); }
    }
    private static IllegalArgumentException invalid() {
        return new IllegalArgumentException("invalid light placement slot option");
    }
}
