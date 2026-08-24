<!-- worldline-map-schema=1 -->
<!-- boundary=dungeon-generation -->
<!-- nonclaims=arbitrary-seeds,mob-spawner-entity-id,loot-probabilities -->
<!-- frozen-trace=546390d97d39a29b825727f8264038033d7e8144f284200a1a8819069ebd78a8 -->

# M626 dungeon generation census behavior map

The official Beta 1.7.3 dedicated server generates and populates the exact chunk matrix `-5..5`
on both axes for seed `17320110707`. The reusable TestKit fixture observes full protocol-14 chunk
snapshots, records every spawner block 52 and nearby chest block 54, and selects an accessible
linked chest. After a clean save and restart, Packet100 must expose a nonempty 27-slot chest whose
loot contents contribute to the frozen trace.

Frozen signal:

```text
region=-5:5:-5:5,chunks=121,dungeon=spawner+linked-chest,loot=nonempty-packet100,replicas=2,disconnect=clean
```

The exact position and loot hashes are frozen by the qualified signature. This boundary does not
generalize to arbitrary seeds, expose the spawner tile's `EntityId`, or claim a loot probability.

Frozen trace SHA-256: `546390d97d39a29b825727f8264038033d7e8144f284200a1a8819069ebd78a8`.
