<!-- worldline-map-schema=1 -->
<!-- boundary=inventory-session -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f47c950ee765fa26735061bdf45cbbafbe66a0c8f8251dbd713bcc7c44ec4f3f -->

# M50 Drop Held Item

The actor starts with one qualified stone stack and the observer confirms that
carried item independently. The actor emits Packet14 status 4, the original
drop-current-item action. Packet103 makes the initiating inventory slot empty;
Packet5 makes the named peer's carried item empty.

The observer never reads the actor's inventory or server memory. Player NBT is
checked only after both clients disconnect and the official server saves,
independently confirming zero persisted inventory entries.

This cycle does not claim arbitrary item drops, throw trajectories, item-entity
tracking, pickup after the official delay, click-window actions, or tick control.

Frozen expected signature SHA-256: `f47c950ee765fa26735061bdf45cbbafbe66a0c8f8251dbd713bcc7c44ec4f3f`
