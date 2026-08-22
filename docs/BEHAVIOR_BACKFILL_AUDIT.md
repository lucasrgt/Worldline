# Behavior backfill audit

## M452 knockback-cooldown

M452 remains pending. Its frozen smoke observes the first zombie melee health
loss and a five-tick no-second-loss window, but its knockback oracle is not
server-authored. After the hit, the client explicitly requests a `0.4`-block
movement away from the last mob position and accepts the resulting pose as
knockback evidence. That check would also pass if the official server applied
no melee impulse.

The reusable contract must not be promoted until a corrected smoke records the
unsolicited server pose transition or another identity-correlated knockback
signal without injecting the claimed movement. The corrected candidate then
needs an isolated official-runtime qualification before its frozen signature
can replace the current evidence.
