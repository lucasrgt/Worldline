<!-- worldline-map-schema=1 -->
<!-- boundary=native-random-outcomes -->
<!-- frozen-trace=d736cd87b51d13ee63548d9710b1ad4eb0d6e8d8db5a189529efbe1e372ec59c -->

# Native random outcome semantic map

`native-random-outcomes` binds `WorldlineEnvironmentBehaviors.NATIVE_RANDOM_OUTCOMES`
to `RandomOutcomeFixture#verify` and immutable `RandomOutcomeEvidence`.

| Atom | Native action | Equatable observations |
| --- | --- | --- |
| Gravel finite drop rate | BlockGravel.idDropped for residues 0..9 | Exact bound, residue, returned item |
| Fishing weather and depth | EntityFish.onUpdate then catchFish at depths 1, 3, 5 | 500/300 draw, catch counter, raw-fish item/count, rod cost, death |
| Catch-window timing | All 30 duration draws followed by native countdown; controlled first-bite sequences | Every counter value, no draws during countdown, expired empty catch, exact first-bite tick |

Mappings use existing hash-pinned named/server Tiny identities. Four fresh native
processes compare complete output; native provenance is checked for every behavior owner.
Setup assigns environment and RNG inputs, never the catch counter or loot outcome.
This adds quantitative evidence for existing Census cells, with a Functional Census
delta of zero. Tests reject omitted trials, reordered residues, changed counters and invented loot.

Frozen semantic SHA-256: `d736cd87b51d13ee63548d9710b1ad4eb0d6e8d8db5a189529efbe1e372ec59c`.

Frozen signal: `family=native-random-outcomes,subjects=2,observations=3228,processes=4,oracle=mapped-official-native-state`.

Frozen trace: `v1|server=official-b1.7.3|family=native-random-outcomes|gravel=10|fishing=500-300|depths=1-3-5|windows=10-39`.

## Reviewed deterministic matrix boundary

This matrix is the explicit rate-reopening path from FRONT2-08. Its first-bite
index is a chosen stimulus sequence, not the successful attempt number of a
seeded retry loop. The three fixed sequences contain respectively zero, four
and sixteen losing residues before residue zero. Native ticks must consume those
inputs and produce the corresponding counter transition. Existing BoundedAttempts
event contracts continue excluding their incidental successful attempt number.
The frozen signature hashes actual equatable evidence, not a DataDrivenCycle trace.
