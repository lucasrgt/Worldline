# M58 Remote Window Lifecycle

M58 adds an explicit close boundary to the single-chest window introduced by
M54. `closeWindow()` takes no caller-provided ID: the adapter derives the exact
active descriptor, requires an observed empty cursor, and writes Packet101 with
that tracked window ID.

The Beta 1.7.3 server does not acknowledge Packet101 and ignores its payload ID
when choosing what to close. Worldline therefore does not publish closure after
the write alone. It selects an empty personal storage slot and sends a left,
non-shift Packet102 no-op on window 0 with an empty prediction. Matching
Packet106 true proves the server restored the personal container. The no-op
uses the same action counter and transaction correlator as M55-M57.

`RemoteWindowClosure` immutably binds the closed container snapshot, proof
action/slot, and unchanged personal views. The active tracker is cleared only
after that proof. Write, timeout, or acknowledgement failure leaves the
lifecycle fail-closed rather than assuming restoration.

The qualification fixture seeds chest `54` directly into official player NBT,
settles the actor from a fixed spawn-relative pose, and chooses a nearby solid
support with a replaceable cell. This removes item-drop pickup and vertical
clearance randomness without changing the Packet101/window proof.

## Boundaries

M58 requires an empty cursor and at least one empty personal storage slot. It
does not claim Packet101 itself is acknowledged, server-forced close echo,
container writes, double chests, workbenches, furnaces, or generic window types.
