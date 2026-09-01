package worldline.b173server;

import worldline.api.RemoteItemStack;
import worldline.test.TestRuntimeRequest;
import worldline.testkit.BlockStateDomainPlan;

/** Validated neutral state-domain slot option translated to an official loadout. */
final class B173StateDomainLoadout {
    final int hotbar;
    final RemoteItemStack item;
    final B173PhysicalSupport support;

    private B173StateDomainLoadout(int hotbar, RemoteItemStack item, B173PhysicalSupport support) {
        this.hotbar = hotbar;
        this.item = item;
        this.support = support;
    }

    static B173StateDomainLoadout from(TestRuntimeRequest request) {
        if (request.testPath() == null) {
            throw new IllegalArgumentException(
                    "state-domain provider requires a TestKit test path");
        }
        String value = request.runtimeOption(BlockStateDomainPlan.PLACEMENT_SLOT_OPTION);
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
                    BlockStateDomainPlan.SUPPORT_STATE_OPTION, 4);
            return new B173StateDomainLoadout(hotbar,
                    new RemoteItemStack(id, count, damage), support);
        } catch (NumberFormatException error) {
            throw invalid();
        } catch (IllegalArgumentException error) {
            throw invalid();
        }
    }

    private static IllegalArgumentException invalid() {
        return new IllegalArgumentException("invalid state-domain placement slot option");
    }
}
