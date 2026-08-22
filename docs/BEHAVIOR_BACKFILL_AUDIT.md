# Behavior backfill audit

Status: candidate implementation complete; official qualification pending.

The historical ratchet now has bindings for all 487 manifests. The final
backfill is grouped around reusable boundaries instead of milestone-specific
test helpers:

- the controlled-client equivalence contract owns native rendering, cobweb and
  soul-sand movement, ladder ascent, and compass needle state;
- the multiplayer and container session contracts own peer swing, vehicle
  attachment, storage-cart windows, pig saddle use, and wolf taming;
- the Aero runtime and cache contracts own server-content scheduling and page
  capacity lifecycle observations;
- `InvariantMinecraftRuntime.stackRecipes()` owns an immutable exact-metadata
  recipe snapshot. Its mapped/official differential enumerates all 25 recipes
  claimed by M315, M328, M336, M348, M368, M395, M396, and M440;
- `B173EntityVelocity` owns server-authored knockback direction from Packet28.

The zero ratchet is not releasable merely because the structural gate passes.
Every new adapter boundary must also pass its queued official-runtime cycle.

## M452 knockback-cooldown

The original frozen smoke observed a health loss and then explicitly requested
a `0.4`-block Packet13 movement away from the mob. That was not knockback
evidence and has been removed.

The replacement candidate correlates the official Packet8 health loss and
Packet38 hurt status with the unsolicited Packet28 velocity for the victim's
entity ID. `B173EntityVelocity.awayFrom` checks that horizontal velocity points
away from the attacking zombie, and the smoke sends no Packet13 movement after
the hit. A bounded no-second-loss window remains the cooldown observation. The
replacement signature still requires its isolated official-runtime rerun.
