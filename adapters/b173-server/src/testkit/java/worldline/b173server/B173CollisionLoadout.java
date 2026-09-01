package worldline.b173server;

import worldline.api.RemoteItemStack;
import worldline.test.TestRuntimeRequest;
import worldline.testkit.BlockCollisionPlan;

/** Validated neutral collision slot option translated to an official loadout. */
final class B173CollisionLoadout {
    final int hotbar;
    final RemoteItemStack item;
    final B173PhysicalSupport support;

    private B173CollisionLoadout(int hotbar, RemoteItemStack item, B173PhysicalSupport support) {
        this.hotbar = hotbar;
        this.item = item;
        this.support = support;
    }

    static B173CollisionLoadout from(TestRuntimeRequest request) {
        if (request.testPath() == null) {
            throw new IllegalArgumentException("collision provider requires a TestKit test path");
        }
        String value = request.runtimeOption(BlockCollisionPlan.PLACEMENT_SLOT_OPTION);
        if (value == null) {
            throw invalid();
        }
        String[] fields = value.split(":", -1);
        if (fields.length != 5) {
            throw invalid();
        }
        try {
            int hotbar = Integer.parseInt(fields[0]);
            int inventory = Integer.parseInt(fields[1]);
            int id = Integer.parseInt(fields[2]);
            int count = Integer.parseInt(fields[3]);
            int damage = Integer.parseInt(fields[4]);
            if (hotbar < 1 || hotbar > 8 || inventory != hotbar + 36) {
                throw invalid();
            }
            B173PhysicalSupport support = B173PhysicalSupport.from(request,
                    BlockCollisionPlan.SUPPORT_STATE_OPTION, 1);
            return new B173CollisionLoadout(hotbar, new RemoteItemStack(id, count, damage),
                    support);
        } catch (NumberFormatException error) {
            throw invalid();
        } catch (IllegalArgumentException error) {
            throw invalid();
        }
    }

    private static IllegalArgumentException invalid() {
        return new IllegalArgumentException("invalid collision placement slot option");
    }
}
