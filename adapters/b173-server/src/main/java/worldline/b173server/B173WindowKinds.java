package worldline.b173server;

import worldline.api.RemoteWindowKind;

/** Protocol-14 Packet100 inventory-type to remote window kind. */
final class B173WindowKinds {
    private B173WindowKinds() {}

    static RemoteWindowKind of(int type) {
        return type == 0 ? RemoteWindowKind.CHEST
                : type == 1 ? RemoteWindowKind.WORKBENCH
                : type == 2 ? RemoteWindowKind.FURNACE
                : type == 3 ? RemoteWindowKind.DISPENSER : null;
    }
}
