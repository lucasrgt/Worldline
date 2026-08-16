package worldline.b173;

import net.minecraft.src.EntityPlayerSP;
import worldline.api.GamePlayer;

/** Neutral live player handle over the controlled local player. */
final class B173Player extends B173Entity implements GamePlayer {
    private final B173ClientBackend owner;
    private final EntityPlayerSP player;

    B173Player(B173ClientBackend owner, EntityPlayerSP player) {
        super(owner, player); this.owner = owner; this.player = player;
    }

    @Override public String username() { return value().username; }

    @Override public int health() { return value().health; }

    @Override public int selectedHotbarSlot() { return value().inventory.currentItem; }

    @Override public void selectHotbarSlot(int slot) {
        if (slot < 0 || slot > 8) throw new IllegalArgumentException("hotbar slot must be 0..8");
        value().inventory.currentItem = slot;
    }

    private EntityPlayerSP value() { owner.client(); return player; }
}
