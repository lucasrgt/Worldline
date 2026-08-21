# M59 Chest Transfer and Restart

M59 adds one bounded single-chest write: move an occupied personal storage slot
into an empty chest-owned slot. The caller supplies only personal slot 9-44 and
chest slot 0-26. Window IDs, action IDs, predictions, button, and cursor remain
adapter-owned.

A single chest exposes 63 combined slots. Owned chest slots are 0-26, while the
36 player storage slots are 27-62. The mapping is uniform:
`combinedSlot = personalSlot + 18`. Before transfer, Worldline verifies every
combined tail item against the canonical window-0 view.

Action 1 takes the combined player source and predicts its exact stack. Action 2
places into the empty chest target and predicts empty. Each Packet106 true first
validates both base views and cursor, then atomically publishes the predicted
active view, personal view, and cursor. The container action counter resets on
the window-open epoch, not the reusable numeric window ID.

Qualification closes through M58, saves and stops the official server, starts a
new official process on the same workspace, and connects a fresh client. The
new Packet100/104 pair exposes the transferred stone in chest slot 0 with an
empty player tail, proving tile-entity persistence across restart.

## Boundaries

M59 does not claim retrieval, merging, splitting, right/shift clicks, rejected
container recovery, concurrent mutation, active-window Packet103 reconciliation,
double chests, or generic containers.
